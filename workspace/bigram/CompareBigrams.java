import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

// Usage: java CompareBigrams.java <unfiltered run dir> <filtered run dir> <stopwords>
public class CompareBigrams {
    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            throw new IllegalArgumentException("Supply two result directories and the stopword file.");
        }
        Path all = Path.of(args[0]);
        Path filtered = Path.of(args[1]);
        List<String> entries = Files.readAllLines(Path.of(args[2])).stream()
                .map(String::strip).filter(word -> !word.isEmpty())
                .map(word -> word.toLowerCase(Locale.ROOT).replace('\u2019', '\'').replace('\u2018', '\''))
                .toList();
        Set<String> stopwords = new HashSet<>(entries);
        List<String> files = parts(all);
        if (files.isEmpty() || !files.equals(parts(filtered))) {
            throw new IOException("The reducer part files do not match.");
        }
        long distinct = 0;
        long retained = 0;
        for (String file : files) {
            // Hadoop sorts both runs by the same key, within the same reducer partition.
            try (BufferedReader original = Files.newBufferedReader(all.resolve("output").resolve(file));
                 BufferedReader actual = Files.newBufferedReader(filtered.resolve("output").resolve(file))) {
                String row;
                while ((row = original.readLine()) != null) {
                    distinct++;
                    int space = row.indexOf(' ');
                    int tab = row.indexOf('\t');
                    if (space <= 0 || tab <= space + 1) {
                        throw new IOException("Malformed bigram: " + row);
                    }
                    if (stopwords.contains(row.substring(0, space))
                            || stopwords.contains(row.substring(space + 1, tab))) {
                        continue;
                    }
                    String next = actual.readLine();
                    if (!row.equals(next)) {
                        throw new IOException("Expected [" + row + "] but found [" + next + "] in " + file);
                    }
                    retained++;
                }
                if (actual.readLine() != null) {
                    throw new IOException("Extra filtered rows in " + file);
                }
            }
        }
        if (distinct != counter(all, "Reduce output records")
                || retained != counter(filtered, "Reduce output records")) {
            throw new IOException("Output row count differs from Hadoop's Reduce output records.");
        }
        System.out.println("Ten books | Stopword filtering comparison");
        System.out.println("Stopwords: " + entries.size() + " total, " + stopwords.size() + " unique");
        System.out.printf("%-20s %12s %12s %12s %12s%n",
                "Measure", "Unfiltered", "Filtered", "Reduction", "Reduction %");
        printChange("Map output bytes", counter(all, "Map output bytes"), counter(filtered, "Map output bytes"));
        printChange("Distinct bigrams", distinct, retained);
        System.out.println("PASS: Both distinct counts match Hadoop Reduce output records.");
        System.out.println("PASS: Filtering the unfiltered output reproduced every filtered key and count.");
        System.out.println("Verified retained keys: " + retained + " | Stopword-containing keys retained: 0");
    }

    private static List<String> parts(Path run) throws IOException {
        try (Stream<Path> paths = Files.list(run.resolve("output"))) {
            return paths.map(path -> path.getFileName().toString())
                    .filter(name -> name.startsWith("part-r-")).sorted().toList();
        }
    }

    private static long counter(Path run, String name) throws IOException {
        for (String line : Files.readAllLines(run.resolve("counters.txt"))) {
            String entry = line.strip();
            if (entry.startsWith(name + "=")) {
                return Long.parseLong(entry.substring(name.length() + 1));
            }
        }
        throw new IOException("Missing " + name + " in " + run);
    }

    private static void printChange(String measure, long before, long after) {
        long reduction = before - after;
        System.out.printf(Locale.ROOT, "%-20s %,12d %,12d %,12d %11.2f%%%n",
                measure, before, after, reduction, 100.0 * reduction / before);
    }
}
