package redistr.app.algorithm;

import redistr.app.algorithm.metrics.CongressionalElection;
import redistr.app.algorithm.metrics.Party;
import redistr.app.interfaces.JSONSerializable;
import redistr.util.Properties;
import redistr.util.PropertyManager;
import redistr.util.RDLogger;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.time.Year;
import java.util.*;


/**
 * @author Thomas Povinelli
 * Created 10/27/18
 * In CSE308
 */
public class State implements JSONSerializable {
  public static final HashMap<String, State> states = new HashMap<>(50);

  public static State fromFields(int stateId, String name, String abbr, Redistricter redistricter) {
    State s = new State(stateId, name, abbr, redistricter);
    states.put(s.abbr, s);
    return s;
  }

  public static Collection<State> loadedStates() {
    return Arrays.asList(get("UT"), get("NM"), get("SC"));
  }

  public static State get(String abbrString) {
    return states.get(abbrString);
  }

  public static State fromModelState(gerrymandering.model.State modelState) {
    State s = new State(modelState);
    states.put(s.abbr, s);
    return s;
  }

  private int id;
  private String name;
  private String abbr;
  private String boundaryJSON;
  private String centerPoint;
  private Map<Year, List<District>> districts;
  private transient Redistricter redistricter;
  private String stateConstitution;

  protected State(int stateId, String name, String abbr, Redistricter redistricter) {
    this.id = stateId;
    this.name = name;
    this.abbr = abbr;
    this.redistricter = redistricter;
  }

  protected State(gerrymandering.model.State modelState) {
    this.id = modelState.getStateId();
    this.name = modelState.getName();
    this.abbr = modelState.getShortName();
    this.stateConstitution = modelState.getConstitutionText();
    this.redistricter = null;
    this.districts = new HashMap<>();
  }

  @Override
  public String toJSONString() {
    return String.format("{\"id\": %d, \"abbr\": \"%s\", \"name\": \"%s\"}", id, abbr, name);
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getBoundaryJSON() {
    return boundaryJSON;
  }

  public void setBoundaryJSON(String boundaryJSON) {
    this.boundaryJSON = boundaryJSON;
  }

  public String getCenterPoint() {
    return centerPoint;
  }

  public void setCenterPoint(String centerPoint) {
    this.centerPoint = centerPoint;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getAbbr() {
    return abbr;
  }

  public void setAbbr(String abbr) {
    this.abbr = abbr;
  }

  public List<District> getDistricts() {
    Integer isoYear = PropertyManager.getInstance().get(Properties.DEFAULT_YEAR, Integer.class);
    Year of = Year.of(isoYear);
    return getDistricts(of);
  }

  public void setDistricts(List<District> districts) {
    this.districts.put(Year.of(PropertyManager.getInstance().get(Properties.DEFAULT_YEAR, Integer.class)), districts);
  }

  public List<District> getDistricts(Year year) {
    RDLogger.getLogger().info(districts.toString());
    districts.computeIfAbsent(year, k -> new ArrayList<>());

    RDLogger.getLogger().info(districts.toString());
    return districts.get(year);
  }

  public Redistricter getRedistricter() {
    return redistricter;
  }

  public void setRedistricter(Redistricter redistricter) {
    this.redistricter = redistricter;
  }

  public String getStateConstitution() {
    return stateConstitution;
  }

  public void setStateConstitution(String stateConstitution) {
    this.stateConstitution = stateConstitution;
  }

  public void loadCongressionalElectionData(File file) throws FileNotFoundException, ParseException {
    Scanner s = new Scanner(file);
    int offset = 0;
    while (s.hasNextLine()) {
      String line = s.nextLine();
      offset += line.length();
      if (line.startsWith("year:")) {
        String[] comp = line.split(":");
        if (Integer.parseInt(comp[1]) < PropertyManager.getInstance().get(Properties.DEFAULT_YEAR, Integer.class)) {
          offset += ignoreYear(s, offset);
          continue;
        }
        String y = comp[1];
        offset += consumeElectionDataYear(s, offset, Year.of(Integer.parseInt(y)));
      } else {
        throw new ParseException("Invalid election format: Expected `year:XXXX` but got " + line, offset);
      }
    }

  }

  private int ignoreYear(final Scanner s, int offset) throws ParseException {
    while (s.hasNextLine()) {
      String line = s.nextLine();
      offset += line.length();
      if (line.startsWith("year_end")) {
        return offset;
      }
    }
    throw new ParseException("Expected `year_end` but found EOF", offset);
  }

  private int consumeElectionDataYear(final Scanner s, int offset, final Year year) throws ParseException {
    while (s.hasNextLine()) {
      String line = s.nextLine();
      offset += line.length();
      if (line.startsWith("district:")) {
        String[] comp = line.split(":");
        String did = comp[1];
        offset += consumeElectionDataDistrict(s, offset, did, year);
        District d = getDistricts(year)
                         .stream()
                         .peek(dst -> RDLogger.getLogger().info(dst.getId()))
                         .filter(dst -> dst.getId().equals(did))
                         .findFirst()
                         .get();
        int totalVotes = d.getCongressionalElection(Year.of(2017)).votes.values().stream().mapToInt(i -> i).sum();
        for (Precinct p : d.getPrecincts()) {

          d.getCongressionalElection(Year.of(2017)).votes.forEach((key, value) -> {
            int pv = (int)(((double)value / totalVotes) * p.getPopulation());
            p.getVotes().put(key, pv);
          });
        }
      } else if (line.startsWith("year_end")) {
        return offset;
      } else {
        throw new ParseException("Invalid election format: Expected `district:(X)X` but got " + line, offset);
      }
    }

    return offset;
  }

  private int consumeElectionDataDistrict(final Scanner s, int offset, final String did, final Year year) {
    District d = getDistricts(year)
                     .stream()
                     .peek(dst -> RDLogger.getLogger().info(dst.getId()))
                     .filter(dst -> dst.getId().equals(did))
                     .findFirst()
                     .get();
    CongressionalElection election = new CongressionalElection();
    while (s.hasNextLine()) {
      String line = s.nextLine();
      offset += line.length();
      if (line.startsWith("district_end")) {
        break;
      }
      String[] comp = line.split(",");
      int votes = Integer.parseInt(comp[1]);
      Party party = Party.valueOf(comp[2]);

      election.votes.compute(party, (p, i) -> {
        if (i == null) {
          return votes;
        } else {
          return i + votes;
        }
      });
    }
    d.addCongressionalElectionData(year, election);


    return offset;
  }

  public List<District> copyOfCurrentDistricts(final int year) {
    List<District> ret = new LinkedList<>();
    for (District d : districts.get(Year.of(year))) {
      ret.add(d.copy());
    }
    return ret;
  }
}
