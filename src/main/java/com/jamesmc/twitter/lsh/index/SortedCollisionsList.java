package com.jamesmc.twitter.lsh.index;

import java.util.LinkedList;

public class SortedCollisionsList extends LinkedList<Tweet> {

	private static final long serialVersionUID = 1L;
	
	private final Tweet tweet;
	
	public SortedCollisionsList(Tweet t) {
		this.tweet = t;
	}
	
	public Tweet getTweet() {
		return this.tweet;
	}

}
