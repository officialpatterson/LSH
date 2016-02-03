package com.jamesmc.twitter.lsh.index;

public class AdditionalInformation {

	private final long userId;
	private final long creationTime;
	private final String originalText;
	
	public AdditionalInformation(long userId, long creationTime, String originalText) {
		this.userId = userId;
		this.creationTime = creationTime;
		this.originalText = originalText;
	}

	public long getUserId() {
		return userId;
	}

	public long getCreationTime() {
		return creationTime;
	}

	public String getOriginalText() {
		return originalText;
	}
	
}
