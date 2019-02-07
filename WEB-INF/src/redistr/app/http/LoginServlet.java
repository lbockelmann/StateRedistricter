package redistr.app.http;

import com.google.gson.Gson;
import redistr.app.account.UserAccount;
import redistr.app.messaging.ContentType;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;

@WebServlet(urlPatterns = "/login")
public class LoginServlet extends HttpServlet {

  @Override
  public void init() {
    UserAccount.setServletPath(getServletContext().getRealPath("."));
  }

  protected void doGet(
      HttpServletRequest request,
      HttpServletResponse response
                      )
  {}

  protected void doPost(
      HttpServletRequest request,
      HttpServletResponse response
                       ) throws IOException, ServletException
  {
    LoginParameters parameters =
        (new Gson())
            .fromJson(
                new InputStreamReader(request.getInputStream()),
                LoginParameters.class
                     );
    String username = parameters.username;
    String password = parameters.password;
    String isLogin = parameters.isLogin;

    // validate login

    String returnStatus;
    boolean successful = false;
    switch (isLogin) {
      case "true":
        if (UserAccount.validateLogin(username, password)) {
          returnStatus = "validlogin";
        } else {
          returnStatus = "invalidlogin";
        }
        break;
      case "false":
        if (UserAccount.createAccount(username, password)) {
          returnStatus = "validcreation";
        } else {
          returnStatus = "invalidcreation";
        }
        break;
      case "adminNewAccount":
        if (UserAccount.createAccount(username, password)) {
          returnStatus = "validcreation";
        } else {
          returnStatus = "invalidcreation";
        }
        break;
      case "deleteAccount":
        if (UserAccount.deleteAccount(username)) {
          returnStatus = "validdeletion";
        } else {
          returnStatus = "invaliddeletion";
        }
        break;
      case "getAccounts":
        returnStatus = UserAccount.getAccounts();
        break;
      case "saveWeights":
        successful = UserAccount.saveWeights(username);
        returnStatus = successful ? "validsave" : "invalidsave";
        break;
      case "loadWeights":
        returnStatus = UserAccount.loadWeights(username);
        break;
      case "savemap":
        forward(request, response, username);
        returnStatus = response.getStatus() == 200 ? "validsave" : "invalidsave";
        break;
      case "getSavedMaps":
        returnStatus = UserAccount.getSavedMaps(username);
        break;
      case "deleteMap":
        if (UserAccount.deleteMap(username)) {
          returnStatus = "validdeletion";
        } else {
          returnStatus = "invaliddeletion";
        }
        break;
      default:
        returnStatus = "invalidoption";
        break;
    }

    response.setStatus(200);
    response.setContentType(ContentType.PLAIN_TEXT);

    response.getOutputStream().print(returnStatus);
    response.getOutputStream().close();
  }

  private void forward(HttpServletRequest request, HttpServletResponse response, final String username) throws ServletException, IOException {
    request.setAttribute("username", username);
    request.setAttribute("mapRequest", Boolean.TRUE);
    request.getRequestDispatcher("/start_algo").forward(request, response);
  }

  class LoginParameters {
    public String username, password, isLogin;
  }
}
