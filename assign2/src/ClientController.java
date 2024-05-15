import java.net.*;
import java.io.*;

public class ClientController {
    public static void main(String[] args) {
        if (args.length < 2) return;
 
        String hostname = args[0];
        int port = Integer.parseInt(args[1]);
 
        try (Socket socket = new Socket(hostname, port)) {

            AuthClient authClient = new AuthClient(socket);
            authClient.start();

            MainMenuClient mainMenuClient = new MainMenuClient(socket);
            GameClient gameClient = new GameClient(socket);

            while (true) {
                int response = mainMenuClient.start();
    
                if (response == 0) {
                    handleHeartbeat(socket);

                    gameClient.start();
                } else if (response == 2) {
                    return;
                }
            }

        } catch (UnknownHostException ex) {
 
            System.out.println("Server not found: " + ex.getMessage());
 
        } catch (IOException ex) {
 
            System.out.println("I/O error: " + ex.getMessage());
        }
    }

    public static void handleHeartbeat(Socket socket) throws IOException {
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);

            while (true) {
                String line = reader.readLine();
                if (line.equals("HEARTBEAT")) {
                    writer.println("HEARTBEAT");
                } else if (line.equals("MATCHED")) {
                    return;
                }
            }
    }
}