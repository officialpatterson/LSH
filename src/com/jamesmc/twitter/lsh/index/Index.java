package com.jamesmc.twitter.lsh.index;

import java.util.concurrent.atomic.AtomicInteger;

import gnu.trove.map.hash.TObjectIntHashMap;

public class Index {
	private final TObjectIntHashMap<String> index = new TObjectIntHashMap<String>();
	private final AtomicInteger threadCounter = new AtomicInteger(0);
	
	public Index() {	
	}
	
	public int getThreadCount() {
		return this.threadCounter.intValue();
	}
	
	public int incrementThread() {
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
