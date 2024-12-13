/**
 * The `Welcome` class contains a method to display a welcome message and list available commands.
 */
public class Welcome {

    /**
     * Displays a welcome message and a list of available commands with their descriptions.
     */
    public static void displayWelcomeMessage() {
        System.out.println("Welcome to MyGit - A simple version control system!");
        System.out.println("Here are the available commands you can execute:\n");

        System.out.println("  init               Initialize a new repository.");
        System.out.println("  add <filename>     Stage a file for the next commit.");
        System.out.println("  add .              Stage all files for the next commit.");
        System.out.println("  status             Show the status of staged files.");
        System.out.println("  unstage <filename> Unstage a file.");
        System.out.println("  unstage --all      Unstage all files.");
        System.out.println("  commit <message>   Create a new commit with the specified message.");
        System.out.println("  log                Show the commit history.");
        System.out.println("  branch             List all branches.");
        System.out.println("  branch <name>      Create a new branch.");
        System.out.println("  checkout <name>    Switch to the specified branch.");
        System.out.println("  merge <name>       Merge the specified branch into the current branch.");
        System.out.println("  current-branch     Display the current branch.");
        System.out.println("  clone <src> <dst>  Clone a repository from source to destination.");
        System.out.println("  diff <filename>    Show differences between the working directory and staging area.");
        System.out.println("  diff <b1> <b2>     Show differences between two branches.");
        System.out.println("  help               Display this help message.\n");

        System.out.println("Usage: java MyGit <command> [<arguments>...]");
    }
}
