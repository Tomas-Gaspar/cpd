import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class UserDB {
    private String header;
    private HashMap<String, List<String>> users;

    public void loadDB() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("users.csv"));
        String line;
        header = reader.readLine(); // Skip header
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(",");
            users.put(parts[0], Arrays.asList(Arrays.copyOfRange(parts, 1, parts.length)));
        }
        reader.close();
    }

    public void storeDB() throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter("users.csv"));
        writer.write(header + "\n");
        for (String username : users.keySet()) {
            StringBuilder sb = new StringBuilder();
            sb.append(username);
            for (int i = 0; i < users.get(username).size(); i++) {
                sb.append(",");
                sb.append(users.get(username).get(i));
            }
            sb.append("\n");
            writer.write(sb.toString());
        }
        writer.close();

    }

    public boolean login(String username, String password) {
        return users.containsKey(username) && users.get(username).get(1).equals(password);
    }

    public boolean register(String username, String password, String passwordConfirm) {
        if (users.containsKey(username) || password.length() < 4 || !password.equals(passwordConfirm)) {
            return false;
        } else {
            users.put(username, Arrays.asList(password, "0"));
            return true;
        }
    }

    public List<Pair<String, Integer>> getLeaderboard() {
        List<Pair<String, Integer>> leaderboard = new ArrayList<>();
        for (String username : users.keySet()) {
            leaderboard.add(new Pair<>(username, Integer.parseInt(users.get(username).get(1))));
        }

        leaderboard.sort((a, b) -> b.getValue() - a.getValue());

        return leaderboard;
    }
}
