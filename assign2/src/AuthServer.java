import java.io.*;

public class AuthServer {
    private BufferedReader reader;
    private PrintWriter writer;
    private UserDB userDB;

    public AuthServer(BufferedReader reader, PrintWriter writer, UserDB userDB) {
        this.reader = reader;
        this.writer = writer;
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
                writer.println("0");
                return username;
            } else {
                writer.println("1");
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
                writer.println("0");
                return username;
            } else {
                writer.println("1");
            }
        }
    }


    public String start() {
        try {
            while (true) {
                String option = reader.readLine();
                switch (option) {
                    case "1":
                        writer.println("0");
                        return login();
                    case "2":
                        writer.println("1");
                        return register();
                    default:
                        writer.println("-1");
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