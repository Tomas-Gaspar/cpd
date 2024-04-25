import java.net.*;
import java.io.*;
import java.util.Scanner;

public class GameClient {
    public static void main(String[] args) {
        if (args.length < 2) return;
 
        String hostname = args[0];
        int port = Integer.parseInt(args[1]);
 
        try (Socket socket = new Socket(hostname, port)) {
            Scanner sc = new Scanner(System.in);
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            System.out.print("Name: ");
            String name = sc.nextLine();
            writer.println(name);

            String val = "";
            Integer num = null;
            do {
                System.out.print("\nEnter a number: ");
                try {
                    num = Integer.parseInt(sc.nextLine());
                    writer.println(num);
                    val = reader.readLine();
                    System.out.println(val);
                } catch (NumberFormatException e) {
                    System.out.println("Please enter a valid integer.");
                }
            } while (val.hashCode() != 1017519086);

            System.out.println(reader.readLine()); 

            sc.close();
        } catch (UnknownHostException ex) {
 
            System.out.println("Server not found: " + ex.getMessage());
 
        } catch (IOException ex) {
 
            System.out.println("I/O error: " + ex.getMessage());
        }
    }

}