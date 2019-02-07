import gerrymandering.HibernateManager;
import redistr.app.algorithm.*;
import redistr.app.algorithm.metrics.ObjectiveFunction;
import redistr.app.messaging.UserParameters;
import redistr.util.IOUtil;
import redistr.util.PropertyManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Thomas Povinelli
 * Created 12/5/18
 * In CSE308
 */
public class TestAlgorithm {

  public static void main(String[] args) throws Throwable {
    TestAlgorithm t = new TestAlgorithm();
    PropertyManager.loadProperties(System.getProperty("user.dir") + "\\WEB-INF\\properties.csv");
    t.fetchAllStates();
    t.preloadStatesData();

    State utah = State.get("UT");

    for (District d: utah.getDistricts()) {
      d.initializeGeometry();
    }

    UserParameters params = new UserParameters();
    params.setCompactness(0.5);
    params.setApproach("AWOL");
    params.setEfficiencyGap(0.5);
    params.setPartisanFairness(0.5);
    params.setPopulationEquality(0.5);
    params.setNumSeedDistricts(4);
    params.setStateAbbr(Abbreviation.UT);

    SimulatedAnnealingNoLA r = new SimulatedAnnealingNoLA(params, utah);

    ObjectiveFunction function = new ObjectiveFunction(r, params);

    r.setDaemon(false);
    r.start();

    r.join();

  }

  private void fetchAllStates() throws Throwable {
    HibernateManager.getInstance()
                    .getAllRecords(gerrymandering.model.State.class)
                    .stream()
                    .map(gerrymandering.model.State.class::cast)
                    .forEach(State::fromModelState);

  }

  private synchronized void preloadStatesData() {
    try {
      fetchAndStoreAllDistricts();
      fetchAndStoreAllPrecincts();
    } catch (Throwable throwable) {
      throwable.printStackTrace();
    }
  }

  /**
   * Queries the database for all precincts (yes <b>all</b> precincts)
   * These precincts are then added to their respective districts in their
   * respective states. As this happens precincts are associated by ID with
   * their objects in a HashMap, this is later used for neighbor finding
   *
   * @throws Throwable if the HibernateManager throws
   */
  private void fetchAndStoreAllPrecincts()
      throws Throwable
  {
    for (State s : State.loadedStates()) {
      HashMap<String, Precinct> statePrecincts = fetchStatePrecincts(s);
      processNeighborsForState(s, statePrecincts);
      processBorderPrecinctsForState(s, statePrecincts);
    }

  }

  private void processBorderPrecinctsForState(State s, HashMap<String, Precinct> statePrecincts) {
    System.out.println("Loading border precincts");

    HashMap<String, String[]> borderPrecincts = loadSCSV(borderPrecinctsFile(s
        .getAbbr()));

    if (borderPrecincts.size() == 0) {
      return;
    }

    for (District d : s.getDistricts()) {
      String[] borderIds = borderPrecincts.get(d.getId());
      for (String precinctId : borderIds) {
        d.getBorderPrecincts().add(statePrecincts.get(precinctId));
      }
    }
    System.out.println("Done loading border precincts");
  }

  public File borderPrecinctsFile(String stateName) {
    File file = null;
    try {
      file = Paths.get(System.getProperty("user.dir"), IOUtil.MAPS_HOME, stateName,
          stateName + "_border_precincts.scsv").toFile();
    } catch (Exception e) {

    }
    return file;
  }

  /**
   * Runs through all state and all districts in the state and and neighbors for
   * every precinct in those districts and adds the neighbors of the precinct
   * to the list within the precinct object
   */
  private void processNeighborsForState(State s, HashMap<String, Precinct> statePrecincts) {
    System.out.println("Finding neighbors for " + s.getName());
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
    System.out.println("Done finding neighbors for " + s.getName());
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
      e.printStackTrace();
    }
    return entries;
  }

  public File neighborFile(String stateName) {
    File file = null;
    try {
      file = Paths.get(System.getProperty("user.dir"), IOUtil.MAPS_HOME, stateName,
          stateName + "_p_neighbors.scsv").toFile();
    } catch (Exception e) {

    }
    return file;
  }

  private HashMap<String, Precinct> fetchStatePrecincts(State s)
      throws Throwable
  {
    HashMap<String, Precinct> statePrecincts = new HashMap<>();
    for (District d : s.getDistricts()) {
      HashMap<String, Object> criteria = new HashMap<>();
      criteria.put("districtId", d.getDatabaseId());
      HibernateManager.getInstance()
                      .getRecordsBasedOnCriteria(gerrymandering.model.Precinct.class, criteria)
                      .stream()
                      .map(gerrymandering.model.Precinct.class::cast)
                      .map(Precinct::new)
                      .peek(p -> p.setContainerDistrict(d))
                      .peek(d.getPrecincts()::add)
                      .forEach(p -> statePrecincts.put(p.getId(), p));
    }
    return statePrecincts;
  }

  /**
   * Queries the database for all districts (yes <b>all</b> districts)
   * These districts are then added to their respective states.
   * As this happens the districts are associated by ID with
   * their objects in a HashMap.
   *
   * @throws Throwable if the HibernateManager throws
   */
  private void fetchAndStoreAllDistricts() throws Throwable
  {

    for (State s : State.loadedStates()) {
      fetchAndStoreDistrictsForState(s);
    }
    System.out.println("Done fetching districts for states");
  }

  private List<District> fetchAndStoreDistrictsForState(State state)
      throws Throwable
  {

    HashMap<String, Object> criteria = new HashMap<>();
    criteria.put("stateId", state.getId());

    Stream<gerrymandering.model.District> modelDistricts = HibernateManager.getInstance()
                                                                           .getRecordsBasedOnCriteria(gerrymandering.model.District.class, criteria)
                                                                           .stream()
                                                                           .map(gerrymandering.model.District.class::cast);
    return modelDistricts.map(District::new)
                         .peek(state.getDistricts()::add)
                         .peek(district -> district.setContainingState(state))
                         .collect(Collectors.toList());
  }

  public Move getNextMove() {
    try {
      District destDistrict = randomFromSet(workingDistricts);

      Precinct borderPrecinct = randomFromSet(destDistrict.getBorderPrecincts());

      Precinct movingPrecinct = randomFromSet(borderPrecinct.getNeighbors(), p ->
          p.getContainerDistrict() != destDistrict);

      District sourceDistrict = movingPrecinct.getContainerDistrict();

      return new Move(sourceDistrict, destDistrict, movingPrecinct);
    } catch (NullPointerException e) {

      return null;
    }
  }

  public <T> T randomFromSet(Collection<T> things) {
    int size = things.size();
    int rnd = (int) (Math.random() * size);
    return things.stream().skip(rnd).findFirst().orElse(null);
  }

  public <T> T randomFromSet(Collection<T> things, Predicate<T> predicate) {
    List<T> filtered = things.stream()
                             .filter(predicate)
                             .collect(Collectors.toList());
    int rnd = (int) (Math.random() * filtered.size());
    return filtered.stream().skip(rnd).findFirst().orElse(null);
  }

  private LinkedList<District> workingDistricts;

}
