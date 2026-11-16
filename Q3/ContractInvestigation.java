import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ContractInvestigation {

    // Broken Student (equals overridden, hashCode NOT overridden)
    static class BrokenStudent {
        String regNo;
        String name;

        BrokenStudent(String regNo, String name) {
            this.regNo = regNo;
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof BrokenStudent))
                return false;
            return Objects.equals(regNo, ((BrokenStudent) o).regNo);
        }
    }

    // Fixed Mutable Student (to test mutating regNo after insertion)
    static class MutableStudent {
        String regNo;
        String name;

        MutableStudent(String regNo, String name) {
            this.regNo = regNo;
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof MutableStudent))
                return false;
            return Objects.equals(regNo, ((MutableStudent) o).regNo);
        }

        @Override
        public int hashCode() {
            return Objects.hash(regNo);
        }
    }

    public static void main(String[] args) {
        // Test 1: Set of scans where HashSet APPEARS to work (distinct students or same
        // object ref)
        Set<BrokenStudent> test1 = new HashSet<>();
        BrokenStudent s1 = new BrokenStudent("192511001", "Arun");
        test1.add(s1);
        test1.add(new BrokenStudent("192511002", "Divya"));
        test1.add(s1); // Exact same object reference scanned twice
        System.out.println("Test 1 (Appears to work) - Predicted: 2 | Observed: " + test1.size());

        // Test 2: Deliberately exposes duplicates (new object with same regNo at
        // another gate)
        Set<BrokenStudent> test2 = new HashSet<>();
        test2.add(new BrokenStudent("192511001", "Arun")); // Gate 1
        test2.add(new BrokenStudent("192511002", "Divya")); // Gate 2
        test2.add(new BrokenStudent("192511001", "Arun")); // Gate 3 (new instance)
        System.out.println("Test 2 (Exposes duplicates) - Predicted: 3 (bug) | Observed: " + test2.size());

        // Test 3: Mutating regNo AFTER insertion into HashSet
        Set<MutableStudent> test3 = new HashSet<>();
        MutableStudent m1 = new MutableStudent("192511001", "Arun");
        test3.add(m1);
        m1.regNo = "192511999"; // Mutated after insertion!
        System.out.println(
                "Test 3 (Post-insert mutation) - contains(m1) Predicted: false | Observed: " + test3.contains(m1));
        test3.add(new MutableStudent("192511999", "Arun"));
        System.out.println("Test 3 (Set size after re-adding mutated ID) - Predicted: 2 | Observed: " + test3.size());
    }
}