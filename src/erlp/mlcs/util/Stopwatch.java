package erlp.mlcs.util;

import java.util.concurrent.TimeUnit;
import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class Stopwatch {
  public static String format(long nanos ){
    TimeUnit unit = chooseUnit(nanos);
    double value = ((double)nanos) / NANOSECONDS.convert(1, unit);
    return String.format("%.4g %s", value, abbreviate(unit));
  }

  private static TimeUnit chooseUnit(long nanos) {
    if (SECONDS.convert(nanos, NANOSECONDS) > 0) return SECONDS;
    if (MILLISECONDS.convert(nanos, NANOSECONDS) > 0) return MILLISECONDS;
    if (MICROSECONDS.convert(nanos, NANOSECONDS) > 0) return MICROSECONDS;
    return NANOSECONDS;
  }

  private static String  abbreviate(TimeUnit unit ) {
     if(unit == NANOSECONDS)return "ns";
     else if(unit == MICROSECONDS)return "μs";
     else if(unit == MILLISECONDS)return "ms";
    else if(unit == SECONDS) return "s";
    else throw new AssertionError();
  }
}