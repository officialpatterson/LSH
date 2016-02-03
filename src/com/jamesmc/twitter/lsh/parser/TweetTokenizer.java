package com.jamesmc.twitter.lsh.parser;

import gnu.trove.map.hash.TObjectByteHashMap;

import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import com.jamesmc.twitter.lsh.utils.Config;

public class TweetTokenizer extends Tokenizer {

	private static StopWordRemover sw = new StopWordRemover();
	private static final Pattern rep = Pattern.compile(".*((\\w){1,2})\\1+\\1+.*");

	public TObjectByteHashMap<String> tokenzie(final String document) {
		if (document.length() == 0)
			return null;

		// Default to a very small size so we don't waste any memory
		TObjectByteHashMap<String> tokens = new TObjectByteHashMap<String>(Config.hashMapInitSize);

		String[] words = document.toLowerCase().split("[\\s,]");
		LinkedList<String> acceptedWords = new LinkedList<String>();
		for (String word : words) {
			if (word.startsWith("http://") || word.startsWith("&"))
				continue;

			for (String w : word.split("[\\.\\-\\/:_;]")) {
				acceptedWords.add(w);
			}
		}

		for (String word : acceptedWords) {
			// Remove anything that isn't a word (e.g. punctuation, symbols,
			// etc.)
			if (Config.removePuctuation)
				word = word.replaceAll("[^a-zA-Z0-9]+", "");

			Matcher matcher = rep.matcher(word);
			if (word.length() < 3 || matcher.matches())
				continue;

			if (Config.doStem) {
				TokenStream ts;
				try {
					ts = Config.lemmaguy.reusableTokenStream("lemmatize", new StringReader(word));
					ts.reset();
					ts.incrementToken();
					ts.incrementToken();
					word = ts.getAttribute(CharTermAttribute.class).toString().toLowerCase();
					ts.end();
					ts.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (Config.removeStopWords && sw.isStopWord(word) || word.length() == 0)
				continue;

			tokens.put(word, (byte) (tokens.get(word) + 1));
		}

		return tokens;
	}
}
