import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class GetClusters {

	public static void main(String agrs[]) {

		ArrayList<LinkedList<Event>> events = new ArrayList<LinkedList<Event>>(1024);
		Set<Long> eventSet = new HashSet<Long>();

		// Load the URL to Vertical mappings
		try {
			InputStream urlfs = new FileInputStream("full-events.txt");
			BufferedReader reader = new BufferedReader(new InputStreamReader(urlfs));
			int hour = 0;
			int count = 0;
			int totalTweets = 0;
			while (reader.ready()) {
				String line = reader.readLine();
				if (line.startsWith("-- Hour ")) {
					System.out.println("-1);");
					hour = new Integer(line.replace("-- Hour ", "").trim());
					events.add(hour, new LinkedList<Event>());
					System.out.println("\n-- Hour " + hour);
					System.out.print("SELECT * FROM tweets_from_hour(" + hour + ", 'LSH-final-3') WHERE thread IN (");
				} else {
					String[] parts = line.split("\t");
					Event e = new Event(hour, new Long(parts[1]), new Long(parts[0]), new Double(parts[2]));
					
					if(eventSet.contains(e.getId()))
						continue;
					
					if(e.getEntropy() < 3.5 || e.getEntropy() > 4.25)
						continue;
					
					eventSet.add(e.getId());
					
					totalTweets += e.getNumUsers();
					
					events.get(hour).add(e);
					if (e.getNumUsers() > 30) {
						System.out.print(e.getId() + ", ");
						//System.out.println(e.getId() + "\t" + e.getNumUsers() + "\t" + String.format("%.2f", e.getEntropy()));
						count++;
					}
				}
			}
			System.out.println("");
			reader.close();
			
			
			System.out.println("Number of clusters: " + count);
			System.out.println(totalTweets / (double) count);
//			
//			System.out.print("(");
//			long lastE = -1;
//			for(Long e : eventSet) {
//				if(lastE != e)
//				System.out.print(e + ", ");
//			}
//			System.out.println(")");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
