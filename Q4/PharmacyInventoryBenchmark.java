package Q4;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public class PharmacyInventoryBenchmark {

    static class Medicine {
        final String code;
        final String name;
        int stock;

        Medicine(String code, String name, int stock) {
            this.code = code;
            this.name = name;
            this.stock = stock;
        }
    }

    private static List<Medicine> generateMedicines(int count) {
        List<Medicine> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            // Unsorted codes to test ordering differences clearly
            String code = String.format("MED-%06d", (i * 7919) % 1_000_000);
            list.add(new Medicine(code, "Drug_" + i, 50 + (i % 100)));
        }
        return list;
    }

    private static void verifyOrderRequirement() {
        List<Medicine> sample = Arrays.asList(
                new Medicine("MED-300", "Paracetamol", 120),
                new Medicine("MED-100", "Amoxicillin", 80),
                new Medicine("MED-200", "Cetirizine", 200));

        Map<String, Medicine> hashMap = new HashMap<>();
        Map<String, Medicine> treeMap = new TreeMap<>();
        Map<String, Medicine> linkedMap = new LinkedHashMap<>();

        for (Medicine m : sample) {
            hashMap.put(m.code, m);
            treeMap.put(m.code, m);
            linkedMap.put(m.code, m);
        }

        System.out.println("=== ORDER VERIFICATION (Inserted: MED-300, MED-100, MED-200) ===");
        System.out.println("HashMap iteration order:       " + hashMap.keySet() + " -> Matches neither requirement");
        System.out.println("TreeMap iteration order:       " + treeMap.keySet() + " -> Matches CODE ORDER naturally");
        System.out.println(
                "LinkedHashMap iteration order: " + linkedMap.keySet() + " -> Matches INSERTION ORDER naturally\n");
    }

    private static void benchmarkSize(int size) {
        List<Medicine> data = generateMedicines(size);
        String[] lookupKeys = new String[100_000];
        Random rand = new Random(42);
        for (int i = 0; i < lookupKeys.length; i++) {
            lookupKeys[i] = data.get(rand.nextInt(size)).code;
        }

        Map<String, Medicine> hashMap = new HashMap<>(size * 2);
        Map<String, Medicine> treeMap = new TreeMap<>();
        Map<String, Medicine> linkedMap = new LinkedHashMap<>(size * 2);

        for (Medicine m : data) {
            hashMap.put(m.code, m);
            treeMap.put(m.code, m);
            linkedMap.put(m.code, m);
        }

        long sink = 0;

        // 1. 100,000 Lookups
        long start = System.nanoTime();
        for (String key : lookupKeys)
            sink += hashMap.get(key).stock;
        double hmLookupMs = (System.nanoTime() - start) / 1_000_000.0;

        start = System.nanoTime();
        for (String key : lookupKeys)
            sink += treeMap.get(key).stock;
        double tmLookupMs = (System.nanoTime() - start) / 1_000_000.0;

        start = System.nanoTime();
        for (String key : lookupKeys)
            sink += linkedMap.get(key).stock;
        double lhmLookupMs = (System.nanoTime() - start) / 1_000_000.0;

        // 2. Evening Report A: All Medicines in Code Order
        // HashMap requires copying keys and sorting O(n log n)
        start = System.nanoTime();
        List<String> sortedKeys = new ArrayList<>(hashMap.keySet());
        Collections.sort(sortedKeys);
        for (String k : sortedKeys)
            sink += hashMap.get(k).stock;
        double hmCodeReportMs = (System.nanoTime() - start) / 1_000_000.0;

        // TreeMap iterates directly in sorted code order O(n)
        start = System.nanoTime();
        for (Medicine m : treeMap.values())
            sink += m.stock;
        double tmCodeReportMs = (System.nanoTime() - start) / 1_000_000.0;

        // LinkedHashMap also requires sorting O(n log n) for code order
        start = System.nanoTime();
        List<String> lhmSortedKeys = new ArrayList<>(linkedMap.keySet());
        Collections.sort(lhmSortedKeys);
        for (String k : lhmSortedKeys)
            sink += linkedMap.get(k).stock;
        double lhmCodeReportMs = (System.nanoTime() - start) / 1_000_000.0;

        // 3. Evening Report B: Insertion Order
        // LinkedHashMap iterates directly in insertion order O(n)
        start = System.nanoTime();
        for (Medicine m : linkedMap.values())
            sink += m.stock;
        double lhmInsertReportMs = (System.nanoTime() - start) / 1_000_000.0;

        if (sink == -1)
            System.out.println(sink);

        System.out.println("==========================================================================");
        System.out.printf("INVENTORY SIZE: %,d medicines | 100,000 Lookups & Evening Reports%n", size);
        System.out.println("==========================================================================");
        System.out.printf("%-34s | %-11s | %-11s | %-13s%n",
                "Operation", "HashMap", "TreeMap", "LinkedHashMap");
        System.out.println("--------------------------------------------------------------------------");
        System.out.printf("%-34s | %-8.3f ms | %-8.3f ms | %-10.3f ms%n",
                "100,000 get(code) Lookups", hmLookupMs, tmLookupMs, lhmLookupMs);
        System.out.printf("%-34s | %-8.3f ms | %-8.3f ms | %-10.3f ms%n",
                "Code-Order Report (with extra work)", hmCodeReportMs, tmCodeReportMs, lhmCodeReportMs);
        System.out.printf("%-34s | %-11s | %-11s | %-10.3f ms%n",
                "Insertion-Order Report", "Impossible*", "Impossible*", lhmInsertReportMs);
        System.out.println("*HashMap & TreeMap do not retain insertion history without extra tracking.\n");
    }

    public static void main(String[] args) {
        verifyOrderRequirement();

        // Warmup
        benchmarkSize(500);
        System.out.println("--- BENCHMARK RESULTS ---\n");

        // Three meaningfully different inventory sizes
        benchmarkSize(1_000);
        benchmarkSize(25_000);
        benchmarkSize(100_000);
    }
}