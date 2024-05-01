import java.net.*;
import java.io.*;
import java.util.Scanner;

public class GameClient {
    private Socket socket;

    public GameClient(Socket socket) {
        this.socket = socket;
    }

    public void start() throws IOException {
        Scanner sc = new Scanner(System.in);
        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

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