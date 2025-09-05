package miroshka.rasch.utils;

import java.awt.Desktop;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import javafx.application.Platform;

public final class UpdateManager {
    private static final int TIMEOUT_MS = 30000; // 30 seconds
    private static final int BUFFER_SIZE = 8192;
    private static final String TEMP_DIR_PREFIX = "rasch-update-";
    private static final String UPDATE_SCRIPT_NAME = "update.bat";
    
    public CompletableFuture<File> downloadUpdateAsync(String downloadUrl, Consumer<Integer> progressCallback) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return downloadUpdate(downloadUrl, progressCallback);
            } catch (Exception e) {
                try {
                    throw new UpdateException("Failed to download update", e);
                } catch (UpdateException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }
    
    public File downloadUpdate(String downloadUrl, Consumer<Integer> progressCallback) throws UpdateException {
        Objects.requireNonNull(downloadUrl, "Download URL cannot be null");
        
        try {
            Path tempDir = Files.createTempDirectory(TEMP_DIR_PREFIX);
            String fileName = extractFileNameFromUrl(downloadUrl);
            File targetFile = tempDir.resolve(fileName).toFile();
            
            downloadFile(downloadUrl, targetFile, progressCallback);
            
            return targetFile;
            
        } catch (IOException e) {
            throw new UpdateException("Failed to download update file", e);
        }
    }
    
    private void downloadFile(String downloadUrl, File targetFile, Consumer<Integer> progressCallback) 
            throws IOException {
        URL url = URI.create(downloadUrl).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        try {
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
            connection.setRequestProperty("User-Agent", "RaschModelCalculator-Updater");
            
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("Download failed with status code: " + responseCode);
            }
            
            long totalSize = connection.getContentLengthLong();
            long downloadedSize = 0;
            
            try (BufferedInputStream input = new BufferedInputStream(connection.getInputStream());
                 FileOutputStream output = new FileOutputStream(targetFile)) {
                
                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                
                while ((bytesRead = input.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                    downloadedSize += bytesRead;
                    
                    if (progressCallback != null && totalSize > 0) {
                        int progress = (int) ((downloadedSize * 100) / totalSize);
                        Platform.runLater(() -> progressCallback.accept(progress));
                    }
                }
            }
            
        } finally {
            connection.disconnect();
        }
    }
    
    private String extractFileNameFromUrl(String url) {
        try {
            String path = URI.create(url).getPath();
            int lastSlash = path.lastIndexOf('/');
            if (lastSlash >= 0 && lastSlash < path.length() - 1) {
                return path.substring(lastSlash + 1);
            }
        } catch (Exception e) {
        }
        return "RaschModelCalculator-update.exe";
    }
    
    public void installUpdateAndRestart(File updateFile) throws UpdateException {
        Objects.requireNonNull(updateFile, "Update file cannot be null");
        
        if (!updateFile.exists()) {
            throw new UpdateException("Update file does not exist: " + updateFile.getAbsolutePath());
        }
        
        try {
            String currentJarPath = getCurrentJarPath();
            if (currentJarPath == null) {
                openInstaller(updateFile);
                return;
            }
            
            File updateScript = createUpdateScript(updateFile, currentJarPath);
            
            executeUpdateScript(updateScript);
            
        } catch (IOException e) {
            throw new UpdateException("Failed to install update", e);
        }
    }
    
    private String getCurrentJarPath() {
        try {
            String executablePath = ProcessHandle.current()
                .info()
                .command()
                .orElse("");
                
            if (!executablePath.isEmpty() && new File(executablePath).exists()) {
                Logger.log("Found executable path from ProcessHandle: " + executablePath);
                return executablePath;
            }
            
            String javaCommand = System.getProperty("sun.java.command");
            if (javaCommand != null && javaCommand.endsWith(".exe")) {
                File exeFile = new File(javaCommand);
                if (exeFile.exists()) {
                    Logger.log("Found executable from sun.java.command: " + javaCommand);
                    return exeFile.getAbsolutePath();
                }
            }
            
            String appImage = System.getProperty("jpackage.app-path");
            if (appImage != null) {
                File appFile = new File(appImage);
                if (appFile.exists()) {
                    Logger.log("Found app path from jpackage.app-path: " + appImage);
                    return appFile.getAbsolutePath();
                }
            }
            
            URL location = UpdateManager.class.getProtectionDomain().getCodeSource().getLocation();
            if (location != null && !"jrt".equals(location.getProtocol())) {
                String path = new File(location.toURI()).getAbsolutePath();
                Logger.log("Found path from code source: " + path);
                return path;
            }
            
            String currentDir = System.getProperty("user.dir");
            File exeFile = findExecutableInDirectory(new File(currentDir));
            if (exeFile != null) {
                Logger.log("Found executable in current directory: " + exeFile.getAbsolutePath());
                return exeFile.getAbsolutePath();
            }
            
            Logger.log("Unable to determine executable path");
            return null;
            
        } catch (Exception e) {
            Logger.error("Exception getting executable path: " + e.getMessage());
            return null;
        }
    }

    private File findExecutableInDirectory(File directory) {
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            return null;
        }

        String[] executableNames = {
            "RaschModelCalculator.exe",
            "RaschCalculator.exe",
            "Rasch.exe"
        };
        
        for (String execName : executableNames) {
            File execFile = new File(directory, execName);
            if (execFile.exists() && execFile.isFile()) {
                return execFile;
            }
        }
        
        File[] files = directory.listFiles((dir, name) -> 
            name.toLowerCase().endsWith(".exe") && 
            (name.toLowerCase().contains("rasch") || 
             name.toLowerCase().contains("calculator"))
        );
        
        if (files != null && files.length > 0) {
            return files[0];
        }
        
        File parentDir = directory.getParentFile();
        if (parentDir != null && !parentDir.equals(directory)) {
            return findExecutableInDirectory(parentDir);
        }
        
        return null;
    }
    
    private void openInstaller(File updateFile) throws UpdateException {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(updateFile);
            } else {
                new ProcessBuilder("cmd", "/c", "start", updateFile.getAbsolutePath()).start();
            }
        } catch (IOException e) {
            throw new UpdateException("Failed to open installer", e);
        }
    }
    
    private File createUpdateScript(File updateFile, String currentJarPath) throws IOException {
        File scriptFile = new File(updateFile.getParent(), UPDATE_SCRIPT_NAME);
        
        StringBuilder script = new StringBuilder();
        script.append("@echo off\n");
        script.append("echo Starting Rasch Model Calculator update...\n");
        script.append("timeout /t 2 /nobreak > nul\n");
        script.append("\n");
        
        script.append("echo Running installer...\n");
        script.append("\"").append(updateFile.getAbsolutePath()).append("\"\n");
        script.append("\n");
        
        script.append("echo Update completed!\n");
        script.append("timeout /t 2 /nobreak > nul\n");
        script.append("\n");
        
        script.append("echo Cleaning up...\n");
        script.append("del \"").append(updateFile.getAbsolutePath()).append("\"\n");
        script.append("del \"").append(scriptFile.getAbsolutePath()).append("\"\n");
        
        try (FileOutputStream fos = new FileOutputStream(scriptFile)) {
            fos.write(script.toString().getBytes("UTF-8"));
        }
        
        return scriptFile;
    }
    
    private void executeUpdateScript(File scriptFile) throws IOException {
        ProcessBuilder pb = new ProcessBuilder("cmd", "/c", scriptFile.getAbsolutePath());
        pb.directory(scriptFile.getParentFile());
        pb.start();
        
        Platform.exit();
        System.exit(0);
    }
    
    public boolean isUpdateSupported() {
        try {
            Logger.log("Checking if update is supported...");

            String executablePath = getCurrentJarPath();
            Logger.log("Current executable path: " + executablePath);

            if (executablePath == null || executablePath.isEmpty()) {
                Logger.log("Executable path is empty - update not supported");
                return false;
            }

            String os = System.getProperty("os.name").toLowerCase();
            Logger.log("Operating system: " + os);

            if (!os.contains("windows")) {
                Logger.log("Not Windows OS - update not supported");
                return false;
            }

            File executableFile = new File(executablePath);
            if (!executableFile.exists()) {
                Logger.log("Executable file does not exist: " + executablePath);
                return false;
            }
            
            Logger.log("Executable file exists: " + executableFile.getAbsolutePath());

            File parentDir = executableFile.getParentFile();
            if (parentDir == null) {
                Logger.log("Parent directory is null");
                return false;
            }
            
            Logger.log("Parent directory: " + parentDir.getAbsolutePath());
            boolean canWrite = parentDir.canWrite();
            Logger.log("Parent directory writable: " + canWrite);
            
            if (!canWrite) {
                File tempTestFile = new File(parentDir, ".update-test-" + System.currentTimeMillis());
                try {
                    if (tempTestFile.createNewFile()) {
                        tempTestFile.delete();
                        canWrite = true;
                        Logger.log("Write test successful");
                    }
                } catch (IOException e) {
                    Logger.log("Write test failed: " + e.getMessage());
                }
            }

            Logger.log("Update supported: " + canWrite);
            return canWrite;

        } catch (Exception e) {
            Logger.error("Exception checking update support: " + e.getMessage());
            return false;
        }
    }
    
    public File createBackup() throws UpdateException {
        try {
            String currentJarPath = getCurrentJarPath();
            if (currentJarPath == null) {
                throw new UpdateException("Cannot create backup: not running from JAR");
            }
            
            File currentJar = new File(currentJarPath);
            String backupName = currentJar.getName() + ".backup";
            File backupFile = new File(currentJar.getParent(), backupName);
            
            Files.copy(currentJar.toPath(), backupFile.toPath());
            
            return backupFile;
            
        } catch (IOException e) {
            throw new UpdateException("Failed to create backup", e);
        }
    }
    
    public void restoreFromBackup(File backupFile) throws UpdateException {
        Objects.requireNonNull(backupFile, "Backup file cannot be null");
        
        if (!backupFile.exists()) {
            throw new UpdateException("Backup file does not exist");
        }
        
        try {
            String currentJarPath = getCurrentJarPath();
            if (currentJarPath == null) {
                throw new UpdateException("Cannot restore: not running from JAR");
            }
            
            File currentJar = new File(currentJarPath);
            Files.copy(backupFile.toPath(), currentJar.toPath());
            
        } catch (IOException e) {
            throw new UpdateException("Failed to restore from backup", e);
        }
    }
    
    public void cleanupOldUpdates() {
        try {
            Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
            Files.list(tempDir)
                .filter(path -> path.getFileName().toString().startsWith(TEMP_DIR_PREFIX))
                .forEach(this::deleteDirectoryQuietly);
        } catch (IOException e) {
            Logger.error("Warning: Failed to cleanup old update files: " + e.getMessage());
        }
    }
    
    private void deleteDirectoryQuietly(Path dir) {
        try {
            Files.walk(dir)
                .sorted((p1, p2) -> p2.getNameCount() - p1.getNameCount())
                .forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException e) {
                    }
                });
        } catch (IOException e) {
        }
    }
    
    public static final class UpdateException extends Exception {
        public UpdateException(String message) {
            super(message);
        }
        
        public UpdateException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
