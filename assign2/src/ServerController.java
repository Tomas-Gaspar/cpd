import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ServerController {
    public static enum MainMenuOption {
        MATCHMAKING,
        QUIT
    }

    public static final int MAX_GUESS_TIMEOUT = 20000;

    private static final Random random = new Random();

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(args[0]);
        ReentrantLock lock = new ReentrantLock();
        UserDB userDB = new UserDB(lock);
        userDB.loadDB();


        MatchmakingServer matchmakingServer = new MatchmakingServer(lock, userDB);
        matchmakingServer.startMatchmaking();

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

                    while (true) {                        
                        MainMenuOption option = mainMenuServer.start();
                        switch (option) {
                            case MATCHMAKING:
                                Condition matchMakingCondition = lock.newCondition(), gameCondition = lock.newCondition();
                                matchmakingServer.addToQueue(clientId, socket, matchMakingCondition, gameCondition);
                                lock.lock();
                                try {
                                    while (matchmakingServer.getPlayerGame(clientId) == null) {
                                        try {
                                            // wait to find a match
                                            matchMakingCondition.await();
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                } finally {
                                    lock.unlock();
                                }

                                GameServer gameServer = matchmakingServer.getPlayerGame(clientId);

                                int guess = GameServer.getGuess(socket);
                                gameServer.addGuess(clientId, guess);

                                lock.lock();
                                try {
                                    while (gameServer.awaitingGuesses()) {
                                        try {
                                            // wait for other players to guess
                                            gameCondition.await();
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                } finally {
                                    lock.unlock();
                                }

                                String result = gameServer.getResult(clientId);
                                GameServer.sendResult(socket, result);

                                matchmakingServer.endGame(clientId);

                                break;
                            case QUIT:
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                return;
                            default:
                                break;
                        }
                    }
                }
            }).start();
        }
    }

    public synchronized static int getRandomNumber() {
        return random.nextInt(100);
    }
}
