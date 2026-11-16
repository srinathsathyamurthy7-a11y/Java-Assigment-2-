import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class TicketBenchmark {

    // Generates sample railway e-ticket fields (PNR, Passenger, Coach, Berth, Fare, etc.)
    private static String[] generateFields(int fieldCount) {
        String[] fields = new String[fieldCount];
        fields[0] = "PNR:4829104721";
        if (fieldCount > 1) fields[1] = "Train:12622-TamilNaduExp";
        if (fieldCount > 2) fields[2] = "Passenger:ArunKumar";
        if (fieldCount > 3) fields[3] = "Coach:B2";
        if (fieldCount > 4) fields[4] = "Berth:42-LB";
        for (int i = 5; i < fieldCount; i++) {
            fields[i] = "Field_" + i + ":Val_" + (100 + i);
        }
        return fields;
    }

    // 1. Repeated String concatenation using '+'
    private static String buildWithString(String[] fields) {
        String ticket = "";
        for (String field : fields) {
            ticket = ticket + field + " | ";
        }
        return ticket;
    }

    // 2. StringBuilder (unsynchronized mutable buffer)
    private static String buildWithStringBuilder(String[] fields) {
        StringBuilder sb = new StringBuilder();
        for (String field : fields) {
            sb.append(field).append(" | ");
        }
        return sb.toString();
    }

    // 3. StringBuffer (synchronized mutable buffer)
    private static String buildWithStringBuffer(String[] fields) {
        StringBuffer sbf = new StringBuffer();
        for (String field : fields) {
            sbf.append(field).append(" | ");
        }
        return sbf.toString();
    }

    // Alternative Approaches for Comparison
    private static String buildWithStringJoin(String[] fields) {
        return String.join(" | ", fields);
    }

    private static String buildWithStringJoiner(String[] fields) {
        StringJoiner joiner = new StringJoiner(" | ");
        for (String field : fields) {
            joiner.add(field);
        }
        return joiner.toString();
    }

    private static long getUsedMemoryKB() {
        System.gc();
        try { Thread.sleep(50); } catch (InterruptedException ignored) {}
        Runtime rt = Runtime.getRuntime();
        return (rt.totalMemory() - rt.freeMemory()) / 1024;
    }

    private static void runWorkload(String workloadName, int ticketCount, int fieldCount, int runs) {
        String[] fields = generateFields(fieldCount);

        // JVM JIT Warm-up
        for (int w = 0; w < 500; w++) {
            buildWithString(fields);
            buildWithStringBuilder(fields);
            buildWithStringBuffer(fields);
        }

        // Benchmark String (+)
        long memBeforeStr = getUsedMemoryKB();
        long timeStr = 0;
        for (int r = 0; r < runs; r++) {
            List<String> tickets = new ArrayList<>(ticketCount);
            long start = System.nanoTime();
            for (int t = 0; t < ticketCount; t++) {
                tickets.add(buildWithString(fields));
            }
            timeStr += (System.nanoTime() - start);
        }
        long memAfterStr = getUsedMemoryKB();
        double avgMsStr = (timeStr / (double) runs) / 1_000_000.0;
        long estStringsStr = (long) ticketCount * fieldCount * 2; // 2 intermediate Strings per iteration

        // Benchmark StringBuilder
        long memBeforeSb = getUsedMemoryKB();
        long timeSb = 0;
        for (int r = 0; r < runs; r++) {
            List<String> tickets = new ArrayList<>(ticketCount);
            long start = System.nanoTime();
            for (int t = 0; t < ticketCount; t++) {
                tickets.add(buildWithStringBuilder(fields));
            }
            timeSb += (System.nanoTime() - start);
        }
        long memAfterSb = getUsedMemoryKB();
        double avgMsSb = (timeSb / (double) runs) / 1_000_000.0;
        long estStringsSb = ticketCount; // 1 final String per ticket

        // Benchmark StringBuffer
        long memBeforeSbf = getUsedMemoryKB();
        long timeSbf = 0;
        for (int r = 0; r < runs; r++) {
            List<String> tickets = new ArrayList<>(ticketCount);
            long start = System.nanoTime();
            for (int t = 0; t < ticketCount; t++) {
                tickets.add(buildWithStringBuffer(fields));
            }
            timeSbf += (System.nanoTime() - start);
        }
        long memAfterSbf = getUsedMemoryKB();
        double avgMsSbf = (timeSbf / (double) runs) / 1_000_000.0;
        long estStringsSbf = ticketCount; // 1 final String per ticket

        System.out.println("===================================================================================");
        System.out.printf("WORKLOAD: %s | Tickets: %d | Fields/Ticket: %d | Runs: %d%n",
                workloadName, ticketCount, fieldCount, runs);
        System.out.println("===================================================================================");
        System.out.printf("%-15s | %-15s | %-24s | %-18s%n",
                "Approach", "Avg Time (ms)", "Est. Intermediate Strings", "Memory Delta (KB)");
        System.out.println("-----------------------------------------------------------------------------------");
        System.out.printf("%-15s | %-15.3f | %-24d | %-18d%n",
                "String (+)", avgMsStr, estStringsStr, Math.max(0, memAfterStr - memBeforeStr));
        System.out.printf("%-15s | %-15.3f | %-24d | %-18d%n",
                "StringBuilder", avgMsSb, estStringsSb, Math.max(0, memAfterSb - memBeforeSb));
        System.out.printf("%-15s | %-15.3f | %-24d | %-18d%n",
                "StringBuffer", avgMsSbf, estStringsSbf, Math.max(0, memAfterSbf - memBeforeSbf));
        System.out.println();
    }

    public static void main(String[] args) {
        // 3 Deliberately Contrasting Workloads
        runWorkload("1. Short Ticket (Light)", 100, 8, 10);
        runWorkload("2. Standard Heavy Load", 10_000, 50, 10);
        runWorkload("3. Peak Tatkal Rush (Extreme)", 25_000, 100, 5);

        // Quick verification of Alternative Concepts
        String[] sample = generateFields(5);
        System.out.println("Sample String.join() output:   " + buildWithStringJoin(sample));
        System.out.println("Sample StringJoiner output:    " + buildWithStringJoiner(sample));
        System.out.println("Sample String.format() output: " +
                String.format("%s | %s | %s", sample[0], sample[1], sample[2]));
    }
}