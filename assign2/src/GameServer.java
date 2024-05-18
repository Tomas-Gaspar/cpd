import java.io.BufferedReader;
import java.io.PrintWriter;
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
    private boolean ranked;

    public GameServer(List<Condition> conditions, Lock lock, boolean ranked) {
        this.number = ServerController.getRandomNumber();
        this.conditions = conditions;
        this.lock = lock;
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

                output += String.format("%d. %-20s %s%n", i+1, results.get(i).getKey(), results.get(i).getValue());
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
        new Thread(new Runnable() {
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

                    for (var entry : guesses.entrySet())
                        results.add(new Pair<>(entry.getKey(), entry.getValue()));
                    results.sort((a, b) -> Math.abs(a.getValue() - number) - Math.abs(b.getValue() - number));

                    if (ranked) {
                        // TODO update elo
                    }
    
                    for (Condition condition : conditions)
                        condition.signal();
                } finally {
                    lock.unlock();
                }

            }
        }).start();
    }

    public static int getGuess(BufferedReader reader, PrintWriter writer) {
        Integer guess = null;
        long startTime = System.currentTimeMillis();

        try {

            while (guess == null) {
                if ((System.currentTimeMillis() - startTime) > ServerController.MAX_GUESS_TIMEOUT) {
                    writer.println("1");
                    writer.println("Timeout");
                    break;
                }
                writer.println("0");

                String line = reader.readLine();
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

    public static int expectedScore(int averageScore, int currentScore){
        return (int) Math.round(1 / (1 + Math.pow(10, (averageScore - currentScore) / 400)));
    }

    public static int calculateScore(int currentScore, int expectedScore, int actualScore, int numPlayers){
        int k = getK(currentScore);
        return Math.min(3000, currentScore + (k/numPlayers) * (actualScore - expectedScore));
    }
    
    public static int getK(int currentScore){
        if (currentScore < 2100)
            return 32;
        else if (currentScore < 2400)
            return 24;
        else
            return 16;
    }
}