package Q5;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

class Player {
    final String name;
    final int score;

    Player(String name, int score) {
        this.name = name;
        this.score = score;
    }

    @Override
    public String toString() {
        return name + "[" + score + "]";
    }
}

public class RankingDesignTest {

    // Helper using wildcard List<? extends T> to add a batch of subtypes
    static <T> void addAllItems(Leaderboard<T> board, List<? extends T> batch) {
        for (T item : batch) {
            board.add(item);
        }
    }

    public static void main(String[] args) {
        Comparator<Team> safeComparator = Comparator
                .comparingInt((Team t) -> t.points).reversed()
                .thenComparing(Comparator.comparingInt((Team t) -> t.goalDiff).reversed())
                .thenComparing(t -> t.name);

        // Dataset 1: Multi-level ties
        Leaderboard<Team> tieBoard = new Leaderboard<>(safeComparator);
        tieBoard.add(new Team("Wolves", 12, 5));
        tieBoard.add(new Team("Titans", 12, 8));
        tieBoard.add(new Team("Alphas", 12, 5));
        tieBoard.add(new Team("Hawks", 12, 5));
        tieBoard.add(new Team("Sharks", 15, 2));
        System.out.println("Test 1 (Multi-level Ties): " + tieBoard.ranking());

        // Dataset 2: Extreme integer values that break faulty subtraction (b.points -
        // a.points)
        Team extremeHigh = new Team("OverflowHigh", Integer.MAX_VALUE, 10);
        Team extremeLow = new Team("OverflowLow", -10, 5);

        Comparator<Team> faultySubtraction = (a, b) -> b.points - a.points; // overflows!
        System.out.println(
                "Faulty subtraction comparator (should be < 0): " + faultySubtraction.compare(extremeHigh, extremeLow));
        System.out.println(
                "Safe Comparator.comparingInt (correctly < 0):  " + safeComparator.compare(extremeHigh, extremeLow));

        // Reusability test: Leaderboard<Player>
        Leaderboard<Player> playerBoard = new Leaderboard<>(
                Comparator.comparingInt((Player p) -> p.score).reversed().thenComparing(p -> p.name));
        List<Player> players = new ArrayList<>();
        players.add(new Player("Kiran", 95));
        players.add(new Player("Ananya", 98));
        players.add(new Player("Deepak", 95));
        addAllItems(playerBoard, players);
        System.out.println("Reused for Players: " + playerBoard.ranking());
    }
}