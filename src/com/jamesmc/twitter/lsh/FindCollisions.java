package com.jamesmc.twitter.lsh;

import com.jamesmc.twitter.lsh.index.Tweet;

import java.util.concurrent.LinkedBlockingQueue;

import com.jamesmc.twitter.lsh.index.LocallitySensitiveHashTable;
import com.jamesmc.twitter.lsh.index.TweetCollisionsMap;

public class FindCollisions implements Runnable {

	private final LocallitySensitiveHashTable[] lsh;
	private final LinkedBlockingQueue<Tweet> input;
	private final LinkedBlockingQueue<TweetCollisionsMap> output;
	private static final byte one = 1; // Save us from casting this over and over again

	public FindCollisions(LocallitySensitiveHashTable[] lsh, LinkedBlockingQueue<Tweet> input,
			LinkedBlockingQueue<TweetCollisionsMap> output) {
		this.lsh = lsh;
		this.input = input;
		this.output = output;
	}

	@Override
	public void run() {
		Tweet targetTweet = null;

		while (true) {
			try {
				targetTweet = input.take();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
				continue;
			}

			TweetCollisionsMap collisions = new TweetCollisionsMap(targetTweet);
			for (LocallitySensitiveHashTable table : lsh)
				for (Tweet tweet : table.getCollisions(targetTweet))
					collisions.adjustOrPutValue(tweet, one, one);

			try {
				output.put(collisions);
			} catch (InterruptedException e) {
				e.printStackTrace();
				continue;
			}

			for (LocallitySensitiveHashTable table : lsh)
				table.add(targetTweet);
		}
	}

}
