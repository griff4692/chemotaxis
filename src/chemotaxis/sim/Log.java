package chemotaxis.sim;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Log {

	private static FileWriter fileWriter;
	private static boolean isVerbose = false;
	private static boolean shouldLog = false;
	
	public static void setLogFile(String filename) {
		try {
			fileWriter = new FileWriter(filename, false);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	public static void assignVerbosityStatus(boolean isVerbose) {
		Log.isVerbose = isVerbose;
	}

	public static boolean getVerbosityStatus() {
		return Log.isVerbose;
	}
	
	public static void assignLoggingStatus(boolean shouldLog) {
		Log.shouldLog = shouldLog;
	}
	
	public static boolean getLoggingStatus() {
		return Log.shouldLog;
	}
	
	public static void writeToVerboseLogFile(String content) {
		if(isVerbose)
			writeToLogFile(content);
	}
	
	public static void writeToLogFile(String content) {
		if(!shouldLog)
			return;
		
		DateFormat dateFormat = new SimpleDateFormat("[yyyy/MM/dd HH:mm:ss.SSS] ");
		Date date = new Date();
		
		String datedContent = dateFormat.format(date) + content + "\n";
		System.out.println(datedContent);

		if(fileWriter == null)
			return;

		try {
			fileWriter.append(datedContent);
		} catch(IOException e) {
			e.printStackTrace();
		}		
	}

	public static void writeResults(String content) {
		try {
			fileWriter.write(content + "\n");
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	public static void closeLogFile() {
		if(fileWriter == null)
			return;

		try {
			fileWriter.close();
		} catch(IOException e) {
			e.printStackTrace();
		}

		fileWriter = null;
	}
}