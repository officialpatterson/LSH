package com.jamesmc.twitter.lsh.index;

import java.util.concurrent.atomic.AtomicLong;

import gnu.trove.map.hash.TObjectIntHashMap;

public class Index {
	private final TObjectIntHashMap<String> index = new TObjectIntHashMap<String>();
	private final AtomicLong threadCounter = new AtomicLong(0);
	
	public Index() {
		threadCounter.set(System.currentTimeMillis());
	}

	
	public long getThreadCount() {
		return this.threadCounter.intValue();
	}
	
	public long incrementThread() {
		return this.threadCounter.incrementAndGet();
	}
	
	public int get(String key) { 
		int i = index.get(key);
		if(i == 0)
			return add(key);
		
		return index.get(key);
	}
	
	public synchronized int add(String key) {
		int i = index.get(key);
		if(i != 0)
			return i;
		
		return index.put(key, index.size() + 1);
	}
	
	public int size() {
		return index.size();
	}
	
}
