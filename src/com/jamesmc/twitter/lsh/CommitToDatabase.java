package com.jamesmc.twitter.lsh;

import com.jamesmc.twitter.lsh.index.Tweet;

import java.util.concurrent.LinkedBlockingQueue;

import com.jamesmc.twitter.lsh.index.Pair;
import com.jamesmc.twitter.lsh.utils.Config;

public class CommitToDatabase implements Runnable {

	private final LinkedBlockingQueue<Pair<Tweet, Tweet>> input;

	public CommitToDatabase(LinkedBlockingQueue<Pair<Tweet, Tweet>> input) {
		this.input = input;
	}

	@Override
	public void run() {
		Pair<Tweet, Tweet> tweets = null;
		Tweet nearest = null;
		Tweet comaprisonTweet = null;

		while (true) {
			try {
				tweets = input.take();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
				continue;
			}

			nearest = tweets.getLeft();
			comaprisonTweet = tweets.getRight();

			if (nearest == null) {
				Config.database.add(comaprisonTweet, true);
			} else {
				Config.database.add(comaprisonTweet, true);
				Config.database.add(nearest, true);
			}

		}
	}

}
