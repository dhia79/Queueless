package queueless;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.io.*;
import java.time.LocalDateTime;

@SpringBootApplication
public class QueueLessApplication {
    public static void main(String[] args) {
        try {
            // Force redirect logs to a file we can definitely read
            File logFile = new File("c:/Users/dhiam/Projet PHP-JS/queueless_debug_logs.txt");
            PrintStream ps = new PrintStream(new FileOutputStream(logFile, true));
            System.setOut(ps);
            System.setErr(ps);
            System.out.println("=== QUEUELESS STARTUP " + java.time.LocalDateTime.now() + " ===");
        } catch (Exception e) {}
        SpringApplication.run(QueueLessApplication.class, args);
    }
}
