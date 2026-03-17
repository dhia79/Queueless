<?php
require_once 'config.php';

try {
    $email = 'admin@queueless.com';
    $password = 'admin123';
    $hash = password_hash($password, PASSWORD_DEFAULT);
    
    // Check if admin exists
    $stmt = $pdo->prepare("SELECT id FROM users WHERE email = ?");
    $stmt->execute([$email]);
    $user = $stmt->fetch();
    
    if ($user) {
        // Update password and ensure role is admin
        $stmt = $pdo->prepare("UPDATE users SET password = ?, role = 'admin' WHERE email = ?");
        $stmt->execute([$hash, $email]);
        echo "<h3>✅ Admin account updated successfully!</h3>";
    } else {
        // Create new admin
        $stmt = $pdo->prepare("INSERT INTO users (name, email, password, role) VALUES ('System Admin', ?, ?, 'admin')");
        $stmt->execute([$email, $hash]);
        echo "<h3>✅ Admin account created successfully!</h3>";
    }
    
    echo "<p>You can now log in securely with:</p>";
    echo "<ul>";
    echo "<li><strong>Email:</strong> " . htmlspecialchars($email) . "</li>";
    echo "<li><strong>Password:</strong> " . htmlspecialchars($password) . "</li>";
    echo "</ul>";
    echo "<a href='../login.html'>Go to Login Page</a>";

} catch (Exception $e) {
    echo "<h3>❌ Database Error:</h3> " . htmlspecialchars($e->getMessage());
}
?>
