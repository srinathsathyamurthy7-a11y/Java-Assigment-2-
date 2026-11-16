import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class PlaylistBenchmark {

    static class Song {
        final int id;
        final String title;

        Song(int id) {
            this.id = id;
            this.title = "Track_" + id;
        }
    }

    private static List<Song> buildArrayList(int size) {
        List<Song> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(new Song(i));
        }
        return list;
    }

    private static List<Song> buildLinkedList(int size) {
        List<Song> list = new LinkedList<>();
        for (int i = 0; i < size; i++) {
            list.add(new Song(i));
        }
        return list;
    }

    private static void runSizeBenchmark(int size) {
        int ops = 2_000; // Number of operations measured per test
        Song sample = new Song(999_999);

        // 1. Add at the end (building playlist of 'size' songs)
        long start = System.nanoTime();
        List<Song> alEnd = new ArrayList<>();
        for (int i = 0; i < size; i++)
            alEnd.add(new Song(i));
        double alAddEndMs = (System.nanoTime() - start) / 1_000_000.0;

        start = System.nanoTime();
        List<Song> llEnd = new LinkedList<>();
        for (int i = 0; i < size; i++)
            llEnd.add(new Song(i));
        double llAddEndMs = (System.nanoTime() - start) / 1_000_000.0;

        // 2. Insert & Remove at the FRONT (index 0)
        List<Song> alFront = buildArrayList(size);
        start = System.nanoTime();
        for (int i = 0; i < ops; i++) {
            alFront.add(0, sample);
            alFront.remove(0);
        }
        double alFrontMs = (System.nanoTime() - start) / 1_000_000.0;

        List<Song> llFront = buildLinkedList(size);
        start = System.nanoTime();
        for (int i = 0; i < ops; i++) {
            llFront.add(0, sample);
            llFront.remove(0);
        }
        double llFrontMs = (System.nanoTime() - start) / 1_000_000.0;

        // 3. Insert & Remove in the MIDDLE (index size / 2)
        List<Song> alMid = buildArrayList(size);
        int mid = size / 2;
        start = System.nanoTime();
        for (int i = 0; i < ops; i++) {
            alMid.add(mid, sample);
            alMid.remove(mid);
        }
        double alMidMs = (System.nanoTime() - start) / 1_000_000.0;

        List<Song> llMid = buildLinkedList(size);
        start = System.nanoTime();
        for (int i = 0; i < ops; i++) {
            llMid.add(mid, sample);
            llMid.remove(mid);
        }
        double llMidMs = (System.nanoTime() - start) / 1_000_000.0;

        // 4. Random Access: get(k)
        List<Song> alGet = buildArrayList(size);
        long sink = 0;
        start = System.nanoTime();
        for (int i = 0; i < ops; i++) {
            sink += alGet.get((i * 31) % size).id;
        }
        double alGetMs = (System.nanoTime() - start) / 1_000_000.0;

        List<Song> llGet = buildLinkedList(size);
        start = System.nanoTime();
        for (int i = 0; i < ops; i++) {
            sink += llGet.get((i * 31) % size).id;
        }
        double llGetMs = (System.nanoTime() - start) / 1_000_000.0;

        // 5. Iterate through the whole list (enhanced for-loop)
        start = System.nanoTime();
        for (Song s : alGet)
            sink += s.id;
        double alIterMs = (System.nanoTime() - start) / 1_000_000.0;

        start = System.nanoTime();
        for (Song s : llGet)
            sink += s.id;
        double llIterMs = (System.nanoTime() - start) / 1_000_000.0;

        // 6. Changed Condition ("Up Next" Queue: addFirst, addLast, pollFirst)
        Deque<Song> deque = new ArrayDeque<>(size);
        for (int i = 0; i < size; i++)
            deque.addLast(new Song(i));
        start = System.nanoTime();
        for (int i = 0; i < ops; i++) {
            deque.addFirst(sample);
            deque.pollFirst();
            deque.addLast(sample);
            deque.pollFirst();
        }
        double dequeUpNextMs = (System.nanoTime() - start) / 1_000_000.0;

        if (sink == -1)
            System.out.println(sink); // prevents dead-code elimination

        System.out.println("=========================================================================");
        System.out.printf("PLAYLIST SIZE: %,d songs (%,d ops per mutation/access test)%n", size, ops);
        System.out.println("=========================================================================");
        System.out.printf("%-30s | %-15s | %-15s%n", "Operation", "ArrayList (ms)", "LinkedList (ms)");
        System.out.println("-------------------------------------------------------------------------");
        System.out.printf("%-30s | %-15.3f | %-15.3f%n", "Add at End (build all)", alAddEndMs, llAddEndMs);
        System.out.printf("%-30s | %-15.3f | %-15.3f%n", "Insert + Remove at Front", alFrontMs, llFrontMs);
        System.out.printf("%-30s | %-15.3f | %-15.3f%n", "Insert + Remove at Middle", alMidMs, llMidMs);
        System.out.printf("%-30s | %-15.3f | %-15.3f%n", "Random Access get(k)", alGetMs, llGetMs);
        System.out.printf("%-30s | %-15.3f | %-15.3f%n", "Full Iteration (for-each)", alIterMs, llIterMs);
        System.out.printf("%-30s | ArrayDeque Up-Next (both ends): %.3f ms%n", "Changed Condition", dequeUpNextMs);
        System.out.println();
    }

    public static void main(String[] args) {
        // Warmup
        runSizeBenchmark(500);
        System.out.println("--- BENCHMARK RESULTS ---\n");

        // Three meaningfully different playlist sizes
        runSizeBenchmark(1_000);
        runSizeBenchmark(25_000);
        runSizeBenchmark(100_000);
    }
}