import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

// Reads a local copy of the Hadoop reducer output.
public class TopMovies {
    private record Movie(long id, long count, double average) {}

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: TopMovies <reducer-output-directory>");
        }
        List<Path> parts;
        try (Stream<Path> paths = Files.list(Path.of(args[0]))) {
            parts = paths.filter(path -> path.getFileName().toString().startsWith("part-r-")).toList();
        }
        if (parts.isEmpty()) {
            throw new IOException("No reducer output in " + args[0]);
        }

        List<Movie> eligible = new ArrayList<>();
        for (Path part : parts) {
            try (BufferedReader reader = Files.newBufferedReader(part)) {
                String row;
                while ((row = reader.readLine()) != null) {
                    String[] fields = row.split("\t", -1);
                    if (fields.length != 3) {
                        throw new IOException("Invalid reducer row in " + part + ": " + row);
                    }
                    long count = Long.parseLong(fields[1]);
                    if (count >= 50) {
                        eligible.add(new Movie(Long.parseLong(fields[0]), count, Double.parseDouble(fields[2])));
                    }
                }
            }
        }
        eligible.sort(Comparator.comparingDouble(Movie::average).reversed().thenComparingLong(Movie::id));
        System.out.println("Rank\tMovie ID\tRating count\tAverage rating");
        for (int i = 0; i < Math.min(20, eligible.size()); i++) {
            Movie movie = eligible.get(i);
            System.out.printf(Locale.ROOT, "%d\t%d\t%d\t%.4f%n", i + 1, movie.id(), movie.count(), movie.average());
        }
    }
}
