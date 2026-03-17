// Real-time polling logic to simulate WebSockets

window.queuePollingInterval = null;

/**
 * Starts polling the server for queue status updates.
 * @param {number} queueId - The queue ID to track.
 * @param {function} callback - Function receiving the updated data.
 * @param {number} intervalMs - Polling interval in milliseconds (default: 5000).
 */
function startQueuePolling(queueId, callback, intervalMs = 5000) {
    if (window.queuePollingInterval) {
        clearInterval(window.queuePollingInterval);
    }

    // Initial fetch immediately
    fetchStatus(queueId, callback);

    // Setup interval
    window.queuePollingInterval = setInterval(() => {
        fetchStatus(queueId, callback);
    }, intervalMs);
}

/**
 * Stops any active polling.
 */
function stopQueuePolling() {
    if (window.queuePollingInterval) {
        clearInterval(window.queuePollingInterval);
        window.queuePollingInterval = null;
    }
}

async function fetchStatus(queueId, callback) {
    try {
        const res = await apiCall('status&queue_id=' + queueId, 'GET', null, 'user_queue.php');
        callback(res);
    } catch (err) {
        console.error("Polling error: ", err);
    }
}
