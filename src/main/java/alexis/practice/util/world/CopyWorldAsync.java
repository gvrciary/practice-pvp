package alexis.practice.util.world;

import alexis.practice.Practice;
import cn.nukkit.Server;
import cn.nukkit.scheduler.AsyncTask;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class CopyWorldAsync extends AsyncTask {

    private final String worldName;
    private final String directory;
    private final String newName;
    private final String newDirectory;

    private final Runnable callback;

    public CopyWorldAsync(String worldName, String directory, String newName, String newDirectory, Runnable callback) {
        this.worldName = worldName;
        this.directory = directory;
        this.newName = newName;
        this.newDirectory = newDirectory;

        this.callback = callback;
    }

    @Override
    public void onRun() {
        String path = directory + File.separator + worldName;
        String newPath = newDirectory + File.separator + newName;

        try {
            copySource(path, newPath);
        } catch (IOException e) {
            System.err.println("Error copying the file: " + e.getMessage());
        }
    }

    private void copySource(String source, String target) throws IOException {
        Path sourcePath = Paths.get(source);
        Path targetPath = Paths.get(target);

        Files.walkFileTree(sourcePath, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path targetDir = targetPath.resolve(sourcePath.relativize(dir));
                Files.createDirectories(targetDir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                try (InputStream in = new BufferedInputStream(Files.newInputStream(file));
                     OutputStream out = new BufferedOutputStream(Files.newOutputStream(targetPath.resolve(sourcePath.relativize(file)), StandardOpenOption.CREATE))) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }


    @Override
    public void onCompletion(Server server) {
        Practice.getInstance().getServer().loadLevel(this.newName);

        if (callback != null) {
            callback.run();
        }
    }
}
