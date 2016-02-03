package com.jamesmc.twitter.lsh.parser;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashSet;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import com.jamesmc.twitter.lsh.utils.Config;

public class StopWordRemover {

	public HashSet<String> stopWords = new HashSet<String>();

	private FileInputStream fs;
	private BufferedReader is;

	public StopWordRemover() {
		try {
			fs = new FileInputStream(Config.projectPath + "stopwords.txt");
			is = new BufferedReader(new InputStreamReader(fs));
			while(is.ready()) {
				String word = is.readLine();
					word = word.replaceAll("[^\\w]+", "");
				
				if(word.length() == 0)
					continue;
				
				if(Config.doStem) {
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
				
				stopWords.add(word);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isStopWord(String word) {
		return stopWords.contains(word.toLowerCase());
	}

}
