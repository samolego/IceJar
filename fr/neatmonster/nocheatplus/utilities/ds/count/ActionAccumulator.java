/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.neatmonster.nocheatplus.utilities.ds.count;

import fr.neatmonster.nocheatplus.utilities.StringUtil;

/**
 * This class is meant to accumulate actions similar to ActionFrequency. 
 * In contrast to ActionFrequency, this class will accumulate values grouped by number of events instead of real-time intervals.
 * @author asofold
 *
 */
public class ActionAccumulator {
    /** Event counter. */
    private final int[] counts;
    /** Value accumulation. */
    private final float[] buckets;
    /** Capacity of each bucket. */
    //
    private final int bucketCapacity;
    
    public ActionAccumulator(final int nBuckets, final int bucketCapacity) {
        this.counts = new int[nBuckets];
        this.buckets = new float[nBuckets];
        this.bucketCapacity = bucketCapacity;
    }
    
    public void add(float value) {
        // This bucket has reached its maximum capacity, shift into the next one, if present.
        if (counts[0] >= bucketCapacity) {
            shift();
        }
        // Increase event counter with each value added
        counts[0] ++;
        // Add value to accumulation.
        buckets[0] += value;
    }
    
    private void shift() {
        // Fill the other buckets.
        for (int i = buckets.length - 1; i > 0; i--) {
            counts[i] = counts[i - 1];
            buckets[i] = buckets[i - 1];
        }
        // Reset the first bucket
        counts[0] = 0;
        buckets[0] = 0;
    }

    /**
     * Get the *total* score (sum of all buckets' accumulated values)
     * @return
     */
    public float score() {
        float score = 0;
        for (int i = 0; i < buckets.length; i++) {
            score += buckets[i];
        }
        return score;
    }
    
    /**
     * Get the *total* events (sum of all buckets' events)
     * @return
     */
    public int count() {
        int count = 0;
        for (int i = 0; i < counts.length; i++) {
            count += counts[i];
        }
        return count;
    }
    
    /**
     * Reset all buckets
     * @return 
     */
    public void clear() {
        for (int i = 0; i < buckets.length; i++) {
            counts[i] = 0;
            buckets[i] = 0;
        }
    }
    
    /**
     * Get the events counted only for this bucket.
     * @return 
     */
    public int bucketCount(final int bucket) {
        return counts[bucket];
    }
    
    /**
     * Get the score only for this bucket.
     * @return 
     */
    public float bucketScore(final int bucket) {
        return buckets[bucket];
    }
    
    public int numberOfBuckets() {
        return buckets.length;
    }
    
    public int bucketCapacity() {
        return bucketCapacity;
    }
    
    /**
     * Simple display of bucket contents, no class name.
     * @return
     */
    public String toInformalString() {
        StringBuilder b = new StringBuilder(buckets.length * 10);
        b.append("|");
        for (int i = 0; i < buckets.length; i++){
            b.append(StringUtil.fdec3.format(buckets[i]) + "/" + counts[i] + "|");
        }
        return b.toString();
    }
    
}
