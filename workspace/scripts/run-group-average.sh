#!/usr/bin/env bash
# Run from the project root: docker compose exec -T namenode bash /workspace/scripts/run-group-average.sh
set -euo pipefail
cd /workspace
input=/hw1_Lala_Huseynova/dataset/ratings.csv
output=/hw1_Lala_Huseynova/output/group_average
logs=logs/group_average
results=results/group_average
mkdir -p "$logs" "$results"
if [[ -n "$(ls -A "$logs")" || -n "$(ls -A "$results")" ]] || hdfs dfs -test -e "$output"; then
    tag=$(date -u +%Y%m%dT%H%M%SZ)
    logs="$logs/$tag"
    results="$results/$tag"
    output="${output}_$tag"
    mkdir "$logs" "$results"
fi
set -o noclobber
exec > >(tee "$logs/commands.log") 2>&1
set -x
sha256sum build/GroupAverage.jar
hdfs dfs -ls "$input" > "$results/input-file.txt"
hadoop jar build/GroupAverage.jar GroupAverage "$input" "$output" > "$logs/job.log" 2>&1
hdfs dfs -test -e "$output/_SUCCESS"
hdfs dfs -get "$output" "$results/output"
echo "$output" > "$results/hdfs-output.txt"
job=$(sed -n 's/.*Job \(job_[0-9_]*\) completed successfully.*/\1/p' "$logs/job.log")
test -n "$job"
mkdir "$logs/history"
hdfs dfs -get "/tmp/hadoop-yarn/staging/history/done_intermediate/hadoop/$job*" "$logs/history/"
grep -E '^[[:space:]]*(Map input records|Map output records|Combine input records|Combine output records|Reduce input records|Reduce input groups|Reduce output records|HEADER|VALID|INVALID)=' "$logs/job.log" > "$results/counters.txt"
java --source 17 groupaverage/VerifyGroupCounts.java "$results" | tee "$results/verification.txt"
