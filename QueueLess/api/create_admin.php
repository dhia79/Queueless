<?php
require_once 'config.php';

header('Content-Type: text/html; charset=utf-8');

try {
    $email = 'admin@queueless.com';
    $password = 'admin123';
    $hash = password_hash($password, PASSWORD_DEFAULT);
    
    $stmt = $pdo->prepare("SELECT id FROM users WHERE email = ?");
    $stmt->execute([$email]);
    $user = $stmt->fetch();
    
    if ($user) {
        $stmt = $pdo->prepare("UPDATE users SET password = ?, role = 'admin' WHERE email = ?");
        $stmt->execute([$hash, $email]);
        echo "<h2>Account updated</h2>";
    } else {
        $stmt = $pdo->prepare("INSERT INTO users (name, email, password, role) VALUES ('System Admin', ?, ?, 'admin')");
        $stmt->execute([$email, $hash]);
        echo "<h2>Account created</h2>";
    }
    
    echo "<ul><li>User: $email</li><li>Pass: $password</li></ul>";
    echo "<a href='../login.html'>Login</a>";

} catch (Exception $e) {
    echo "Error: " . htmlspecialchars($e->getMessage());
}
