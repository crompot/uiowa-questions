package uiowa.test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This class is used to record the times at which events occur and the total number of events marked. It has
 * various methods to report back on these event times.
 * @author Chris Rompot
 */
public class MeteredList {

	public static final long MS_1_MIN = 60000;
	public static final long MS_15_MIN = MS_1_MIN * 15;
	public static final long MS_1_HR = MS_1_MIN * 60;
	public static final long MS_4_HR = MS_1_HR * 4;
	public static final long MS_24_HR = MS_1_HR * 24;
	
	private int totalCount;
	private LinkedList<Long> events;

	private Timer timer;
	
	public MeteredList() {
		events = new LinkedList<Long>();
	}
	
	/**
	 * Alternate constructor that can be used to enable the purging of day old events on an hourly basis.
	 * @param purgeDayOld whether day old events should be purged off on an hourly basis
	 */
	public MeteredList(boolean purgeDayOld) {
		this();
		if (purgeDayOld) {
			timer = new Timer("purgeDayOldEvents", true);
			timer.schedule(new PurgeDayOldEvents(), System.currentTimeMillis() + MS_24_HR, MS_1_HR);
		}
	}

	/**
	 * Adds an event to the front of the list and increments the total count.
	 */
	public void mark() {
		totalCount++;
		events.addFirst(System.currentTimeMillis());
	}

	/**
	 * Used for testing purposes; adds an event to the list with the specified time and increments the total count.
	 * @param time time in milliseconds
	 */
	public void secondaryMark(long time) {
		totalCount++;
		events.addFirst(time);
	}

	/**
	 * Return the number of events that occurred in the past minute.
	 * @return count of events in the past minute
	 */
	public int getCountPastMinute() {
		return getCountPastPeriod(MS_1_MIN);
	}
	
	/**
	 * Return the number of events that occurred in the past 15 minutes.
	 * @return count of events in the past 15 minutes
	 */
	public int getCountPast15Minutes() {
		return getCountPastPeriod(MS_15_MIN);
	}
	
	/**
	 * Return the number of events that occurred in the past hour.
	 * @return count of events in the past hour
	 */
	public int getCountPastHour() {
		return getCountPastPeriod(MS_1_HR);
	}
	
	/**
	 * Returns the number of events that occurred in the specified past period.
	 * @param period past period of time in milliseconds
	 * @return count of events in the specified past period
	 */
	private int getCountPastPeriod(long period) {
		int count = 0;
		long cutoffTime = System.currentTimeMillis() - period;
		ListIterator<Long> iterator = events.listIterator();
		while (iterator.hasNext()) {
			Long time = iterator.next();
			if (time > cutoffTime) {
				count++;
			} else {
				break;
			}
		}
		return count;
	}

	/**
	 * Returns the number of events per minute for the past hour in a list having a size of 60 with element 0 containing
	 * the number of events from 0 to 1 minute ago, element 1 containing the number of events from 1 to 2 minutes ago, etc.
	 * @return list containing the number of events per minute for the past 60 minutes
	 */
	public List<Integer> getEventsPerMinutePastHour() {
		return getEventsPerMinutePastHours(1);
	}

	/**
	 * Returns the number of events per minute for the past 4 hours in a list having a size of 240 with element 0 containing
	 * the number of events from 0 to 1 minute ago, element 1 containing the number of events from 1 to 2 minutes ago, etc.
	 * @return list containing the number of events per minute for the past 240 minutes
	 */	
	public List<Integer> getEventsPerMinutePast4Hours() {
		return getEventsPerMinutePastHours(4);
	}

	/**
	 * Returns the number of events per minute for the past 24 hours in a list having a size of 1440 with element 0 containing
	 * the number of events from 0 to 1 minute ago, element 1 containing the number of events from 1 to 2 minutes ago, etc.
	 * @return list containing the number of events per minute for the past 1440 minutes
	 */	
	public List<Integer> getEventsPerMinutePast24Hours() {
		return getEventsPerMinutePastHours(24);
	}
	
	/**
	 * Returns the number of events per minute for the past x hours in a list having a size of x*60 with element 0 containing
	 * the number of events from 0 to 1 minute ago, element 1 containing the number of events from 1 to 2 minutes ago, etc.
	 * @return list containing the number of events per minute for the past x*60 minutes
	 */	
	public List<Integer> getEventsPerMinutePastHours(int hours) {
		int minutes = hours * 60;
		ArrayList<Integer> buckets = new ArrayList<Integer>(minutes);
		// initialize the contents of all minute buckets to zero
		for (int i = 0; i < minutes; i++) {
			buckets.add(i, 0);
		}
		long startTime = System.currentTimeMillis();
		long cutoffTime = startTime - minutes * MS_1_MIN;
		ListIterator<Long> iterator = events.listIterator();
		while (iterator.hasNext()) {
			Long time = iterator.next();
			if (time > cutoffTime) {
				int minute = (int) ((startTime - time) / MS_1_MIN);  // any fractional part of result will be truncated
				buckets.set(minute, buckets.get(minute) + 1);  // add 1 to event count for the appropriate minute bucket
			} else {
				break;
			}
		}
		return buckets;
	}
	
	private class PurgeDayOldEvents extends TimerTask {
		public void run() {
			long dayOld = System.currentTimeMillis() - MS_24_HR;
			while (true) {
				long event = events.peekLast();
				if (event < dayOld) {
					events.removeLast();
				} else {
					return;
				}
			}
		}
	}
	
	/**
	 * @return current number of event times held in the metered list, which may differ from the total if day old event times are being purged
	 */
	public int getCurrentCount() {
		return events.size();
	}

	/**
	 * @return total number of events marked since the metered list was created
	 */
	public int getTotalCount() {
		return totalCount;
	}
}
