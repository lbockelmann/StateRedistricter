package redistr.util;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class PropertyManager extends HashMap<String, Object> {
  private static PropertyManager manager;

  public static PropertyManager getInstance() {
    return manager;
  }

  public static synchronized boolean loadProperties(String file) {
    manager = new PropertyManager();
    try {
      Scanner s = new Scanner(new File(file));
      while (s.hasNextLine()) {
        String line = s.nextLine();
        String[] values = line.split(",");
        String key = values[0];
        Object value = coerceToType(values[1], values[2]);
        manager.put(key, value);
      }
      return true;
    } catch ( FileNotFoundException e ) {
      e.printStackTrace();
      RDLogger.getLogger().throwing(PropertyManager.class.getName(), "", e);
      return false;
    }
  }

  private static Object coerceToType(String rawValue, String typeName) {
    switch ( typeName ) {
      case "Integer":
      case "int":
        return Integer.parseInt(rawValue);
      case "Double":
      case "double":
        return Double.parseDouble(rawValue);
      case "String":
        return rawValue;
      default:
        throw new IllegalArgumentException(
            "Type " + typeName + " unexpected in properties list");
    }
  }

  private PropertyManager() { }

  public void saveProperties(String file) throws IOException {
    PrintStream writer = new PrintStream(new FileOutputStream(file));
    for (Map.Entry<String, Object> entry : entrySet()) {
      writer.print(entry.getKey());
      writer.print(',');
      writer.print(entry.getValue());
      writer.print(',');
      writer.print(entry.getValue().getClass().getSimpleName());
      writer.print('\n');
    }
  }

  public <T> T get(String key, Class<T> type) {
    return type.cast(get(key));
  }
}
