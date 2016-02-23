package com.jamesmc.twitter.lsh;

import com.jamesmc.twitter.lsh.index.Tweet;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.jamesmc.twitter.lsh.index.Index;
import com.jamesmc.twitter.lsh.index.LocallitySensitiveHashTable;
import com.jamesmc.twitter.lsh.index.Pair;
import com.jamesmc.twitter.lsh.index.SortedCollisionsList;
import com.jamesmc.twitter.lsh.index.TweetCollisionsMap;
import com.jamesmc.twitter.lsh.utils.Config;

public class RunLSH {

	private int numberHashTables;
	private LocallitySensitiveHashTable[] lsh;

	private final Index index = new Index();
	private static JSONParser parser = new JSONParser();

	// Input and output queues for the threads. These help to keep everything
	// running in parallel where possible.
	private final LinkedBlockingQueue<Tweet> streamInput = new LinkedBlockingQueue<Tweet>(500);
	private final LinkedBlockingQueue<TweetCollisionsMap> tweetCollisions = new LinkedBlockingQueue<TweetCollisionsMap>(500);
	private final LinkedBlockingQueue<SortedCollisionsList> sortedCollisions = new LinkedBlockingQueue<SortedCollisionsList>(500);
	private final LinkedBlockingQueue<Pair<Tweet, Tweet>> databaseInput = new LinkedBlockingQueue<Pair<Tweet, Tweet>>(500);

	private final Thread findCollisions, sortCollisons, nearestNeighbour, databaseQueue;

	@SuppressWarnings("unchecked")
	public RunLSH() {

		// Calculate the number of hash tables we'll need given the desired
		// probability of finding the closest match
		double pColl = Math.pow((1 - (Config.distance / Math.PI)), Config.bits);
		this.numberHashTables = (int) Math.ceil(Math.log(Config.probablity) / Math.log(1 - pColl));
		System.out.println("Using " + this.numberHashTables + " LSHTables for a probablity of\n" + Config.probablity + " with " + Config.bits
				+ " hyperplanes per table.\n");

		Config.numberHashTables = this.numberHashTables;

		// Generate the number of hash tables we'll need to get the
		// desireProbability
		System.out.print("Generating LSHTables...");
		lsh = new LocallitySensitiveHashTable[numberHashTables];
		for (int i = 0; i < numberHashTables; i++)
			lsh[i] = new LocallitySensitiveHashTable(index, Config.bits);
		System.out.println("..........DONE");

		// Create a new thread which will check for tweets in the LSH which
		// collide with input tweets
		System.out.print("Intialising threads...");
		findCollisions = new Thread(new FindCollisions(lsh, streamInput, tweetCollisions));
		findCollisions.setName("CollisionFinder");
		findCollisions.start();

		// A thread to sort the collisions
		sortCollisons = new Thread(new SortCollisions(tweetCollisions, sortedCollisions, 3 * this.numberHashTables));
		sortCollisons.setName("SortCollisons");
		sortCollisons.start();

		// A thread to find the nearest neighbour
		// We are specifying that the first pass when searching for a NN should
		// use 3x the number of hash tables generated.
		nearestNeighbour = new Thread(new FindNearestNeighbour(sortedCollisions, databaseInput, 3 * this.numberHashTables, index));
		nearestNeighbour.setName("NearestNeighbour");
		nearestNeighbour.start();

		databaseQueue = new Thread(new CommitToDatabase(databaseInput));
		databaseQueue.setName("DatabaseQueue");
		databaseQueue.start();
		System.out.println("..........DONE");

	}
	public void cluster(Reader inputStreamReader){

		try {
			BufferedReader f = new BufferedReader(inputStreamReader);
			DateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy");

			int count = 0;

			String x;
			//take a json object from the inputstream

			while( (x = f.readLine()) != null )
			{
				Map<String, Object> status;
				try {
					status = (Map<String, Object>) parser.parse(x);

					String text = (String) status.get("text");
					long id = (Long) status.get("id");
					long createdAt = -1;

					Map<String, Object> user = (Map<String, Object>) status.get("user");
					long userId = (Long) user.get("id");


					//ignore retweets
					if(status.get("retweeted_status") != null || status.get("quoted_status") != null)
						continue;


					try {
						Date date = formatter.parse((String) status.get("created_at"));
						createdAt = date.getTime();
					} catch (java.text.ParseException e) {
						e.printStackTrace();
					}

					Tweet t = new Tweet(id, text, userId, createdAt);

					if (++count % 100000 == 0)
						System.out.println(count + " tweets processed...");


					// If the tweet has less than 2 words in it after
					// tokenization, don't even bother with it
					if (t.getTokens().size() <= 2)
						continue;

					// Add the tweet to the input queue and let it do its
					// thing.
					try {
						this.streamInput.put(t);
					} catch (InterruptedException e) {e.printStackTrace();}

				} catch (ParseException e) {
					System.out.println("-------------------------------------------Unparsable Line!-------------------------------------------");
					System.out.println(x);
					System.out.println("------------------------------------------------------------------------------------------------------");
				}
			}



		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {

		RunLSH lsh = new RunLSH();


		try{
			if(args[0].equals("--file")){//use a file
				try {
					System.out.println("Clustering tweets from file");
					lsh.cluster(new FileReader(args[1]));
				} catch(FileNotFoundException e) {
					System.out.print("ERROR: File not found!");
				}
			}

			if(args[0].equals("--stream")){ //read from so

				System.out.println("Clustering tweets from standard input");
				lsh.cluster(new InputStreamReader(System.in));
			}


		}catch(ArrayIndexOutOfBoundsException exception){
			System.out.println("ERROR: incorrect usage, must specify input source");
		}

	}

}
