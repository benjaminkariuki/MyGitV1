import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

/**
 * The `RepositoryInitializer` class provides functionality to initialize a new repository.
 * It sets up the `.mygit` directory structure, including creating necessary folders
 * and setting up the default branch (`main`) and the `HEAD` file.
 */

public class RepositoryInitializer {


    /** The name of the Git directory where repository data is stored. */

    private static final String GIT_DIR_NAME = ".mygit";

  /**
     * Initializes a new repository in the current directory.
     * Creates the `.mygit` directory and sets up the default structure.
     */
    
      public static void initRepository() {

                // Get the current working directory

        File currentDir = new File(System.getProperty("user.dir"));

                // Define the .mygit directory path

        File myGitDir = new File(currentDir, GIT_DIR_NAME);

        // Check if the repository already exists

        if (myGitDir.exists()) {
            System.out.println("Repository already initialized in this directory.");
            return;
        }

        // Create the .mygit directory

        if (myGitDir.mkdirs()) {
            System.out.println("Initialized empty MyGit repository in " + myGitDir.getAbsolutePath());
            createInitialStructure(myGitDir);
        } else {
            System.out.println("Failed to initialize repository.");
        }
    }

/**
     * Creates the initial structure inside the `.mygit` directory.
     * This includes creating the `objects` and `refs/heads` directories,
     * and initializing the `HEAD` file to point to the `main` branch.
     *
     * @param myGitDir The `.mygit` directory where the initial structure will be created.
     */

    private static void createInitialStructure(File myGitDir) {
        try {
            // Create the 'objects' directory for storing Git objects
            new File(myGitDir, "objects").mkdirs();

            // Create the 'refs/heads' directory for storing branch references

            new File(myGitDir, "refs/heads").mkdirs();

            // Create the default 'main' branch pointing to an empty commit (or placeholder)
            Files.write(Paths.get(myGitDir.getPath(), "refs/heads/main"), "".getBytes());

            // Set HEAD to point to the 'main' branch
            Files.write(Paths.get(myGitDir.getPath(), "HEAD"), "ref: refs/heads/main".getBytes());

            System.out.println("Created default branch 'main' and set HEAD to 'main'.");
            System.out.println("Created initial repository structure.");
        } catch (IOException e) {
            System.out.println("Error creating initial repository structure: " + e.getMessage());
        }
    }

}
