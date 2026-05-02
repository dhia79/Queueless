import java.io.*;

public class LoggerRedirect {
    public static void main(String[] args) throws Exception {
        PrintStream out = new PrintStream(new FileOutputStream("/tmp/queueless_logs.txt", true));
        System.setOut(out);
        System.setErr(out);
        System.out.println("--- LOGGER REDIRECT STARTED ---");
    }
}
