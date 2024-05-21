import java.util.List;
import java.io.*;

public class MainMenuServer {
    private BufferedReader reader;
    private PrintWriter writer;
    private UserDB userDB;
    private String clientId;

    public MainMenuServer(BufferedReader reader, PrintWriter writer, UserDB userDB, String clientId) {
        this.reader = reader;
        this.writer = writer;
        this.userDB = userDB;
        this.clientId = clientId;
    }

    public ServerController.MainMenuOption start() {
        try {
            writer.println("Welcome to the Main Menu, " + clientId + "!");
            while (true) {
                String option = reader.readLine();
                switch (option) {
                    case "1":
                        writer.println("0");
                        return ServerController.MainMenuOption.UNRANKED;
                    case "2":
                        writer.println("1");
                        return ServerController.MainMenuOption.RANKED;
                    case "3":
                        writer.println("2");
                        List<Pair<String,Integer>> leaderboard = userDB.getLeaderboard();
                        writer.format("%-20s %s\n", "Username", "Score");
                        writer.println("-".repeat(26));;
                        for (Pair<String,Integer> entry : leaderboard) {
                            writer.format("%-20s %d\n", entry.getKey(), entry.getValue());
                        }
                        writer.println("2");
                        break;
                    case "4":
                        writer.println("3");
                        return ServerController.MainMenuOption.QUIT;
                    default:
                        writer.println("4");
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