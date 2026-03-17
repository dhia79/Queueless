# QueueLess - Smart Queue Management Platform

QueueLess is a practical academic web project designed to help users avoid waiting in physical lines by joining virtual queues for businesses like pharmacies, banks, and clinics. 

Built entirely without frameworks using **HTML, CSS, JavaScript, PHP, and MySQL**.

## Project Structure

```
QueueLess/
│
├── api/                        # PHP Backend (REST API)
│   ├── config.php              # Database connection
│   ├── auth.php                # Registration, Login, Session handling
│   ├── user_queue.php          # Customer API (list businesses, join/leave queue, status)
│   ├── business_queue.php      # Business API (create profile, manage queues, call next)
│   └── admin.php               # Admin API (system statistics, manage businesses)
│
├── assets/                     # Frontend Assets
│   ├── css/
│   │   └── style.css           # Global custom styling (Variables, Flexbox, Grid)
│   ├── js/
│   │   ├── main.js             # Global helpers, Auth check, API wrapper (`fetch`)
│   │   └── realtime.js         # JavaScript simulated real-time polling mechanism
│
├── index.html                  # Landing page
├── register.html               # Multi-role registration page
├── login.html                  # Login page
├── user_dashboard.html         # Customer interface (search services, join queue, see wait time)
├── business_dashboard.html     # Business interface (create queues, manage line, call next)
├── admin_dashboard.html        # Admin interface (overview stats, remove businesses)
│
├── database.sql                # SQL dump to recreate the database
└── README.md                   # This file
```

## Setup Instructions

1.  **Environment:** 
    *   Ensure you have a local server environment like XAMPP, WAMP, or MAMP installed.
    *   Drop the `QueueLess` folder into your server's document root (e.g., `C:\xampp\htdocs\QueueLess` for XAMPP).
2.  **Database:**
    *   Open phpMyAdmin (usually `http://localhost/phpmyadmin`).
    *   Create a new database named `queueless_db`.
    *   Import the provided `database.sql` file into the `queueless_db` database to create the necessary tables.
    *   *Note: The `api/config.php` file uses `root` as the user with an empty password by default. Adjust if your local database credentials differ.*
3.  **Run & Populate Data:**
    *   Open your browser and navigate to `http://localhost/QueueLess/index.html`.
    *   Click the "Generate Mock Data" button at the top of the homepage (or navigate directly to `http://localhost/QueueLess/api/seed.php`). This will instantly populate the database with dummy businesses, customers, queues, and people waiting in line.

## Smart Queue Algorithm Explained

The system calculates the **Estimated Wait Time** dynamically in `api/user_queue.php`.

The logic:
1.  **Count people ahead:** Queries the database for all users in the same queue with `status = 'waiting'` and a `position` strictly less than the current user's position. Let this be $P$.
2.  **Average service time:** Retrieves the average service time configured by the business for that specific queue. Let this be $T$.
3.  **Calculation:** Estimated Wait Time = $P \times T$.

*For example: If a clinic sets consultations to 15 minutes on average, and there are 4 people ahead of you, your estimated wait time is $4 \times 15 = 60$ minutes.*

## Core Features Implemented

*   **Role-Based Access Control:** Distinct roles for 'customer', 'business', and 'admin'.
*   **Asynchronous API:** All backend communication uses the JavaScript `fetch` API, enabling seamless UI updates without reloading the page.
*   **Real-time Simulation:** The `assets/js/realtime.js` script implements polling to fetch queue status updates every 5 seconds, creating a real-time feel for the user.
*   **Modern Vanilla CSS:** Utilizes CSS variables, flexbox, grid, and a clean, responsive color palette.
*   **Audio Notifications:** The user dashboard attempts to play a notification sound when their turn is approaching (0 or 1 person ahead).

## Suggestions for Future Improvements (Academic Presentation Points)

To expand on this project for a higher grade or real-world application:

1.  **True WebSockets:** Replace the current HTTP polling mechanism (`setInterval` in JS) with actual WebSockets (e.g., via Node.js/Socket.io or PHP Ratchet) for lower latency and better server resource management.
2.  **Advanced Wait Algorithm:** Integrate machine learning or historical data analysis to dynamically adjust the `avg_service_time` based on real performance rather than a static business input.
3.  **Multi-Teller Support:** Expand the database and algorithm to calculate wait times based on *multiple* service representatives handling the same queue concurrently.
4.  **Geolocation:** Use the browser's Geolocation API to automatically sort nearby businesses by distance on the user dashboard.
5.  **QR Code Integration:** Allow businesses to print a QR code that users can scan with their phone camera to instantly join a queue without searching.
