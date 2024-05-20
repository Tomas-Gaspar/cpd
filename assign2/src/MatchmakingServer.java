import java.net.Socket;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class MatchmakingServer {
    private static final int MIN_PLAYERS = 2;
    private static final int MAX_PLAYERS = 6;
    private static final int WAITING_THRESHOLD = 30;
    private static final int WAITING_TOLERANCE = 5;
    private static final int MAX_ELO = 100;
    private static final int ELO_DIVISIONS = 6;


    private List<ClientInfo> matchmakingQueue = new ArrayList<>();
    private List<ClientInfo> unrankedQueue = new ArrayList<>();
    private Map<String, GameServer> games = new HashMap<>();
    private Lock lock;
    private UserDB userDB;

    public MatchmakingServer(Lock lock, UserDB userDB) {
        this.lock = lock;
        this.userDB = userDB;
    }

    public boolean addToQueue(String clientId, Socket socket, Condition matchMakingCondition, Condition gameCondition) {
        lock.lock();
        try {
            for (int i = 0; i < unrankedQueue.size(); i++) {
                if (unrankedQueue.get(i).getClientId().equals(clientId)) {
                    if (unrankedQueue.get(i).getSocket() != null)
                        return false;
                }
            }

            for (int i = 0; i < matchmakingQueue.size(); i++) {
                if (matchmakingQueue.get(i).getClientId().equals(clientId)) {
                    if (matchmakingQueue.get(i).getSocket() != null)
                        return false;
                    matchmakingQueue.set(i, new ClientInfo(clientId, socket, matchMakingCondition, gameCondition, matchmakingQueue.get(i).getEntryTime()));
                    return true;
                }
            }
            matchmakingQueue.add(new ClientInfo(clientId, socket, matchMakingCondition, gameCondition, LocalDateTime.now()));
        } finally {
            lock.unlock();
        }
        return true;
    }

    public boolean addToUnrankedQueue(String clientId, Socket socket, Condition matchMakingCondition, Condition gameCondition) {
        lock.lock();
        try {
            for (int i = 0; i < matchmakingQueue.size(); i++) {
                if (matchmakingQueue.get(i).getClientId().equals(clientId)) {
                    if (matchmakingQueue.get(i).getSocket() != null)
                        return false;
                }
            }

            for (int i = 0; i < unrankedQueue.size(); i++) {
                if (unrankedQueue.get(i).getClientId().equals(clientId)) {
                    if (unrankedQueue.get(i).getSocket() != null)
                        return false;
                    unrankedQueue.set(i, new ClientInfo(clientId, socket, matchMakingCondition, gameCondition, unrankedQueue.get(i).getEntryTime()));
                    return true;
                }
            }
            unrankedQueue.add(new ClientInfo(clientId, socket, matchMakingCondition, gameCondition, LocalDateTime.now()));
        } finally {
            lock.unlock();
        }
        return true;
    }

    public void startMatchmaking() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    /*
                     * UNRANKED MATCHMAKING
                     */
                    lock.lock();
                    try {
                        if (unrankedQueue.size() >= MAX_PLAYERS) {
                            List<Condition> matchmakingConditions = new ArrayList<>(), gameConditions = new ArrayList<>();
                            List<String> players = new ArrayList<>();
                            for (int i = 0; i < MAX_PLAYERS; i++) {
                                ClientInfo client = unrankedQueue.remove(0);
                                matchmakingConditions.add(client.getMatchMakingCondition());
                                gameConditions.add(client.getGameCondition());
                                players.add(client.getClientId());
                            }

                            GameServer game = new GameServer(gameConditions, lock, userDB, false);
                            for (String clientId : players)
                                games.put(clientId, game);
                            game.startGame();

                            for (Condition condition : matchmakingConditions)
                                condition.signal();
                        }
                    } finally {
                        lock.unlock();
                    } 


                    /*
                     * RANKED MATCHMAKING
                     */
                    boolean minPlayers = false;
                    lock.lock();
                    try {
                        if (matchmakingQueue.size() < MIN_PLAYERS) {
                            minPlayers = true;
                        }
                    }
                    finally {
                        lock.unlock();
                    }

                    if (minPlayers) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        continue;
                    }
                    
                    List<Condition> matchmakingConditions = new ArrayList<>(), gameConditions = new ArrayList<>();
                    List<List<ClientInfo>> clusters = new ArrayList<>();

                    for (int i = 0; i < ELO_DIVISIONS; i++)
                        clusters.add(new ArrayList<>());

                    boolean changes;
                    int clusterTolerance = 0;
                    // multiple iterations:
                    // -> first try to create a match with players on their cluster
                    // -> then if players have been in the queue for a while, try to put them in adjacent clusters
                    do {
                        changes = false;
                        LocalDateTime time = LocalDateTime.now();

                        lock.lock();
                        try {
                            // cluster players by elo
                            for (ClientInfo client : matchmakingQueue) {
                                // Connection to client has been lost. 
                                // Leave them in the queue for the case that they reconnect but don't put them into a game.
                                if (client.getSocket() == null)
                                    continue;

                                int cluster = Math.min(userDB.getElo(client.getClientId()) / (MAX_ELO / ELO_DIVISIONS), ELO_DIVISIONS - 1);
    
                                if (clusterTolerance == 0) {
                                    clusters.get(cluster).add(client);
                                    changes = true;
                                } else {
                                    if (time.minusSeconds(WAITING_THRESHOLD).isAfter(client.getEntryTime())) {
                                        Duration duration = Duration.between(client.getEntryTime(), time.minusSeconds(WAITING_THRESHOLD));
                                        int clusterOffset = (int) Math.ceil(duration.getSeconds() / (double) WAITING_TOLERANCE);
    
                                        // the longer a player has been waiting the more tolerant we are with the cluster
                                        if (clusterOffset >= clusterTolerance) {
                                            if (cluster - clusterTolerance >= 0) {
                                                clusters.get(cluster - clusterTolerance).add(client);
                                                changes = true;
                                            }
                                            
                                            if (cluster + clusterTolerance < ELO_DIVISIONS) {
                                                clusters.get(cluster + clusterTolerance).add(client);
                                                changes = true;
                                            }
    
                                        }
                                    }
                                }
                            }
                        } finally {
                            lock.unlock();
                        }

                        for (List<ClientInfo> cluster : clusters) {
                            // will never be able to create a match with less than MIN_PLAYERS
                            if (cluster.size() >= MIN_PLAYERS) {
                                cluster.sort((a, b) -> a.getEntryTime().compareTo(b.getEntryTime()));
                                while (cluster.size() >= MAX_PLAYERS) {
                                    List<ClientInfo> players = new ArrayList<>();
                                    for (int i = 0; i < MAX_PLAYERS; i++) {
                                        ClientInfo client = cluster.remove(0);
                                        matchmakingConditions.add(client.getMatchMakingCondition());
                                        gameConditions.add(client.getGameCondition());
                                        players.add(client);

                                        for (List<ClientInfo> otherCluster : clusters)
                                            otherCluster.remove(client);
                                    }

                                    lock.lock();
                                    try {
                                        GameServer game = new GameServer(gameConditions, lock, userDB, true);
                                        for (ClientInfo client : players) {
                                            matchmakingQueue.remove(client);
                                            games.put(client.getClientId(), game);
                                        }
                                        game.startGame();
    
                                        for (Condition condition : matchmakingConditions)
                                            condition.signal();
                                    } finally {
                                        lock.unlock();
                                    }
                                }

                                // will only create matches with less than MAX_PLAYERS if one of the players has been waiting for a long time
                                if (cluster.size() >= MIN_PLAYERS) {
                                    Duration duration = Duration.between(cluster.get(0).getEntryTime(), time.minusSeconds(WAITING_THRESHOLD));
                                    if (duration.getSeconds() < 0)
                                        continue;
                                    int clusterOffset = (int) Math.ceil(duration.getSeconds() / (double) WAITING_TOLERANCE);

                                    if (cluster.size() >= MAX_PLAYERS - clusterOffset) {
                                        List<ClientInfo> players = new ArrayList<>();
                                        int clusterSize = cluster.size();
                                        for (int i = 0; i < clusterSize; i++) {
                                            ClientInfo client = cluster.remove(0);
                                            matchmakingConditions.add(client.getMatchMakingCondition());
                                            gameConditions.add(client.getGameCondition());
                                            players.add(client);

                                            for (List<ClientInfo> otherCluster : clusters)
                                                otherCluster.remove(client);
                                        }

                                        lock.lock();
                                        try {
                                            GameServer game = new GameServer(gameConditions, lock, userDB, true);
                                            for (ClientInfo client : players) {
                                                matchmakingQueue.remove(client);
                                                games.put(client.getClientId(), game);
                                            }
                                            game.startGame();
    
                                            for (Condition condition : matchmakingConditions)
                                                condition.signal();
                                        } finally {
                                            lock.unlock();
                                        }
                                    }
                                }
                            }
                        }

                        clusterTolerance++;
                    } while(changes);

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
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

    public void endGame(String clientId) {
        lock.lock();
        try {
            games.put(clientId, null);
        } finally {
            lock.unlock();
        }
    }

    public void connectionLost(String clientId) {
        System.out.println("Connection lost for " + clientId);
        lock.lock();
        try {
            for (ClientInfo client : matchmakingQueue) {
                if (client.getClientId().equals(clientId)) {
                    client.invalidateSocket();
                    return;
                }
            }

            for (ClientInfo client : unrankedQueue) {
                if (client.getClientId().equals(clientId)) {
                    client.invalidateSocket();
                    return;
                }
            }
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
    private LocalDateTime entryTime;

    public ClientInfo(String clientId, Socket socket, Condition matchMakingCondition, Condition gameCondition, LocalDateTime entryTime) {
        this.clientId = clientId;
        this.socket = socket;
        this.matchMakingCondition = matchMakingCondition;
        this.gameCondition = gameCondition;
        this.entryTime = entryTime;
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

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public void invalidateSocket() {
        socket = null;
    }
}