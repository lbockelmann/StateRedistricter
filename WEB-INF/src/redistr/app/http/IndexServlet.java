package redistr.app.http;

import gerrymandering.HibernateManager;
import gerrymandering.model.District;
import gerrymandering.model.State;
import org.locationtech.jts.io.ParseException;
import redistr.app.algorithm.Precinct;
import redistr.app.messaging.ContentType;
import redistr.util.IOUtil;
import redistr.util.Properties;
import redistr.util.PropertyManager;
import redistr.util.RDLogger;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static redistr.util.Properties.*;

@WebServlet(urlPatterns = { "/", "/index" })
public class IndexServlet extends HttpServlet {


  @Override
  public void init() {
    try {
      String root = getServletContext().getRealPath(".");
      PropertyManager.loadProperties(Paths.get(root, "WEB-INF", "properties.csv")
                                          .toString());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
  {
    try {
      if (req.getQueryString() != null) {
        Map<String, String> params = IOUtil.getQueryFromRequest(req);
        RDLogger.getLogger().info(String.valueOf(params));

        String stateAbbr = params.get(PropertyManager.getInstance()
                                                     .get(QUERY_SELECTOR_STATE, String.class));


        if (stateAbbr == null) {
          sendAllStateBoundaries(resp);
          return;
        }


        if (stateAbbr.equals("all")) {
          System.out.println("Sending  all states");
          sendAllStateBoundaries(resp);
          RDLogger.getLogger().info("Done sending boundaries");
          new Thread(this::preloadStatesData).start();
        } else {
          String year = params.get(PropertyManager.getInstance().get(HISTORICAL_YEAR_QUERY_SELECTED, String.class));
          RDLogger.getLogger().info("year is: " + year);
          if (year != null) {
            loadHistoricalDistrictsFromData(stateAbbr, Integer.parseInt(year));
            sendHistoricalData(resp, stateAbbr, Integer.parseInt(year));
            return;
          }

          String zoomLevelString = params.get(PropertyManager.getInstance()
                                                             .get(QUERY_SELECTOR_ZOOM, String.class));
          double zoomLevel = -1;
          if (zoomLevelString != null) {
            zoomLevel = Double.parseDouble(zoomLevelString);
          }
          try {
            sendStateData(resp, zoomLevel, stateAbbr);
          } catch (Throwable throwable) {
            throwable.printStackTrace();
            RDLogger.getLogger().throwing(getClass().getName(), "", throwable);
          }
        }
      } else {
        sendIndexPage(resp);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void sendAllStateBoundaries(HttpServletResponse resp) {
    try {

      List<redistr.app.algorithm.State> states = fetchAllStates();
      AllStatesResponse allStatesResponse = new AllStatesResponse(states);
      System.out.println("Before GSON");
      String statesJson = allStatesResponse.toJSONString();
      System.out.println("After GSON");
      resp.setContentType(ContentType.JSON);
      resp.setStatus(200);
      resp.getOutputStream().println(statesJson);
      resp.getOutputStream().flush();
    } catch (Throwable throwable) {
      RDLogger.getLogger().throwing(getClass().getName(), "", throwable);
    }
  }

  private void loadHistoricalDistrictsFromData(final String stateAbbr, final int year) {
    //    String geojsonData = readFile(districtFileForState(stateAbbr, year));

  }

  private void sendHistoricalData(HttpServletResponse resp, String stateAbbr, int year) throws IOException {
    try {
      String geojsonData = readFile(districtFileForState(stateAbbr, year));
      String precinctData = readFile(historicalPrecinctFile(stateAbbr, year));
      resp.setStatus(200);
      resp.setContentType(ContentType.JSON);
      resp.getOutputStream().print("{\"precincts\":" + precinctData + ", \"districts\": " + geojsonData + "}");
      resp.getOutputStream().flush();
    } catch (Exception e) {
      RDLogger.getLogger().throwing(getClass().getName(), "sendHistoricalData", e);
    }
  }

  private synchronized void sendStateData(HttpServletResponse resp, double zoomLevel, String stateAbbr)
      throws Throwable
  {
    //    File districtFile = districtFileForState(stateAbbr);
    //    File precinctFile = precinctFileForState(stateAbbr, zoomLevel);

    /*
    TODO: I think if this is still happening we should reply with LOADING and
    set a repeated timer to check if still loading if it is then we should
    respond appropriately otherwise not
     */

    redistr.app.algorithm.State currentState = redistr.app.algorithm.State.get(stateAbbr);

    RDLogger.getLogger().info("Getting districts");
    String precinctShapes = currentState.getDistricts()
                                        .stream()
                                        .flatMap(d -> d.getPrecincts()
                                                       .stream()
                                                       .map(redistr.app.algorithm.Precinct::getBoundaryData))
                                        .collect(Collectors.toList())
                                        .toString();
    String districtShapes = currentState.getDistricts()
                                        .stream()
                                        .map(redistr.app.algorithm.District::getBoundaryData)
                                        .collect(Collectors.toList())
                                        .toString();
    RDLogger.getLogger().info("Processing district geometries");
    currentState.getDistricts().forEach(district -> {
      try {
        district.initializeGeometry();
      } catch (ParseException e) {
        RDLogger.getLogger().throwing(getClass().getName(), "sendStateData", e);
      }
    });
    RDLogger.getLogger().info("Done processing district geometries");
    RDLogger.getLogger().info("Loading congressional election data");
    // TODO: actually load districts before trying to add election data to them
    currentState.loadCongressionalElectionData(electionDataFileForState(currentState));
    RDLogger.getLogger().info("Done loading congressional election data");
    // language=JSON
    String response =
        "{\"precincts\": " + precinctShapes + ", \"districts\": " +
        districtShapes + "}";
    resp.setContentType(ContentType.JSON);
    resp.setStatus(200);
    resp.getOutputStream().println(response);
    resp.getOutputStream().flush();
  }

  private void sendIndexPage(final HttpServletResponse resp) throws IOException
  {
    List<State> states = new LinkedList<>();
    try {
      List<Object> stateObjs = HibernateManager.getInstance()
                                               .getAllRecords(State.class);
      for (Object o : stateObjs) {
        states.add((State) o);
      }
    } catch (Throwable throwable) {
      RDLogger.getLogger().throwing(getClass().getName(), "", throwable);
    }

    BufferedReader r = new BufferedReader(new FileReader(fromRoot(PropertyManager
                                                                      .getInstance()
                                                                      .get(INDEX_PAGE_HTML, String.class))));

    String f = String.join("\n", r.lines().collect(Collectors.toList()));

    resp.setContentType(ContentType.HTML);
    resp.setStatus(200);
    resp.getOutputStream().print(f);
    resp.getOutputStream().flush();
  }

  private List<redistr.app.algorithm.State> fetchAllStates() throws Throwable {
    //    List<redistr.app.algorithm.State> states = new LinkedList<>();
    //    List<Object> os = HibernateManager.getInstance().getAllRecords(State.class);
    //    for (Object o : os) {
    //      redistr.app.algorithm.State s = redistr.app.algorithm.State.fromModelState((State) o);
    //      states.add(s);
    //    }

    return HibernateManager.getInstance()
                           .getAllRecords(State.class)
                           .stream()
                           .map(State.class::cast)
                           .map(redistr.app.algorithm.State::fromModelState)
                           .collect(Collectors.toList());
  }

  private String readFile(File f) {
    try (BufferedReader br = new BufferedReader(new FileReader(f))) {
      String response;
      response = br.lines().collect(Collectors.joining("\n"));

      return response;
    } catch (IOException exception) {
      RDLogger.getLogger()
              .throwing(getClass().getName(), "readFile", exception);
      return "";
    }
  }

  private File districtFileForState(String s, int year) {
    File file = null;
    try {
      file = fromRoot(IOUtil.MAPS_HOME, s, "" + year, s + "_" + year + "_D" +
                                                      PropertyManager.getInstance()
                                                                     .get(Properties.GEO_JSON_SUFFIX, String.class));
    } catch (Exception e) {
      RDLogger.getLogger()
              .throwing(getClass().getName(), "districtFileForState", e);
    }
    return file;
  }

  private File historicalPrecinctFile(final String stateAbbr, final int year) {
    File file = null;
    try {
      file = fromRoot(IOUtil.MAPS_HOME, stateAbbr, "" + year, stateAbbr + "_zoom" +
                                                              String.format("%.1f", 7.0)
                                                                    .replace(".", "_") +
                                                              PropertyManager.getInstance()
                                                                             .get(GEO_JSON_SUFFIX, String.class));
    } catch (Exception e) {
      RDLogger.getLogger().throwing(getClass().getName(), "", e);
    }
    return file;
  }

  private File electionDataFileForState(final redistr.app.algorithm.State currentState) {
    Path filePath = Paths.get("election_data", currentState.getAbbr() + ".eld");
    String fileName = getServletContext().getRealPath(filePath.toString());
    return new File(fileName);
  }

  private File fromRoot(String... paths) {
    String dir = getServletContext().getRealPath(".");
    return Paths.get(dir, paths).toFile();
  }

  private synchronized void preloadStatesData() {
    try {
      fetchAndStoreAllDistricts();
      fetchAndStoreAllPrecincts();
    } catch (Throwable throwable) {
      RDLogger.getLogger()
              .throwing(getClass().getName(), "preloadStateData", throwable);
    }
  }

  /**
   * Queries the database for all districts (yes <b>all</b> districts)
   * These districts are then added to their respective states.
   * As this happens the districts are associated by ID with
   * their objects in a HashMap.
   *
   * @return {@link java.util.HashMap HashMap} of {@link redistr.app.algorithm.District District} objects keyed by district id
   * @throws Throwable if the HibernateManager throws
   */
  private void fetchAndStoreAllDistricts()
      throws Throwable
  {
    HashMap<Integer, redistr.app.algorithm.District> internalDistricts = new HashMap<>(300, 0.75f);

    RDLogger.getLogger().info("Fetching districts for states");
    for (redistr.app.algorithm.State s : redistr.app.algorithm.State.loadedStates()) {
      if (s != null) {
        fetchAndStoreDistrictsForState(s);
      }
    }
    RDLogger.getLogger().info("Done fetching districts for states");
  }

  /**
   * Queries the database for all precincts (yes <b>all</b> precincts)
   * These precincts are then added to their respective districts in their
   * respective states. As this happens precincts are associated by ID with
   * their objects in a HashMap, this is later used for neighbor finding
   *
   * @return {@link java.util.HashMap HashMap} of precincts keyed by precinct id
   * @throws Throwable if the HibernateManager throws
   */
  private void fetchAndStoreAllPrecincts()
      throws Throwable
  {
    for (redistr.app.algorithm.State s : redistr.app.algorithm.State.loadedStates()) {
      HashMap<String, Precinct> statePrecincts = fetchStatePrecincts(s);
      processNeighborsForState(s, statePrecincts);
      processBorderPrecinctsForState(s, statePrecincts);
    }

  }



  private void fetchAndStoreDistrictsForState(redistr.app.algorithm.State state)
      throws Throwable
  {

    HashMap<String, Object> criteria = new HashMap<>();
    criteria.put("stateId", state.getId());

    HibernateManager.getInstance()
                    .getRecordsBasedOnCriteria(District.class, criteria)
                    .stream()
                    .map(District.class::cast)
                    .map(redistr.app.algorithm.District::new)
                    .peek(state.getDistricts()::add)
                    .forEach(district -> district.setContainingState(state));
  }

  private HashMap<String, Precinct> fetchStatePrecincts(redistr.app.algorithm.State s)
      throws Throwable
  {
    HashMap<String, String> pops = loadCSV(populationFileForState(s));
    HashMap<String, Precinct> statePrecincts = new HashMap<>();
    for (redistr.app.algorithm.District d : s.getDistricts()) {
      HashMap<String, Object> criteria = new HashMap<>();
      criteria.put("districtId", d.getDatabaseId());
      HibernateManager.getInstance()
                      .getRecordsBasedOnCriteria(gerrymandering.model.Precinct.class, criteria)
                      .stream()
                      .map(gerrymandering.model.Precinct.class::cast)
                      .map(Precinct::new)
                      .peek(p -> p.setContainerDistrict(d, true))
                      .peek(p -> {
                        try {
                          p.setPopulation(Integer.parseInt(pops.get(p.getId())));
                        } catch (Exception e) {
                          //                          RDLogger.getLogger().throwing(getClass().getName(), "peek", e);
                        }
                      })
                      .peek(d.getPrecincts()::add)
                      .forEach(p -> statePrecincts.put(p.getId(), p));
    }
    return statePrecincts;
  }

  private void processNeighborsForState(redistr.app.algorithm.State s, HashMap<String, Precinct> statePrecincts) {
    RDLogger.getLogger().info("Finding neighbors for " + s.getName());
    List<String> missingNeighborIDs = new LinkedList<>();
    HashMap<String, String[]> precinctNeighbors = loadSCSV(neighborFile(s.getAbbr()));
    for (redistr.app.algorithm.District d : s.getDistricts()) {
      for (redistr.app.algorithm.Precinct p : d.getPrecincts()) {
        String[] neighbors = precinctNeighbors.get(p.getId());
        if (neighbors != null) {
          for (String id : neighbors) {
            Precinct neighbor = statePrecincts.get(id);
            if (neighbor != null) {
              p.getNeighbors().add(neighbor);
            }
          }
        }
      }
    }
    RDLogger.getLogger().info("Done finding neighbors for " + s.getName());
  }

  private void processBorderPrecinctsForState(redistr.app.algorithm.State s, HashMap<String, Precinct> statePrecincts) {
    RDLogger.getLogger().info("Loading border precincts");

    HashMap<String, String[]> borderPrecincts = loadSCSV(borderPrecinctsFile(s.getAbbr()));

    if (borderPrecincts.size() == 0) {
      return;
    }

    for (redistr.app.algorithm.District d : s.getDistricts()) {
      String[] borderIds = borderPrecincts.get(d.getId());
      for (String precinctId : borderIds) {
        d.getBorderPrecincts().add(statePrecincts.get(precinctId));
      }
    }
    RDLogger.getLogger().info("Done loading border precincts");
  }

  private HashMap<String, String> loadCSV(final File scsvFile) {
    HashMap<String, String> entries = new HashMap<>();
    try {
      Scanner s = new Scanner(scsvFile);
      while (s.hasNextLine()) {
        String line = s.nextLine();

        String[] neighbors = line.split(",");
        entries.put(neighbors[0], neighbors[1]);
      }
    } catch (FileNotFoundException e) {
      RDLogger.getLogger().throwing(getClass().getName(), "loadCSV", e);
    }
    return entries;
  }

  private File populationFileForState(redistr.app.algorithm.State state) {
    return Paths.get(getServletContext().getRealPath("."), "population_data", state.getAbbr(), state.getAbbr() + "Population.csv").toFile();
  }

  /**
   * Loads a SCSV (special comma separated values) file
   * These files have the format of `key|val1,val2,val3,val4,...valn`
   * This method loads the file and returns a HashMap of String to String[]
   * where the keys are the `key` and the values an array of `vals`
   *
   * @param scsvFile the file to be loaded
   * @return a HashMap of `key`s to `String[]{val1, val2, val3, ...valn}`
   */
  private HashMap<String, String[]> loadSCSV(final File scsvFile) {
    HashMap<String, String[]> entries = new HashMap<>();
    try {
      Scanner s = new Scanner(scsvFile);
      while (s.hasNextLine()) {
        String line = s.nextLine();
        String[] entry = line.split("\\|");
        String[] neighbors = entry[1].split(",");
        entries.put(entry[0], neighbors);
      }
    } catch (FileNotFoundException e) {
      RDLogger.getLogger().throwing(getClass().getName(), "loadSCSV", e);
    }
    return entries;
  }

  public File neighborFile(String stateName) {
    File file = null;
    try {
      file = fromRoot(IOUtil.MAPS_HOME, stateName,
                      stateName + "_p_neighbors.scsv"
                     );
    } catch (Exception e) {
      RDLogger.getLogger().throwing(getClass().getName(), "neighborFile", e);
    }
    return file;
  }

  public File borderPrecinctsFile(String stateName) {
    File file = null;
    try {
      file = fromRoot(IOUtil.MAPS_HOME, stateName,
                      stateName + "_border_precincts.scsv"
                     );
    } catch (Exception e) {
      RDLogger.getLogger()
              .throwing(getClass().getName(), "borderPrecinctsFile", e);
    }
    return file;
  }

  public File precinctFileForState(String stateName, double zoomLevel) {
    File file = null;
    try {
      file = fromRoot(IOUtil.MAPS_HOME, stateName, stateName + "_zoom" +
                                                   String.format("%.1f", zoomLevel)
                                                         .replace(".", "_") +
                                                   PropertyManager.getInstance()
                                                                  .get(GEO_JSON_SUFFIX, String.class));
    } catch (Exception e) {
      RDLogger.getLogger().throwing(getClass().getName(), "", e);
    }
    return file;
  }

  private String getGeojsonFile(File precinctFile) {
    try (BufferedReader br = new BufferedReader(new FileReader(precinctFile))) {
      String response;
      response = br.lines().collect(Collectors.joining("\n"));

      return response;
    } catch (IOException exception) {
      RDLogger.getLogger()
              .throwing(getClass().getName(), "getGeoJsonFile", exception);
    }
    return "{\"precincts\":[], \"districts\":[]}";
  }

  class NeighborData {
    public HashMap<String, HashMap<String, List<String>>> data;

    @Override
    public String toString() {
      return "NeighborData{" + "data=" + data + '}';
    }
  }
}
