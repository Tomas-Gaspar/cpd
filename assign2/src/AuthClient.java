import java.io.*;
import java.util.Arrays;
import java.util.Scanner;

public class AuthClient {
    private Scanner sc;
    private BufferedReader reader;
    private PrintWriter writer;

    public AuthClient(BufferedReader reader, PrintWriter writer) {
        this.reader = reader;
        this.writer = writer;
    }

    public void login() throws IOException {
        System.out.println("\n=================================");
        System.out.println("|             Login             |");
        System.out.println("=================================\n");

        Console console = System.console();
        while (true) {
            System.out.print("Username: ");
            String username = sc.nextLine();
            writer.println(username);
            
            char[] passwordArray = console.readPassword("Password: ");
            String password = new String(passwordArray);
            writer.println(password);
            Arrays.fill(passwordArray, '0');

            String response = reader.readLine();

            switch (response) {
                case "0":
                    System.out.println("Login successful!");
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
        System.out.println("\n=================================");
        System.out.println("|           Register            |");
        System.out.println("=================================\n");

        Console console = System.console();
        while (true) {
            System.out.print("Username: ");
            String username = sc.nextLine();
            writer.println(username);
            
            char[] passwordArray = console.readPassword("Password: ");
            String password = new String(passwordArray);
            writer.println(password);
            Arrays.fill(passwordArray, '0');

            char[] passwordConfirmArray = console.readPassword("Confirm password: ");
            String passwordConfirm = new String(passwordConfirmArray);
            writer.println(passwordConfirm);
            Arrays.fill(passwordConfirmArray, '0');

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

        while (true) {
            System.out.println("\n=================================");
            System.out.println("|            Welcome            |");
            System.out.println("=================================\n");
            System.out.println("[1] Login");
            System.out.println("[2] Register");
            System.out.print(">> ");

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