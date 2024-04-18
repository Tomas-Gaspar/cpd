import java.util.HashMap;

public class UserDB {
    private HashMap<String, String> users;

    public boolean loadDB() { // TODO: Change to read from file
        users = new HashMap<>();
        users.put("user1", "password1");
        users.put("user2", "password2");

        return true;
    }

    public boolean storeDB() { // TODO: Change to write to file
        return false;
    }

    public boolean authenticate(String username, String password) {
        if (users.containsKey(username) && users.get(username).equals(password)) {
            return true;
        } else {
            return false;
        }
    }
}
