import java.io.*;
import java.net.*;
import java.util.Scanner;

public class AuthClient {
    private static PrintWriter writer;
    private static BufferedReader reader;
    private static int clientID;

    public static Boolean login() throws IOException {
        System.out.println("Login");
        Scanner sc = new Scanner(System.in);

        System.out.print("Username: ");
        String username = sc.nextLine();
        writer.println(username);

        System.out.print("Password: ");
        String password = sc.nextLine();
        writer.println(password);

        writer.println(1);

        int response = Integer.parseInt(reader.readLine());

        switch (response) {
            case 0 -> {
                System.out.println("Invalid credentials");
                return false;
            }
            case 1 -> {
                System.out.println("Login successful");
                clientID = Integer.parseInt(reader.readLine());
                return true;
            }
            default -> {
                System.out.println("Unexpected error");
                return false;
            }
        }
    }

    public static Boolean register() throws IOException {
        System.out.println("Register");
        Scanner sc = new Scanner(System.in);

        System.out.print("Username: ");
        String username = sc.nextLine();
        writer.println(username);

        System.out.print("Password: ");
        String password = sc.nextLine();
        writer.println(password);

        writer.println(1);

        int response = Integer.parseInt(reader.readLine());

        switch (response) {
            case 0 -> {
                System.out.println("User already exists");
                return false;
            }
            case 1 -> {
                System.out.println("User registered successfully");
                clientID = Integer.parseInt(reader.readLine());
                return true;
            }
            default -> {
                System.out.println("Unexpected error");
                return false;
            }
        }
    }

    public static void authenticate() throws IOException {
        boolean authenticated = false;

        while (!authenticated) {
            System.out.println("Select an option:");
            System.out.println("1 - Register");
            System.out.println("2 - Login");

            Scanner sc = new Scanner(System.in);
            String option = sc.nextLine();

            switch (option) {
                case "1" -> authenticated = register();
                case "2" -> authenticated = login();
                default -> System.out.println("Invalid option");
            }

            System.out.println("\n");
        }
    }

    public static void main(String[] args) {
        if (args.length < 2) return;

        String hostname = args[0];
        int port = Integer.parseInt(args[1]);

        try (Socket socket = new Socket(hostname, port)) {
            Scanner sc = new Scanner(System.in);
            OutputStream output = socket.getOutputStream();
            writer = new PrintWriter(output, true);
            InputStream input = socket.getInputStream();
            reader = new BufferedReader(new InputStreamReader(input));

            authenticate();

            System.out.println(reader.readLine());

            sc.close();
        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        }
    }
}