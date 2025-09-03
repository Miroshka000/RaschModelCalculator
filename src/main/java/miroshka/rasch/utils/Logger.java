package miroshka.rasch.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class Logger {
    private static final String LOG_FILE_NAME = "log.txt";
    private static PrintWriter fileWriter;
    private static boolean initialized = false;

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void initialize() {
        if (initialized) {
            return;
        }

        try {
            String programDir = getProgramDirectory();
            File logFile = new File(programDir, LOG_FILE_NAME);

            File parentDir = logFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            fileWriter = new PrintWriter(new FileWriter(logFile, true), true);

            initialized = true;

            log("Логгер инициализирован. Файл логов: " + logFile.getAbsolutePath());

        } catch (IOException e) {
            System.err.println("Ошибка инициализации логгера: " + e.getMessage());
        }
    }

    public static void log(String message) {
        log(message, LogLevel.INFO);
    }

    public static void log(String message, LogLevel level) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String formattedMessage = String.format("[%s] [%s] %s",
            timestamp, level.name(), message);

        System.out.println(formattedMessage);

        if (fileWriter != null) {
            try {
                fileWriter.println(formattedMessage);
                fileWriter.flush();
            } catch (Exception e) {
                System.err.println("Ошибка записи в лог файл: " + e.getMessage());
            }
        }
    }

    public static void error(String message) {
        log(message, LogLevel.ERROR);
    }

    public static void error(String message, Throwable throwable) {
        log(message + ": " + throwable.getMessage(), LogLevel.ERROR);
        if (throwable.getCause() != null) {
            log("Причина: " + throwable.getCause().getMessage(), LogLevel.ERROR);
        }
    }

    public static void close() {
        if (fileWriter != null) {
            fileWriter.close();
            fileWriter = null;
        }
        initialized = false;
    }

    private static String getProgramDirectory() {
        try {
            String path = new File(Logger.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI()).getAbsolutePath();

            File file = new File(path);

            if (path.endsWith(".jar")) {
                return file.getParent();
            }

            if (file.isDirectory()) {
                return System.getProperty("user.dir");
            }

            return file.getParent();

        } catch (Exception e) {
            return System.getProperty("user.dir");
        }
    }

    public enum LogLevel {
        INFO, WARN, ERROR, DEBUG
    }
}
