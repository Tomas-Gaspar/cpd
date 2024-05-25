import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class GameServer {
    private int number;
    private boolean awaitingGuesses = true;
    private List<Condition> conditions;
    private Map<String, Integer> guesses = new HashMap<>();
    private List<Pair<String, Integer>> results = new ArrayList<>();
    private Lock lock;
    private UserDB userDB;
    private boolean ranked;

    public GameServer(List<Condition> conditions, Lock lock, UserDB userDB, boolean ranked) {
        this.number = ServerController.getRandomNumber();
        this.conditions = conditions;
        this.lock = lock;
        this.userDB = userDB;
        this.ranked = ranked;
    }

    public void addGuess(String clientId, Integer guess) {
        lock.lock();
        try {
            guesses.put(clientId, guess);
        } finally {
            lock.unlock();
        }
    }

    public boolean awaitingGuesses() {
        return awaitingGuesses;
    }

    public String getResult(String clientId) {
        String output = "";

        lock.lock();
        try {
            Integer clientPos = null;
            for (int i = 0; i < results.size(); i++) {
                if (results.get(i).getKey().equals(clientId)) {
                    clientPos = i;
                }

                Integer score = results.get(i).getValue();
                String scoreS;
                if (score == null) scoreS = "No guess";
                else scoreS = Integer.toString(score);

                output += String.format("%d. %-20s %s%n", i+1, results.get(i).getKey(), scoreS);
            }

            if (clientPos != null)
                output += "\nYour place: " + (clientPos+1) + "\n";
            output += "Correct number: " + number + "\n";
        } finally {
            lock.unlock();
        }

        return output;
    }

    public void startGame() {
        Thread.ofVirtual().start(new Runnable() {
            @Override
            public void run() {
                long startTime = System.currentTimeMillis();
                // wait for all guesses (max 20 seconds)
                while (guesses.keySet().size() < conditions.size() && (System.currentTimeMillis() - startTime) < ServerController.MAX_GUESS_TIMEOUT) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                lock.lock();
                try {
                    awaitingGuesses = false;

                    double averageLobbyElo = 0;
                    for (var entry : guesses.entrySet()) {
                        results.add(new Pair<>(entry.getKey(), entry.getValue()));
                        averageLobbyElo += userDB.getElo(entry.getKey());
                    }
                    results.sort((a, b) -> {
                        if (a.getValue() == null && b.getValue() == null)
                            return 0;
                        else if (a.getValue() == null)
                            return 1;
                        else if (b.getValue() == null)
                            return -1;
                        else return Math.abs(a.getValue() - number) - Math.abs(b.getValue() - number);
                    });
                    averageLobbyElo /= guesses.size();

                    if (ranked) {
                        for (int i = 0; i < results.size(); i++) {
                            int currentPlayerElo = userDB.getElo(results.get(i).getKey());
                            double expectedPlacement = expectedPlacement(averageLobbyElo, currentPlayerElo);
                            int newElo = calculateElo(currentPlayerElo, expectedPlacement, i+1, results.size());
                            userDB.updateElo(results.get(i).getKey(), newElo);
                        }
                    }
    
                    for (Condition condition : conditions)
                        condition.signal();
                } finally {
                    lock.unlock();
                }

            }
        });
    }

    public static Integer getGuess(Socket socket, BufferedReader reader, PrintWriter writer) {
        Integer guess = null;
        long startTime = System.currentTimeMillis();

        try {

            while (guess == null) {
                int time = (int) (System.currentTimeMillis() - startTime);
                if (time > ServerController.MAX_GUESS_TIMEOUT) {
                    writer.println("1");
                    writer.println("Timeout");
                    break;
                }
                writer.println("0");

                socket.setSoTimeout(ServerController.MAX_GUESS_TIMEOUT - time);
                String line = null;
                try {
                    line = reader.readLine();
                } catch (SocketTimeoutException e) {
                    continue;
                } finally {
                    socket.setSoTimeout(0);
                }

                try {
                    guess = Integer.parseInt(line);
                } catch (NumberFormatException e) {
                    writer.println("1");
                    writer.println("Invalid number");
                    continue;
                }
            }

        } catch (Exception ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }

        return guess;
    }

    public static void sendResult(PrintWriter writer, String result) {
        try {

            writer.println("2");
            writer.println(result);
            writer.println("2");
        } catch (Exception ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static double expectedPlacement(double averageLobbyElo, int currentPlayerElo){
        return 1 / (1 + Math.pow(10, (averageLobbyElo - currentPlayerElo) / 400));
    }

    public static int calculateElo(int currentPlayerElo, double expectedPlacement, int position, int numPlayers){
        double actualPlacement = 1 - (double)(position - 1) / (numPlayers - 1);
        int k = getK(currentPlayerElo);
        return Math.max(0, Math.min(3000, (int) Math.round(currentPlayerElo + (k/numPlayers) * (actualPlacement - expectedPlacement))));
    }
    
    public static int getK(int currentPlayerElo){
        if (currentPlayerElo < 2100)
            return 32;
        else if (currentPlayerElo < 2400)
            return 24;
        else
            return 16;
    }
}