import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class GroupAverage {
    public enum RatingRecords { HEADER, VALID, INVALID }

    public static class SumCount implements Writable {
        private double sum;
        private long count;

        void set(double sum, long count) {
            this.sum = sum;
            this.count = count;
        }

        static SumCount aggregate(Iterable<SumCount> values) {
            SumCount total = new SumCount();
            for (SumCount value : values) {
                total.sum += value.sum;
                total.count += value.count;
            }
            return total;
        }

        @Override
        public void write(DataOutput out) throws IOException {
            out.writeDouble(sum);
            out.writeLong(count);
        }

        @Override
        public void readFields(DataInput in) throws IOException {
            sum = in.readDouble();
            count = in.readLong();
        }
    }

    public static class RatingMapper extends Mapper<LongWritable, Text, LongWritable, SumCount> {
        private final LongWritable movie = new LongWritable();
        private final SumCount contribution = new SumCount();

        @Override
        protected void setup(Context context) {
            // Include zero-valued categories in the job counters as well.
            for (RatingRecords category : RatingRecords.values()) {
                context.getCounter(category).increment(0);
            }
        }

        @Override
        protected void map(LongWritable offset, Text line, Context context)
                throws IOException, InterruptedException {
            String record = line.toString();
            if (offset.get() == 0 && record.equals("userId,movieId,rating,timestamp")) {
                context.getCounter(RatingRecords.HEADER).increment(1);
                return;
            }

            String[] fields = record.split(",", -1);
            if (fields.length != 4) {
                context.getCounter(RatingRecords.INVALID).increment(1);
                return;
            }
            long id;
            double rating;
            try {
                id = Long.parseLong(fields[1].trim());
                rating = Double.parseDouble(fields[2].trim());
            } catch (NumberFormatException e) {
                context.getCounter(RatingRecords.INVALID).increment(1);
                return;
            }
            if (id <= 0 || !Double.isFinite(rating) || rating < 0.5 || rating > 5.0) {
                context.getCounter(RatingRecords.INVALID).increment(1);
                return;
            }
            movie.set(id);
            contribution.set(rating, 1);
            context.write(movie, contribution);
            context.getCounter(RatingRecords.VALID).increment(1);
        }
    }

    public static class SumCombiner extends Reducer<LongWritable, SumCount, LongWritable, SumCount> {
        @Override
        protected void reduce(LongWritable movie, Iterable<SumCount> values, Context context)
                throws IOException, InterruptedException {
            context.write(movie, SumCount.aggregate(values));
        }
    }

    public static class AverageReducer extends Reducer<LongWritable, SumCount, LongWritable, Text> {
        @Override
        protected void reduce(LongWritable movie, Iterable<SumCount> values, Context context)
                throws IOException, InterruptedException {
            SumCount total = SumCount.aggregate(values);
            double average = total.sum / total.count;
            context.write(movie, new Text(total.count + "\t" + average));
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: GroupAverage <input> <new-output>");
            System.exit(2);
        }
        Job job = Job.getInstance(new Configuration(), "GroupAverage by movieId");
        job.setJarByClass(GroupAverage.class);
        job.setInputFormatClass(TextInputFormat.class);
        job.setMapperClass(RatingMapper.class);
        job.setCombinerClass(SumCombiner.class);
        job.setReducerClass(AverageReducer.class);
        job.setMapOutputKeyClass(LongWritable.class);
        job.setMapOutputValueClass(SumCount.class);
        job.setOutputKeyClass(LongWritable.class);
        job.setOutputValueClass(Text.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
