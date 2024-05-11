import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Score {
    public int expectedScore(int averageScore, int currentScore){
        return (int) Math.round(1 / (1 + Math.pow(10, (averageScore - currentScore) / 400)));

    }

    public int calculateScore(int currentScore, int expectedScore, int actualScore, int numPlayers){
        int k = getK(currentScore);
        return Math.min(3000, currentScore + (k/numPlayers) * (actualScore - expectedScore));
    }
    
    public int getK(int currentScore){
        if (currentScore < 2100)
            return 32;
        else if (currentScore < 2400)
            return 24;
        else
            return 16;
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

    public void setScore(int score) {
        this.score = score;
    }

    public LocalDateTime getEntryTime() {
        return this.entryTime;
    }
}