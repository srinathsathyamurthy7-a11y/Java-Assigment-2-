# CSA0904 – Java Assignment 2 (CO2)
## Strings, Regular Expressions, Generics & the Java Collection Framework

### Student Details
- **Name:** PURUSHOTHAMAN.V
- **Register Number:** 192511064
- **Department:** Computer Science and Engineering

### Development Environment
- **IDE:** Visual Studio Code (VS Code / Antigravity IDE)
- **Programming Language:** Java
- **JDK:** Java 8 (verify using `java -version`)
- **Operating System:** Windows
- **Terminal:** VS Code Integrated Terminal (PowerShell)

### Java Version
To check the installed Java version, execute:

```bash
java -version
javac -version
```

**Java Version:** Java SE Development Kit 8 (JDK 1.8)

### Compilation and Execution
All Java programs were developed, benchmarked, and executed using Visual Studio Code.

Commands used in the VS Code integrated terminal (from the root repository folder):

**Compile a Java program:**
```bash
javac Q1/TicketBenchmark.java
```

**Run a Java program (with package declaration):**
```bash
java -cp . Q1.TicketBenchmark
```

**Run a Java program (without package declaration):**
```bash
java -cp Q1 TicketBenchmark
```

**Commands for All Tasks (Q1 – Q7):**
```bash
# Q1 - Railway e-Ticket Text Building Benchmark
javac Q1/TicketBenchmark.java
java -cp . Q1.TicketBenchmark

# Q2 - Music Playlist Editor Benchmark
javac Q2/PlaylistBenchmark.java
java -cp . Q2.PlaylistBenchmark

# Q3 - Event Attendance De-duplication & Contract Investigation
javac Q3/Attendance.java Q3/ContractInvestigation.java
java -cp . Q3.Attendance
java -cp . Q3.ContractInvestigation

# Q4 - Hospital Pharmacy Inventory Map Benchmark
javac Q4/PharmacyInventoryBenchmark.java
java -cp . Q4.PharmacyInventoryBenchmark

# Q5 - Inter-College Sports Generic Leaderboard
javac Q5/LeaderboardDemo.java Q5/RankingDesignTest.java
java -cp . Q5.LeaderboardDemo
java -cp . Q5.RankingDesignTest

# Q6 - Hospital Emergency Triage Queue & Growth Investigation
javac Q6/Triage.java Q6/TriageInvestigation.java
java -cp . Q6.Triage
java -cp . Q6.TriageInvestigation

# Q7 - Day-4 Design Challenge: Fair Bucketed Triage Redesign
javac Q7/TriageRedesignValidation.java
java -cp . Q7.TriageRedesignValidation
```

### Assignment Results

| Task | Description | Status | Output / Key Finding |
|---|---|---|---|
| Q1 | Building Text Under Load (`String`, `StringBuilder`, `StringBuffer`) | Completed | `StringBuilder` ($O(n)$) outperformed `String` `+` ($O(n^2)$) by ~7x at scale |
| Q2 | Music Playlist Editor (`ArrayList`, `LinkedList`, `ArrayDeque`) | Completed | `ArrayList` best for $O(1)$ indexing; `ArrayDeque` recommended for "Up Next" queue |
| Q3 | Event Attendance De-duplication (`HashSet` & `hashCode`–`equals`) | Completed | Fixed duplicate entries (`3` → `2`) by overriding `hashCode()` with `Objects.hash(regNo)` |
| Q4 | Hospital Pharmacy Inventory (`HashMap`, `TreeMap`, `LinkedHashMap`) | Completed | `LinkedHashMap` chosen for $O(1)$ daytime lookups and native insertion-order reports |
| Q5 | Generic Ranking Component (`Generics`, `Comparable`, `Comparator`) | Completed | Built reusable `Leaderboard<T>` with chained `Comparator` and strict weak ordering |
| Q6 | Hospital Emergency Triage (`Queue` & `PriorityQueue`) | Completed | Preserved FIFO ties via arrival counter; designed aging rule to prevent starvation |
| Q7 | Day-4 Design Challenge (Diagnose, Redesign & Validate) | Completed | Redesigned triage with 5 FIFO `ArrayDeque` buckets + aging for $O(1)$ fair dispatch |

### Project Objectives
- Analyse time and space trade-offs across Java Strings and the Collection Framework using empirical benchmarks.
- Enforce object equality contracts (`equals` and `hashCode`) and type-safe generic comparators.
- Evaluate priority scheduling, fairness, and starvation in queue-based systems.
- Diagnose algorithmic bottlenecks and validate an engineered redesign under load.

### Screenshots
Screenshots of the source code execution and benchmark outputs are maintained in their respective task folders (`Q1` through `Q7`).

### Conclusion
This assignment provided hands-on engineering experience in benchmarking, diagnosing bottlenecks, and selecting appropriate Java collections and generic designs using Visual Studio Code.

---
**Submitted by:** PURUSHOTHAMAN.V  
**Register Number:** 192511064
