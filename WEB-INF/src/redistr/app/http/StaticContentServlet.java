package redistr.app.http;

import redistr.app.messaging.ContentType;
import redistr.util.IOUtil;
import redistr.util.RDLogger;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
/**
 * @author Thomas Povinelli
 * Created 10/23/18
 * In CSE308
 */
@WebServlet({ "/css/*", "/js/*" })
public class StaticContentServlet extends HttpServlet {

    private File fromRoot(String... paths) {
        String dir = getServletContext().getRealPath(".");
        return Paths.get(dir, paths).toFile();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    {
        try {
            String[] uriComponents = req.getRequestURI().split("/");
            String[] fileRequest = Arrays.copyOfRange(uriComponents, 2, uriComponents.length);
            String content = fileRequest[0].contains("js") ? ContentType.JS : ContentType.CSS;
            resp.setContentType(content);
            IOUtil.writeFileToResponse(resp, fromRoot(fileRequest));
        } catch (Exception e) {
            RDLogger.getLogger().throwing(getClass().getName(), "", e);
        }
    }
}
