package com.jamesmc.twitter.lsh.parser;

import gnu.trove.map.hash.TObjectByteHashMap;

public abstract class Tokenizer {

	/**
	 * Take a block of text and return the tokens
	 * @param document The string of text t tokenzie
	 * @return The tokens and their count
	 */
	public abstract TObjectByteHashMap<String> tokenzie(final String document);

}
