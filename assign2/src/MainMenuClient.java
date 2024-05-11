import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class MainMenuClient {
    private Socket socket;

    public MainMenuClient(Socket socket) {
        this.socket = socket;
    }

    public int start() throws IOException {
        Scanner sc = new Scanner(System.in);
        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        System.out.println("\n=================================");
        System.out.println("|           Main Menu           |");
        System.out.println("=================================\n");
        String welcomeMessage = reader.readLine();
        System.out.println(welcomeMessage + '\n');
        while (true) {
            System.out.println("[1] Play");
            System.out.println("[2] Leaderboard");
            System.out.println("[3] Quit");
            System.out.print(">> ");

            String option = sc.nextLine();
            writer.println(option);

            String response = reader.readLine();
            switch (response) {
                case "0":
                    System.out.println("\nEntering Queue...");
                    return 0;
                case "1":
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.equals("1"))
                            break;
                        System.out.println(line);
                    }
                    break;
                case "2":
                    System.out.println("Quitting...");
                    return 2;
                case "3":
                    System.out.println("Invalid option");
                    break;
            }
        }
    } 
}