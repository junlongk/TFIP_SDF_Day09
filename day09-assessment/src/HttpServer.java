import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServer {

    public static void startServer(Integer port, String directory) throws Exception {
        // Check if each directory exists, is a valid directory or is readable.
        String[] directories = directory.split(":");
        for (String dir : directories) {
            System.out.printf("Checking directory... %s\n", dir);
            Path path = Paths.get(dir);
            if (!Files.exists(path)) {
                System.out.printf("%s does not exist!\nExiting the server...\n", path);
                System.exit(1);
            }
            if (!Files.isDirectory(path)) {
                System.out.printf("%s is not a directory!\nExiting the server...\n", path);
                System.exit(1);
            }
            if (!Files.isReadable(path)) {
                System.out.printf("%s is not readable!\nExiting the server...\n", path);
                System.exit(1);
            }
            System.out.printf("Passed!\n");
        }

        // Create threadpool
        ExecutorService threadPool = Executors.newFixedThreadPool(3);

        // Initiate server
        ServerSocket server = new ServerSocket(port);
        System.out.printf("Listening on port: %d\n========\n", port);

        // Server loop
        while(true) {
            // Wait for connections
            System.out.printf("Waiting for connections...\n");
            Socket socket = server.accept();

            // Create thread to handle client connection and submit to threadpool
            HttpClientConnection thr = new HttpClientConnection(socket, directories);
            threadPool.submit(thr);
        }

    }
}
