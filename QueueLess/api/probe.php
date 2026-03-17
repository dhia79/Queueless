<?php
require_once 'config.php';

header('Content-Type: application/json');

try {
    $user_id = checkAuth();
    $stmt = $pdo->prepare("SELECT role FROM users WHERE id = ?");
    $stmt->execute([$user_id]);
    if ($stmt->fetchColumn() !== 'admin') exit(json_encode(['status' => 'error']));
} catch (Exception $e) {
    exit(json_encode(['status' => 'error']));
}

$diagnostics = [
    'php' => PHP_VERSION,
    'server' => $_SERVER['SERVER_SOFTWARE'] ?? 'Unknown',
    'extensions' => [
        'pdo' => extension_loaded('pdo'),
        'pdo_mysql' => extension_loaded('pdo_mysql'),
        'session' => extension_loaded('session')
    ],
    'db' => 'Connected',
    'timestamp' => date('Y-m-d H:i:s'),
    'memory' => round(memory_get_usage() / 1024 / 1024, 2) . ' MB'
];

echo json_encode(['status' => 'success', 'diagnostics' => $diagnostics]);
