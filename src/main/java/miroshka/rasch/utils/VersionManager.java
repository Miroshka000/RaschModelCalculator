package miroshka.rasch.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class VersionManager {
    private static final String GITHUB_API_URL = "https://api.github.com/repos/Miroshka000/RaschModelCalculator/releases/latest";
    private static final String GITHUB_RELEASES_URL = "https://github.com/Miroshka000/RaschModelCalculator/releases";
    private static final Pattern VERSION_PATTERN = Pattern.compile("^v?(\\d+)\\.(\\d+)\\.(\\d+)(?:-(\\w+))?$");
    private static final int TIMEOUT_MS = 10000; // 10 seconds
    
    private final String currentVersion;
    
    public VersionManager(String currentVersion) {
        this.currentVersion = Objects.requireNonNull(currentVersion, "Current version cannot be null");
    }
    
    public CompletableFuture<UpdateInfo> checkForUpdatesAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return checkForUpdates();
            } catch (Exception e) {
                try {
                    throw new UpdateCheckException("Failed to check for updates", e);
                } catch (UpdateCheckException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }
    
    public UpdateInfo checkForUpdates() throws UpdateCheckException {
        try {
            Logger.log("Checking for updates... Current version: " + currentVersion);

            String latestVersionJson = fetchLatestVersionInfo();
            String latestVersion = parseVersionFromJson(latestVersionJson);
            String downloadUrl = parseDownloadUrlFromJson(latestVersionJson);

            Logger.log("Latest version from GitHub: " + latestVersion);
            Logger.log("Download URL: " + downloadUrl);

            if (latestVersion == null) {
                throw new UpdateCheckException("Could not parse version from GitHub response");
            }

            boolean isUpdateAvailable = isNewerVersion(latestVersion, currentVersion);
            Logger.log("Update available: " + isUpdateAvailable);

            return UpdateInfo.builder()
                .currentVersion(currentVersion)
                .latestVersion(latestVersion)
                .isUpdateAvailable(isUpdateAvailable)
                .downloadUrl(downloadUrl)
                .releaseNotesUrl(GITHUB_RELEASES_URL + "/tag/" + latestVersion)
                .build();

        } catch (IOException e) {
            Logger.error("Network error while checking for updates: " + e.getMessage());
            throw new UpdateCheckException("Network error while checking for updates", e);
        } catch (Exception e) {
            Logger.error("Unexpected error during update check: " + e.getMessage());
            throw new UpdateCheckException("Unexpected error during update check", e);
        }
    }
    
    private String fetchLatestVersionInfo() throws IOException {
        URL url = URI.create(GITHUB_API_URL).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
            connection.setRequestProperty("User-Agent", "RaschModelCalculator-UpdateChecker");

            Logger.log("Fetching data from: " + GITHUB_API_URL);
            int responseCode = connection.getResponseCode();
            Logger.log("GitHub API response code: " + responseCode);

            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("GitHub API returned status code: " + responseCode);
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }

            String responseStr = response.toString();
            Logger.log("GitHub API response length: " + responseStr.length() + " characters");

            return responseStr;

        } finally {
            connection.disconnect();
        }
    }
    
    private String parseVersionFromJson(String json) {
        Pattern tagNamePattern = Pattern.compile("\"tag_name\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = tagNamePattern.matcher(json);
        
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        return null;
    }
    
    private String parseDownloadUrlFromJson(String json) {
        Pattern exePattern = Pattern.compile("\"browser_download_url\"\\s*:\\s*\"([^\"]*\\.exe)\"");
        Matcher exeMatcher = exePattern.matcher(json);

        if (exeMatcher.find()) {
            return exeMatcher.group(1);
        }

        Pattern jarPattern = Pattern.compile("\"browser_download_url\"\\s*:\\s*\"([^\"]*\\.jar)\"");
        Matcher jarMatcher = jarPattern.matcher(json);

        if (jarMatcher.find()) {
            return jarMatcher.group(1);
        }

        return null;
    }
    
    public static boolean isNewerVersion(String version1, String version2) {
        if (version1 == null || version2 == null) {
            return false;
        }
        
        SemanticVersion v1 = parseSemanticVersion(version1);
        SemanticVersion v2 = parseSemanticVersion(version2);
        
        if (v1 == null || v2 == null) {
            return false;
        }
        
        return v1.compareTo(v2) > 0;
    }
    
    private static SemanticVersion parseSemanticVersion(String version) {
        String cleanVersion = version.startsWith("v") ? version.substring(1) : version;
        Matcher matcher = VERSION_PATTERN.matcher(cleanVersion);
        
        if (!matcher.matches()) {
            return null;
        }
        
        try {
            int major = Integer.parseInt(matcher.group(1));
            int minor = Integer.parseInt(matcher.group(2));
            int patch = Integer.parseInt(matcher.group(3));
            String preRelease = matcher.group(4);
            
            return new SemanticVersion(major, minor, patch, preRelease);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    public static final class UpdateInfo {
        private final String currentVersion;
        private final String latestVersion;
        private final boolean isUpdateAvailable;
        private final String downloadUrl;
        private final String releaseNotesUrl;
        
        private UpdateInfo(Builder builder) {
            this.currentVersion = builder.currentVersion;
            this.latestVersion = builder.latestVersion;
            this.isUpdateAvailable = builder.isUpdateAvailable;
            this.downloadUrl = builder.downloadUrl;
            this.releaseNotesUrl = builder.releaseNotesUrl;
        }
        
        public String getCurrentVersion() { return currentVersion; }
        public String getLatestVersion() { return latestVersion; }
        public boolean isUpdateAvailable() { return isUpdateAvailable; }
        public Optional<String> getDownloadUrl() { return Optional.ofNullable(downloadUrl); }
        public Optional<String> getReleaseNotesUrl() { return Optional.ofNullable(releaseNotesUrl); }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static final class Builder {
            private String currentVersion;
            private String latestVersion;
            private boolean isUpdateAvailable;
            private String downloadUrl;
            private String releaseNotesUrl;
            
            public Builder currentVersion(String currentVersion) {
                this.currentVersion = currentVersion;
                return this;
            }
            
            public Builder latestVersion(String latestVersion) {
                this.latestVersion = latestVersion;
                return this;
            }
            
            public Builder isUpdateAvailable(boolean isUpdateAvailable) {
                this.isUpdateAvailable = isUpdateAvailable;
                return this;
            }
            
            public Builder downloadUrl(String downloadUrl) {
                this.downloadUrl = downloadUrl;
                return this;
            }
            
            public Builder releaseNotesUrl(String releaseNotesUrl) {
                this.releaseNotesUrl = releaseNotesUrl;
                return this;
            }
            
            public UpdateInfo build() {
                return new UpdateInfo(this);
            }
        }
        
        @Override
        public String toString() {
            return String.format("UpdateInfo{current=%s, latest=%s, updateAvailable=%s}", 
                currentVersion, latestVersion, isUpdateAvailable);
        }
    }
    
    private static final class SemanticVersion implements Comparable<SemanticVersion> {
        private final int major;
        private final int minor;
        private final int patch;
        private final String preRelease;
        
        public SemanticVersion(int major, int minor, int patch, String preRelease) {
            this.major = major;
            this.minor = minor;
            this.patch = patch;
            this.preRelease = preRelease;
        }
        
        @Override
        public int compareTo(SemanticVersion other) {
            int result = Integer.compare(this.major, other.major);
            if (result != 0) return result;
            
            result = Integer.compare(this.minor, other.minor);
            if (result != 0) return result;
            
            result = Integer.compare(this.patch, other.patch);
            if (result != 0) return result;
            
            if (this.preRelease == null && other.preRelease != null) return 1;
            if (this.preRelease != null && other.preRelease == null) return -1;
            if (this.preRelease != null && other.preRelease != null) {
                return this.preRelease.compareTo(other.preRelease);
            }
            
            return 0;
        }
        
        @Override
        public String toString() {
            return String.format("%d.%d.%d%s", major, minor, patch, 
                preRelease != null ? "-" + preRelease : "");
        }
    }
    
    public static final class UpdateCheckException extends Exception {
        public UpdateCheckException(String message) {
            super(message);
        }
        
        public UpdateCheckException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
