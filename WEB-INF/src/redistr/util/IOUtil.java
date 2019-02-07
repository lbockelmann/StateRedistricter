package redistr.util;

import redistr.app.interfaces.JSONSerializable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Thomas Povinelli
 * Created 10/23/18
 * In CSE308
 */
public class IOUtil {
  public static final String MAPS_HOME = Paths.get("js", "Maps").toString();

  public static void writeFileToResponse(HttpServletResponse resp, File file)
      throws IOException
  {
    BufferedReader r = new BufferedReader(new FileReader(file));
    List<String> fileLines = r.lines().collect(Collectors.toList());

    String text = String.join("\n", fileLines);
    resp.getOutputStream().print(text);
    resp.getOutputStream().flush();
  }

  public static String reqBody(HttpServletRequest req)  {
    return reqBody(req, 4096);
  }

  public static String reqBody(HttpServletRequest req, int bufsize) {
    try {
      InputStreamReader r = new InputStreamReader(req.getInputStream());
      char[] buf = new char[bufsize];
      StringBuilder builder = new StringBuilder();
      while (r.read(buf) != -1) {
        builder.append(buf);
      }
      return builder.toString();
    } catch (IOException e) {
      RDLogger.getLogger().severe("Error in writing reqBody");
      RDLogger.getLogger().throwing(IOUtil.class.getName(), "", e);
      return "";
    }
  }

  public static Map<String, String> getQueryFromRequest(HttpServletRequest req) {
    try {
      String[] entries = req.getQueryString().split("&");
      Map<String, String> queryParameters = new HashMap<>();
      for (String entry : entries) {
        String[] kv = entry.split("=");
        queryParameters.put(kv[0], kv[1]);
      }
      return queryParameters;
    } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
      RDLogger.getLogger().info("Error in getting query parameters");
      RDLogger.getLogger().throwing(IOUtil.class.getName(), "", e);
      return new HashMap<>();
    }
  }

  public static <T extends JSONSerializable> String listToJSONString(List<T> list) {
    List<String> jsonStrings = list.stream()
                               .map(JSONSerializable::toJSONString)
                               .collect(Collectors.toList());
    return jsonStrings.toString();
  }
}

