#!/usr/bin/env bash
# From the project root in PowerShell:
# docker compose exec -T namenode bash /workspace/scripts/run-bigram.sh unfiltered
set -euo pipefail
case ${1:-} in
    unfiltered) output_name=bigram_without_stopwords; options=() ;;
    filtered) output_name=bigram_with_stopwords; options=(/hw1_Lala_Huseynova/stopwords/stopwords.txt) ;;
    *) echo "Usage: run-bigram.sh {unfiltered|filtered}" >&2; exit 2 ;;
esac
run=$1
base=/hw1_Lala_Huseynova
output=$base/output/$output_name
if [[ -e /workspace/results/$run || -e /workspace/logs/$run ]] || hdfs dfs -test -e "$output"; then
    suffix=$(date -u +%Y%m%dT%H%M%SZ)
    run=${run}_$suffix
    output=${output}_$suffix
fi
results=/workspace/results/$run
logs=/workspace/logs/$run
mkdir "$logs" "$results"
exec > >(tee "$logs/commands.log") 2>&1
set -x
hdfs dfs -ls "$base/books" > "$results/input-files.txt"
sha256sum /workspace/build/BigramCount.jar
hadoop jar /workspace/build/BigramCount.jar BigramCount "$base/books" "$output" "${options[@]}" > "$logs/job.log" 2>&1
hdfs dfs -test -e "$output/_SUCCESS"
hdfs dfs -get "$output" "$results/output"
echo "$output" > "$results/hdfs-output.txt"
