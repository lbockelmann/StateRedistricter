package redistr.app.http;

import redistr.app.messaging.ContentType;
import redistr.util.IOUtil;
import redistr.util.Properties;
import redistr.util.PropertyManager;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * @author Thomas Povinelli
 * Created 2018-Nov-28
 * In CSE308
 */
@WebServlet(urlPatterns = {"/election_data"})
public class ElectionDataServlet extends HttpServlet {


  @java.lang.Override
  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
    Map<String, String> params = IOUtil.getQueryFromRequest(req);

    String stateName = params.get(PropertyManager.getInstance().get(Properties.QUERY_SELECTOR_STATE, String.class));
    String jsonSuffix = PropertyManager.getInstance().get(Properties.JSON_SUFFIX, String.class);
    String electionDataDir = PropertyManager.getInstance().get(Properties.ED_DIR, String.class);

    Path filePath = Paths.get(getServletContext().getRealPath("."), electionDataDir, stateName + jsonSuffix);
    File electionDataFile = new File(filePath.toString());
    resp.setContentType(ContentType.JSON);
    IOUtil.writeFileToResponse(resp, electionDataFile);
  }
}
