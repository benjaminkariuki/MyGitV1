public class Help {

    // Method to display the help menu with available commands and their descriptions
    public static void showHelp() {
        System.out.println("\nAvailable commands:");
        System.out.println("-------------------------------------------------");
        System.out.println("init             : Initialize a new repository.");
        System.out.println("add <filename>   : Stage a specific file.");
        System.out.println("add .            : Stage all files in the directory.");
        System.out.println("status           : Show the files currently staged.");
        System.out.println("unstage <file>   : Unstage a specific file.");
        System.out.println("unstage --all    : Unstage all files.");
        System.out.println("commit <message> : Commit the staged changes with a message.");
        System.out.println("log              : Show the commit history.");
        System.out.println("branch           : List all branches.");
        System.out.println("branch <name>    : Create a new branch.");
        System.out.println("checkout <name>  : Switch to a specific branch.");
        System.out.println("merge <name>     : Merge a branch into the current branch.");
        System.out.println("current-branch   : Display the current branch.");
        System.out.println("clone <src> <dst>: Clone a repository from source to destination.");
        System.out.println("help             : Display this help message.");
        System.out.println("-------------------------------------------------\n");
    }
}