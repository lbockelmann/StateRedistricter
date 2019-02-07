package redistr.app.algorithm;

import gerrymandering.model.ElectionData;
import redistr.app.algorithm.metrics.Party;
import redistr.app.interfaces.RDCopying;
import utils.ElectionType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Thomas Povinelli
 * Created 10/28/18
 * In CSE308
 */
public class Precinct implements RDCopying<Precinct> {
  private String id;
  private Set<Precinct> neighbors;
  private double area;
  private double stateBorderDistance;
  private boolean seed;
  private boolean borderPrecinct;
  private int population;
  private Map<Integer, Set<ElectionData>> electionData = new HashMap<>();
  private District containerDistrict;
  private String boundaryData;

  private HashMap<Party, Integer> votes = new HashMap<>();

  public HashMap<Party, Integer> getVotes() {
    return votes;
  }

  public Precinct(final gerrymandering.model.Precinct p) {
    this.id = p.getExternalId();
    this.neighbors = new HashSet<>();
    this.boundaryData = p.getBoundaryJSON();
  }

  private Precinct() {

  }

  public String getId() { return id; }

  public void setId(String id) { this.id = id; }

  public Set<Precinct> getNeighbors() { return neighbors; }

  public void setNeighbors(Set<Precinct> neighbors) {
    this.neighbors = neighbors;
  }

  public double getArea() { return area; }

  public void setArea(double area) { this.area = area; }

  public double getStateBorderDistance() { return stateBorderDistance; }

  public void setStateBorderDistance(double stateBorderDistance) {
    this.stateBorderDistance = stateBorderDistance;
  }

  public boolean isSeed() { return seed; }

  public void setSeed(boolean seed) { this.seed = seed; }

  public boolean isBorderPrecinct() { return borderPrecinct; }

  public void setBorderPrecinct(boolean borderPrecinct) {
    this.borderPrecinct = borderPrecinct;
  }

  public int getPopulation() { return population; }

  public void setPopulation(int population) { this.population = population; }

  public District getContainerDistrict() { return containerDistrict; }

  public void setContainerDistrict(District containerDistrict, boolean first) {
    if (this.containerDistrict != null && first) {
      throw new IllegalStateException("Resetting container district");
    }
    this.containerDistrict = containerDistrict;
  }

  public void setContainerDistrict(District containerDistrict) {
    this.containerDistrict = containerDistrict;
  }

  public List<ElectionData> getElectionData(int year, ElectionType type) {
    return electionData.get(year)
        .stream()
        .filter(i -> i.getElectionType().equals(type))
        .collect(Collectors.toList());
  }

  public String getBoundaryData() { return boundaryData; }

  @Override
  public String toString() {
    return id;
  }

  @Override
  public Precinct copy() {
    Precinct ret = new Precinct();
    ret.id = id;
    ret.neighbors = neighbors;
    ret.area = area;
    ret.stateBorderDistance = stateBorderDistance;
    ret.seed = seed;
    ret.borderPrecinct = borderPrecinct;
    ret.population = population;
    ret.electionData  = electionData;
    ret.containerDistrict = containerDistrict;
    ret.boundaryData = boundaryData;

    return ret;
  }


}
