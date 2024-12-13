import java.io.*;
import java.nio.file.*;
import java.util.*;


/**
 * The `Diff` class handles comparing files and branches in the MyGit repository.
 * It provides functionality to display differences between the working directory
 * and the staging area, as well as differences between two branches.
 */

public class Diff {
    

        // Constants representing file paths for refs, objects, HEAD, and the index file

    private static final String REFS_DIR = ".mygit/refs/heads";
    private static final String OBJECTS_DIR = ".mygit/objects";
    private static final String HEAD_FILE = ".mygit/HEAD";
    private static final String INDEX_FILE = ".mygit/index";



 /**
     * Shows differences between the working directory and the staging area for a single file.
     *
     * @param filename The name of the file to compare.
     */
    public static void showWorkingDirectoryDiff(String filename) {
        try {
            Path filePath = Paths.get(filename);

            if (!Files.exists(filePath)) {
                System.out.println("File not found in working directory: " + filename);
                return;
            }

            // Read current file content
            List<String> currentContent = Files.readAllLines(filePath);

            // Read staged file content from the index
            String stagedHash = getStagedHash(filename);
            if (stagedHash == null) {
                System.out.println("File not staged: " + filename);
                return;
            }

            Path objectPath = Paths.get(OBJECTS_DIR, stagedHash);
            if (!Files.exists(objectPath)) {
                System.out.println("Staged object not found: " + stagedHash);
                return;
            }

            List<String> stagedContent = Files.readAllLines(objectPath);

            // Compare the two contents
            System.out.println("Diff for " + filename + ":");
            displayDiff(stagedContent, currentContent);

        } catch (IOException e) {
            System.out.println("Error showing diff: " + e.getMessage());
        }
    }


  /**
     * Retrieves the hash of a staged file from the index.
     *
     * @param filename The name of the file to look up in the index.
     * @return The hash of the staged file, or null if the file is not staged.
     * @throws IOException If an I/O error occurs while reading the index.
     */

    private static String getStagedHash(String filename) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(INDEX_FILE));
        for (String line : lines) {
            String[] parts = line.split(" ", 2);
            if (parts.length == 2 && parts[1].equals(filename)) {
                return parts[0];
            }
        }
        return null;
    }


    /**
     * Shows differences between two branches.
     *
     * @param branch1 The name of the first branch.
     * @param branch2 The name of the second branch.
     */

    public static void showBranchDiff(String branch1, String branch2) {
        try {
            // Get the commit hashes for both branches
            String commitHash1 = getBranchCommitHash(branch1);
            String commitHash2 = getBranchCommitHash(branch2);

            if (commitHash1 == null || commitHash2 == null) {
                System.out.println("Error: One or both branches do not exist.");
                return;
            }

            // Get the tree hashes for both commits
            String treeHash1 = getTreeHashFromCommit(commitHash1);
            String treeHash2 = getTreeHashFromCommit(commitHash2);

            if (treeHash1 == null || treeHash2 == null) {
                System.out.println("Error: One or both commits do not have a tree object.");
                return;
            }

            // Get the file listings for both trees
            Map<String, String> filesInBranch1 = getFilesFromTree(treeHash1);
            Map<String, String> filesInBranch2 = getFilesFromTree(treeHash2);

            // Compare the file listings
            compareBranches(filesInBranch1, filesInBranch2);

        } catch (IOException e) {
            System.out.println("Error showing branch diff: " + e.getMessage());
        }
    }

      /**
     * Retrieves the commit hash associated with a branch.
     *
     * @param branchName The name of the branch.
     * @return The commit hash associated with the branch, or null if the branch does not exist.
     * @throws IOException If an I/O error occurs while reading the branch file.
     */

    private static String getBranchCommitHash(String branchName) throws IOException {
        Path branchPath = Paths.get(REFS_DIR, branchName);
        if (Files.exists(branchPath)) {
            return new String(Files.readAllBytes(branchPath)).trim();
        }
        return null;
    }


 /**
     * Retrieves the tree hash from a commit.
     *
     * @param commitHash The commit hash.
     * @return The tree hash associated with the commit, or null if not found.
     * @throws IOException If an I/O error occurs while reading the commit file.
     */
    private static String getTreeHashFromCommit(String commitHash) throws IOException {
        Path commitPath = Paths.get(OBJECTS_DIR, commitHash);
        if (!Files.exists(commitPath)) {
            return null;
        }

        List<String> lines = Files.readAllLines(commitPath);
        for (String line : lines) {
            if (line.startsWith("tree ")) {
                return line.substring(5).trim();
            }
        }
        return null;
    }


  /**
     * Retrieves the files and their hashes from a tree object.
     *
     * @param treeHash The tree hash.
     * @return A map containing filenames as keys and their hashes as values.
     * @throws IOException If an I/O error occurs while reading the tree object.
     */
    private static Map<String, String> getFilesFromTree(String treeHash) throws IOException {
        Path treePath = Paths.get(OBJECTS_DIR, treeHash);
        Map<String, String> files = new HashMap<>();

        if (!Files.exists(treePath)) {
            return files;
        }

        List<String> lines = Files.readAllLines(treePath);
        for (String line : lines) {
            String[] parts = line.split(" ", 2);
            if (parts.length == 2) {
                files.put(parts[1], parts[0]);
            }
        }

        return files;
    }


        /**
     * Compares the files between two branches and displays the differences.
     *
     * @param files1 The file hash mappings for the first branch.
     * @param files2 The file hash mappings for the second branch.
     * @throws IOException If an I/O error occurs while reading file contents.
     */

    private static void compareBranches(Map<String, String> files1, Map<String, String> files2) throws IOException {
        Set<String> allFiles = new HashSet<>();
        allFiles.addAll(files1.keySet());
        allFiles.addAll(files2.keySet());

        for (String file : allFiles) {
            String hash1 = files1.get(file);
            String hash2 = files2.get(file);

            if (hash1 == null) {
                System.out.println("File added in second branch: " + file);
            } else if (hash2 == null) {
                System.out.println("File deleted in second branch: " + file);
            } else if (!hash1.equals(hash2)) {
                System.out.println("File modified: " + file);
                showFileDiff(hash1, hash2, file);
            }
        }
    }



/**
     * Displays the differences between two file versions.
     *
     * @param hash1    The hash of the first file version.
     * @param hash2    The hash of the second file version.
     * @param filename The name of the file being compared.
     * @throws IOException If an I/O error occurs while reading the files.
     */
    private static void showFileDiff(String hash1, String hash2, String filename) throws IOException {
        Path filePath1 = Paths.get(OBJECTS_DIR, hash1);
        Path filePath2 = Paths.get(OBJECTS_DIR, hash2);

        List<String> content1 = Files.exists(filePath1) ? Files.readAllLines(filePath1) : Collections.emptyList();
        List<String> content2 = Files.exists(filePath2) ? Files.readAllLines(filePath2) : Collections.emptyList();

        System.out.println("--- " + filename + " (branch1)");
        System.out.println("+++ " + filename + " (branch2)");
        displayDiff(content1, content2);
    }


  
    private static void displayDiff(List<String> oldContent, List<String> newContent) {
        for (int i = 0; i < Math.max(oldContent.size(), newContent.size()); i++) {
            String oldLine = i < oldContent.size() ? oldContent.get(i) : "";
            String newLine = i < newContent.size() ? newContent.get(i) : "";

            if (!oldLine.equals(newLine)) {
                System.out.println("- " + oldLine);
                System.out.println("+ " + newLine);
            }
        }
    }
}
