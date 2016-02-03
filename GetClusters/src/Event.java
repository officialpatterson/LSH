public class Event {

	public final long time;
	public final long numUsers;
	public final long id;
	public final double entropy;

	public Event(long time, long numUsers, long id, double entropy) {
		this.time = time;
		this.numUsers = numUsers;
		this.id = id;
		this.entropy = entropy;
	}

	public long getTime() {
		return time;
	}

	public long getNumUsers() {
		return numUsers;
	}

	public long getId() {
		return id;
	}

	public double getEntropy() {
		return entropy;
	}

	public String toString() {
		return String.format("(%d) ID: %-10d  Users: %-10d E: %-1.5f", time, id, numUsers, entropy);
	}
	
}
