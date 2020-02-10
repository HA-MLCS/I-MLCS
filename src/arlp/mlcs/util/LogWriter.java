package arlp.mlcs.util;

import java.io.*;
import java.util.Properties;

public class LogWriter {

    // configuration files
    public static final String LOG_CONFIGFILE_NAME = "log.properties";
    public static final String LOGFILE_TAG_NAME = "logfile";


    private final String DEFAULT_LOG_FILE_NAME = "./logtext.txt";
    // the only instance of the class
    private static LogWriter logWriter;
    // file output stream
    private static PrintWriter writer;
    // file name
    private String logFileName;


    private LogWriter() {
        this.init();
    }

    private LogWriter(String fileName) {
        this.logFileName = fileName;
        this.init();
    }


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
     * initialize LogWriter
     *
     * @throws LogException
     */
    private void init() {

        if (this.logFileName == null) {
            this.logFileName = this.getLogFileNameFromConfigFile();

            if (this.logFileName == null) {
                this.logFileName = DEFAULT_LOG_FILE_NAME;
            }
        }
        File logFile = new File(this.logFileName);
        try {

            writer = new PrintWriter(new FileWriter(logFile, true), true);
            System.out.println("location of log file：" + logFile.getAbsolutePath());
        } catch (IOException ex) {
            String errmsg = "can not open log file:" + logFile.getAbsolutePath();
            System.out.println(errmsg);

        }
    }

    /**
     * get log file name from log file
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
                System.out.println("can not open: log.properties");
            }
        } catch (IOException ex) {
            System.err.println("can not open : log.properties");
        }
        return null;
    }

    // close LogWriter
    public static void close() {
        logWriter = null;
        if (writer != null) {
            writer.close();
        }
    }
}
