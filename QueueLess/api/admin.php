<?php
require_once 'config.php';
$user_id = checkAuth();

header('Content-Type: application/json');

$stmt = $pdo->prepare("SELECT role FROM users WHERE id = ?");
$stmt->execute([$user_id]);
if ($stmt->fetchColumn() !== 'admin') {
    echo json_encode(['status' => 'error', 'message' => 'Unauthorized']);
    exit;
}

$action = $_GET['action'] ?? '';

if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    if ($action === 'stats') {
        $stats = [
            'total_users' => $pdo->query("SELECT COUNT(*) FROM users WHERE role = 'customer'")->fetchColumn(),
            'total_businesses' => $pdo->query("SELECT COUNT(*) FROM users WHERE role = 'business'")->fetchColumn(),
            'total_queues' => $pdo->query("SELECT COUNT(*) FROM queues")->fetchColumn(),
            'total_served' => $pdo->query("SELECT COUNT(*) FROM queue_entries WHERE status = 'served'")->fetchColumn()
        ];
        echo json_encode(['status' => 'success', 'stats' => $stats]);
    } elseif ($action === 'businesses') {
        $stmt = $pdo->query("SELECT u.id as user_id, u.email, u.name as owner_name, u.created_at, b.id as business_id, b.name as business_name, b.location, b.service_type FROM users u LEFT JOIN businesses b ON u.id = b.user_id WHERE u.role = 'business'");
        echo json_encode(['status' => 'success', 'businesses' => $stmt->fetchAll(PDO::FETCH_ASSOC)]);
    } elseif ($action === 'customers') {
        $stmt = $pdo->query("SELECT id, name, email, created_at FROM users WHERE role = 'customer'");
        echo json_encode(['status' => 'success', 'customers' => $stmt->fetchAll(PDO::FETCH_ASSOC)]);
    }
} elseif ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $data = json_decode(file_get_contents("php://input"), true);
    
    if ($action === 'delete_user') {
        $id = $data['id'] ?? 0;
        if ($id == $user_id) exit(json_encode(['status' => 'error', 'message' => 'Self-deletion forbidden']));

        $stmt = $pdo->prepare("DELETE FROM users WHERE id = ? AND role != 'admin'");
        echo $stmt->execute([$id]) ? json_encode(['status' => 'success']) : json_encode(['status' => 'error']);
    } elseif ($action === 'add_user') {
        $name = trim($data['name'] ?? '');
        $email = trim($data['email'] ?? '');
        $password = $data['password'] ?? '';
        
        if (empty($name) || empty($email) || empty($password)) exit(json_encode(['status' => 'error', 'message' => 'Missing fields']));

        try {
            $hash = password_hash($password, PASSWORD_DEFAULT);
            $stmt = $pdo->prepare("INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, ?)");
            $stmt->execute([$name, $email, $hash, $data['role'] ?? 'customer']);
            echo json_encode(['status' => 'success']);
        } catch (PDOException $e) {
            echo json_encode(['status' => 'error', 'message' => 'Duplicate entry']);
        }
    } elseif ($action === 'update_user') {
        $id = $data['id'] ?? 0;
        $name = trim($data['name'] ?? '');
        $email = trim($data['email'] ?? '');
        $role = $data['role'] ?? 'customer';

        if (empty($id) || empty($name) || empty($email)) exit(json_encode(['status' => 'error']));
        if ($id == $user_id && $role !== 'admin') exit(json_encode(['status' => 'error', 'message' => 'Cannot demote self']));

        try {
            if (!empty($data['password'])) {
                $hash = password_hash($data['password'], PASSWORD_DEFAULT);
                $stmt = $pdo->prepare("UPDATE users SET name = ?, email = ?, role = ?, password = ? WHERE id = ?");
                $stmt->execute([$name, $email, $role, $hash, $id]);
            } else {
                $stmt = $pdo->prepare("UPDATE users SET name = ?, email = ?, role = ? WHERE id = ?");
                $stmt->execute([$name, $email, $role, $id]);
            }
            echo json_encode(['status' => 'success']);
        } catch (PDOException $e) {
            echo json_encode(['status' => 'error']);
        }
    }
}
