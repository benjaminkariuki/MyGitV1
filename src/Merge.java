import java.io.*;
import java.nio.file.*;
import java.util.*;


/**
 * The `Merge` class handles merging one branch into the current branch in the MyGit repository.
 * It supports identifying common ancestors, performing a three-way merge, and creating merge commits.
 */

public class Merge {

        // Constants representing file paths for refs, objects, and the HEAD file

    private static final String REFS_DIR = ".mygit/refs/heads";
    private static final String HEAD_FILE = ".mygit/HEAD";
    private static final String OBJECTS_DIR = ".mygit/objects";

    /**
     * Merges the specified branch into the current branch.
     *
     * @param sourceBranch The branch to merge into the current branch.
     */
     
    public static void mergeBranch(String sourceBranch) {
        try {
            // Get the current branch name
            String currentBranch = getCurrentBranchName();
            if (currentBranch == null) {
                System.out.println("Error: No current branch found.");
                return;
            }

            if (currentBranch.equals(sourceBranch)) {
                System.out.println("Already on branch '" + sourceBranch + "'. Nothing to merge.");
                return;
            }

            // Get commit hashes for current and source branches
            Path sourceBranchPath = Paths.get(REFS_DIR, sourceBranch);
            Path currentBranchPath = Paths.get(REFS_DIR, currentBranch);

            if (!Files.exists(sourceBranchPath)) {
                System.out.println("Error: Branch '" + sourceBranch + "' does not exist.");
                return;
            }

            String sourceCommitHash = new String(Files.readAllBytes(sourceBranchPath)).trim();
            String currentCommitHash = new String(Files.readAllBytes(currentBranchPath)).trim();

            if (currentCommitHash.isEmpty() || sourceCommitHash.isEmpty()) {
                System.out.println("Error: One of the branches has no commits to merge.");
                return;
            }

            // Find the common ancestor
            String commonAncestorHash = findCommonAncestor(currentCommitHash, sourceCommitHash);
            if (commonAncestorHash == null) {
                System.out.println("Error: No common ancestor found.");
                return;
            }

            // Perform the three-way merge
            boolean conflicts = performThreeWayMerge(commonAncestorHash, currentCommitHash, sourceCommitHash);

            if (conflicts) {
                System.out.println("Merge completed with conflicts. Please resolve them manually.");
            } else {
                // Create a merge commit
                createMergeCommit(currentCommitHash, sourceCommitHash, "Merge branch '" + sourceBranch + "'");
                System.out.println("Merge completed successfully.");
            }

        } catch (IOException e) {
            System.out.println("Error during merge: " + e.getMessage());
        }
    }

    
   /**
     * Finds the common ancestor of two commits using Breadth-First Search (BFS).
     *
     * @param commit1 The first commit hash.
     * @param commit2 The second commit hash.
     * @return The hash of the common ancestor commit, or null if no common ancestor is found.
     * @throws IOException If an I/O error occurs while reading commit data.
     */     
    private static String findCommonAncestor(String commit1, String commit2) throws IOException {
        // Queue for BFS traversal
        Queue<String> queue1 = new LinkedList<>();
        Queue<String> queue2 = new LinkedList<>();
        
        // Sets to track visited commits
        Set<String> visited1 = new HashSet<>();
        Set<String> visited2 = new HashSet<>();

        queue1.add(commit1);
        queue2.add(commit2);

        while (!queue1.isEmpty() || !queue2.isEmpty()) {
            if (!queue1.isEmpty()) {
                String current1 = queue1.poll();
                if (visited2.contains(current1)) {
                    return current1; // Found common ancestor
                }
                visited1.add(current1);
                queue1.addAll(getParentCommits(current1));
            }

            if (!queue2.isEmpty()) {
                String current2 = queue2.poll();
                if (visited1.contains(current2)) {
                    return current2; // Found common ancestor
                }
                visited2.add(current2);
                queue2.addAll(getParentCommits(current2));
            }
        }

        return null; // No common ancestor found
    }

  
   /**
     * Retrieves the parent commits of a given commit.
     *
     * @param commitHash The commit hash.
     * @return A list of parent commit hashes.
     * @throws IOException If an I/O error occurs while reading commit data.
     */   

    private static List<String> getParentCommits(String commitHash) throws IOException {
        List<String> parents = new ArrayList<>();
        Path commitPath = Paths.get(OBJECTS_DIR, commitHash);
        if (!Files.exists(commitPath)) {
            return parents;
        }

        List<String> lines = Files.readAllLines(commitPath);
        for (String line : lines) {
            if (line.startsWith("parent ")) {
                parents.add(line.substring(7).trim());
            }
        }

        return parents;
    }

    
    /**
     * Performs a three-way merge and detects conflicts.
     *
     * @param baseHash    The hash of the common ancestor commit.
     * @param currentHash The hash of the current branch's commit.
     * @param sourceHash  The hash of the source branch's commit.
     * @return True if there are conflicts, otherwise false.
     * @throws IOException If an I/O error occurs while reading commit data.
     */
    
    private static boolean performThreeWayMerge(String baseHash, String currentHash, String sourceHash) throws IOException {
        // For simplicity, let's assume each commit represents a single file change.
        String baseContent = getCommitContent(baseHash);
        String currentContent = getCommitContent(currentHash);
        String sourceContent = getCommitContent(sourceHash);

        boolean conflicts = false;
        StringBuilder mergedContent = new StringBuilder();

        if (currentContent.equals(sourceContent)) {
            mergedContent.append(currentContent);
        } else if (baseContent.equals(currentContent)) {
            mergedContent.append(sourceContent);
        } else if (baseContent.equals(sourceContent)) {
            mergedContent.append(currentContent);
        } else {
            conflicts = true;
            mergedContent.append("<<<<<<< CURRENT BRANCH\n")
                         .append(currentContent)
                         .append("\n=======\n")
                         .append(sourceContent)
                         .append("\n>>>>>>> SOURCE BRANCH\n");
        }

        Files.write(Paths.get("merged_file.txt"), mergedContent.toString().getBytes());
        return conflicts;
    }

    
   /**
     * Creates a merge commit with two parents.
     *
     * @param parent1 The hash of the current branch's commit.
     * @param parent2 The hash of the source branch's commit.
     * @param message The merge commit message.
     * @throws IOException If an I/O error occurs while creating the commit.
     */

    private static void createMergeCommit(String parent1, String parent2, String message) throws IOException {
        String commitContent = "parent " + parent1 + "\n" +
                               "parent " + parent2 + "\n" +
                               "message " + message + "\n";

        String commitHash = UUID.randomUUID().toString();
        Files.write(Paths.get(OBJECTS_DIR, commitHash), commitContent.getBytes());

        String currentBranch = getCurrentBranchName();
        Files.write(Paths.get(REFS_DIR, currentBranch), commitHash.getBytes());
    }

    
 /**
     * Retrieves the content of a commit.
     *
     * @param commitHash The commit hash.
     * @return The content of the commit, or an empty string if the commit does not exist.
     * @throws IOException If an I/O error occurs while reading the commit file.
     */

    private static String getCommitContent(String commitHash) throws IOException {
        Path commitPath = Paths.get(OBJECTS_DIR, commitHash);
        if (!Files.exists(commitPath)) {
            return "";
        }
        return new String(Files.readAllBytes(commitPath)).trim();
    }

   
  /**
     * Retrieves the current branch name from the HEAD file.
     *
     * @return The name of the current branch, or null if HEAD does not exist.
     * @throws IOException If an I/O error occurs while reading the HEAD file.
     */     
    private static String getCurrentBranchName() throws IOException {
        Path headPath = Paths.get(HEAD_FILE);
        if (!Files.exists(headPath)) {
            return null;
        }

        String headContent = new String(Files.readAllBytes(headPath)).trim();
        if (headContent.startsWith("ref: refs/heads/")) {
            return headContent.substring(16);
        } else {
            return null;
        }
    }
}
