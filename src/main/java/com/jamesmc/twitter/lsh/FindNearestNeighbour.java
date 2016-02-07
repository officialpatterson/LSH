package com.jamesmc.twitter.lsh;

import com.jamesmc.twitter.lsh.index.Tweet;

import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

import com.jamesmc.twitter.lsh.index.Index;
import com.jamesmc.twitter.lsh.index.Pair;
import com.jamesmc.twitter.lsh.index.SortedCollisionsList;
import com.jamesmc.twitter.lsh.utils.Config;

public class FindNearestNeighbour implements Runnable {

	private final LinkedBlockingQueue<SortedCollisionsList> input;
	private final LinkedBlockingQueue<Pair<Tweet, Tweet>> output;
	private final LinkedList<Tweet> mostRecent = new LinkedList<Tweet>();
	private final Index index;

	private final int limit;

	public FindNearestNeighbour(LinkedBlockingQueue<SortedCollisionsList> sortedMap, LinkedBlockingQueue<Pair<Tweet, Tweet>> dbQueue, int limit, Index index) {
		this.input = sortedMap;
		this.output = dbQueue;
		this.index = index;
		this.limit = limit;
	}

	@Override
	public void run() {
		SortedCollisionsList sortedTweets = null;
		Tweet comaprisonTweet = null;

		while (true) {
			try {
				sortedTweets = input.take();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
				continue;
			}

			comaprisonTweet = sortedTweets.getTweet();

			int count = 0;
			Tweet nearest = null;
			double distance = 1;
			for (Tweet t : sortedTweets) {
				if (count++ >= limit)
					break;

				double d = 1 - comaprisonTweet.cosine(t);
				if (d < distance) {
					distance = d;
					nearest = t;
				}
			}

			if (distance > Config.distance) {
				for (Tweet t : mostRecent) {
					double d = 1 - comaprisonTweet.cosine(t);
					if (d < distance) {
						distance = d;
						nearest = t;
					}
				}
			}

			if (mostRecent.size() == Config.varianceReductionSize)
				mostRecent.remove();
			mostRecent.add(comaprisonTweet);

			Pair<Tweet, Tweet> pair = null;

			if (nearest != null && distance <= Config.distance) {
				if (nearest.getThread() >= 0) {
					comaprisonTweet.setThread(nearest.getThread());
					pair = new Pair<Tweet, Tweet>(null, comaprisonTweet);
				} else {
					comaprisonTweet.setThread(index.incrementThread());
					nearest.setThread(comaprisonTweet.getThread());
					pair = new Pair<Tweet, Tweet>(nearest, comaprisonTweet);
				}

				try {
					output.put(pair);
				} catch (InterruptedException e) {
					e.printStackTrace();
					continue;
				}
			}

		}
	}

}
