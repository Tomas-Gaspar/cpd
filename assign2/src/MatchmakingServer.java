import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class MatchmakingServer {
    private static final int MIN_PLAYERS = 2;

    private List<ClientInfo> matchmakingQueue = new ArrayList<>();
    private Map<String, GameServer> games = new HashMap<>();
    private Lock lock;

    public MatchmakingServer(Lock lock) {
        this.lock = lock;
    }

    public void addToQueue(String clientId, Socket socket, Condition matchMakingCondition, Condition gameCondition) {
        lock.lock();
        try {
            for (int i = 0; i < matchmakingQueue.size(); i++) {
                if (matchmakingQueue.get(i).getClientId().equals(clientId)) {
                    matchmakingQueue.set(i, new ClientInfo(clientId, socket, matchMakingCondition, gameCondition));
                    return;
                }
            }
            matchmakingQueue.add(new ClientInfo(clientId, socket, matchMakingCondition, gameCondition));
        } finally {
            lock.unlock();
        }
    }

    public void startMatchmaking() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    lock.lock();
                    try {
                        if (matchmakingQueue.size() >= MIN_PLAYERS) {
                            List<Condition> matchmakingConditions = new ArrayList<>(), gameConditions = new ArrayList<>();
                            List<String> players = new ArrayList<>();
                            for (int i = 0; i < MIN_PLAYERS; i++) {
                                ClientInfo client = matchmakingQueue.remove(0);
                                matchmakingConditions.add(client.getMatchMakingCondition());
                                gameConditions.add(client.getGameCondition());
                                players.add(client.getClientId());
                            }

                            GameServer game = new GameServer(gameConditions, lock);
                            for (String clientId : players)
                                games.put(clientId, game);
                            game.startGame();

                            for (Condition condition : matchmakingConditions)
                                condition.signal();
                        }
                    } finally {
                        lock.unlock();
                    }                    
                }
            }
        }).start();
    }

    public GameServer getPlayerGame(String clientId) {
        lock.lock();
        try {
            return games.get(clientId);
        } finally {
            lock.unlock();
        }
    }
}

class ClientInfo {
    private String clientId;
    private Socket socket;
    private Condition matchMakingCondition;
    private Condition gameCondition;

    public ClientInfo(String clientId, Socket socket, Condition matchMakingCondition, Condition gameCondition) {
        this.clientId = clientId;
        this.socket = socket;
        this.matchMakingCondition = matchMakingCondition;
        this.gameCondition = gameCondition;
    }

    public String getClientId() {
        return clientId;
    }

    public Socket getSocket() {
        return socket;
    }

    public Condition getMatchMakingCondition() {
        return matchMakingCondition;
    }

    public Condition getGameCondition() {
        return gameCondition;
    }

}