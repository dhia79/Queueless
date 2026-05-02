const QueueMonitor = {
    interval: null,

    start(queueId, callback, intervalMs = 5000) {
        this.stop();
        this.sync(queueId, callback);
        this.interval = setInterval(() => this.sync(queueId, callback), intervalMs);
    },

    stop() {
        if (this.interval) {
            clearInterval(this.interval);
            this.interval = null;
        }
    },

    async sync(queueId, callback) {
        try {
            const data = await apiCall('status&queue_id=' + queueId, 'GET', null, 'user_queue.php');
            callback(data);
        } catch (err) {
            console.error('[Monitor] Sync failed:', err);
        }
    }
};

window.startQueuePolling = (id, cb, ms) => QueueMonitor.start(id, cb, ms);
window.stopQueuePolling = () => QueueMonitor.stop();

