<?php
header('Content-Type: application/json');

// Handle CORS if needed
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET, POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type");

$host = 'localhost';
$db   = 'queueless_db';
$user = 'root'; // default for xampp/wamp
$pass = '';     // default empty

try {
    $pdo = new PDO("mysql:host=$host;dbname=$db;charset=utf8", $user, $pass);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
} catch (PDOException $e) {
    die(json_encode(['status' => 'error', 'message' => 'Database connection failed: ' . $e->getMessage()]));
}

// Helper to check authentication
function checkAuth() {
    session_start();
    if (!isset($_SESSION['user_id'])) {
        echo json_encode(['status' => 'error', 'message' => 'Unauthorized']);
        exit;
    }
    return $_SESSION['user_id'];
}
?>
