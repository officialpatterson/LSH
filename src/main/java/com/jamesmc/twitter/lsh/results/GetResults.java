package com.jamesmc.twitter.lsh.results;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.jamesmc.twitter.lsh.utils.Config;

public class GetResults implements Runnable {

	private Connection connection;
	private BufferedWriter out;

	public GetResults() {
		try {
			Class.forName("org.postgresql.Driver");
			this.connection = DriverManager.getConnection(Config.dbhost, Config.dbuser, Config.dbpass);

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			connection = null;
		} catch (SQLException e) {
			e.printStackTrace();
		}

		try {
			FileWriter fstream = new FileWriter(Config.eventsOutput);
			this.out = new BufferedWriter(fstream);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		int i = 0;
		while (true) {
			try {
				while (true) {
					synchronized (this) {
						try {
							this.wait();
						} catch (InterruptedException e) {
							System.out.println("Interrupt!");
							break;
						}
					}
				}
				System.out.println("Events for hour " + i);
				out.write("-- Hour " + i + "\n");
				Statement statement = connection.createStatement();
				ResultSet results = statement.executeQuery("SELECT thread, number_users, entropy "
						+ "FROM (SELECT thread, number_users, entropy(thread, maxid, '" + Config.runId + "') as entropy, maxid "
						+ "FROM (SELECT max(id) as maxid, thread, count(user_id) as number_users FROM tweets_from_hour(" + i + ", '" + Config.runId + "') "
						+ "GROUP BY thread ORDER BY number_users DESC LIMIT 100) threads) as events WHERE entropy >= 3.5 ORDER BY number_users DESC");

				while (results.next()) {
					out.write(results.getLong(1) + "\t" + results.getLong(2) + "\t" + results.getDouble(3) + "\n");
				}
				out.flush();
				System.out.println("Flush");
				i++;
			} catch (SQLException e1) {
				e1.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String args[]) {

		for (int i = 0; i < 4; i++) {
			final int j = 170 * i;
			new Thread(new Runnable() {

				@Override
				public void run() {
					Connection connection = null;
					BufferedWriter out = null;

					try {
						Class.forName("org.postgresql.Driver");

						connection = DriverManager.getConnection(Config.dbhost, Config.dbuser, Config.dbpass);

					} catch (ClassNotFoundException e) {
						e.printStackTrace();
						connection = null;
					} catch (SQLException e) {
						e.printStackTrace();
					}

					try {
						FileWriter fstream = new FileWriter("lsh_events_final_4.output" + j);
						out = new BufferedWriter(fstream);
					} catch (IOException e) {
						e.printStackTrace();
					}

					int i = j;
					while (i < (j + 170)) {
						try {
							System.out.println("Events for hour " + i);
							out.write("-- Hour " + i + "\n");
							Statement statement = connection.createStatement();
							ResultSet results = statement
									.executeQuery("SELECT thread, number_users, entropy "
											+ "FROM (SELECT thread, number_users, entropy(thread, maxid, '"
											+ Config.runId
											+ "') as entropy, maxid "
											+ "FROM (SELECT max(id) as maxid, thread, count(DISTINCT user_id) as number_users FROM tweets_from_hour("
											+ i
											+ ", '"
											+ Config.runId
											+ "') "
											+ "GROUP BY thread HAVING count(DISTINCT user_id) >= 15 ORDER BY number_users DESC LIMIT 100) threads) as events WHERE entropy >= 3.5 ORDER BY number_users DESC");

							while (results.next()) {
								out.write(results.getLong(1) + "\t" + results.getLong(2) + "\t" + results.getDouble(3) + "\n");
							}
							out.flush();
							System.out.println("Flush from thread" + j);
							i++;
						} catch (SQLException e1) {
							e1.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}).start();
		}
	}
}
