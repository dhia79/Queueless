<?php
require_once 'config.php';
$user_id = checkAuth();

// Quick check if user is admin
$stmt = $pdo->prepare("SELECT role FROM users WHERE id = ?");
$stmt->execute([$user_id]);
$role = $stmt->fetchColumn();

if ($role !== 'admin') {
    echo json_encode(['status' => 'error', 'message' => 'Access denied']);
    exit;
}

$action = $_GET['action'] ?? '';

if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    if ($action === 'stats') {
        $stats = [];
        $stats['total_users'] = $pdo->query("SELECT COUNT(*) FROM users WHERE role = 'customer'")->fetchColumn();
        $stats['total_businesses'] = $pdo->query("SELECT COUNT(*) FROM users WHERE role = 'business'")->fetchColumn();
        $stats['total_queues'] = $pdo->query("SELECT COUNT(*) FROM queues")->fetchColumn();
        $stats['total_served'] = $pdo->query("SELECT COUNT(*) FROM queue_entries WHERE status = 'served'")->fetchColumn();
        
        echo json_encode(['status' => 'success', 'stats' => $stats]);
    } elseif ($action === 'businesses') {
        // Fetch users who are businesses alongside their business profile data
        $stmt = $pdo->query("SELECT u.id as user_id, u.email, u.name as owner_name, u.created_at, b.id as business_id, b.name as business_name, b.location, b.service_type FROM users u LEFT JOIN businesses b ON u.id = b.user_id WHERE u.role = 'business'");
        echo json_encode(['status' => 'success', 'businesses' => $stmt->fetchAll(PDO::FETCH_ASSOC)]);
    } elseif ($action === 'customers') {
        // Fetch users who are customers
        $stmt = $pdo->query("SELECT id, name, email, created_at FROM users WHERE role = 'customer'");
        echo json_encode(['status' => 'success', 'customers' => $stmt->fetchAll(PDO::FETCH_ASSOC)]);
    }
} elseif ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $data = json_decode(file_get_contents("php://input"), true);
    
    // Deletes an entire user (cascade will handle businesses, queues, and queue entries depending on DB schema)
    if ($action === 'delete_user') {
        $id = $data['id'] ?? 0;
        
        if ($id == $user_id) {
            echo json_encode(['status' => 'error', 'message' => 'Cannot delete yourself']);
            exit;
        }

        $stmt = $pdo->prepare("DELETE FROM users WHERE id = ? AND role != 'admin'");
        if ($stmt->execute([$id])) {
            echo json_encode(['status' => 'success', 'message' => 'User deleted successfully']);
        } else {
            echo json_encode(['status' => 'error', 'message' => 'Failed to delete user']);
        }
    } elseif ($action === 'add_user') {
        $name = trim($data['name'] ?? '');
        $email = trim($data['email'] ?? '');
        $password = $data['password'] ?? '';
        $role = $data['role'] ?? 'customer';

        if (empty($name) || empty($email) || empty($password)) {
            echo json_encode(['status' => 'error', 'message' => 'Name, email, and password are required']);
            exit;
        }

        $hash = password_hash($password, PASSWORD_DEFAULT);

        try {
            $stmt = $pdo->prepare("INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, ?)");
            $stmt->execute([$name, $email, $hash, $role]);
            echo json_encode(['status' => 'success', 'message' => 'User created successfully']);
        } catch (PDOException $e) {
            echo json_encode(['status' => 'error', 'message' => 'Email already exists or database error.']);
        }

    } elseif ($action === 'update_user') {
        $id = $data['id'] ?? 0;
        $name = trim($data['name'] ?? '');
        $email = trim($data['email'] ?? '');
        $role = $data['role'] ?? 'customer';
        $password = $data['password'] ?? ''; // Optional

        if (empty($id) || empty($name) || empty($email)) {
            echo json_encode(['status' => 'error', 'message' => 'ID, name, and email are required']);
            exit;
        }

        if ($id == $user_id && $role !== 'admin') {
            echo json_encode(['status' => 'error', 'message' => 'Cannot change your own role from admin']);
            exit;
        }

        try {
            if (!empty($password)) {
                $hash = password_hash($password, PASSWORD_DEFAULT);
                $stmt = $pdo->prepare("UPDATE users SET name = ?, email = ?, role = ?, password = ? WHERE id = ?");
                $stmt->execute([$name, $email, $role, $hash, $id]);
            } else {
                $stmt = $pdo->prepare("UPDATE users SET name = ?, email = ?, role = ? WHERE id = ?");
                $stmt->execute([$name, $email, $role, $id]);
            }
            echo json_encode(['status' => 'success', 'message' => 'User updated successfully']);
        } catch (PDOException $e) {
            echo json_encode(['status' => 'error', 'message' => 'Database error (maybe email already in use).']);
        }
    }
}
?>
