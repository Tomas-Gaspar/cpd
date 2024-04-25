import java.io.*;
import java.net.*;
import java.util.Scanner;

public class AuthClient {
    private Scanner sc;
    private BufferedReader reader;
    private PrintWriter writer;

    private Socket socket;

    public AuthClient(Socket socket) {
        this.socket = socket;
    }

    public void login() throws IOException {
        System.out.println("Login");

        while (true) {
            System.out.print("Username: ");
            String username = sc.nextLine();
            writer.println(username);
            
            System.out.print("Password: ");
            String password = sc.nextLine();
            writer.println(password);

            String response = reader.readLine();

            switch (response) {
                case "0":
                    System.out.println("Login successful");
                    return;
                case "1":
                    System.out.println("Invalid credentials");
                    break;
                default:
                    break;
            }
        }
    }

    public void register() throws IOException {
        System.out.println("Register");

        while (true) {
            System.out.print("Username: ");
            String username = sc.nextLine();
            writer.println(username);
            
            System.out.print("Password: ");
            String password = sc.nextLine();
            writer.println(password);

            System.out.print("Confirm password: ");
            String passwordConfirm = sc.nextLine();
            writer.println(passwordConfirm);

            String response = reader.readLine();

            switch (response) {
                case "0":
                    System.out.println("Register successful");
                    return;
                case "1":
                    if (password.length() < 4)
                        System.out.println("Password must be at least 4 characters long");
                    else if (!password.equals(passwordConfirm))
                        System.out.println("Passwords do not match");
                    else
                        System.out.println("Username already exists");
                default:
                    break;
            }
        }
    }

    public void start() throws IOException {
        sc = new Scanner(System.in);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new PrintWriter(socket.getOutputStream(), true);

        while (true) {
            System.out.println("Select an option:");
            System.out.println("1 - Login");
            System.out.println("2 - Register");

            String option = sc.nextLine();
            writer.println(option);
            String response = reader.readLine();

            switch (response) {
                case "0":
                    login();
                    return;
                case "1":
                    register();
                    return;
                default:
                    System.out.println("Invalid option");
                    break;
            }
        }
    }
}