package com.jamesmc.twitter.lsh.index;

import gnu.trove.list.array.TByteArrayList;
import gnu.trove.map.hash.TObjectByteHashMap;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Random;

import com.jamesmc.twitter.lsh.utils.Config;

public class LocallitySensitiveHashTable {

	private final Index index;
	private final int bitsPerKey;
	private final Hyperplane plane[];
	private final LinkedList<Tweet> buckets[];

	private static final Random rand = new Random();

	/**
	 * Create a new Locality Sensitive HashTable
	 * 
	 * @param index
	 *            The inverted index to use
	 * @param bitsPerKey
	 *            The number of bits to use for the key. The more bits used the
	 *            more hyperplanes we need to generate.
	 */
	@SuppressWarnings("unchecked")
	public LocallitySensitiveHashTable(Index index, int bitsPerKey) {
		int numBuckets = (int) Math.pow(2, bitsPerKey);
		this.index = index;
		this.bitsPerKey = bitsPerKey;
		this.plane = new Hyperplane[bitsPerKey];
		this.buckets = new LinkedList[numBuckets];

		for (int i = 0; i < this.bitsPerKey; i++)
			this.plane[i] = new Hyperplane(10000);

		for (int i = 0; i < Math.pow(2, bitsPerKey); i++)
			this.buckets[i] = new LinkedList<Tweet>();
	}

	private class Hyperplane {

		// The indices used by the hyperplane.
		private TByteArrayList indices;

		/**
		 * Generate a new random hyperplane
		 * 
		 * @param size
		 *            The size of the hyperplane
		 */
		public Hyperplane(int size) {
			this.indices = new TByteArrayList(size);

			for (int i = 0; i < size; i++) {
				byte b = (byte) Math.floor(rand.nextGaussian());
				if (b < 0)
					b = 0;
				this.indices.add(b);
			}
		}

		/**
		 * Double the size of the hyperplane. This is useful as the size of the
		 * Index grows.
		 * 
		 * We don't need to worry about this effect any previous hashes as any
		 * hashes using the newly created values would have given 0 and have had
		 * no effect on the results.
		 */
		private void growHyperplane() {
			int currentSize = this.indices.size();
			for (int i = this.indices.size() - 1; i < (currentSize * 2); i++) {
				byte b = (byte) Math.floor(rand.nextGaussian());
				if (b < 0)
					b = 0;
				this.indices.add(b);
			}
		}

		/**
		 * Generate the hash for a set of Term Frequencies
		 * 
		 * @param tObjectByteHashMap
		 *            A map of terms to their frequencies
		 * @return The hash of the map and the hyperplane. The hash is given as
		 *         the dot product of the map and the hyperplane. If the dp is >
		 *         1 then it is 1, otherwise it is 0.
		 */
		public short hash(TObjectByteHashMap<String> tObjectByteHashMap) {
			int sum = 0;
			for (String k : tObjectByteHashMap.keySet()) {
				int i = index.get(k);

				if ((this.indices.size() - 1) < i)
					this.growHyperplane();

				sum += tObjectByteHashMap.get(k) * this.indices.get(i);
			}

			if (sum > 0)
				return 1;
			else
				return 0;
		}

	}

	/**
	 * Add a Document to the table
	 * 
	 * @param d
	 *            The document to add
	 * @return The index where the document was added
	 */
	public void add(Tweet d) {
		int index = 0;

		// Calculate the individual hash values for each plane and convert the
		// binary output into decimal
		// TODO: Convert this to using bit shifts
		for (int i = 0; i < this.bitsPerKey; i++) {
			int h = this.plane[i].hash(d.getTokens());
			if (h > 0)
				index += Math.pow(2, i);
		}

		buckets[index].add(d);

		// If the size of the LSHTable has grown too large then start removing
		// the old documents for the index we're
		// about to add to.
		if (buckets[index].size() > Config.maxBucketSize)
			buckets[index].remove();
	}

	/**
	 * Get all the Documents from a given index
	 * 
	 * @param index
	 *            The index to get the Documents from
	 * @return All of the documents at the given index.
	 */
	public Collection<Tweet> get(int index) {
		return buckets[index];
	}

	/**
	 * Find any collisions with the given Document in the table and return them.
	 * 
	 * @param d
	 *            The Document to look for collisions of
	 * @return Documents which collide with the given Document
	 */
	public Collection<Tweet> getCollisions(Tweet d) {
		int index = 0;
		for (int i = 0; i < this.bitsPerKey; i++) {
			int h = this.plane[i].hash(d.getTokens());
			if (h > 0)
				index += Math.pow(2, i);
		}

		LinkedList<Tweet> coll = new LinkedList<Tweet>(buckets[index]);
		coll.remove(d);
		return coll;
	}

}
