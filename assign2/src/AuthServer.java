import java.io.*;
import java.net.*;

public class AuthServer {
    private BufferedReader reader;
    private PrintWriter writer;

    private Socket socket;
    private UserDB userDB;

    public AuthServer(Socket socket, UserDB userDB) {
        this.socket = socket;
        this.userDB = userDB;
    }

    public String login() {
        String username, password;
        while (true) {
            try {
                username = reader.readLine();
                password = reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

            if (userDB.login(username, password)) {
                writer.print(0);
                return username;
            } else {
                writer.print(1);
            }
        }
    }

    public String register() {
        String username, password, passwordConfirm;
        while (true) {
            try {
                username = reader.readLine();
                password = reader.readLine();
                passwordConfirm = reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

            if (userDB.register(username, password, passwordConfirm)) {
                writer.print(0);
                return username;
            } else {
                writer.print(1);
            }
        }
    }


    public String start() {
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);

            while (true) {
                String option = reader.readLine();
                switch (option) {
                    case "1":
                        writer.print(0);
                        return login();
                    case "2":
                        writer.print(1);
                        return register();
                    default:
                        writer.print(-1);
                        break;
                }
            }
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }

        return null;
    }
}