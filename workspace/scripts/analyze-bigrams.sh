#!/usr/bin/env bash
# After /workspace/scripts/run-bigram.sh, pass its results directory name.
set -euo pipefail
run=${1:?Supply the results directory name}
[[ "$run" =~ ^(unfiltered|filtered)(_[0-9]{8}T[0-9]{6}Z)?$ ]]
results=/workspace/results/$run
logs=/workspace/logs/$run
log=$logs/job.log
set -o noclobber
job=$(sed -n 's/.*Job \(job_[0-9_]*\) completed successfully.*/\1/p' "$log")
app=$(sed -n 's/.*Submitted application \(application_[0-9_]*\).*/\1/p' "$log")
test -n "$job" && test -n "$app"
history=/tmp/hadoop-yarn/staging/history/done_intermediate/hadoop
mkdir "$logs/history"
hdfs dfs -get "$history/$job*" "$logs/history/"
grep -E '^[[:space:]]*(Map input records|Map output records|Map output bytes|Map output materialized bytes|Combine input records|Combine output records|Reduce shuffle bytes|Reduce input groups|Reduce output records|Failed Shuffles)=' "$log" > "$results/counters.txt"
java --source 17 /workspace/bigram/BigramAnalysis.java "$results/output" > "$results/ranking.txt"
distinct=$(sed -n 's/^Distinct bigrams (output records) = //p' "$results/ranking.txt")
total=$(sed -n 's/^Total occurrences (sum of counts) = //p' "$results/ranking.txt")
test "$distinct" = "$(sed -n 's/^[[:space:]]*Reduce output records=//p' "$log")"
test "$total" = "$(sed -n 's/^[[:space:]]*Map output records=//p' "$log")"
{
    echo "Ten books | ${run%%_*} bigrams | Top 25"
    echo "$job | $app | SUCCEEDED"
    grep -E '^[[:space:]]*(Map output bytes|Reduce output records)=' "$results/counters.txt" | sed 's/^[[:space:]]*//'
    cat "$results/ranking.txt"
} > "$results/top25.txt"
cat "$results/top25.txt"
