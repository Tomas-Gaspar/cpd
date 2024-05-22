import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class MainMenuClient {
    private BufferedReader reader;
    private PrintWriter writer;

    public MainMenuClient(BufferedReader reader, PrintWriter writer) {
        this.reader = reader;
        this.writer = writer;
    }

    public int start() throws IOException {
        Scanner sc = new Scanner(System.in);

        boolean start = true;

        while (true) {
            System.out.println("\n=================================");
            System.out.println("|           Main Menu           |");
            System.out.println("=================================\n");

            if (start){
                String welcomeMessage = reader.readLine();
                System.out.println(welcomeMessage + '\n');
                start = false;
            }
            
            System.out.println("[1] Simple Game");
            System.out.println("[2] Ranked Game");
            System.out.println("[3] Leaderboard");
            System.out.println("[4] Quit");
            System.out.print(">> ");

            String option = sc.nextLine();
            writer.println(option);

            String response = reader.readLine();
            switch (response) {
                case "0":
                    System.out.println("\nEntering Simple Game Queue...\n");
                    response = reader.readLine();
                    if (response.equals("0")) {
                        return 0;
                    } else if (response.equals("1")) {
                        System.out.println("ERROR: It looks like this account is already in a queue.\n");
                        return 2;
                    }
                case "1":
                    System.out.println("\nEntering Ranked Game Queue...\n");
                    response = reader.readLine();
                    if (response.equals("0")) {
                        return 1;
                    } else if (response.equals("1")) {
                        System.out.println("ERROR: It looks like this account is already in a queue.\n");
                        return 2;
                    }
                case "2":
                    System.out.println("\n=================================");
                    System.out.println("|          Leaderboard          |");
                    System.out.println("=================================\n");
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.equals("2"))
                            break;
                        System.out.println(line);
                    }
                    System.out.println();

                    break;
                case "3":
                    System.out.println("Quitting...");
                    return 3;
                case "4":
                    System.out.println("Invalid option");
                    break;
            }
        }
    } 
}