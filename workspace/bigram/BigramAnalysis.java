import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

// Usage: java BigramAnalysis.java <local reducer-output directory>
public class BigramAnalysis {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalArgumentException("Supply the directory containing part-r-* files.");
        }
        Path output = Path.of(args[0]);
        List<Path> parts;
        try (Stream<Path> paths = Files.list(output)) {
            parts = paths.filter(path -> path.getFileName().toString().startsWith("part-r-"))
                    .sorted().toList();
        }
        if (parts.isEmpty()) {
            throw new IOException("No reducer output files in " + output);
        }

        Map<String, Long> counts = new HashMap<>();
        long total = 0;
        for (Path part : parts) {
            try (BufferedReader reader = Files.newBufferedReader(part)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    int tab = line.lastIndexOf('\t');
                    if (tab <= 0) {
                        throw new IOException("Missing key or count in " + part + ": " + line);
                    }
                    String bigram = line.substring(0, tab);
                    long count = Long.parseLong(line.substring(tab + 1));
                    if (count <= 0 || counts.putIfAbsent(bigram, count) != null) {
                        throw new IOException("Invalid count or duplicate bigram: " + line);
                    }
                    total += count;
                }
            }
        }

        var order = Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder())
                .thenComparing(Map.Entry.comparingByKey());
        var top = counts.entrySet().stream().sorted(order).limit(25).toList();
        System.out.println("Distinct bigrams (output records) = " + counts.size());
        System.out.println("Total occurrences (sum of counts) = " + total);
        System.out.println("Rank  Bigram                    Count    Rank  Bigram                    Count");
        int rows = (top.size() + 1) / 2;
        for (int i = 0; i < rows; i++) {
            System.out.print(cell(i + 1, top.get(i)));
            if (i + rows < top.size()) {
                System.out.print("    " + cell(i + rows + 1, top.get(i + rows)));
            }
            System.out.println();
        }
    }

    private static String cell(int rank, Map.Entry<String, Long> pair) {
        return String.format(Locale.ROOT, "%4d  %-21s %8d", rank, pair.getKey(), pair.getValue());
    }
}
