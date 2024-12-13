import java .io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * The `Commit` class handles creating and managing commits in the MyGit repository.
 * It includes functionality to create commits, generate tree objects,
 * update HEAD references, store objects, and display commit history.
 */

public class Commit {

    // Constants representing file paths for the objects directory, HEAD, and the index file

    private static final String OBJECTS_DIR = ".mygit/objects";
    private static final String HEAD_FILE = ".mygit/HEAD";
    private static final String INDEX_FILE = ".mygit/index";


    
 /**
     * Creates a new commit with the given message.
     *
     * @param message The commit message describing the changes.
     */

public static void createCommit(String message) {
    try {
        // Generate the tree object
        String treeHash = generateTree();

        // Get the parent commit hash from HEAD
        String parentHash = getCurrentCommitHash();

        // Prepare commit metadata
        String author = System.getProperty("user.name");
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String commitContent = "tree " + treeHash + "\n";

        if (parentHash != null && !parentHash.isEmpty()) {
            commitContent += "parent " + parentHash + "\n";
        }

        commitContent += "author " + author + "\n";
        commitContent += "date " + timestamp + "\n\n";
        commitContent += message + "\n";

        // Compute the commit hash
        String commitHash = computeSHA1(commitContent);

        // Store the commit object in .mygit/objects
        storeObject(commitHash, commitContent);

        // Update HEAD to point to the new commit
        updateHEAD(commitHash);

        // Clear the index after a successful commit
        clearIndex();

        System.out.println("Committed successfully with commit hash: " + commitHash);

    } catch (IOException | NoSuchAlgorithmException e) {
        System.out.println("Error creating commit: " + e.getMessage());
    }
}





 /**
     * Clears the index (staging area) by overwriting it with an empty file.
     *
     * @throws IOException If an I/O error occurs while clearing the index.
     */    
    private static void clearIndex() throws IOException {
        Files.write(Paths.get(INDEX_FILE), new byte[0]); // Overwrite the index with an empty file
    }


      /**
     * Generates a tree object from the index and returns its SHA-1 hash.
     *
     * @return The SHA-1 hash of the generated tree object.
     * @throws IOException              If an I/O error occurs while reading the index.
     * @throws NoSuchAlgorithmException If the SHA-1 algorithm is not available.
     */

   private static String generateTree() throws IOException, NoSuchAlgorithmException {
        File indexFile = new File(INDEX_FILE);
        if (!indexFile.exists()) {
            System.out.println("Nothing to commit. The staging area is empty.");
            System.exit(1);
        }

        List<String> lines = Files.readAllLines(indexFile.toPath());
        StringBuilder treeContent = new StringBuilder();

        for (String line : lines) {
            treeContent.append(line).append("\n");
        }

        // Compute the tree hash
        String treeHash = computeSHA1(treeContent.toString());

        // Store the tree object
        storeObject(treeHash, treeContent.toString());

        return treeHash;
    }

 /**
     * Retrieves the current commit hash from the HEAD file.
     *
     * @return The current commit hash, or null if it cannot be determined.
     * @throws IOException If an I/O error occurs while reading the HEAD file.
     */

 private static String getCurrentCommitHash() throws IOException {
    Path headPath = Paths.get(HEAD_FILE);
    if (!Files.exists(headPath)) {
        return null;
    }

    String headContent = new String(Files.readAllBytes(headPath)).trim();
    if (headContent.startsWith("ref:")) {
        // HEAD points to a branch reference
        Path branchPath = Paths.get(".mygit", headContent.substring(5));
        if (Files.exists(branchPath)) {
            return new String(Files.readAllBytes(branchPath)).trim();
        }
    } else {
        // HEAD contains a direct commit hash (detached HEAD state)
        return headContent;
    }
    return null;
}



 /**
     * Updates the HEAD file to point to the new commit hash.
     *
     * @param commitHash The new commit hash to update HEAD with.
     * @throws IOException If an I/O error occurs while updating HEAD.
     */
private static void updateHEAD(String commitHash) throws IOException {
    Path headPath = Paths.get(HEAD_FILE);
    String headContent = new String(Files.readAllBytes(headPath)).trim();

    if (headContent.startsWith("ref:")) {
        // Extract the branch reference and update the branch file with the new commit hash
        Path branchPath = Paths.get(".mygit", headContent.substring(5));
        Files.write(branchPath, commitHash.getBytes());
    } else {
        // If HEAD contains a direct commit hash (detached HEAD state), update HEAD directly
        Files.write(headPath, commitHash.getBytes());
    }
}



    /**
     * Stores an object (commit/tree) in the objects directory.
     *
     * @param hash    The SHA-1 hash of the object.
     * @param content The content of the object to store.
     * @throws IOException If an I/O error occurs while writing the object.
     */

  private static void storeObject(String hash, String content) throws IOException {
        File objectFile = new File(OBJECTS_DIR, hash);
        Files.write(objectFile.toPath(), content.getBytes());
    }



 /**
     * Computes the SHA-1 hash of the given content.
     *
     * @param content The content to compute the hash for.
     * @return The SHA-1 hash as a hexadecimal string.
     * @throws NoSuchAlgorithmException If the SHA-1 algorithm is not available.
     */
    
        private static String computeSHA1(String content) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        byte[] hashBytes = digest.digest(content.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

 /**
     * Displays the commit history by traversing through the commits starting from the current commit.
     */
    
    public static void showCommitHistory() {
    try {
        String commitHash = getCurrentCommitHash();
        while (commitHash != null && !commitHash.isEmpty()) {
            Path commitPath = Paths.get(OBJECTS_DIR, commitHash);
            
            if (!Files.exists(commitPath)) {
                System.out.println("Error: Commit object not found for hash: " + commitHash);
                break;
            }

            String commitContent = new String(Files.readAllBytes(commitPath));
            System.out.println("Commit: " + commitHash);
            System.out.println(commitContent);
            System.out.println("------------------------");

            // Get the parent commit hash
            commitHash = null;
            for (String line : commitContent.split("\n")) {
                if (line.startsWith("parent ")) {
                    commitHash = line.substring(7).trim();
                    break;
                }
            }
        }
    } catch (IOException e) {
        System.out.println("Error displaying commit history: " + e.getMessage());
    }
}

}
