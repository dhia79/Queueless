<?php
require_once 'config.php';
$user_id = checkAuth();

header('Content-Type: application/json');

$stmt = $pdo->prepare("SELECT role FROM users WHERE id = ?");
$stmt->execute([$user_id]);
if ($stmt->fetchColumn() !== 'admin') exit(json_encode(['status' => 'error', 'message' => 'Unauthorized']));

try {
    $suffix = substr(md5(uniqid()), 0, 5);
    $hash = password_hash('password123', PASSWORD_DEFAULT);

    $users = [
        ['name' => "Pharmacy Owner $suffix", 'email' => "pharmacy_$suffix@business.com", 'password' => $hash, 'role' => 'business'],
        ['name' => "Bank Manager $suffix", 'email' => "bank_$suffix@business.com", 'password' => $hash, 'role' => 'business'],
        ['name' => "Burger Chef $suffix", 'email' => "burger_$suffix@business.com", 'password' => $hash, 'role' => 'business'],
        ['name' => "Clinic Admin $suffix", 'email' => "clinic_$suffix@business.com", 'password' => $hash, 'role' => 'business'],
        ['name' => "Alice $suffix", 'email' => "alice_$suffix@email.com", 'password' => $hash, 'role' => 'customer'],
        ['name' => "Bob $suffix", 'email' => "bob_$suffix@email.com", 'password' => $hash, 'role' => 'customer']
    ];

    $stmt = $pdo->prepare("INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, ?)");
    foreach ($users as $user) {
        $stmt->execute([$user['name'], $user['email'], $user['password'], $user['role']]);
    }

    $b_pharmacy = $pdo->query("SELECT id FROM users WHERE email='pharmacy_$suffix@business.com'")->fetchColumn();
    $b_bank = $pdo->query("SELECT id FROM users WHERE email='bank_$suffix@business.com'")->fetchColumn();

    $businesses = [
        ['user_id' => $b_pharmacy, 'name' => "City Central Pharmacy $suffix", 'location' => '123 Main St', 'service_type' => 'Healthcare'],
        ['user_id' => $b_bank, 'name' => "National Trust Bank $suffix", 'location' => '45 Financial District', 'service_type' => 'Banking']
    ];

    $stmt = $pdo->prepare("INSERT INTO businesses (user_id, name, location, service_type) VALUES (?, ?, ?, ?)");
    foreach ($businesses as $b) {
        $stmt->execute([$b['user_id'], $b['name'], $b['location'], $b['service_type']]);
    }

    $biz_pharmacy = $pdo->query("SELECT id FROM businesses WHERE user_id=$b_pharmacy")->fetchColumn();
    $stmt = $pdo->prepare("INSERT INTO queues (business_id, service_name, avg_service_time) VALUES (?, ?, ?)");
    $stmt->execute([$biz_pharmacy, "Prescription Pickup $suffix", 5]);

    echo json_encode(['status' => 'success', 'seed' => $suffix]);

} catch (PDOException $e) {
    echo json_encode(['status' => 'error', 'message' => $e->getMessage()]);
}
