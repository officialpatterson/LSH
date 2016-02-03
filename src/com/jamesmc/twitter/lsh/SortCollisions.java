package com.jamesmc.twitter.lsh;

import com.jamesmc.twitter.lsh.index.Tweet;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

import com.jamesmc.twitter.lsh.index.SortedCollisionsList;
import com.jamesmc.twitter.lsh.index.TweetCollisionsMap;
import com.jamesmc.twitter.lsh.utils.Config;

public class SortCollisions implements Runnable {

	private final LinkedBlockingQueue<SortedCollisionsList> output;
	private final LinkedBlockingQueue<TweetCollisionsMap> input;
	private final int limit;

	private LinkedList<Tweet> buckets[];

	public SortCollisions(LinkedBlockingQueue<TweetCollisionsMap> input, LinkedBlockingQueue<SortedCollisionsList> output, int limit) {
		this.input = input;
		this.output = output;
		this.limit = limit;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		TweetCollisionsMap foundCollisions = null;

		while (true) {
			try {
				foundCollisions = input.take();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
				continue;
			}

			this.buckets = new LinkedList[Config.numberHashTables];
			for (int i = Config.numberHashTables - 1; i >= 0; i--)
				this.buckets[i] = new LinkedList<Tweet>();

			// Sort the tweets by the number of collisions
			for (Tweet t : foundCollisions.keySet())
				this.buckets[foundCollisions.get(t) - 1].add(t);
			SortedCollisionsList inOrderTweets = new SortedCollisionsList(foundCollisions.getTweet());
			for (int i = Config.numberHashTables - 1; i >= 0; i--) {
				if (this.buckets[i].size() > 0) {
					for (Tweet t : this.buckets[i]) {
						inOrderTweets.add(t);
						if (inOrderTweets.size() > limit)
							break;
					}
				}
			}
			
			try {
				output.put(inOrderTweets);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}

/*
 * Sorts primevally based upon number of collisions, however if the number of
 * collisions is the same, then it sorts based up on the ID of the tweet.
 */
class ValueComparator implements Comparator<Tweet> {
	TweetCollisionsMap base;

	public ValueComparator(TweetCollisionsMap tweets) {
		this.base = tweets;
	}

	public int compare(Tweet a, Tweet b) {
		if (base.get(a) > base.get(b)) {
			return -1;
		} else if (base.get(a) == base.get(b)) {
			if (a.getId() < b.getId())
				return -1;
			else if (a.getId() == b.getId())
				return 0;
			else
				return 1;
		} else {
			return 1;
		}
	}
}
