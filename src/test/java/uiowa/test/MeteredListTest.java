package uiowa.test;

import static uiowa.test.MeteredList.MS_1_HR;
import static uiowa.test.MeteredList.MS_1_MIN;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MeteredListTest extends Assert {

	public static final long MS_1_SEC = 1000;	
	public static final long MS_2_SEC = MS_1_SEC *2;
	public static final long MS_5_SEC = MS_1_SEC * 5;
	public static final long MS_10_SEC = MS_1_SEC * 10;
	public static final long MS_10_MIN = MS_1_MIN * 10;
	public static final long MS_30_MIN = MS_1_MIN * 30;
	
	MeteredList list;
	
	@BeforeMethod
	public void setUp() {
		list = new MeteredList();			
	}
	
	@Test
	public void testBasicFunctionality() {
		list.mark();
		list.mark();
		list.mark();
		assertEquals(list.getTotalCount(), 3);
		assertEquals(list.getCurrentCount(), 3);		
	}

	@Test
	public void testEventCounts() {
		long time = System.currentTimeMillis();

		// events must be marked oldest to newest since new ones are added to the front of the list
		
		// older than 1 hour, so should not be counted
		list.secondaryMark(time - (MS_1_HR + MS_10_SEC)); 
	
		// two in the past hour
		list.secondaryMark(time - (MS_30_MIN + MS_5_SEC)); 
		list.secondaryMark(time - (MS_30_MIN + MS_1_SEC));

		// and three in the past 15 minutes
		list.secondaryMark(time - (MS_10_MIN + MS_10_SEC)); 
		list.secondaryMark(time - (MS_10_MIN + MS_5_SEC));
		list.secondaryMark(time - (MS_10_MIN + MS_1_SEC));
		
		// and four in the past minute
		list.secondaryMark(time - MS_10_SEC);
		list.secondaryMark(time - MS_5_SEC);
		list.secondaryMark(time - MS_2_SEC);
		list.secondaryMark(time - MS_1_SEC);
		
		assertEquals(list.getTotalCount(), 10);
		assertEquals(list.getCountPastMinute(), 4);
		assertEquals(list.getCountPast15Minutes(), 7);
		assertEquals(list.getCountPastHour(), 9);
	}

	@Test
	public void testEventsPerMinuteStreams() {
		long time = System.currentTimeMillis();
		// events must be marked oldest to newest since new ones are added to the front of the list
		for (int hours = 23; hours > -1; hours--) {  // mark events for the past 24 hours (23-0)
			for (int minutes = 59; minutes > -1; minutes--) {  // mark events for every minute (59-0)
				for (int marks = 0; marks < minutes; marks++) {  // mark a number of events equal to the minute # (59-0)
					list.secondaryMark(time - (hours * MS_1_HR + minutes * MS_1_MIN + MS_1_SEC));			
				}
			}
		}
		
		List<Integer> buckets = list.getEventsPerMinutePastHour();
		checkBuckets(buckets, 1);

		buckets = list.getEventsPerMinutePast4Hours();
		checkBuckets(buckets, 4);
		
		buckets = list.getEventsPerMinutePast24Hours();
		checkBuckets(buckets, 24);
	}
	
	public void checkBuckets(List<Integer> buckets, int hours) {
		assertEquals(buckets.size(), hours * 60);
		for (int h = 0; h < hours; h++) {
			for (int i = 0; i < 60; i++) {
				assertEquals(buckets.get(i), Integer.valueOf(i)); // events per minute should equal minute # (0-59)
			}
		}	
	}
}
