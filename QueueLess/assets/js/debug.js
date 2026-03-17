/**
 * QueueLess Developer Debugging Suite
 * provides an in-app console to monitor state, network, and system diagnostics.
 */

class DebugConsole {
    constructor() {
        this.isOpen = false;
        this.logs = [];
        this.maxLogs = 20;
        this.init();
    }

    init() {
        this.createStyles();
        this.createUI();
        this.setupListeners();
        this.refreshSession();
        console.log("🛠️ QueueLess Debug Console Initialized");
    }

    createStyles() {
        const style = document.createElement('style');
        style.textContent = `
            #debug-trigger {
                position: fixed;
                bottom: 24px;
                right: 24px;
                z-index: 10000;
                background: #020617;
                color: #6366f1;
                border: 1px solid rgba(99, 102, 241, 0.4);
                padding: 12px 20px;
                border-radius: 99px;
                cursor: pointer;
                font-family: inherit;
                font-weight: 700;
                font-size: 13px;
                box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
                display: flex;
                align-items: center;
                gap: 8px;
                transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            }
            #debug-trigger:hover { transform: translateY(-2px); box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1); background: #0f172a; }

            #debug-panel {
                position: fixed;
                bottom: 88px;
                right: 24px;
                width: 420px;
                height: 560px;
                background: rgba(2, 6, 23, 0.9);
                backdrop-filter: blur(24px);
                border: 1px solid rgba(255, 255, 255, 0.1);
                border-radius: 24px;
                z-index: 9999;
                display: none;
                flex-direction: column;
                color: #f1f5f9;
                font-family: 'Consolas', monospace;
                box-shadow: var(--shadow-premium);
                overflow: hidden;
            }
            #debug-panel.active { display: flex; }

            .debug-header {
                padding: 20px 24px;
                border-bottom: 1px solid rgba(255, 255, 255, 0.05);
                display: flex;
                justify-content: space-between;
                align-items: center;
            }
            .debug-tabs {
                display: flex;
                background: #0f172a;
                padding: 4px;
                border-radius: 12px;
            }
            .debug-tab {
                padding: 6px 14px;
                cursor: pointer;
                font-size: 11px;
                border-radius: 8px;
                color: #94a3b8;
                transition: all 0.2s;
            }
            .debug-tab.active { background: #6366f1; color: white; font-weight: 700; }

            .debug-content { flex: 1; overflow-y: auto; padding: 20px; font-size: 12px; }
            .debug-footer {
                padding: 20px;
                border-top: 1px solid rgba(255, 255, 255, 0.05);
                display: flex;
                gap: 12px;
            }
            .debug-btn {
                background: rgba(255,255,255,0.03);
                border: 1px solid rgba(255, 255, 255, 0.1);
                color: white;
                padding: 10px 16px;
                border-radius: 12px;
                font-size: 11px;
                cursor: pointer;
                flex: 1;
                transition: all 0.2s;
            }
            .debug-btn-danger { color: #f87171; border-color: rgba(248, 113, 113, 0.2); }
            .debug-btn:hover { background: rgba(255, 255, 255, 0.08); }

            .log-item { border-bottom: 1px solid rgba(255,255,255,0.03); padding: 10px 0; }
            .log-time { color: #475569; margin-right: 8px; }
            .log-action { color: #6366f1; font-weight: 700; }
            .log-status-success { color: #10b981; }
            .log-status-error { color: #ef4444; }

            .stat-row { display: flex; justify-content: space-between; margin-bottom: 10px; }
            .stat-label { color: #64748b; }
            .stat-value { color: #6366f1; font-weight: 700; }
        `;
        document.head.appendChild(style);
    }

    createUI() {
        const trigger = document.createElement('button');
        trigger.id = 'debug-trigger';
        trigger.innerHTML = '🛠️ DEBUG';
        document.body.appendChild(trigger);

        const panel = document.createElement('div');
        panel.id = 'debug-panel';
        panel.innerHTML = `
            <div class="debug-header">
                <span style="font-weight: 700; color: #38bdf8;">QueueLess Console</span>
                <div class="debug-tabs">
                    <div class="debug-tab active" data-tab="session">Session</div>
                    <div class="debug-tab" data-tab="network">Network</div>
                    <div class="debug-tab" data-tab="system">System</div>
                </div>
            </div>
            <div class="debug-content" id="debug-content">
                <!-- Content here -->
            </div>
            <div class="debug-footer">
                <button class="debug-btn" id="debug-refresh">Refresh Diagnostics</button>
                <button class="debug-btn debug-btn-danger" id="debug-restart">Restart App</button>
            </div>
        `;
        document.body.appendChild(panel);
    }

    setupListeners() {
        document.getElementById('debug-trigger').onclick = () => {
            this.isOpen = !this.isOpen;
            document.getElementById('debug-panel').classList.toggle('active', this.isOpen);
        };

        document.querySelectorAll('.debug-tab').forEach(tab => {
            tab.onclick = () => {
                document.querySelectorAll('.debug-tab').forEach(t => t.classList.remove('active'));
                tab.classList.add('active');
                this.renderTab(tab.dataset.tab);
            };
        });

        document.getElementById('debug-restart').onclick = () => {
            if (confirm("Restart Application? This will clear all local data and sessions.")) {
                localStorage.clear();
                window.location.href = 'index.html';
            }
        };

        document.getElementById('debug-refresh').onclick = () => this.fetchDiagnostics();

        // Initial render
        this.renderTab('session');
    }

    renderTab(tab) {
        const container = document.getElementById('debug-content');
        container.innerHTML = '';

        if (tab === 'session') {
            const user = JSON.parse(localStorage.getItem('user') || 'null');
            const activeQueue = JSON.parse(localStorage.getItem('active_queue') || 'null');
            
            container.innerHTML = `
                <h4 style="margin-bottom: 12px; color: #38bdf8;">Storage State</h4>
                <div class="stat-row"><span class="stat-label">User ID:</span><span class="stat-value">${user?.id || 'null'}</span></div>
                <div class="stat-row"><span class="stat-label">User Role:</span><span class="stat-value">${user?.role || 'Guest'}</span></div>
                <div class="stat-row"><span class="stat-label">User Name:</span><span class="stat-value">${user?.name || '-'}</span></div>
                <div class="stat-row"><span class="stat-label">Active Queue ID:</span><span class="stat-value">${activeQueue?.id || 'None'}</span></div>
                <div class="stat-row" style="margin-top: 20px;"><span class="stat-label">Cookies Support:</span><span class="stat-value">${navigator.cookieEnabled}</span></div>
            `;
        } else if (tab === 'network') {
            if (this.logs.length === 0) {
                container.innerHTML = '<p style="color: #64748b; text-align: center; margin-top: 40px;">No API traffic captured yet.</p>';
            } else {
                this.logs.slice().reverse().forEach(log => {
                    const item = document.createElement('div');
                    item.className = 'log-item';
                    item.innerHTML = `
                        <div>
                            <span class="log-time">${log.time}</span>
                            <span class="log-action">${log.action}</span>
                            <span class="log-status-${log.status}">${log.status === 'success' ? '✓' : '✗'}</span>
                        </div>
                        <div style="font-size: 10px; color: #64748b; margin-top: 4px;">${log.endpoint}</div>
                    `;
                    container.appendChild(item);
                });
            }
        } else if (tab === 'system') {
            container.innerHTML = '<p style="color: #64748b; text-align: center; margin-top: 40px;">Loading Diagnostics...</p>';
            this.fetchDiagnostics();
        }
    }

    async fetchDiagnostics() {
        const user = JSON.parse(localStorage.getItem('user') || 'null');
        if (user?.role !== 'admin') {
            document.getElementById('debug-content').innerHTML = `
                <div style="text-align:center; margin-top: 40px;">
                    <p style="color:#f87171;">Access Denied</p>
                    <p style="font-size: 11px; margin-top:8px;">Only Administrators can view system diagnostics via the probe API.</p>
                </div>
            `;
            return;
        }

        try {
            const res = await apiCall('probe', 'GET', null, 'probe.php');
            if (res.status === 'success') {
                const d = res.diagnostics;
                document.getElementById('debug-content').innerHTML = `
                    <h4 style="margin-bottom: 12px; color: #38bdf8;">Server Info</h4>
                    <div class="stat-row"><span class="stat-label">PHP Version:</span><span class="stat-value">${d.php_version}</span></div>
                    <div class="stat-row"><span class="stat-label">Software:</span><span class="stat-value" style="font-size:10px">${d.server_software}</span></div>
                    <div class="stat-row"><span class="stat-label">DB Contact:</span><span class="stat-value">${d.db_status}</span></div>
                    <div class="stat-row"><span class="stat-label">Server Time:</span><span class="stat-value" style="font-size:10px">${d.time_server}</span></div>
                    
                    <h4 style="margin: 20px 0 12px; color: #38bdf8;">Extensions</h4>
                    <div class="stat-row"><span class="stat-label">PDO / MySQL:</span><span class="stat-value">${d.extensions.pdo ? '✓' : '✗'} / ${d.extensions.pdo_mysql ? '✓' : '✗'}</span></div>
                    <div class="stat-row"><span class="stat-label">Session:</span><span class="stat-value">${d.extensions.session ? '✓' : '✗'}</span></div>
                    <div class="stat-row"><span class="stat-label">Memory:</span><span class="stat-value">${d.memory_usage}</span></div>
                `;
            } else {
                document.getElementById('debug-content').innerHTML = `<p style="color:#f87171;">Error: ${res.message}</p>`;
            }
        } catch (e) {
            document.getElementById('debug-content').innerHTML = `<p style="color:#f87171;">Failed to connect to probe.php</p>`;
        }
    }

    addLog(action, status, endpoint) {
        this.logs.push({
            time: new Date().toLocaleTimeString(),
            action,
            status,
            endpoint
        });
        if (this.logs.length > this.maxLogs) this.logs.shift();
        
        // If the network tab is active, re-render
        const activeTab = document.querySelector('.debug-tab.active');
        if (activeTab && activeTab.dataset.tab === 'network') {
            this.renderTab('network');
        }
    }

    refreshSession() {
        // Poll storage every 2s
        setInterval(() => {
            const activeTab = document.querySelector('.debug-tab.active');
            if (activeTab && activeTab.dataset.tab === 'session') {
                this.renderTab('session');
            }
        }, 2000);
    }
}

// Global instance
window.QueueLessDebug = new DebugConsole();
