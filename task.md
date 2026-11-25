INFT 4836 – Intro to Big Data Analytics

Homework 1

 

In this homework you go one step past the word-count lab. First you count word pairs (bigrams) in books you choose. Then you compute averages on a real dataset from Kaggle. Last, you answer questions about how MapReduce behaves at scale.

Write everything in Java and run it on Hadoop.

 

Part 1: Data (10 points)

 

Books: Download 10 different books as plain text files. Each of the files you download must be at least 100 kB in size, but the larger the book the more interesting your results may be. Once you have the books downloaded, add them to your Hadoop instance so they will be readable by your MapReduce application.

 

Kaggle dataset. Choose one of the two datasets (Amazon Fine Food Reviews: Reviews.csv, The Movies Dataset: ratings.csv), download it with a free Kaggle account, and load the file into HDFS. Group them by ProductId and movieId respectively.

 

Stopwords. Make a list of at least 50 common English words.

 

Part 2: Bigram Count (35 points)

Write BigramCount.java to count how often each pair of adjacent words (bigrams) appears across all 10 books.

 

Requirements:

Ignore case and remove punctuation, except punctuation within words (e.g., don’t should remain one word).
Bigrams must not cross line breaks.
Use a combiner to reduce intermediate data.
Run your application twice:
Without stopword removal: Count all bigrams.
With stopword removal: Exclude any bigram containing at least one word from your stopword list.
Save the output of both runs separately for comparison.
 

Part 3: Average per Group (35 points)

 

Write GroupAverage.java using one of the Kaggle datasets selected in Part 1.

Amazon Fine Food Reviews: Group records by ProductId and calculate the average Score for each product.
The Movies Dataset: Group records by movieId and calculate the average rating for each movie.
Report the 20 groups with the highest average, considering only groups with at least 50 records.

 

Part 4: Analysis (20 points)

Answer the following questions in your report. For each question, include screenshots of the relevant application output or Hadoop job counters to support your answers.

(5 points) What are the 25 most frequent bigrams and their respective counts when stopwords are not removed?
(5 points) What are the 25 most frequent bigrams and their respective counts when stopwords are removed?
(5 points) Based on your application output and Hadoop job counters, how does removing stopwords affect the total number of bytes emitted by the mappers? Explain one specific way this change could affect the performance of your MapReduce job.
(5 points) What is the size of the keyspace (number of distinct bigrams) with and without stopword removal? How does the difference relate to the number of stopwords in your list? Explain your observations.
 

 

Deliverables and Grading

Submit one ZIP file named hw1_Name_Surname.zip, containing:

Java source code (BigramCount.java and GroupAverage.java).
Your stopword list.
All 10 books as plain text files.
Any additional files required to run your applications.
report.pdf, containing:
The list of books and their sources.
The 20 highest-ranked groups from Part 3, with record counts and averages.
Answers to all Part 4 questions, with supporting screenshots.
A link to your video explanation on the first page.
 

Do not include the Kaggle dataset in your ZIP file.

 

Video Explanation

Record a video explaining your homework and upload it to YouTube as Unlisted.

In your video:

Walk through your Java implementations of BigramCount and GroupAverage.
Demonstrate both applications running on Hadoop.
Show and explain the outputs.
Explain your Part 4 findings, including the impact of stopword removal.
Include the YouTube link on the first page of report.pdf.

Before submitting, open the link in a private/incognito browser window to verify that the video is accessible.

A missing or inaccessible video link will result in a 10-point deduction from your homework grade.