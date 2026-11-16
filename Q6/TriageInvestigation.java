package Q6;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Random;

public class TriageInvestigation {

    static class PatientRecord {
        final String name;
        final int severity;
        final long arrival;

        PatientRecord(String name, int severity, long arrival) {
            this.name = name;
            this.severity = severity;
            this.arrival = arrival;
        }

        @Override
        public String toString() {
            return name + "(S" + severity + ",#" + arrival + ")";
        }
    }

    private static PriorityQueue<PatientRecord> createQueue() {
        return new PriorityQueue<>(
                Comparator.comparingInt((PatientRecord p) -> p.severity)
                        .thenComparingLong(p -> p.arrival));
    }

    private static void verify8Patients() {
        PriorityQueue<PatientRecord> pq = createQueue();
        long seq = 0;

        // 8 patients arriving in mixed severity order
        pq.add(new PatientRecord("P1_Arun", 4, seq++));
        pq.add(new PatientRecord("P2_Bala", 2, seq++));
        pq.add(new PatientRecord("P3_Chitra", 1, seq++));
        pq.add(new PatientRecord("P4_Deepa", 3, seq++));
        pq.add(new PatientRecord("P5_Ezhil", 1, seq++));
        pq.add(new PatientRecord("P6_Farhan", 5, seq++));
        pq.add(new PatientRecord("P7_Gita", 2, seq++));
        pq.add(new PatientRecord("P8_Hari", 3, seq++));

        System.out.println("=== 1. DIRECT PRINT (Internal Min-Heap Array Order - NOT Sorted) ===");
        System.out.println(pq);

        System.out.println("\n=== 2. TREATMENT ORDER via poll() (Matches Hand-Worked Order) ===");
        StringBuilder sb = new StringBuilder();
        while (!pq.isEmpty()) {
            sb.append(pq.poll()).append(" -> ");
        }
        System.out.println(sb.toString() + "DONE\n");
    }

    private static void benchmarkGrowth(int queueSize) {
        PriorityQueue<PatientRecord> pq = createQueue();
        Random rand = new Random(42);

        // Pre-fill queue to 'queueSize'
        for (int i = 0; i < queueSize; i++) {
            pq.add(new PatientRecord("Pat_" + i, 1 + rand.nextInt(5), i));
        }

        int ops = 50_000;
        long start = System.nanoTime();
        for (int i = 0; i < ops; i++) {
            pq.add(new PatientRecord("New_" + i, 1 + rand.nextInt(5), queueSize + i));
            pq.poll();
        }
        double totalMs = (System.nanoTime() - start) / 1_000_000.0;
        double nsPerPair = (totalMs * 1_000_000.0) / ops;

        System.out.printf("Queue Size: %,9d | 50,000 (add + poll) pairs: %7.3f ms | Avg per pair: %6.1f ns%n",
                queueSize, totalMs, nsPerPair);
    }

    public static void main(String[] args) {
        verify8Patients();

        System.out.println("=== 3. GROWTH INVESTIGATION (O(log n) Scaling) ===");
        // Warmup
        benchmarkGrowth(500);
        System.out.println("-------------------------------------------------------------------------");

        // Three meaningfully different queue sizes
        benchmarkGrowth(1_000);
        benchmarkGrowth(50_000);
        benchmarkGrowth(500_000);
    }
}