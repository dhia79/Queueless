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

try {
    // 1. Generate unique suffix to avoid collisions
    $suffix = substr(md5(uniqid()), 0, 5);
    $password_hash = password_hash('password123', PASSWORD_DEFAULT);

    // 2. Insert Users
    $users = [
        ['name' => "Pharmacy Owner $suffix", 'email' => "pharmacy_$suffix@business.com", 'password' => $password_hash, 'role' => 'business'],
        ['name' => "Bank Manager $suffix", 'email' => "bank_$suffix@business.com", 'password' => $password_hash, 'role' => 'business'],
        ['name' => "Burger Chef $suffix", 'email' => "burger_$suffix@business.com", 'password' => $password_hash, 'role' => 'business'],
        ['name' => "Clinic Admin $suffix", 'email' => "clinic_$suffix@business.com", 'password' => $password_hash, 'role' => 'business'],

        ['name' => "Alice $suffix", 'email' => "alice_$suffix@email.com", 'password' => $password_hash, 'role' => 'customer'],
        ['name' => "Bob $suffix", 'email' => "bob_$suffix@email.com", 'password' => $password_hash, 'role' => 'customer'],
        ['name' => "Charlie $suffix", 'email' => "charlie_$suffix@email.com", 'password' => $password_hash, 'role' => 'customer'],
        ['name' => "Diana $suffix", 'email' => "diana_$suffix@email.com", 'password' => $password_hash, 'role' => 'customer'],
        ['name' => "Evan $suffix", 'email' => "evan_$suffix@email.com", 'password' => $password_hash, 'role' => 'customer']
    ];

    $stmt = $pdo->prepare("INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, ?)");
    foreach ($users as $user) {
        $stmt->execute([$user['name'], $user['email'], $user['password'], $user['role']]);
    }

    // Get User IDs for businesses
    $b_pharmacy_id = $pdo->query("SELECT id FROM users WHERE email='pharmacy_$suffix@business.com'")->fetchColumn();
    $b_bank_id = $pdo->query("SELECT id FROM users WHERE email='bank_$suffix@business.com'")->fetchColumn();
    $b_burger_id = $pdo->query("SELECT id FROM users WHERE email='burger_$suffix@business.com'")->fetchColumn();
    $b_clinic_id = $pdo->query("SELECT id FROM users WHERE email='clinic_$suffix@business.com'")->fetchColumn();

    // 3. Insert Businesses
    $businesses = [
        ['user_id' => $b_pharmacy_id, 'name' => "City Central Pharmacy $suffix", 'location' => '123 Main St, City Center', 'service_type' => 'Healthcare'],
        ['user_id' => $b_bank_id, 'name' => "National Trust Bank $suffix", 'location' => '45 Financial District', 'service_type' => 'Banking'],
        ['user_id' => $b_burger_id, 'name' => "Fast Bites Burgers $suffix", 'location' => '88 Food Court Ave', 'service_type' => 'Restaurant'],
        ['user_id' => $b_clinic_id, 'name' => "Sunrise General Clinic $suffix", 'location' => '10 Health Parkway', 'service_type' => 'Medical']
    ];

    $stmt = $pdo->prepare("INSERT INTO businesses (user_id, name, location, service_type) VALUES (?, ?, ?, ?)");
    foreach ($businesses as $b) {
        $stmt->execute([$b['user_id'], $b['name'], $b['location'], $b['service_type']]);
    }

    // Get Business IDs
    $biz_pharmacy = $pdo->query("SELECT id FROM businesses WHERE user_id=$b_pharmacy_id")->fetchColumn();
    $biz_bank = $pdo->query("SELECT id FROM businesses WHERE user_id=$b_bank_id")->fetchColumn();
    $biz_burger = $pdo->query("SELECT id FROM businesses WHERE user_id=$b_burger_id")->fetchColumn();
    $biz_clinic = $pdo->query("SELECT id FROM businesses WHERE user_id=$b_clinic_id")->fetchColumn();

    // 4. Insert Queues
    $queues = [
        ['business_id' => $biz_pharmacy, 'service_name' => "Prescription Pickup $suffix", 'avg_service_time' => 5],
        ['business_id' => $biz_bank, 'service_name' => "Teller Services $suffix", 'avg_service_time' => 8],
        ['business_id' => $biz_burger, 'service_name' => "Dine-in Waitlist $suffix", 'avg_service_time' => 45],
    ];

    $stmt = $pdo->prepare("INSERT INTO queues (business_id, service_name, avg_service_time) VALUES (?, ?, ?)");
    foreach ($queues as $q) {
        $stmt->execute([$q['business_id'], $q['service_name'], $q['avg_service_time']]);
    }

    // Get Queue IDs
    $q_pharma_pickup = $pdo->query("SELECT id FROM queues WHERE service_name='Prescription Pickup $suffix'")->fetchColumn();
    $q_bank_teller = $pdo->query("SELECT id FROM queues WHERE service_name='Teller Services $suffix'")->fetchColumn();
    $q_burger_waitlist = $pdo->query("SELECT id FROM queues WHERE service_name='Dine-in Waitlist $suffix'")->fetchColumn();

    // Get Customer IDs
    $c_alice = $pdo->query("SELECT id FROM users WHERE email='alice_$suffix@email.com'")->fetchColumn();
    $c_bob = $pdo->query("SELECT id FROM users WHERE email='bob_$suffix@email.com'")->fetchColumn();
    $c_charlie = $pdo->query("SELECT id FROM users WHERE email='charlie_$suffix@email.com'")->fetchColumn();
    $c_diana = $pdo->query("SELECT id FROM users WHERE email='diana_$suffix@email.com'")->fetchColumn();
    $c_evan = $pdo->query("SELECT id FROM users WHERE email='evan_$suffix@email.com'")->fetchColumn();

    // 5. Insert Queue Entries
    $entries = [
        ['user_id' => $c_alice, 'queue_id' => $q_pharma_pickup, 'position' => 1, 'status' => 'waiting'],
        ['user_id' => $c_bob, 'queue_id' => $q_pharma_pickup, 'position' => 2, 'status' => 'waiting'],
        ['user_id' => $c_charlie, 'queue_id' => $q_bank_teller, 'position' => 1, 'status' => 'waiting'],
        ['user_id' => $c_evan, 'queue_id' => $q_burger_waitlist, 'position' => 1, 'status' => 'waiting']
    ];

    $stmt = $pdo->prepare("INSERT INTO queue_entries (user_id, queue_id, position, status) VALUES (?, ?, ?, ?)");
    foreach ($entries as $e) {
        $stmt->execute([$e['user_id'], $e['queue_id'], $e['position'], $e['status']]);
    }

    echo json_encode(['status' => 'success', 'message' => 'Mock data generated successfully!']);

} catch (PDOException $e) {
    echo json_encode(['status' => 'error', 'message' => 'Error: ' . $e->getMessage()]);
}
?>
