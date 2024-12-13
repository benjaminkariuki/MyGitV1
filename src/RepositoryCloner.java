import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;


/**
 * The `RepositoryCloner` class provides functionality to clone a MyGit repository
 * from a source path to a specified destination path.
 */
public class RepositoryCloner {


    /**
     * Clones the MyGit repository from the source directory to the destination directory.
     *
     * @param sourcePath      The path to the source repository to be cloned.
     * @param destinationPath The path to the destination where the repository will be cloned.
     */
       public static void cloneRepository(String sourcePath, String destinationPath) {
        Path sourceDir = Paths.get(sourcePath);
        Path destinationDir = Paths.get(destinationPath);

        // Validate source repository
        if (!Files.exists(sourceDir.resolve(".mygit"))) {
            System.out.println("Error: The source path is not a valid repository.");
            return;
        }

        try {
            // Copy the entire source directory to the destination
            copyDirectory(sourceDir, destinationDir);

            System.out.println("Repository cloned successfully to: " + destinationDir.toAbsolutePath());
        } catch (IOException e) {
            System.out.println("Error cloning repository: " + e.getMessage());
        }
    }


  /**
     * Recursively copies a directory and its contents from the source to the destination.
     *
     * @param source      The path to the source directory.
     * @param destination The path to the destination directory.
     * @throws IOException If an I/O error occurs during the copying process.
     */
    
        private static void copyDirectory(Path source, Path destination) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path targetDir = destination.resolve(source.relativize(dir));
                if (!Files.exists(targetDir)) {
                    Files.createDirectories(targetDir);
                }
                return FileVisitResult.CONTINUE;
            }


 /**
             * Called when visiting a file. Copies the file to the corresponding location
             * in the destination directory.
             *
             * @param file  The current file being visited.
             * @param attrs The file's basic attributes.
             * @return {@link FileVisitResult#CONTINUE} to continue visiting files.
             * @throws IOException If an error occurs during file copying.
             */

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path targetFile = destination.resolve(source.relativize(file));
                Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }



}