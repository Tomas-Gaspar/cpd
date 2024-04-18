import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class MainMenuClient {
    private Socket socket;

    public MainMenuClient(Socket socket) {
        this.socket = socket;
    }

    public void start() {
        try {
            Scanner sc = new Scanner(System.in);
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            while (true) {
                System.out.println(reader.readLine());
                System.out.println("Please select an option:");
                System.out.println("1. Play");
                System.out.println("2. Leaderboard");
                System.out.println("3. Quit");

                String option = sc.nextLine();
                writer.println(option);
                
                switch (option) {
                    case "1":
                        return;
                    case "2":
                        String leaderboard = reader.readLine();
                        System.out.println(leaderboard);
                        break;
                    case "3":
                        System.out.println("Goodbye!");
                        return;
                    default:
                        System.out.println("Invalid option. Please try again.");
                        break;
                }
            }
        } 
        catch (IOException ex) {
 
            System.out.println("I/O error: " + ex.getMessage());
        }
    }
}