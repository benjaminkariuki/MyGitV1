import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class GitUtils {

    // Get the current commit hash from HEAD
    public static String getCurrentCommitHash() throws IOException {
        Path headPath = Paths.get(Constants.HEAD_FILE);
        if (!Files.exists(headPath)) {
            return null;
        }

        String headContent = new String(Files.readAllBytes(headPath)).trim();
        if (headContent.startsWith("ref:")) {
            Path branchPath = Paths.get(Constants.GIT_DIR, headContent.substring(5));
            if (Files.exists(branchPath)) {
                return new String(Files.readAllBytes(branchPath)).trim();
            }
        } else {
            return headContent;
        }
        return null;
    }

    // Get the current branch name from HEAD
    public static String getCurrentBranchName() throws IOException {
        Path headPath = Paths.get(Constants.HEAD_FILE);
        if (!Files.exists(headPath)) {
            return null;
        }

        String headContent = new String(Files.readAllBytes(headPath)).trim();
        if (headContent.startsWith("ref: refs/heads/")) {
            return headContent.substring(16); // Extract branch name
        } else {
            return null;
        }
    }

    // Compute the SHA-1 hash of the given content
    public static String computeSHA1(String content) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        byte[] hashBytes = digest.digest(content.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    // Read the content of a file as a String
    public static String readFileContent(Path path) throws IOException {
        return new String(Files.readAllBytes(path)).trim();
    }

    // Write content to a file
    public static void writeFileContent(Path path, String content) throws IOException {
        Files.write(path, content.getBytes());
    }

    // Create directories if they do not exist
    public static void createDirectoriesIfNotExist(Path path) throws IOException {
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
    }

    // Copy a file to the target destination
    public static void copyFile(Path source, Path target) throws IOException {
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
    }

    // Get the content of a commit
    public static String getCommitContent(String commitHash) throws IOException {
        Path commitPath = Paths.get(Constants.OBJECTS_DIR, commitHash);
        return Files.exists(commitPath) ? readFileContent(commitPath).trim() : "";
    }
}
