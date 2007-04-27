package net.spy.diggwatch;

/**
 * An item that's tracked via the Updater.
 */
public class TrackedItem<T> {

	private T item=null;
	private long lastUpdate=0;

	/**
	 * Construct with a tracked item.
	 */
	public TrackedItem(T i) {
		super();
		item=i;
	}

	/**
	 * Get thet racked item.
	 */
	public T getItem() {
		return item;
	}

	/**
	 * Get the timestamp of the last update.
	 */
	public long getLastUpdate() {
		return lastUpdate;
	}

	/**
	 * Set the timestamp of the last update.
	 */
	public void setLastUpdate(long to) {
		lastUpdate=to;
	}
}
