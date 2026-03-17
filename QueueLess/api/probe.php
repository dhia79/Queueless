<?php
require_once 'config.php';

// Only admins can access the probe for security
try {
    $user_id = checkAuth();
    $stmt = $pdo->prepare("SELECT role FROM users WHERE id = ?");
    $stmt->execute([$user_id]);
    $role = $stmt->fetchColumn();

    if ($role !== 'admin') {
        echo json_encode(['status' => 'error', 'message' => 'Admin only']);
        exit;
    }
} catch (Exception $e) {
    echo json_encode(['status' => 'error', 'message' => 'Unauthorized']);
    exit;
}

// Gather system info
$info = [
    'php_version' => PHP_VERSION,
    'server_software' => $_SERVER['SERVER_SOFTWARE'] ?? 'Unknown',
    'extensions' => [
        'pdo' => extension_loaded('pdo'),
        'pdo_mysql' => extension_loaded('pdo_mysql'),
        'session' => extension_loaded('session'),
        'json' => extension_loaded('json')
    ],
    'db_status' => 'Connected',
    'time_server' => date('Y-m-d H:i:s'),
    'memory_usage' => round(memory_get_usage() / 1024 / 1024, 2) . ' MB'
];

echo json_encode(['status' => 'success', 'diagnostics' => $info]);
?>
