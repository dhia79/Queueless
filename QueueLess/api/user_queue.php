<?php
require_once 'config.php';
$user_id = checkAuth();

header('Content-Type: application/json');

$action = $_GET['action'] ?? '';

if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    if ($action === 'businesses') {
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
        $queue_id = $_GET['queue_id'] ?? 0;
        
        $stmt = $pdo->prepare("SELECT e.id, e.position, e.status, e.join_time, b.name as business_name, q.service_name 
                               FROM queue_entries e
                               JOIN queues q ON e.queue_id = q.id
                               JOIN businesses b ON q.business_id = b.id
                               WHERE e.user_id = ? AND e.queue_id = ? 
                               ORDER BY e.id DESC LIMIT 1");
        $stmt->execute([$user_id, $queue_id]);
        $entry = $stmt->fetch(PDO::FETCH_ASSOC);
        
        if (!$entry) {
            echo json_encode(['status' => 'error', 'message' => 'Record not found']);
            exit;
        }

        if ($entry['status'] !== 'waiting') {
            echo json_encode(['status' => 'success', 'entry' => $entry, 'wait_time' => 0, 'people_ahead' => 0]);
            exit;
        }

        $stmt = $pdo->prepare("SELECT COUNT(*) FROM queue_entries WHERE queue_id = ? AND status = 'waiting' AND position < ?");
        $stmt->execute([$queue_id, $entry['position']]);
        $people_ahead = $stmt->fetchColumn();

        $stmt = $pdo->prepare("SELECT avg_service_time FROM queues WHERE id = ?");
        $stmt->execute([$queue_id]);
        $avg_time = $stmt->fetchColumn();

        echo json_encode([
            'status' => 'success', 
            'entry' => $entry, 
            'people_ahead' => (int)$people_ahead, 
            'estimated_wait_time' => $people_ahead * $avg_time
        ]);
    }
} elseif ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $data = json_decode(file_get_contents("php://input"), true);
    
    if ($action === 'join') {
        $queue_id = $data['queue_id'] ?? 0;
        
        $stmt = $pdo->prepare("SELECT id FROM queue_entries WHERE user_id = ? AND queue_id = ? AND status = 'waiting'");
        $stmt->execute([$user_id, $queue_id]);
        if ($stmt->fetch()) {
            echo json_encode(['status' => 'error', 'message' => 'Already active in queue']);
            exit;
        }

        $stmt = $pdo->prepare("SELECT MAX(position) FROM queue_entries WHERE queue_id = ?");
        $stmt->execute([$queue_id]);
        $next_pos = (int)$stmt->fetchColumn() + 1;

        $stmt = $pdo->prepare("INSERT INTO queue_entries (user_id, queue_id, position, status) VALUES (?, ?, ?, 'waiting')");
        $stmt->execute([$user_id, $queue_id, $next_pos]);
        
        echo json_encode(['status' => 'success']);
    } elseif ($action === 'leave') {
        $queue_id = $data['queue_id'] ?? 0;
        $stmt = $pdo->prepare("UPDATE queue_entries SET status = 'cancelled' WHERE user_id = ? AND queue_id = ? AND status = 'waiting'");
        $stmt->execute([$user_id, $queue_id]);
        echo json_encode(['status' => 'success']);
    }
}
