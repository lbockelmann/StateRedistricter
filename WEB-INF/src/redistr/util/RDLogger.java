package redistr.util;

import java.io.IOException;
import java.util.logging.*;

/**
 * @author Thomas Povinelli
 * Created 2018-Dec-04
 * In CSE308
 */
public class RDLogger {
  public static Logger logger = Logger.getLogger("RedistrictingLogger");
  private static volatile int tempCount = 1;
  static {
    try {
      logger.setLevel(Level.ALL);
      SimpleFormatter formatter = new SimpleFormatter();
      Handler[] handlers = logger.getHandlers();
      for (Handler handler: handlers) {
        logger.removeHandler(handler);
      }
      FileHandler fh = new FileHandler("RedistrictingLogger.log", true);
      fh.setFormatter(formatter);
      logger.addHandler(fh);
    } catch ( IOException | SecurityException e ) {
      e.printStackTrace();
    }
  }


  public static Logger getLogger() {
    return logger;
  }

  public static Logger getTempFileLogger() {
    try {
      Logger tempLogger = Logger.getAnonymousLogger();
      tempLogger.setLevel(Level.ALL);
      Handler[] handlers = tempLogger.getHandlers();
      for (Handler handler: handlers) {
        tempLogger.removeHandler(handler);
      }
      SimpleFormatter formatter = new SimpleFormatter();
      FileHandler fh = new FileHandler("temp" + getTempCount() + ".log");
      fh.setFormatter(formatter);
      tempLogger.addHandler(fh);
      return tempLogger;
    } catch ( IOException | SecurityException e ) {
      e.printStackTrace();
      return null;
    }
  }

  private static synchronized int getTempCount() {
    return tempCount++;
  }


}
