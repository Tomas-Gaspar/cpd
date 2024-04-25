import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MatchmakingServer {
    private List<Pair<String, Socket>> matchmakingQueue = new ArrayList<>();

    public synchronized void addToQueue(String clientId, Socket socket) {
        for (int i = 0; i < matchmakingQueue.size(); i++) {
            if (matchmakingQueue.get(i).getKey().equals(clientId)) {
                matchmakingQueue.set(i, new Pair<String,Socket>(clientId, socket));
                return;
            }
        }
        matchmakingQueue.add(new Pair<String,Socket>(clientId, socket));
    }

    public void startMatchmaking() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    // TODO matchmaking
                }
            }
        }).start();
    }    
}
