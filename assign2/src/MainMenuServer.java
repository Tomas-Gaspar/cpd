import java.net.*;
import java.util.List;
import java.io.*;

public class MainMenuServer {
    private Socket socket;
    private UserDB userDB;
    private String clientId;

    public MainMenuServer(Socket socket, UserDB userDB, String clientId) {
        this.socket = socket;
        this.userDB = userDB;
        this.clientId = clientId;
    }

    public ServerController.MainMenuOption start() {
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
                        writer.write(0);
                        return ServerController.MainMenuOption.MATCHMAKING;
                    case "2":
                        writer.write(1);
                        List<Pair<String,Integer>> leaderboard = userDB.getLeaderboard();
                        writer.format("%-20s %s\n", "Username", "Score");
                        for (Pair<String,Integer> entry : leaderboard) {
                            writer.format("%-20s %d\n", entry.getKey(), entry.getValue());
                        }
                        break;
                    case "3":
                        writer.write(2);
                        return ServerController.MainMenuOption.QUIT;
                    default:
                        writer.write(3);
                        break;
                }
            }
        } catch (Exception ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
        return ServerController.MainMenuOption.QUIT;
    }
}