import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HttpClientConnection implements Runnable {

    private Socket socket;
    private String[] directories;

    public HttpClientConnection (Socket socket, String[] directories) {
        this.socket = socket;
        this.directories = directories;
    }

    @Override
    public void run() {
        try {
            // Initialise reader
            InputStream is = socket.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);

            // Initialise writer
            OutputStream os = socket.getOutputStream();
            HttpWriter writer = new HttpWriter(os);

            // Get current thread name for identity purpose
            String threadName = Thread.currentThread().getName();

            // Read request from client (browser)
            String clientReq = br.readLine();
            System.out.printf("[%s] Request from browser: %s\n", threadName, clientReq);

            String[] clientReqTerms = clientReq.split(" ");

            // Check for request method
            String requestMethod = clientReqTerms[0];
            if (!requestMethod.equals("GET")) {
                System.out.printf("Sending to browser: 405 Method not allowed");
                writer.writeString("HTTP/1.1 405 Method Not Allowed");
                writer.writeString("");
                writer.writeString(requestMethod + " not supported");
                writer.flush();
                writer.close();
                socket.close();
            }

            // Check for requested resources
            String requestedRes = clientReqTerms[1];
            if (requestedRes.equals("/") ) {
                requestedRes = "/index.html";
            }

            // Loop through the directories to find the requested file
            boolean fileFound = false;
            for (String dir : directories) {
                boolean isPng = requestedRes.contains(".png");
                byte[] requestedFile = Files.readAllBytes(Paths.get("%s%s".formatted(dir, requestedRes)));

                Path directory = Paths.get(dir);
                String fileName = requestedRes.replaceAll("/", "");
                fileFound = Files.walk(directory)
                                .filter(Files::isRegularFile)
                                .anyMatch(f -> f.endsWith(fileName));

                if (fileFound && !isPng) {
                    System.out.printf("File <%s> found!\n", fileName);
                    System.out.printf("Sending to browser: 200 OK\n");

                    writer.writeString("HTTP/1.1 200 OK");
                    writer.writeString("Content-Type: text/html");
                    writer.writeString("Content-Length: " + requestedFile.length);
                    writer.writeString("");
                    writer.writeBytes(requestedFile);
                    writer.flush();
                    writer.close();
                    socket.close();
                    break;
                } else if (fileFound && isPng) {
                    System.out.printf("Image <%s> found!\n", fileName);
                    System.out.printf("Sending to browser: 200 OK\n");

                    writer.writeString("HTTP/1.1 200 OK");
                    writer.writeString("Content-Type: image/png");
                    writer.writeString("Content-Length: " + requestedFile.length);
                    writer.writeString("");
                    writer.writeBytes(requestedFile);
                    writer.flush();
                    writer.close();
                    socket.close();
                    break;
                } else {
                    continue;
                }
            }

            if (fileFound == false) {
                System.out.printf("File not found!\n");
                System.out.printf("Sending to browser: 404 Resource not found\n");
                writer.writeString("HTTP/1.1 404 Not Found");
                writer.writeString("");
                writer.writeString(requestedRes + " not found");
                writer.flush();
                writer.close();
                socket.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
