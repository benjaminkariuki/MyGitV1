import java.io.*;
import java.nio.file.*;

public class Branch {

    /**
 * The `Branch` class provides functionality for managing branches in the MyGit repository.
 * It includes operations such as creating a new branch, switching between branches,
 * listing all branches, and displaying the current branch.
 */


    // Constants representing file paths for HEAD and refs directory

    private static final String HEAD_FILE = Constants.HEAD_FILE;
    private static final String REFS_DIR = Constants.REFS_DIR;

      /**
     * Creates a new branch pointing to the current commit.
     *
     * @param branchName The name of the new branch to be created.
     */
    public static void createBranch(String branchName){

        try{
            //Get the current commit hash from HEAD
            String currentCommit = getCurrentCommitHash();

            if(currentCommit == null){
                System.out.println("Error: No commits found. Please make  a commit first.");
                return;

            }

            //create the branch file in .mygit/refs/heads
            Path branchPath = Paths.get(REFS_DIR, branchName);
            Files.createDirectories(branchPath.getParent());
            Files.write(branchPath, currentCommit.getBytes());

            System.out.println("Branch '" + branchName + "' created successfully.");

        }catch(IOException e){

            System.out.println("Error creating branch: " + e.getMessage());

        }

    }

    // Switch to the specified branch.
    // @param branchName The name of the branch to switch to

    public static void switchBranch(String branchName){

        Path branchPath = Paths.get(REFS_DIR, branchName);

        // Check if the branch exists

        if(!Files.exists(branchPath)){

            System.out.println("Error: Branch '" + branchName + "' does not exist.");
            return;

        }

        try{
            //Update HEAD to point to the branch
            String refContent = "ref: refs/heads/" + branchName;
            Files.write(Paths.get(HEAD_FILE), refContent.getBytes());

            System.out.println("Switched to branch '" + branchName + "'.");
        }catch(IOException e){
            System.out.println("Error switching branch: " + e.getMessage());

        }

    }


  /**
     * Lists all branches and indicates the current branch with an asterisk (*).
     */
public static void listBranches() {
    try {
        Path refsDir = Paths.get(REFS_DIR);

            // Check if the refs directory exists

        if (!Files.exists(refsDir)) {
            System.out.println("No branches found.");
            return;
        }

            // Get the current branch name

        String currentBranch = getCurrentBranchName();


            // List all branch names and mark the current branch with an asterisk

        Files.list(refsDir).forEach(branchPath -> {
            String branchName = branchPath.getFileName().toString();
            if (branchName.equals(currentBranch)) {
                System.out.println("* " + branchName); // Indicate current branch with an asterisk
            } else {
                System.out.println("  " + branchName);
            }
        });

    } catch (IOException e) {
        System.out.println("Error listing branches: " + e.getMessage());
    }
}


   // Get the current commit hash from HEAD.
    
    private static String getCurrentCommitHash() throws IOException {
        Path headPath = Paths.get(HEAD_FILE);
        if (!Files.exists(headPath)) {
            return null;
        }

        String headContent = new String(Files.readAllBytes(headPath)).trim();
        if (headContent.startsWith("ref:")) {
            Path branchPath = Paths.get(".mygit", headContent.substring(5));
            return new String(Files.readAllBytes(branchPath)).trim();
        } else {
            return headContent;
        }
    }


  
     // Get the current branch name from HEAD.
    
private static String getCurrentBranchName() throws IOException {
    Path headPath = Paths.get(HEAD_FILE);
    if (!Files.exists(headPath)) {
        return null;
    }

    String headContent = new String(Files.readAllBytes(headPath)).trim();
    System.out.println("HEAD content: " + headContent); // Debugging statement

    if (headContent.startsWith("ref: refs/heads/")) {
        return headContent.substring(16); // Extract branch name
    } else {
        return null;
    }
}





//   Display the current branch name.

public static void showCurrentBranch() {
    try {
        String currentBranch = getCurrentBranchName();
        if (currentBranch != null) {
            System.out.println("Current branch: " + currentBranch);
        } else {
            System.out.println("Error: Could not determine the current branch.");
        }
    } catch (IOException e) {
        System.out.println("Error displaying current branch: " + e.getMessage());
    }
}

}
