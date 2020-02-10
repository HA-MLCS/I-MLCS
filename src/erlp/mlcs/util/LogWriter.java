package erlp.mlcs.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Properties;

public class LogWriter {
	// Log configuration file
	public static final String LOG_CONFIGFILE_NAME = "log.properties";
	// Label of log file name in configuration file
	public static final String LOGFILE_TAG_NAME = "logfile";

	// default log file path and file name
	private final String DEFAULT_LOG_FILE_NAME = "./logtext.txt";
	// the only instance of this class
	private static LogWriter logWriter;
	// File Output Stream
	private static PrintWriter writer;
	// Log File Name
	private String logFileName;


	private LogWriter() {
		this.init();
	}

	private LogWriter(String fileName) {
		this.logFileName = fileName;
		this.init();
	}

	/**
	 * Get the unique instance of LogWriter
	 *
	 * 
	 */
	public synchronized static LogWriter getLogWriter() {
		if (logWriter == null) {
			logWriter = new LogWriter();
		}
		return logWriter;
	}

	public synchronized static LogWriter getLogWriter(String logFileName) {
		logWriter = new LogWriter(logFileName);
		return logWriter;
	}


	public synchronized static void log(String logMsg) {
		writer.println(logMsg);
		System.out.println(new java.util.Date() + ": " + logMsg);
	}


	public synchronized void log(Exception ex) {
		writer.println(new java.util.Date() + ": ");
		ex.printStackTrace(writer);
	}

	/**
	 * Initialize LogWriter
	 *
	 * 
	 */
	private void init() {
		// If the user does not specify the log file name in the parameter, it is retrieved from the configuration file
		if (this.logFileName == null) {
			this.logFileName = this.getLogFileNameFromConfigFile();
			
			if (this.logFileName == null) {
				this.logFileName = DEFAULT_LOG_FILE_NAME;
			}
		}
		File logFile = new File(this.logFileName);
		try {

			writer = new PrintWriter(new FileWriter(logFile, true), true);
			System.out.println("Location of log files：" + logFile.getAbsolutePath());
		} catch (IOException ex) {
			String errmsg = "unable to open log file:" + logFile.getAbsolutePath();
			System.out.println(errmsg);

		}
	}

	/**
	 * Get the log file name from the configuration file
	 *
	 * @return
	 */
	private String getLogFileNameFromConfigFile() {
		try {
			Properties pro = new Properties();
			
			InputStream fin = getClass().getResourceAsStream(
					LOG_CONFIGFILE_NAME);
			if (fin != null) {
				pro.load(fin);
				fin.close();
				return pro.getProperty(LOGFILE_TAG_NAME);
			} else {
				System.out.println("Unable to open property profile: log.properties");
			}
		} catch (IOException ex) {
			System.err.println("Unable to open property profile: log.properties");
		}
		return null;
	}

	
	public static void close() {
		logWriter = null;
		if (writer != null) {
			writer.close();
		}
	}
}
