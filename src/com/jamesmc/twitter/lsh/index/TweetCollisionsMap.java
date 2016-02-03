package com.jamesmc.twitter.lsh.index;

import gnu.trove.map.hash.TObjectByteHashMap;

public class TweetCollisionsMap extends TObjectByteHashMap<Tweet>{

	private static final long serialVersionUID = 1L;
	
	private final Tweet tweet;
	
	public TweetCollisionsMap(Tweet t) {
		this.tweet = t;
	}
	
	public Tweet getTweet() {
		return this.tweet;
	}
	
	

}
