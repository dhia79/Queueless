<?php
require_once 'config.php';
$user_id = checkAuth();

// Quick check if user is a business
$stmt = $pdo->prepare("SELECT role FROM users WHERE id = ?");
$stmt->execute([$user_id]);
$role = $stmt->fetchColumn();

if ($role !== 'business') {
    echo json_encode(['status' => 'error', 'message' => 'Access denied']);
    exit;
}

$action = $_GET['action'] ?? '';

// Check if user has a business profile, if not and action isn't create_profile, fail
$stmt = $pdo->prepare("SELECT id FROM businesses WHERE user_id = ?");
$stmt->execute([$user_id]);
$business_id = $stmt->fetchColumn();

if ($_SERVER['REQUEST_METHOD'] === 'GET') {
     if ($action === 'profile') {
          if ($business_id) {
               $stmt = $pdo->prepare("SELECT * FROM businesses WHERE user_id = ?");
               $stmt->execute([$user_id]);
               echo json_encode(['status' => 'success', 'business' => $stmt->fetch(PDO::FETCH_ASSOC)]);
          } else {
               echo json_encode(['status' => 'error', 'message' => 'No business profile']);
          }
     } elseif ($action === 'queues') {
         if (!$business_id) exit(json_encode(['status' => 'error', 'message' => 'No business profile']));
         
         $stmt = $pdo->prepare("SELECT * FROM queues WHERE business_id = ?");
         $stmt->execute([$business_id]);
         $queues = $stmt->fetchAll(PDO::FETCH_ASSOC);
         
         foreach ($queues as &$q) {
             // get current waiting count
             $s = $pdo->prepare("SELECT count(*) as count FROM queue_entries WHERE queue_id = ? AND status = 'waiting'");
             $s->execute([$q['id']]);
             $q['waiting_count'] = $s->fetchColumn();
             
             // get next user
             $s = $pdo->prepare("SELECT qe.id, u.name, qe.position, qe.join_time 
                                 FROM queue_entries qe 
                                 JOIN users u ON qe.user_id = u.id 
                                 WHERE qe.queue_id = ? AND qe.status = 'waiting' 
                                 ORDER BY qe.position ASC LIMIT 1");
             $s->execute([$q['id']]);
             $q['next_customer'] = $s->fetch(PDO::FETCH_ASSOC);
         }
         echo json_encode(['status' => 'success', 'queues' => $queues]);
     }
} elseif ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $data = json_decode(file_get_contents("php://input"), true);
    
    if ($action === 'create_profile') {
        if ($business_id) exit(json_encode(['status' => 'error', 'message' => 'Profile already exists']));
        
        $name = $data['name'] ?? '';
        $location = $data['location'] ?? '';
        $type = $data['service_type'] ?? '';
        
        $stmt = $pdo->prepare("INSERT INTO businesses (user_id, name, location, service_type) VALUES (?, ?, ?, ?)");
        $stmt->execute([$user_id, $name, $location, $type]);
        echo json_encode(['status' => 'success', 'message' => 'Profile created']);
    } elseif ($action === 'create_queue') {
         if (!$business_id) exit(json_encode(['status' => 'error', 'message' => 'No business profile']));
         
         $service_name = $data['service_name'] ?? '';
         $avg_time = $data['avg_service_time'] ?? 5; // default 5 mins
         
         $stmt = $pdo->prepare("INSERT INTO queues (business_id, service_name, avg_service_time) VALUES (?, ?, ?)");
         $stmt->execute([$business_id, $service_name, $avg_time]);
         echo json_encode(['status' => 'success', 'message' => 'Queue created']);
    } elseif ($action === 'call_next') {
         if (!$business_id) exit(json_encode(['status' => 'error', 'message' => 'No business profile']));
         $queue_id = $data['queue_id'] ?? 0;
         
         // Security Check: Does this queue belong to this business?
         $stmt = $pdo->prepare("SELECT id FROM queues WHERE id = ? AND business_id = ?");
         $stmt->execute([$queue_id, $business_id]);
         if (!$stmt->fetch()) {
             echo json_encode(['status' => 'error', 'message' => 'Unauthorized queue access']);
             exit;
         }

         // mark the first waiting person as served
         $stmt = $pdo->prepare("SELECT id FROM queue_entries WHERE queue_id = ? AND status = 'waiting' ORDER BY position ASC LIMIT 1");
         $stmt->execute([$queue_id]);
         $entry_id = $stmt->fetchColumn();
         
         if ($entry_id) {
             $stmt = $pdo->prepare("UPDATE queue_entries SET status = 'served' WHERE id = ?");
             $stmt->execute([$entry_id]);
             echo json_encode(['status' => 'success', 'message' => 'Customer marked as served']);
         } else {
             echo json_encode(['status' => 'error', 'message' => 'No customers waiting']);
         }
    }
}
?>
