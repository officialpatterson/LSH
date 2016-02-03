package com.jamesmc.twitter.lsh.utils;

import com.jamesmc.twitter.lsh.parser.EnglishLemmaAnalyzer;
import com.jamesmc.twitter.lsh.parser.Tokenizer;
import com.jamesmc.twitter.lsh.parser.TweetTokenizer;

public class Config {

	public static final String runId = "LSH-final-3";

	// The maximum size of any bucket in the hash tables
	public static final int maxBucketSize = 100;

	// The numbers of bits used by the hash tables. The number of buckets in
	// each hash table is 2^bits
	public static final int bits = 13;

	// The maximum distance that a tweet can be from its approximate nearest
	// neighbour
	public static final double distance = 0.45;

	// The desired probability of missing the nearest neighbour. The smaller
	// this is, the more hash tables are created, using more memory and taking
	// more time to compute
	public static final double probablity = 0.02;

	// The number of most recent tweets to compare the incoming tweet against if
	// no suitable NN is found using the LSH
	public static final int varianceReductionSize = 2000;

	// The path to the root of the project
	// public static final String projectPath = "/scratch/jamesmcm/LSH/";
	public static final String projectPath = "/home/jorge/experiments/transport_twitter_experiment/Event Detection Algorithms/LSH/";

	public static final String lemmaModelsPath = Config.projectPath + "models/english-bidirectional-distsim.tagger";

	// The input filename of the files containing the collections
	// public static final String[] inputFiles = new String[] { "/scratch/jamesmcm/collection.json"};
	public static final String[] inputFiles = new String[] { "/media/jorge/Babilon1/data/transport_twitter_experiment/twitter/chicago.json"};

	// Configuration for the tokenizer.
	public static final boolean doStem = true;
	public static final boolean removePuctuation = true;
	public static final boolean removeStopWords = true;

	// The starting default size for hashmaps. The small this number, the less
	// memory used. Java uses 16 by default, however it 99% of cases, a tweet
	// will contain significantly less than 16 tokens after tokenzation and stop
	// word removal
	public static final int hashMapInitSize = 2;

	public static final Database database = new Database();

	public static EnglishLemmaAnalyzer lemmaguy;
	static {
		try {
			lemmaguy = new EnglishLemmaAnalyzer(Config.lemmaModelsPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// The tokenizer to use for tokenization
	public static final Tokenizer tokenzier = new TweetTokenizer();

	public static final String eventsOutput = projectPath + runId + "-events.list";

	public static int numberHashTables = -1;

}
