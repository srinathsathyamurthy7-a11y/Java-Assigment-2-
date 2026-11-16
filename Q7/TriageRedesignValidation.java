package Q7;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.PriorityQueue;
import java.util.Random;

public class TriageRedesignValidation {

    static class Patient {
        final String name;
        final int severity; // 1 (critical) to 5 (minor)
        final long arrivalSeq;
        final long arrivalTimeMs;

        Patient(String name, int severity, long arrivalSeq, long arrivalTimeMs) {
            this.name = name;
            this.severity = severity;
            this.arrivalSeq = arrivalSeq;
            this.arrivalTimeMs = arrivalTimeMs;
        }
    }

    // =========================================================================
    // ORIGINAL DESIGN (From Q6): Single Binary Min-Heap PriorityQueue
    // Bottleneck 1: O(log n) sift-up/sift-down + Comparator calls on every op
    // Bottleneck 2: Severity-5 patients starve indefinitely under heavy load
    // =========================================================================
    static class OriginalHeapTriage {
        private long counter = 0;
        private final PriorityQueue<Patient> queue = new PriorityQueue<>(
                Comparator.comparingInt((Patient p) -> p.severity)
                        .thenComparingLong(p -> p.arrivalSeq));

        void admit(String name, int severity, long nowMs) {
            queue.add(new Patient(name, severity, counter++, nowMs));
        }

        Patient next(long nowMs) {
            return queue.poll();
        }

        int size() {
            return queue.size();
        }
    }

    // =========================================================================
    // REDESIGNED SOLUTION: 5 Bounded FIFO ArrayDeque Buckets + Aging Rule
    // Exploits bounded integer domain (severity 1..5) for O(1) admit & poll
    // Prevents starvation by promoting patients waiting > maxWaitMs
    // =========================================================================
    static class BucketedFairTriage {
        @SuppressWarnings("unchecked")
        private final Deque<Patient>[] buckets = new ArrayDeque[6]; // indices 1..5
        private long counter = 0;
        private int totalSize = 0;
        private final long starvationThresholdMs;

        BucketedFairTriage(long starvationThresholdMs) {
            this.starvationThresholdMs = starvationThresholdMs;
            for (int i = 1; i <= 5; i++) {
                buckets[i] = new ArrayDeque<>();
            }
        }

        // O(1) amortized insertion directly into severity bucket
        void admit(String name, int severity, long nowMs) {
            buckets[severity].addLast(new Patient(name, severity, counter++, nowMs));
            totalSize++;
        }

        // O(1) worst-case removal: checks aging promotion first, then highest severity
        Patient next(long nowMs) {
            if (totalSize == 0)
                return null;

            // Fairness Check (Aging): If head of a lower-priority queue waited too long,
            // escalate them so minor cases never starve indefinitely.
            for (int sev = 5; sev >= 2; sev--) {
                Patient oldestInBucket = buckets[sev].peekFirst();
                if (oldestInBucket != null && (nowMs - oldestInBucket.arrivalTimeMs) >= starvationThresholdMs) {
                    totalSize--;
                    return buckets[sev].pollFirst();
                }
            }

            // Normal Priority Dispatch: Serve most critical non-empty bucket in O(1)
            for (int sev = 1; sev <= 5; sev++) {
                if (!buckets[sev].isEmpty()) {
                    totalSize--;
                    return buckets[sev].pollFirst();
                }
            }
            return null;
        }

        int size() {
            return totalSize;
        }
    }

    private static long getUsedMemoryKB() {
        System.gc();
        try {
            Thread.sleep(50);
        } catch (InterruptedException ignored) {
        }
        Runtime rt = Runtime.getRuntime();
        return (rt.totalMemory() - rt.freeMemory()) / 1024;
    }

    // Validation 1: Throughput & Scaling Comparison (Original vs Redesigned)
    private static void comparePerformance(int backlogSize, int operations) {
        Random rand = new Random(42);
        int[] severities = new int[backlogSize + operations];
        for (int i = 0; i < severities.length; i++) {
            severities[i] = 1 + rand.nextInt(5);
        }

        // Measure Original Heap Triage
        long memBeforeOrig = getUsedMemoryKB();
        OriginalHeapTriage orig = new OriginalHeapTriage();
        for (int i = 0; i < backlogSize; i++) {
            orig.admit("P" + i, severities[i], i * 10L);
        }
        long start = System.nanoTime();
        for (int i = 0; i < operations; i++) {
            orig.admit("N" + i, severities[backlogSize + i], (backlogSize + i) * 10L);
            orig.next((backlogSize + i) * 10L);
        }
        double origMs = (System.nanoTime() - start) / 1_000_000.0;
        long memAfterOrig = getUsedMemoryKB();

        // Measure Redesigned Bucketed Fair Triage
        long memBeforeRed = getUsedMemoryKB();
        BucketedFairTriage red = new BucketedFairTriage(30_000L); // 30s simulated threshold
        for (int i = 0; i < backlogSize; i++) {
            red.admit("P" + i, severities[i], i * 10L);
        }
        start = System.nanoTime();
        for (int i = 0; i < operations; i++) {
            red.admit("N" + i, severities[backlogSize + i], (backlogSize + i) * 10L);
            red.next((backlogSize + i) * 10L);
        }
        double redMs = (System.nanoTime() - start) / 1_000_000.0;
        long memAfterRed = getUsedMemoryKB();

        double speedup = origMs / Math.max(0.001, redMs);
        System.out.printf(
                "Backlog: %,7d | Ops: %,7d | Original Heap: %7.3f ms (%d KB) | Redesigned Buckets: %7.3f ms (%d KB) | Speedup: %.2fx%n",
                backlogSize, operations,
                origMs, Math.max(0, memAfterOrig - memBeforeOrig),
                redMs, Math.max(0, memAfterRed - memBeforeRed),
                speedup);
    }

    // Validation 2: Starvation Fairness Proof under Continuous Critical Arrivals
    private static void verifyStarvationFix() {
        OriginalHeapTriage orig = new OriginalHeapTriage();
        BucketedFairTriage red = new BucketedFairTriage(100L); // 100 ms aging limit

        // 1 minor patient arrives at t = 0
        orig.admit("Minor_Patient_S5", 5, 0L);
        red.admit("Minor_Patient_S5", 5, 0L);

        // 20 critical (S1) patients arrive continuously from t = 10 to t = 200
        for (int i = 1; i <= 20; i++) {
            long t = i * 10L;
            orig.admit("Critical_S1_" + i, 1, t);
            red.admit("Critical_S1_" + i, 1, t);
        }

        int origWaitTurns = 0;
        while (orig.size() > 0) {
            origWaitTurns++;
            if (orig.next(250L).name.equals("Minor_Patient_S5"))
                break;
        }

        int redWaitTurns = 0;
        while (red.size() > 0) {
            redWaitTurns++;
            if (red.next(250L).name.equals("Minor_Patient_S5"))
                break;
        }

        System.out.println("\n=== STARVATION VALIDATION (1 Minor S5 patient vs 20 Critical S1 arrivals) ===");
        System.out.println(
                "Original PriorityQueue: Minor patient treated at turn #" + origWaitTurns + " (Last - Starved!)");
        System.out.println("Redesigned Fair Queue:  Minor patient treated at turn #" + redWaitTurns
                + " (Bounded wait via Aging!)");
    }

    public static void main(String[] args) {
        // Warmup
        comparePerformance(1_000, 10_000);

        System.out.println("\n=== PERFORMANCE VALIDATION: ORIGINAL vs REDESIGNED ===");
        comparePerformance(1_000, 100_000);
        comparePerformance(50_000, 100_000);
        comparePerformance(500_000, 100_000);

        verifyStarvationFix();
    }
}