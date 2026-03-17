<?php
require_once 'config.php';
session_start();

$action = $_GET['action'] ?? '';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $data = json_decode(file_get_contents("php://input"), true);
    
    if ($action === 'register') {
        $name = trim($data['name'] ?? '');
        $email = trim($data['email'] ?? '');
        $password = $data['password'] ?? '';
        $role = $data['role'] ?? 'customer'; 
        
        if (empty($name) || empty($email) || empty($password)) {
            echo json_encode(['status' => 'error', 'message' => 'All fields are required']);
            exit;
        }

        if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
            echo json_encode(['status' => 'error', 'message' => 'Invalid email format']);
            exit;
        }

        if (strlen($password) < 6) {
            echo json_encode(['status' => 'error', 'message' => 'Password must be at least 6 characters']);
            exit;
        }
        
        $hash = password_hash($password, PASSWORD_DEFAULT);
        
        try {
            $stmt = $pdo->prepare("INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, ?)");
            $stmt->execute([$name, $email, $hash, $role]);
            
            echo json_encode(['status' => 'success', 'message' => 'Registration successful']);
        } catch (PDOException $e) {
            echo json_encode(['status' => 'error', 'message' => 'Email already exists or database error.']);
        }
    } elseif ($action === 'login') {
        $email = $data['email'] ?? '';
        $password = $data['password'] ?? '';
        
        $stmt = $pdo->prepare("SELECT * FROM users WHERE email = ?");
        $stmt->execute([$email]);
        $user = $stmt->fetch(PDO::FETCH_ASSOC);
        
        if ($user && password_verify($password, $user['password'])) {
            $_SESSION['user_id'] = $user['id'];
            $_SESSION['role'] = $user['role'];
            echo json_encode([
                'status' => 'success', 
                'message' => 'Login successful', 
                'user' => [
                    'id' => $user['id'], 
                    'name' => $user['name'], 
                    'email' => $user['email'], 
                    'role' => $user['role']
                ]
            ]);
        } else {
            echo json_encode(['status' => 'error', 'message' => 'Invalid email or password']);
        }
    }
} elseif ($_SERVER['REQUEST_METHOD'] === 'GET') {
     if ($action === 'logout') {
         session_destroy();
         echo json_encode(['status' => 'success', 'message' => 'Logged out']);
     } elseif ($action === 'me') {
         if (isset($_SESSION['user_id'])) {
             $stmt = $pdo->prepare("SELECT id, name, email, role FROM users WHERE id = ?");
             $stmt->execute([$_SESSION['user_id']]);
             $user = $stmt->fetch(PDO::FETCH_ASSOC);
             if($user){
                echo json_encode(['status' => 'success', 'user' => $user]);
             } else {
                echo json_encode(['status' => 'error', 'message' => 'User not found']);
             }
         } else {
             echo json_encode(['status' => 'error', 'message' => 'Not authenticated']);
         }
     }
}
?>
