import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerController {
    public static enum MainMenuOption {
        MATCHMAKING,
        QUIT
    }

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(args[0]);
        UserDB userDB = new UserDB();
        userDB.loadDB();

        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server is listening on port " + port);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    userDB.storeDB();
                    serverSocket.close();
                } catch (IOException e) {
                    System.out.println("Error closing server.");
                    e.printStackTrace();
                }
            }
        });

        while (true) {
            Socket socket = serverSocket.accept();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    AuthServer authServer = new AuthServer(socket, userDB);
                    String clientId = authServer.start();

                    MainMenuServer mainMenuServer = new MainMenuServer(socket, userDB, clientId);
                    MainMenuOption option = mainMenuServer.start();
                    switch (option) {
                        case MATCHMAKING:
                            // go to matchmaking
                            break;
                        case QUIT:
                            try {
                                socket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        default:
                            break;
                    }
                }
            }).start();
        }
    }
}
