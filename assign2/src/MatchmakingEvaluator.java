import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


/**
 * MatchmakingEvaluator
 */
public class MatchmakingEvaluator {

    public static int MAX_LEVEL = 100;
    public static int DIVISIONS = 5;
    public static int MAX_WAITING_TIME = 2;
    public static int MIN_NUM_PLAYERS = 2;
    public static int MAX_NUM_PLAYERS = 6;

    /**
     * This method returns a list with the clusters of a queue of clients given their rankings.
     */
    public static List<Cluster> getClusters(List<ClientInfo> clients) {
        
        List<Cluster> clusters = new ArrayList<>(List.of());
        
        for (int i = 0; i < DIVISIONS; i++) {
            clusters.add(new Cluster());
        }

        for (int i = 0; i < clients.size(); i++) {
            ClientInfo client = clients.get(i);

            int score = client.getScore();
            int index = Math.min(score / (MAX_LEVEL / DIVISIONS), DIVISIONS - 1);

            Cluster c = clusters.get(index);
            c.addClient(client);
        }

        return clusters;
    }

    public static Pair<List<List<ClientInfo>>, List<ClientInfo>> getResult(List<Cluster> clusters) {
        
        List<List<ClientInfo>> newMatches = new ArrayList<>();
        List<ClientInfo> newClients = new ArrayList<>();
        List<ClientInfo> looseClients = new ArrayList<>();
        int freeGame = -1;

        for (int i = 0; i < clusters.size(); i++) {
            Cluster cluster = clusters.get(i);
            int clusterSize = cluster.size();

            // If there are not enough players to start a game
            if (clusterSize < MIN_NUM_PLAYERS) {
                for (int j = 0; j < clusterSize; j++) {

                    ClientInfo client = cluster.getClient(j);

                    if (client.isPriority(MAX_WAITING_TIME)){
                        
                        if (i < clusters.size() - 1){
                            Cluster nextCluster = clusters.get(i + 1);
                            nextCluster.addClient(client);
                        }
                        else looseClients.add(client);
                    }
                    else
                        newClients.add(cluster.getClient(j));
                }
            }
            // If there are too many players to start a game
            else if (clusterSize > MAX_NUM_PLAYERS) {

                while (clusterSize > 0) {
                    if (clusterSize < MIN_NUM_PLAYERS) {
                        for (int j = 0; j < clusterSize; j++) {
                            if (cluster.getClient(j).isPriority(MAX_WAITING_TIME))
                                looseClients.add(cluster.getClient(j));
                            else
                                newClients.add(cluster.getClient(j));
                        }
                        break;
                    }
                    else if (clusterSize <= MAX_NUM_PLAYERS) {
                        if (clusterSize != MAX_NUM_PLAYERS) freeGame = newMatches.size();
                        newMatches.add(cluster.getClients());
                        break;
                    }
                    else if (clusterSize - MAX_NUM_PLAYERS < MIN_NUM_PLAYERS) {
                        List<ClientInfo> players = cluster.remove(MIN_NUM_PLAYERS);
                        clusterSize = cluster.size();
                        if (clusterSize != MAX_NUM_PLAYERS) freeGame = newMatches.size();
                        newMatches.add(players);
                    }
                    else {
                        if (clusterSize != MAX_NUM_PLAYERS) freeGame = newMatches.size();
                        newMatches.add(cluster.getClients());
                        cluster.remove(MAX_NUM_PLAYERS);
                        clusterSize = cluster.size();
                    }
                }
            }
            // If there are enough players to start a game
            else {
                if (clusterSize != MAX_NUM_PLAYERS) freeGame = newMatches.size();
                newMatches.add(cluster.getClients());
            }
        }

        int looseClientsSize = looseClients.size();

        if (looseClientsSize > 0){
            if (looseClientsSize >= MIN_NUM_PLAYERS){
                // If there are enough players to start a game
                if (looseClientsSize <= MAX_NUM_PLAYERS){
                    newMatches.add(looseClients);
                }
                // If there are too many players to start a game
                else {
                    while (looseClientsSize > 0) {
                        if (looseClientsSize <= MAX_NUM_PLAYERS) {
                            newMatches.add(looseClients);
                            break;
                        }
                        // TODO Test this
                        else if (looseClientsSize - MAX_NUM_PLAYERS < MIN_NUM_PLAYERS) {
                            looseClients = looseClients.subList(0, looseClientsSize - MIN_NUM_PLAYERS);
                            looseClientsSize = looseClients.size();
                            newMatches.add(looseClients);
                        }
                        // TODO Test this
                        else {
                            looseClients = looseClients.subList(0, looseClientsSize - MAX_NUM_PLAYERS);
                            looseClientsSize = looseClients.size();
                            newMatches.add(looseClients);
                        }
                    }
                }
            }
            // If there are not enough players to start a game
            else{
                if (!newClients.isEmpty()){
                    for (int i = 0; i < MIN_NUM_PLAYERS - looseClientsSize; i++)
                        looseClients.add(newClients.remove(newClients.size() - 1));

                    newMatches.add(looseClients);
                }
                else if (!newMatches.isEmpty()){
                    if (freeGame != -1){
                        List<ClientInfo> match = newMatches.get(freeGame);
                        match.addAll(looseClients);
                    }
                    else {
                        for (int i = 0; i < newMatches.size(); i++){
                            List<ClientInfo> match = newMatches.get(i);
                            for (int j = 0; j < match.size(); j++){
                                ClientInfo client = match.get(j);
                                if (!client.isPriority(MAX_WAITING_TIME)){
                                    match.remove(client);
                                    match.addAll(looseClients);
                                    newClients.add(client);
                                    return new Pair<>(newMatches, newClients);
                                }
                            }
                        }
                    }
                }
                else {
                    newClients = looseClients;
                }
            }
        }

        return new Pair<>(newMatches, newClients);
    }

    public static void printClusters(List<Cluster> clusters) {
        System.out.println("==== CLUSTERS ====");
        for (int i = 0; i < clusters.size(); i++) {
            Cluster cluster = clusters.get(i);
            
            if (cluster.size() > 0) {
                int lmin = MAX_LEVEL / DIVISIONS * i;
                int lmax = MAX_LEVEL / DIVISIONS * (i + 1);

                if (lmax == MAX_LEVEL) System.out.print("[" + lmin + ", " + lmax + "[" + " - [");
                else System.out.print("[" + lmin + ", " + lmax + "[" + " - [");

                System.out.print(cluster.getClient(0).getClientId());

                for (int j = 1; j < cluster.size(); j++) {
                    System.out.print(", " + cluster.getClient(j).getClientId());
                }

                System.out.println("]");
            }
        }
        System.out.println("==================\n");
    }

    public static void printLoose(List<ClientInfo> looseClients) {
        System.out.println("==== LOOSE CLIENTS ====");
        if (looseClients.size() == 0) {
            System.out.println("[]");
            return;
        }
        System.out.print("[" + looseClients.get(0).getClientId());

        for (int i = 1; i < looseClients.size(); i++) {
            System.out.print(", " + looseClients.get(i).getClientId());
        }

        System.out.println("]");

        System.out.println("=======================\n");
    }

    public static void printNewClients(List<ClientInfo> looseClients) {
        System.out.println("==== NEW CLIENTS ====");
        if (looseClients.size() == 0) {
            System.out.println("[]");
            return;
        }
        System.out.print("[" + looseClients.get(0).getClientId());

        for (int i = 1; i < looseClients.size(); i++) {
            System.out.print(", " + looseClients.get(i).getClientId());
        }

        System.out.println("]");

        System.out.println("=====================\n");
    }

    public static void printMatches(List<List<ClientInfo>> matches) {
        System.out.println("==== MATCHES ====");
        for (int i = 0; i < matches.size(); i++) {
            List<ClientInfo> match = matches.get(i);
            
            if (match.size() > 0) {
                System.out.print("[Game " + (i + 1) + "]" + " - [");

                System.out.print(match.get(0).getClientId());

                for (int j = 1; j < match.size(); j++) {
                    System.out.print(", " + match.get(j).getClientId());
                }

                System.out.println("]");
            }
        }
        System.out.println("==================\n");
    }

    public static Pair<List<List<ClientInfo>>, List<ClientInfo>> evaluate(List<ClientInfo> clients) {

        int numClients = clients.size();

        if (numClients < MIN_NUM_PLAYERS) {
            return new Pair<>(new ArrayList<>(), clients);
        }
    
        List<Cluster> clusters = getClusters(clients);

        printClusters(clusters);

        Pair<List<List<ClientInfo>>, List<ClientInfo>> result = getResult(clusters);
        List<List<ClientInfo>> newMatches = result.getKey();
        List<ClientInfo> newClients = result.getValue();
        
        printMatches(newMatches);
        printNewClients(newClients);

        return result;
    }

    public static void main(String[] args) {
        // creating a list with some dummy clients info
        LocalDateTime curr = LocalDateTime.now();
        List<ClientInfo> clients = new ArrayList<>(List.of(
            new ClientInfo("pedro", 79, curr.minusMinutes(3)),
            new ClientInfo("maria", 35, curr.minusMinutes(2)),
            new ClientInfo("joao", 10, LocalDateTime.now()),
            new ClientInfo("ana", 50, LocalDateTime.now()),
            new ClientInfo("carlos", 25, curr.minusMinutes(1)),
            new ClientInfo("jose", 18, LocalDateTime.now()),
            new ClientInfo("luis", 20, curr.minusMinutes(1)),
            new ClientInfo("marta", 3, curr.minusMinutes(1)),
            new ClientInfo("matilde", 3, curr.minusMinutes(1)),
            new ClientInfo("sofia", 3, curr.minusMinutes(1)),
            new ClientInfo("sónia", 3, curr.minusMinutes(1)),
            new ClientInfo("ricardo", 3, curr.minusMinutes(1)),
            new ClientInfo("julia", 100, curr.minusMinutes(1))
        ));
        evaluate(clients);
    }
}

/**
 * Cluster Class
 */
class Cluster {
    private List<ClientInfo> clients;

    public Cluster() {
        this.clients = new ArrayList<>();
    }
    
    public Cluster(List<ClientInfo> clients) {
        this.clients = clients;
    }

    public List<ClientInfo> getClients() {
        return this.clients;
    }

    public void addClient(ClientInfo client) {
        this.clients.add(client);
    }

    public ClientInfo getClient(int index) {
        return this.clients.get(index);
    }

    public int size() {
        return this.clients.size();
    }

    public List<ClientInfo> remove(int num) {
        List<ClientInfo> removedClients = new ArrayList<>();

        int size = this.size();

        for (int i = 0; i < num; i++) {
            removedClients.add(this.clients.remove(size - 1));
            size--;
        }

        return removedClients;
    }
}

/**
 * ClientInfo Class
 */
class ClientInfo {
    private String clientId;
    private int score;
    private LocalDateTime entryTime;

    public ClientInfo(String clientId, int score, LocalDateTime entryTime) {
        this.clientId = clientId;
        this.score = score;
        this.entryTime = entryTime;
    }

    public String getClientId() {
        return this.clientId;
    }

    public int getScore() {
        return this.score;
    }

    public LocalDateTime getEntryTime() {
        return this.entryTime;
    }

    public boolean isPriority(int maxWaitingTime) {
        LocalDateTime curr_time = LocalDateTime.now();
        long diff = java.time.Duration.between(entryTime, curr_time).toMinutes();
        if (diff >= maxWaitingTime) return true;
        return false;
    }
}