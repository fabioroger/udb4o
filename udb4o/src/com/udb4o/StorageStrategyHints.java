package com.udb4o;

public interface StorageStrategyHints {

	/**
	 * <p>
	 * Hints the underlying storage on how long this slot will live before its
	 * removed.
	 * <p>
	 * The storage is not expected to remove the object automatically after the
	 * life expectancy is reached.
	 * 
	 * @param timeInMillis
	 *            how long the object is expected to live, ranging from 0 to
	 *            {@link Long#MAX_VALUE}.
	 */
	void lifeExpectancy(long timeInMillis);

	/**
	 * <p>
	 * Instructs the underlying storage that this object may be garbage
	 * collected after the wall clock time supplied as argument.
	 * <p>
	 * After the expiration date, references to this slot may point to
	 * <code>null</code>.
	 * <p>
	 * Note that implementing this feature is optional for the container.
	 * 
	 * @param timeInMillis
	 *            wall clock time references to this slot may become null.
	 */
	void expirationDate(long timeInMillis);

	/**
	 * <p>
	 * Hints the underlying container on whats the expected frequency this slot
	 * will be read.
	 * <p>
	 * 
	 * @param intervalInMillis
	 */
	void expectedReadFrequency(long intervalInMillis);

	/**
	 * <p>
	 * Hints the underlying container on whats the expected frequency this slot
	 * will be updated.
	 * <p>
	 * 
	 * @param intervalInMillis
	 */
	void expectedUpdateFrequency(long intervalInMillis);
	
	/**
	 * <p>
	 * Slots hinted as not reliable may disappear from the underlying storage
	 * anytime.
	 * <p>
	 * The default for all storages should be true.
	 * 
	 * @param reliable
	 *            whether or not this slot is expected to be reliable.
	 */
	void reliable(boolean reliable);

	/**
	 * <p>
	 * Tell the underlying storage what is the expected size for this slot.
	 * <p>
	 * This may help the container pre-allocate buffers.
	 * 
	 * @param size
	 *            expected size in bytes.
	 */
	void expectedSize(int size);
}
