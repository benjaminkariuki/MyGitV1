import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;


/**
 * The `StagingArea` class provides functionality for staging, unstaging, and managing files in the staging area
 * of a MyGit repository. It supports adding, removing, and displaying staged files.
 */
public class StagingArea {

    // Constants for paths

    private static final String OBJECTS_DIR = ".mygit/objects";
    private static final String INDEX_FILE = ".mygit/index";
    private static final String IGNORE_FILE = ".mygitignore";

    // Store ignore patterns in a Set
    private static Set<String> ignorePatterns = new HashSet<>();

 /**
     * Loads ignore patterns from the `.mygitignore` file into the `ignorePatterns` set.
     * Lines that are empty or start with `#` (comments) are ignored.
     */
    
        private static void loadIgnorePatterns() {
        ignorePatterns.clear();
        Path ignoreFilePath = Paths.get(IGNORE_FILE);
        if (Files.exists(ignoreFilePath)) {
            try {
                ignorePatterns = Files.lines(ignoreFilePath)
                        .map(String::trim)
                        .filter(line -> !line.isEmpty() && !line.startsWith("#")) // Ignore empty lines and comments
                        .collect(Collectors.toSet());
            } catch (IOException e) {
                System.out.println("Error reading .mygitignore: " + e.getMessage());
            }
        }
    }

/**
     * Checks if a file should be ignored based on the patterns in the `.mygitignore` file.
     *
     * @param filename The name of the file to check.
     * @return {@code true} if the file matches any ignore pattern, otherwise {@code false}.
     */
    
        private static boolean isIgnored(String filename) {
        for (String pattern : ignorePatterns) {
            if (filenameMatchesPattern(filename, pattern)) {
                return true;
            }
        }
        return false;
    }

 /**
     * Performs simple pattern matching with basic wildcard support.
     *
     * @param filename The filename to check.
     * @param pattern  The pattern to match against.
     * @return {@code true} if the filename matches the pattern, otherwise {@code false}.
     */
    
        private static boolean filenameMatchesPattern(String filename, String pattern) {
        String regex = pattern.replace(".", "\\.").replace("*", ".*").replace("?", ".");
        return filename.matches(regex);
    }



 /**
     * Stages a single file by storing its contents as a blob in the objects directory
     * and updating the index file with the file's hash and name.
     *
     * @param filename The name of the file to stage.
     */
    public static void stageFile(String filename) {
        loadIgnorePatterns(); // Load ignore patterns before staging

        if (isIgnored(filename)) {
            System.out.println("Ignored: " + filename);
            return;
        }

        File file = new File(filename);

        // Check if the file exists
        if (!file.exists()) {
            System.out.println("Error: " + filename + " does not exist.");
            return;
        }

        try {
            // Read file content
            String content = new String(Files.readAllBytes(file.toPath()));

            // Compute the SHA-1 hash of the content
            String hash = computeSHA1(content);

            // Store the file content as a blob in the objects directory
            storeBlob(hash, content);

            // Update the index file with the filename and hash
            updateIndex(filename, hash);

            System.out.println("Staged " + filename);
        } catch (IOException | NoSuchAlgorithmException e) {
            System.out.println("Error staging file: " + e.getMessage());
        }
    }

 /**
     * Stages all files in the current directory and its subdirectories,
     * excluding files that match ignore patterns or are within the `.mygit` directory.
     */
    
        public static void addAllFiles() {
        loadIgnorePatterns(); // Load ignore patterns before staging all files

        try {
            Path myGitPath = Paths.get(".mygit").toAbsolutePath().normalize();

            Files.walk(Paths.get("."))
                .filter(Files::isRegularFile)
                .filter(path -> !path.toAbsolutePath().normalize().startsWith(myGitPath)) // Exclude .mygit directory
                .filter(path -> !isIgnored(path.toString())) // Exclude ignored files
                .forEach(path -> stageFile(path.toString()));
        } catch (IOException e) {
            System.out.println("Error staging all files: " + e.getMessage());
        }
    }

 /**
     * Displays all files currently staged in the index.
     */
    
        public static void showStagedFiles() {
        File indexFile = new File(INDEX_FILE);

        if (!indexFile.exists()) {
            System.out.println("No files are staged.");
            return;
        }

        try {
            List<String> lines = Files.readAllLines(indexFile.toPath());

            if (lines.isEmpty()) {
                System.out.println("No files are staged.");
            } else {
                System.out.println("Staged files:");
                lines.forEach(System.out::println);
            }
        } catch (IOException e) {
            System.out.println("Error reading staged files: " + e.getMessage());
        }
    }

    // Unstage a file by removing its entry from the index
    public static void unstageFile(String filename) {
        File indexFile = new File(INDEX_FILE);

        if (!indexFile.exists()) {
            System.out.println("No files are staged.");
            return;
        }

        try {
            List<String> lines = Files.readAllLines(indexFile.toPath());
            List<String> updatedLines = new ArrayList<>();

            boolean found = false;

            for (String line : lines) {
                if (!line.endsWith(" " + filename)) {
                    updatedLines.add(line);
                } else {
                    found = true;
                }
            }

            if (found) {
                Files.write(indexFile.toPath(), updatedLines);
                System.out.println("Unstaged " + filename);
            } else {
                System.out.println("File " + filename + " is not staged.");
            }
        } catch (IOException e) {
            System.out.println("Error unstaging file: " + e.getMessage());
        }
    }

    // Unstage multiple files by removing their entries from the index
    public static void unstageFiles(List<String> filenames) {
        File indexFile = new File(INDEX_FILE);

        if (!indexFile.exists()) {
            System.out.println("No files are staged.");
            return;
        }

        try {
            List<String> lines = Files.readAllLines(indexFile.toPath());
            List<String> updatedLines = new ArrayList<>();

            Set<String> filenamesToUnstage = new HashSet<>(filenames);

            boolean found = false;

            for (String line : lines) {
                String[] parts = line.split(" ", 2);
                if (parts.length == 2 && filenamesToUnstage.contains(parts[1])) {
                    found = true;
                    System.out.println("Unstaged " + parts[1]);
                } else {
                    updatedLines.add(line);
                }
            }

            if (!found) {
                System.out.println("No matching files found to unstage.");
            }
            Files.write(indexFile.toPath(), updatedLines);
        } catch (IOException e) {
            System.out.println("Error unstaging files: " + e.getMessage());
        }
    }

    // Unstage all files by clearing the index file
    public static void unstageAll() {
        File indexFile = new File(INDEX_FILE);

        if (!indexFile.exists()) {
            System.out.println("No files are staged.");
            return;
        }

        try {
            new PrintWriter(indexFile).close();
            System.out.println("All files have been unstaged.");
        } catch (IOException e) {
            System.out.println("Error clearing the staging area: " + e.getMessage());
        }
    }

    // Compute the SHA-1 hash of the given content
    private static String computeSHA1(String content) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        byte[] hashBytes = digest.digest(content.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    // Store the file content as a blob in the objects directory
    private static void storeBlob(String hash, String content) throws IOException {
        File objectsDir = new File(OBJECTS_DIR);
        if (!objectsDir.exists()) {
            objectsDir.mkdirs();
        }

        File blobFile = new File(objectsDir, hash);
        if (!blobFile.exists()) {
            Files.write(blobFile.toPath(), content.getBytes());
        }
    }

    // Update the index file with the filename and blob hash
    private static void updateIndex(String filename, String hash) throws IOException {
        File indexFile = new File(INDEX_FILE);

        indexFile.getParentFile().mkdirs();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(indexFile, true))) {
            writer.write(hash + " " + filename);
            writer.newLine();
        }
    }
}
