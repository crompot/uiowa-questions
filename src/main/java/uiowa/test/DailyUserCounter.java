package uiowa.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

/**
 * Provides the number of unique user logins for a list of dates by reading through all of the files in a logs directory and its subdirectories.
 * NOTE: For brevity, the directory "/logs" is assumed to exist, and valid input dates are presumed, i.e. real world validation is not being performed.
 * @author Chris Rompot
 */
public class DailyUserCounter {

	public static File LOG_DIRECTORY = new File("/logs");
	public static final Pattern USER_ENTRY_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2},\\d{3} ((FATAL)|(ERROR)|(WARN)|(INFO)|(DEBUG)|(TRACE)) +\\[(\\w+)@");	
	
	/**
	 * Displays the number of unique user logins for a list of dates in date input order.
	 * @param args dates (format CCYY-MM-DD) to display the number of unique users for
	 */
	public static void main(String[] args) {
		
		ArrayList<String> dates = new ArrayList<String>();
		for (String arg : args) {
			dates.add(arg);
		}
		Map<String, Integer> dateCounts = determineDailyUserCounts(LOG_DIRECTORY, dates);
		Set<String> dateSet = dateCounts.keySet();
		System.out.println("Count of Unique Users");
		System.out.println("----------------------------");
		for (String date : dateSet) {
			System.out.println(date + ": " + dateCounts.get(date));		
		}
	}
	
	/**
	 * Determines the number of unique users for a list of dates.
	 * @param logDirectory root log directory
	 * @param dates list of dates in CCYY-MM-DD format
	 * @return map keyed on date having the count of unique users as the values
	 */
	static Map<String, Integer> determineDailyUserCounts(File logDirectory, List<String> dates) {
		
		LinkedHashMap<String, Integer> dailyCounts = new LinkedHashMap<String, Integer>();
		for (String date : dates) {
			int count = determineDaysCount(logDirectory, date);
			dailyCounts.put(date, count);
		}
		return dailyCounts;
	}
	
	/**
	 * Determines the number of unique users for a particular day across all files in the logs directory and subdirectories.
	 * @param logDirectory root log directory
	 * @param date date in CCYY-MM-DD format
	 * @return day's count of unique users
	 */
	static int determineDaysCount(File logDirectory, String date) {
		
		HashSet<String> userSet = new HashSet<String>();
		Pattern logFileNamePattern = Pattern.compile("log." + date + "$");		
		Iterator<File> fileIterator = FileUtils.iterateFiles(logDirectory, TrueFileFilter.TRUE, TrueFileFilter.TRUE);
		while (fileIterator.hasNext()) {
			File file = fileIterator.next();
			Matcher logFileNameMatcher = logFileNamePattern.matcher(file.getName());
			if (logFileNameMatcher.find()) {
				try {
					addFileUsers(file, userSet);
				} catch (IOException e) {
					return -1;
				}
			}
		}
		return userSet.size();
	}
	
	/**
	 * Adds the users from a specific log file to a day's set of users.
	 * @param file log file
	 * @param userSet user identities
	 * @throws IOException
	 */
	static void addFileUsers(File file, HashSet<String> userSet) throws IOException {
	
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line;
		while ((line = reader.readLine()) != null) {
			Matcher matcher = USER_ENTRY_PATTERN.matcher(line);
			if (matcher.find()) {
				String user = matcher.group(matcher.groupCount());  // user identity is in the last regex capture group
				userSet.add(user);
			}
		}
	}
	
	/**
	 * This method is only intended for use during unit testing.
	 * @param logDirectory name of root log directory
	 */
	static void setLogDirectory(String logDirectory) {
		LOG_DIRECTORY = new File(logDirectory);
	}
}
