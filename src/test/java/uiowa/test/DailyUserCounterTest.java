package uiowa.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import uiowa.test.DailyUserCounter;

public class DailyUserCounterTest {

	@Test
	public void addFileUsers() {
		File file = new File("src/test/resources/logs/foo-app/uiowa.foo-app.log.2011-01-03");
		HashSet<String> userSet = new HashSet<String>();
		try {
			DailyUserCounter.addFileUsers(file, userSet);
		} catch (IOException e) {
			fail();
		}
		assertEquals(3, userSet.size());
		assertTrue(userSet.contains("mlepidus"));
		assertTrue(userSet.contains("mantonius"));
		assertTrue(userSet.contains("gocatavius"));
	}
	
	@Test
	public void determineDaysCountEmptyFile() {
		assertEquals(0, DailyUserCounter.determineDaysCount(new File("src/test/resources/logs"), "2010-12-31"));
	}
	
	@Test
	public void determineDaysCountSingleFile() {
		assertEquals(1, DailyUserCounter.determineDaysCount(new File("src/test/resources/logs"), "2011-01-01"));
	}
	
	@Test
	public void determineDaysCountMultipleFiles() {
		assertEquals(6, DailyUserCounter.determineDaysCount(new File("src/test/resources/logs"), "2011-01-03"));
	}
	
	@Test
	public void determineDailyUserCounts() {
		List<String> dates = new ArrayList<String>();
		dates.add("2011-01-01");
		dates.add("2011-01-03");
		Map<String, Integer> datesCounts = DailyUserCounter.determineDailyUserCounts(new File("src/test/resources/logs"), dates);
		assertEquals(2, datesCounts.size());
		assertEquals(1, ((Integer) datesCounts.get("2011-01-01")).intValue());
		assertEquals(6, ((Integer) datesCounts.get("2011-01-03")).intValue());			
	}
	
	@Test
	public void mainTest() {
		String[] dates = new String[] {"2010-12-01","2011-01-01","2011-01-02","2011-01-03","2011-01-04"};
		DailyUserCounter.setLogDirectory("src/test/resources/logs");
		DailyUserCounter.main(dates);
	}	
}
