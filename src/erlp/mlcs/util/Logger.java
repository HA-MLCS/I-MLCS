package erlp.mlcs.util;

public class Logger {
  int level = 1;

  public static Logger getLogger(Class<?> clazz) {
    String level = System.getProperty("mlcs.logger");
    int l = 1;
    if (null != level && level.toLowerCase().equals("debug")) {
      l = 0;
    }
    return new Logger(l);
  }

  public Logger() {
    super();
  }

  public Logger(int level) {
    super();
    this.level = level;
  }

  public boolean isDebugEnabled() {
    return level == 0;
  }

  public void info(String msg) {
    System.out.println(msg);
  }

  public void debug(String msg) {
    if (level == 0) System.out.println(msg);
  }
}
