package algo.trading.tgalerter.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

/** Utility class for formatting various data types. */
public class FormatUtil {
  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  /**
   * Formats Instant time to readable string.
   *
   * @param time the timestamp to format (nullable)
   * @return formatted time string or "null"
   */
  public static String formatTime(Instant time) {
    if (time == null) {
      return "null";
    }
    return DATE_TIME_FORMATTER.withZone(ZoneId.systemDefault()).format(time);
  }

  /**
   * Appends labeled value to StringBuilder if not null.
   *
   * @param sb target StringBuilder
   * @param label field label
   * @param value value to append (nullable)
   */
  public static void appendIfNotNull(StringBuilder sb, String label, Object value) {
    if (value != null) {
      sb.append(label).append(": ").append(value).append("\n");
    }
  }

  /**
   * Gets first N lines of exception stack trace.
   *
   * @param ex exception to process
   * @param lines number of stacktrace lines to return
   * @return formatted stacktrace string
   */
  public static String getFirstLinesOfStackTrace(Exception ex, int lines) {
    StringWriter sw = new StringWriter();
    ex.printStackTrace(new PrintWriter(sw));
    return sw.toString().lines().limit(lines).collect(Collectors.joining("\n"));
  }
}
