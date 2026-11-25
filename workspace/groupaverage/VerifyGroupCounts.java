import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

// Cross-check the full reducer output with the saved counters from the same job.
public class VerifyGroupCounts {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalArgumentException("Supply the group-average results directory.");
        }
        Path results = Path.of(args[0]);
        Map<String, Long> counters = new HashMap<>();
        for (String line : Files.readAllLines(results.resolve("counters.txt"))) {
            String[] entry = line.strip().split("=", 2);
            counters.put(entry[0], Long.parseLong(entry[1]));
        }
        List<Path> parts;
        try (Stream<Path> paths = Files.list(results.resolve("output"))) {
            parts = paths.filter(path -> path.getFileName().toString().startsWith("part-r-")).toList();
        }
        if (parts.isEmpty()) {
            throw new IOException("Missing reducer output.");
        }

        long ratings = 0;
        Set<Long> movies = new HashSet<>();
        for (Path part : parts) {
            try (BufferedReader reader = Files.newBufferedReader(part)) {
                String row;
                while ((row = reader.readLine()) != null) {
                    String[] fields = row.split("\t", -1);
                    if (fields.length != 3) {
                        throw new IOException("Invalid reducer row: " + row);
                    }
                    long id = Long.parseLong(fields[0]);
                    long count = Long.parseLong(fields[1]);
                    double average = Double.parseDouble(fields[2]);
                    if (id <= 0 || count <= 0 || !Double.isFinite(average)
                            || average < 0.5 || average > 5.0 || !movies.add(id)) {
                        throw new IOException("Invalid or duplicate movie in reducer row: " + row);
                    }
                    ratings += count;
                }
            }
        }
        long valid = counters.get("VALID");
        long accounted = valid + counters.get("INVALID") + counters.get("HEADER");
        if (accounted != counters.get("Map input records") || valid != counters.get("Map output records")
                || ratings != valid || movies.size() != counters.get("Reduce output records")) {
            throw new IOException("Hadoop counters and reducer totals disagree.");
        }
        System.out.println("Input records accounted for (header + valid + invalid) = " + accounted);
        System.out.println("Sum of rating counts across all movies = " + ratings);
        System.out.println("Distinct movie IDs in reducer output = " + movies.size());
        System.out.println("PASS: input totals, valid ratings and movie counts match Hadoop counters.");
    }
}
