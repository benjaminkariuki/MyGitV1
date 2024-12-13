import java.util.Arrays;

/**
 * The `MyGit` class acts as the entry point for the MyGit version control system.
 * It processes user commands and delegates tasks to the appropriate classes.
 */
public class MyGit {

    /**
     * The main method processes user input and performs the corresponding Git-like commands.
     *
     * @param args Command-line arguments, where the first argument is the command and
     *             subsequent arguments are parameters for the command.
     */
    public static void main(String[] args) {

        // Display a welcome message and help when no arguments are provided
        if (args.length < 1) {
            Welcome.displayWelcomeMessage();
            return;
        }

        // Extract the command from the first argument
        String command = args[0];

        // Process the command using a switch statement
        switch (command) {

            case "init":
                RepositoryInitializer.initRepository();
                break;

            case "add":
                if (args.length == 2 && args[1].equals(".")) {
                    StagingArea.addAllFiles();
                } else if (args.length >= 2) {
                    StagingArea.stageFile(args[1]);
                } else {
                    System.out.println("Usage: java MyGit add <filename> or java MyGit add .");
                }
                break;

            case "status":
                StagingArea.showStagedFiles();
                break;

            case "unstage":
                if (args.length == 2 && args[1].equals("--all")) {
                    StagingArea.unstageAll();
                } else if (args.length >= 2) {
                    StagingArea.unstageFiles(Arrays.asList(Arrays.copyOfRange(args, 1, args.length)));
                } else {
                    System.out.println("Usage: java MyGit unstage <filename>... or java MyGit unstage --all");
                }
                break;

            case "commit":
                if (args.length >= 2) {
                    String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                    Commit.createCommit(message);
                } else {
                    System.out.println("Usage: java MyGit commit <message>");
                }
                break;

            case "log":
                Commit.showCommitHistory();
                break;

            case "branch":
                if (args.length == 2) {
                    Branch.createBranch(args[1]);
                } else {
                    Branch.listBranches();
                }
                break;

            case "checkout":
                if (args.length == 2) {
                    Branch.switchBranch(args[1]);
                } else {
                    System.out.println("Usage: java MyGit checkout <branch_name>");
                }
                break;

            case "merge":
                if (args.length == 2) {
                    Merge.mergeBranch(args[1]);
                } else {
                    System.out.println("Usage: java MyGit merge <branch_name>");
                }
                break;

            case "current-branch":
                Branch.showCurrentBranch();
                break;

            case "clone":
                if (args.length == 3) {
                    RepositoryCloner.cloneRepository(args[1], args[2]);
                } else {
                    System.out.println("Usage: java MyGit clone <source_path> <destination_path>");
                }
                break;

            case "diff":
                if (args.length == 3) {
                    Diff.showBranchDiff(args[1], args[2]);
                } else if (args.length == 2) {
                    Diff.showWorkingDirectoryDiff(args[1]);
                } else {
                    System.out.println("Usage: java MyGit diff <filename> or java MyGit diff <branch1> <branch2>");
                }
                break;

            case "help":
                Help.showHelp();
                break;

            default:
                System.out.println("Unknown command: " + command);
                Help.showHelp();
        }
    }
}
