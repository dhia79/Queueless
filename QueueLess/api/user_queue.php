<?php
require_once 'config.php';
$user_id = checkAuth();

$action = $_GET['action'] ?? '';

if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    if ($action === 'businesses') {
        // List businesses and their active queues
        $stmt = $pdo->query("SELECT b.id, b.name, b.location, b.service_type, 
                             q.id as queue_id, q.service_name, q.avg_service_time 
                             FROM businesses b 
                             LEFT JOIN queues q ON b.id = q.business_id AND q.is_active = TRUE");
        $rows = $stmt->fetchAll(PDO::FETCH_ASSOC);
        
        $businesses = [];
        foreach ($rows as $row) {
            $b_id = $row['id'];
            if (!isset($businesses[$b_id])) {
                $businesses[$b_id] = [
                    'id' => $b_id,
                    'name' => $row['name'],
                    'location' => $row['location'],
                    'service_type' => $row['service_type'],
                    'queues' => []
                ];
            }
            if ($row['queue_id']) {
                $businesses[$b_id]['queues'][] = [
                    'id' => $row['queue_id'],
                    'service_name' => $row['service_name'],
                    'avg_service_time' => $row['avg_service_time']
                ];
            }
        }
        
        echo json_encode(['status' => 'success', 'businesses' => array_values($businesses)]);
        exit;
    } elseif ($action === 'status') {
        // Get user's current queue status
        $queue_id = $_GET['queue_id'] ?? 0;
        
        // 1. Get user's entry with business and service details
        $stmt = $pdo->prepare("SELECT e.id, e.position, e.status, e.join_time, b.name as business_name, q.service_name 
                               FROM queue_entries e
                               JOIN queues q ON e.queue_id = q.id
                               JOIN businesses b ON q.business_id = b.id
                               WHERE e.user_id = ? AND e.queue_id = ? 
                               ORDER BY e.id DESC LIMIT 1");
        $stmt->execute([$user_id, $queue_id]);
        $entry = $stmt->fetch(PDO::FETCH_ASSOC);
        
        if (!$entry) {
            echo json_encode(['status' => 'error', 'message' => 'Not in this queue']);
            exit;
        }

        if ($entry['status'] !== 'waiting') {
            echo json_encode(['status' => 'success', 'entry' => $entry, 'wait_time' => 0, 'people_ahead' => 0]);
            exit;
        }

        // 2. Count people ahead
        $stmt = $pdo->prepare("SELECT count(*) as count FROM queue_entries WHERE queue_id = ? AND status = 'waiting' AND position < ?");
        $stmt->execute([$queue_id, $entry['position']]);
        $people_ahead = $stmt->fetch(PDO::FETCH_ASSOC)['count'];

        // 3. Get avg service time
        $stmt = $pdo->prepare("SELECT avg_service_time FROM queues WHERE id = ?");
        $stmt->execute([$queue_id]);
        $avg_time = $stmt->fetch(PDO::FETCH_ASSOC)['avg_service_time'];

        // Smart Calculation using the given formula
        // Assuming 1 service employee for simplicity in this basic version, can be expanded later
        $estimated_wait_time = $people_ahead * $avg_time;

        echo json_encode([
            'status' => 'success', 
            'entry' => $entry, 
            'people_ahead' => $people_ahead, 
            'estimated_wait_time' => $estimated_wait_time
        ]);
    }
} elseif ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $data = json_decode(file_get_contents("php://input"), true);
    
    if ($action === 'join') {
        $queue_id = $data['queue_id'] ?? 0;
        
        // Check if already in queue
        $stmt = $pdo->prepare("SELECT id FROM queue_entries WHERE user_id = ? AND queue_id = ? AND status = 'waiting'");
        $stmt->execute([$user_id, $queue_id]);
        if ($stmt->fetch()) {
            echo json_encode(['status' => 'error', 'message' => 'Already in this queue']);
            exit;
        }

        // Get max position for this queue
        $stmt = $pdo->prepare("SELECT MAX(position) as max_pos FROM queue_entries WHERE queue_id = ?");
        $stmt->execute([$queue_id]);
        $res = $stmt->fetch(PDO::FETCH_ASSOC);
        $next_pos = ($res['max_pos'] ?? 0) + 1;

        // Join queue
        $stmt = $pdo->prepare("INSERT INTO queue_entries (user_id, queue_id, position, status) VALUES (?, ?, ?, 'waiting')");
        $stmt->execute([$user_id, $queue_id, $next_pos]);
        
        echo json_encode(['status' => 'success', 'message' => 'Joined queue successfully']);
    } elseif ($action === 'leave') {
        $queue_id = $data['queue_id'] ?? 0;
        $stmt = $pdo->prepare("UPDATE queue_entries SET status = 'cancelled' WHERE user_id = ? AND queue_id = ? AND status = 'waiting'");
        $stmt->execute([$user_id, $queue_id]);
        echo json_encode(['status' => 'success', 'message' => 'Left queue']);
    }
}
?>
