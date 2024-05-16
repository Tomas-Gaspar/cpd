import java.io.*;
import java.util.Scanner;

public class GameClient {
    private BufferedReader reader;
    private PrintWriter writer;

    public GameClient(BufferedReader reader, PrintWriter writer) {
        this.reader = reader;
        this.writer = writer;
    }

    public void start() throws IOException {
        Scanner sc = new Scanner(System.in);

        while (true) {
            String response = reader.readLine();

            switch (response) {
                case "0":
                    System.out.print("Enter a number [0, 100[: ");
                    String num = sc.nextLine();
                    writer.println(num);
                    break;
                case "1":
                    String errorMessage = reader.readLine();
                    System.out.println(errorMessage);
                    break;
                case "2":
                    while (true) {
                        String line = reader.readLine();
                        if (line.equals("2")) {
                            return;
                        }
                        System.out.println(line);
                    }
            
                default:
                    break;
            }
        }
    }
}