package com.jamesmc.twitter.lsh.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.jamesmc.twitter.lsh.index.Tweet;
import com.jamesmc.twitter.lsh.results.GetResults;

public class Database {

	private Connection connection;
	private long count = 0;
	private final Thread getEventsThread = new Thread(new GetResults());
	private boolean getEvents = false;
	private long lastTime = 0;

	public Database() {
		try {
			Class.forName("org.postgresql.Driver");
			this.connection = DriverManager.getConnection(Config.dbhost, Config.dbuser, Config.dbpass);
			this.connection.setAutoCommit(false);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			this.connection = null;
		} catch (SQLException e) {
			this.connection = null;
			e.printStackTrace();
		}
		getEventsThread.start();
	}

	public void add(Tweet comaprisonTweet, boolean removeAdditionalData) {
		try {
			PreparedStatement prest = connection
					.prepareStatement("INSERT INTO tweet (id, text, user_id, created_at, run, thread, tokens) VALUES (?, ?, ?, ?, ?, ?, ?)");
			prest.setLong(1, comaprisonTweet.getId());
			prest.setString(2, comaprisonTweet.getAdditionalInformation().getOriginalText());
			prest.setLong(3, comaprisonTweet.getAdditionalInformation().getUserId());
			prest.setLong(4, comaprisonTweet.getAdditionalInformation().getCreationTime());
			prest.setString(5, Config.runId);
			prest.setLong(6, comaprisonTweet.getThread());
			prest.setString(7, comaprisonTweet.getTokensAsString());
			prest.execute();

			if (lastTime == 0)
				lastTime = comaprisonTweet.getAdditionalInformation().getCreationTime();

			if ((comaprisonTweet.getAdditionalInformation().getCreationTime() - lastTime) >= 3600000) {
				getEvents = true;
				System.out.println("Should show events soon..." + comaprisonTweet.getAdditionalInformation().getCreationTime());
				lastTime = comaprisonTweet.getAdditionalInformation().getCreationTime();
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		count++;

		if (count % 100 == 0) {
			try {
				connection.commit();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			if (getEvents == true) {
				getEvents = false;
				getEventsThread.interrupt();
			}
		}

		if (removeAdditionalData)
			comaprisonTweet.removeAdditionalInformation();

	}

}
