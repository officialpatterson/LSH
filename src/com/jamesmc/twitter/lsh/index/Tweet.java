package com.jamesmc.twitter.lsh.index;


import com.jamesmc.twitter.lsh.index.AdditionalInformation;
import com.jamesmc.twitter.lsh.index.Document;
import com.jamesmc.twitter.lsh.utils.Config;

public class Tweet extends Document {

	private int threadId = -1;

	private AdditionalInformation additionalInfo;

	public Tweet(final long id, final String document, long userId, long creationTime) {
		super();

		this.tokens = Config.tokenzier.tokenzie(document);
		this.id = id;
		this.additionalInfo = new AdditionalInformation(userId, creationTime, document);
	}

	public int getThread() {
		return this.threadId;
	}

	public void setThread(int threadId) {
		this.threadId = threadId;
	}

	public String toString() {
		String out = id + " ";
		for (Object s : tokens.keys()) {
			out += s + " ";
		}
		return out;
	}

	public AdditionalInformation getAdditionalInformation() {
		return this.additionalInfo;
	}

	public AdditionalInformation removeAdditionalInformation() {
		AdditionalInformation a = this.additionalInfo;
		this.additionalInfo = null;
		return a;
	}

	public String getTokensAsString() {
		String out = "";
		for (String s : this.getTokens().keySet()) {
			for (int i = 0; i < this.getTokens().get(s); i++)
				out += s + " ";
		}
		return out.substring(0, out.length()-1);
	}

}
