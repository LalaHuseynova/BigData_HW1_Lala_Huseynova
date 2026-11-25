import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class BigramCount {
    private static final String STOPWORD_PATH = "bigram.stopwords.path";
    // Keep single connectors inside words, as in don't, well-read and U.S.
    private static final Pattern WORD = Pattern.compile(
            "[\\p{L}\\p{N}][\\p{L}\\p{M}\\p{N}]*(?:['._-][\\p{L}\\p{N}][\\p{L}\\p{M}\\p{N}]*)*");

    private static String normalize(String text) {
        return text.toLowerCase(Locale.ROOT)
                .replace('\u2018', '\'').replace('\u2019', '\'')
                .replace('\u2010', '-').replace('\u2011', '-');
    }

    public static class BigramMapper extends Mapper<LongWritable, Text, Text, LongWritable> {
        private static final LongWritable ONE = new LongWritable(1);
        private final Text bigram = new Text();
        private final Set<String> stopwords = new HashSet<>();

        @Override
        protected void setup(Context context) throws IOException {
            Configuration conf = context.getConfiguration();
            String filename = conf.get(STOPWORD_PATH);
            if (filename == null) {
                return;
            }
            Path path = new Path(filename);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    path.getFileSystem(conf).open(path), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String word = normalize(line.strip());
                    if (!word.isEmpty()) {
                        stopwords.add(word);
                    }
                }
            } catch (IOException e) {
                throw new IOException("Cannot load stopword file " + path, e);
            }
        }

        @Override
        protected void map(LongWritable offset, Text line, Context context)
                throws IOException, InterruptedException {
            Matcher words = WORD.matcher(normalize(line.toString()));
            String previous = null;
            while (words.find()) {
                String current = words.group();
                if (previous != null && !stopwords.contains(previous) && !stopwords.contains(current)) {
                    bigram.set(previous + " " + current);
                    context.write(bigram, ONE);
                }
                // Advance even after a rejected pair to preserve original adjacency.
                previous = current;
            }
        }
    }

    public static class SumReducer extends Reducer<Text, LongWritable, Text, LongWritable> {
        private final LongWritable total = new LongWritable();

        @Override
        protected void reduce(Text key, Iterable<LongWritable> counts, Context context)
                throws IOException, InterruptedException {
            long sum = 0;
            for (LongWritable count : counts) {
                sum += count.get();
            }
            total.set(sum);
            context.write(key, total);
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2 && args.length != 3) {
            System.err.println("Usage: BigramCount <input> <new-output> [stopword-file]");
            System.exit(2);
        }

        Configuration conf = new Configuration();
        if (args.length == 3) {
            conf.set(STOPWORD_PATH, args[2]);
        }
        Job job = Job.getInstance(conf, "BigramCount " + (args.length == 3 ? "filtered" : "unfiltered"));
        job.setJarByClass(BigramCount.class);
        job.setInputFormatClass(TextInputFormat.class);
        job.setMapperClass(BigramMapper.class);
        job.setCombinerClass(SumReducer.class);
        job.setReducerClass(SumReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(LongWritable.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
