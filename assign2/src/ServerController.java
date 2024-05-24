import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ServerController {
    public static enum MainMenuOption {
        RANKED,
        UNRANKED,
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

        serverSocket.setSoTimeout(0);

        Thread virtualThread = Thread.ofVirtual().factory().newThread(() -> {
            try {
                userDB.storeDB();
                serverSocket.close();
            } catch (IOException e) {
                System.out.println("Error closing server.");
                e.printStackTrace();
            }
        });
        
        Runtime.getRuntime().addShutdownHook(virtualThread);

        while (true) {
            Socket socket = serverSocket.accept();

            Thread.ofVirtual().start(new Runnable() {
                @Override
                public void run() {
                    BufferedReader reader;
                    PrintWriter writer;
                    try {
                        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        writer = new PrintWriter(socket.getOutputStream(), true);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }

                    AuthServer authServer = new AuthServer(reader, writer, userDB);
                    String clientId = authServer.start();

                    MainMenuServer mainMenuServer = new MainMenuServer(reader, writer, userDB, clientId);

                    while (true) {                        
                        MainMenuOption option = mainMenuServer.start();
                        switch (option) {
                            case RANKED, UNRANKED:
                                Condition matchMakingCondition = lock.newCondition(), gameCondition = lock.newCondition();
                                if (option == MainMenuOption.RANKED) {
                                    if (matchmakingServer.addToQueue(clientId, socket, matchMakingCondition, gameCondition)) {
                                        writer.println("0");
                                    } else {
                                        writer.println("1");
                                        continue;
                                    }
                                } else {
                                    if (matchmakingServer.addToUnrankedQueue(clientId, socket, matchMakingCondition, gameCondition)) {
                                        writer.println("0");
                                    } else {
                                        writer.println("1");
                                        continue;
                                    }
                                }
                                lock.lock();
                                try {
                                    socket.setSoTimeout(2000);
                                    while (matchmakingServer.getPlayerGame(clientId) == null) {
                                        try {
                                            // wait to find a match
                                            matchMakingCondition.awaitNanos(1000000000);
                                            writer.println("HEARTBEAT");
                                            String line = reader.readLine();
                                            if (line == null || !line.equals("HEARTBEAT")){
                                                matchmakingServer.connectionLost(clientId);
                                                return;
                                            }
                                            
                                        } catch (SocketTimeoutException e) {
                                            matchmakingServer.connectionLost(clientId);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    socket.setSoTimeout(0);

                                    writer.println("MATCHED");

                                } catch (IOException e) {
                                    e.printStackTrace();
                                } finally {
                                    lock.unlock();
                                }

                                GameServer gameServer = matchmakingServer.getPlayerGame(clientId);

                                Integer guess = GameServer.getGuess(socket, reader, writer);
                                gameServer.addGuess(clientId, guess);

                                // If timeout occured consume the guess
                                if (guess == null) {
                                    try {
                                        reader.readLine();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }

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
                                GameServer.sendResult(writer, result);

                                matchmakingServer.endGame(clientId);

                                break;
                            case QUIT:
                                try {
                                    socket.close();
                                    reader.close();
                                    writer.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                return;
                            default:
                                break;
                        }
                    }
                }
            });
        }
    }

    public synchronized static int getRandomNumber() {
        return random.nextInt(100);
    }
}
