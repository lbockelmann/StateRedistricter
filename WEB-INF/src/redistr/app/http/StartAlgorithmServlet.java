package redistr.app.http;

import com.google.gson.Gson;
import redistr.app.account.UserAccount;
import redistr.app.algorithm.*;
import redistr.app.messaging.ContentType;
import redistr.app.messaging.MapUpdate;
import redistr.app.messaging.UserParameters;
import redistr.util.IOUtil;
import redistr.util.Properties;
import redistr.util.PropertyManager;
import redistr.util.RDLogger;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Map;

@WebServlet(urlPatterns = { "/start_algo" })
public class StartAlgorithmServlet extends HttpServlet {
  public static final String SHOULD_CONTINUE = PropertyManager.getInstance().get(Properties.UPDATE_CONT, String.class);
  public static final String KEEP_GOING = PropertyManager.getInstance().get(Properties.ALGO_KEEP_GOING, String.class);
  private Redistricter redistricter;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
  {
    try {
      Map<String, String> params = IOUtil.getQueryFromRequest(req);

      if ("true".equals(params.get("reset"))) {

        redistricter.resetWorkingDistricts();

        return;
      }
      if ("true".equals(params.get("getSeedIds"))) {
        String[] seedIds = ((RegionGrowing)redistricter).getSeedIds();
        String seedList = String.join(",", seedIds);
        resp.getOutputStream().println(seedList);
        resp.setStatus(200);
        resp.setContentType(ContentType.PLAIN_TEXT);
        resp.getOutputStream().flush();
        return ;
      }

      MapUpdate update = redistricter.getUpdates();
      String response = update.toJSONString();
      redistricter.signalRequest(params.get(SHOULD_CONTINUE).equals(KEEP_GOING));
      resp.setStatus(200);
      resp.setContentType(ContentType.JSON);
      resp.getOutputStream().println(response);
      resp.getOutputStream().close();
    } catch (Exception e) {
      e.printStackTrace();
      RDLogger.getLogger().throwing(getClass().getName(), "doGet", e);
    }
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException
  {

    Boolean isSaveRequest = (Boolean) req.getAttribute("mapRequest");
    if (isSaveRequest != null && isSaveRequest) {
      resp.setStatus(500);
      if (redistricter != null) {
        boolean success = UserAccount.saveMap((String) req.getAttribute("username"), redistricter.getCurrentState(), redistricter.getYear(), redistricter.isDone(), Collections.unmodifiableList(redistricter.getMoves()));
        if (success) {
          resp.setStatus(200);
        }
      }
      return;
    }

    Gson gson = new Gson();
    UserParameters params = gson.fromJson(new InputStreamReader(req.getInputStream()), UserParameters.class);
    RDLogger.getLogger().info(String.valueOf(params));
    switch (params.getApproach()) {
      case Properties.AnnealingWithLookAhead:
        redistricter = new SimulatedAnnealingLA(params, State.get(params.getStateAbbr().getAbbr()));
        break;
      case Properties.AnnealingWithoutLookAhead:
        redistricter = new SimulatedAnnealingNoLA(params, State.get(params.getStateAbbr().getAbbr()));
        break;
      case Properties.RegionGrowingByPopulation:
        redistricter = new RegionGrowingPopulation(params, State.get(params.getStateAbbr().getAbbr()));
        break;
      case Properties.RegionGrowingUniform:
        redistricter = new RegionGrowingUniform(params, State.get(params.getStateAbbr().getAbbr()));
        break;
      default:
        throw new UnsupportedOperationException("Approach " + params.getApproach() + " is not supported");
    }

    redistricter.start();

    resp.setStatus(200);
    resp.setContentType(ContentType.PLAIN_TEXT);
    resp.getOutputStream().println("success");
    resp.getOutputStream().close();
  }
}
