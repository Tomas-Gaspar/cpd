import java.net.*;
import java.io.*;

public class MainMenuServer {
    private Socket socket;
    private String clientId;

    public static enum MainMenuOption {
        MATCHMAKING,
        QUIT
    }

    public MainMenuServer(Socket socket, String clientId) {
        this.socket = socket;
        this.clientId = clientId;
    }

    public MainMenuOption start() {
        try {
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);

            writer.println("Welcome to the Main Menu, " + clientId + "!");
            while (true) {
                String option = reader.readLine();
                switch (option) {
                    case "1":
                        return MainMenuOption.MATCHMAKING;
                    case "2":
                        // go to leaderboard
                        break;
                    case "3":
                        return MainMenuOption.QUIT;
                    default:
                        break;
                }
            }
        } catch (Exception ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
        return MainMenuOption.QUIT;
    }
}