import java.io.*;
import java.net.*;
import java.util.HashMap;

public class Auth {
    private HashMap<String, String> users;
    private static BufferedReader reader;
    private PrintWriter writer;

    public Auth() {
        users = new HashMap<>();
        users.put("user1", "password1");
        users.put("user2", "password2");
    }

    public int autenticate() throws IOException {
        String username = reader.readLine();
        String password = reader.readLine();

        int response = Integer.parseInt(reader.readLine());

        switch (response) {
            case 1 -> {
                if (users.containsKey(username)) {
                    writer.println(0);
                    return 0;
                } else if (password.length() < 4) {
                    writer.println(2);
                    return 0;
                } else {
                    users.put(username, password);
                    writer.println(1);
                    System.out.println("User added to DB: " + users);
                    return 1;
                }
            }
            case 2 -> {
                if (users.containsKey(username) && users.get(username).equals(password)) {
                    writer.println(1);
                    return 1;
                } else {
                    writer.println(0);
                    return 0;
                }
            }
            default -> {
                System.out.println("Unexpected error");
                return -1;
            }
        }
    }

    public void start(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");

                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new PrintWriter(socket.getOutputStream(), true);

                int response = autenticate();

                if (response == 1)
                    writer.println();
            }
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Auth server = new Auth();
        server.start(5000);
    }
}