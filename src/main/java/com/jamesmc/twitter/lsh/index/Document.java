package com.jamesmc.twitter.lsh.index;

import gnu.trove.map.hash.TObjectByteHashMap;

public class Document {

	// TObjectIntHashMap is a mapping between some object and an Integer. It's
	// like a HashMap<String, Integer> but much more efficient
	protected TObjectByteHashMap<String> tokens = null;
	protected long id = -1;

	protected Document() {
	}

	/**
	 * Create a copy of the tokens from this document and return them
	 * 
	 * @return A copy of this documents tokens
	 */
	public TObjectByteHashMap<String> getTokens() {
		return new TObjectByteHashMap<String>(tokens);
	}


	public long getId() {
		return this.id;
	}
	
	/**
	 * Calculate the cosine of 2 given Documents
	 * 
	 * @return the cosine value of both documents
	 */
	public double cosine(Document q) {
		int top = 0;
		for (String t : this.tokens.keySet()) {
			Byte t1 = this.tokens.get(t);
			Byte t2 = q.tokens.get(t);

			if (t1 != null && t2 != null)
				top += t1 * t2;
		}

		int b1 = 0;
		for (int i : this.tokens.values())
			b1 += i * i;

		int b2 = 0;
		for (int i : q.tokens.values())
			b2 += i * i;

		double bottom = Math.sqrt(b1) * Math.sqrt(b2);

		return (double) (top / bottom);
	}

}
