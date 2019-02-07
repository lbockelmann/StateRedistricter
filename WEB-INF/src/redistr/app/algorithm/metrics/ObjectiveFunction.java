package redistr.app.algorithm.metrics;

import redistr.app.algorithm.District;
import redistr.app.algorithm.Redistricter;
import redistr.app.messaging.UserParameters;
import redistr.util.RDLogger;

import java.time.Year;
import java.util.List;

/**
 * @author Thomas Povinelli
 * Created 10/27/18
 * In CSE308
 */
public class ObjectiveFunction {

  public static Double map(Double self, Double os, Double oe, Double ns, Double ne) {
    return ns + (((self - os) / (oe - os)) * (ne - ns));
  }

  private final Redistricter redistricter;
  private final UserParameters parameters;

  public ObjectiveFunction(Redistricter redistricter, UserParameters parameters) {
    this.parameters = parameters;
    this.redistricter = redistricter;
  }

  public double calculateRacialFairness() { return 0; }

  public double calculateNaturalBoundaries() { return 0; }

  public double calculateValue() {
    double totalCompactness = calculateCompactness();
        RDLogger.getLogger().info("Total compactness value: " + totalCompactness);
    double totalEfficiencyGap = calculateEfficiencyGap();
        RDLogger.getLogger().info("Efficiency Gap value: " + totalEfficiencyGap);
    double totalPopulationEquality = calculatePopulationEquality();
        RDLogger.getLogger().info("Population equality value: " + totalPopulationEquality);
    double objFunValue = parameters.getCompactness() * totalCompactness + parameters.getEfficiencyGap() * totalEfficiencyGap +
                         totalPopulationEquality * parameters.getPopulationEquality();
    //    RDLogger.getLogger().info("Objective function value: " + objFunValue);
    return (objFunValue);

  }

  public double calculateCompactness() {
    int it = redistricter.getCurrentIteration();
    double avg1 = redistricter.getWorkingDistricts()
                              .stream()
                              .mapToDouble(d -> 10 * calculatePolsbyPopper(d.getArea(it), d.getPerimeter(it)))
                              .average()
                              .orElse(-10);
    double avg2 = redistricter.getWorkingDistricts()
                              .stream()
                              .mapToDouble(d -> 10 * calculateSchawrtzBergCompactness(d.getArea(it), d.getPerimeter(it)))
                              .average()
                              .orElse(-100);
    return sigmoid(((avg1 + avg2) / 2) * 3);

  }

  public double calculateEfficiencyGap() {
/*
    double totalWaste = 0;
    int districtCount = 0;
    for (District d : redistricter.workingDistricts) {
      HashMap<PartyName, Double> votes = new HashMap<>();
      for (Precinct p : d.getPrecincts()) {
        for (ElectionData data : p.getElectionData(PropertyManager.getInstance().get(Properties.DEFAULT_YEAR, Integer.class), ElectionType.Congress)) {
          votes.compute(data.getPartyName(), (name, val) -> val != null ? val : 0.0 + data.getVoteCount());
        }
      }

      double totalVotes = votes.values()
                               .stream()
                               .mapToDouble(Double::doubleValue)
                               .sum();

      Map<PartyName, Double> wastedVotes = new HashMap<>();
      PartyName winningParty = null;
      for (Map.Entry<PartyName, Double> partyVotes : votes.entrySet()) {
        if (partyVotes.getValue() / totalVotes > 50) {
          winningParty = partyVotes.getKey();
          wastedVotes.put(partyVotes.getKey(), partyVotes.getValue() - (totalVotes / 2 + 1));
        } else {
          wastedVotes.put(partyVotes.getKey(), partyVotes.getValue());
        }
      }

      double winningGapSum = 0.0;
      int partyCount = 0;
      for (Map.Entry<PartyName, Double> partyWasted : wastedVotes.entrySet()) {
        partyCount++;
        if (partyWasted.getKey().equals(winningParty)) {
          continue;
        }
        winningGapSum += wastedVotes.get(winningParty) - partyWasted.getValue();
      }
      double districtAverageGap = winningGapSum / (totalVotes);
      totalWaste += districtAverageGap;
      districtCount++;
    }

    return 1 - (totalWaste / districtCount);
    */

        EfficiencyGap g = new EfficiencyGap();
        StateMetricResult r = g.calculate(redistricter.getCurrentState(), Year.of(redistricter.getYear()));
        return sigmoid((Double) r.getScore() * 28);
  }

  public double calculatePopulationEquality() {
    List<District> districts = redistricter.getWorkingDistricts();

    double statePopulation = 0;
    int totalDistricts = 0;

    District smallest = null;
    District largest = null;

    for (final District d : districts) {
      statePopulation += d.getTotalPopulation();
      totalDistricts++;
      if (smallest == null ||
          d.getTotalPopulation() < smallest.getTotalPopulation())
      {
        smallest = d;
      }
      if (largest == null ||
          d.getTotalPopulation() > largest.getTotalPopulation())
      {
        largest = d;
      }
    }
    double idealPopulation = statePopulation / totalDistricts;
    double percentDeviation = (largest.getTotalPopulation() - smallest.getTotalPopulation()) / ((double) largest.getTotalPopulation());
    return sigmoid((1 - percentDeviation) * 3.5);
  }

  public static double sigmoid(double d) {
    return 1 / (1 + Math.exp(2.4 * -d + 4.7));
  }

  public Double calculatePolsbyPopper(double area, double perimeter) {
    // Formula: (4Pi * area) / (Perimeter^2)
    return (4 * Math.PI * area) / Math.pow(perimeter, 2);
  }

  public double calculateSchawrtzBergCompactness(double area, double perimeter) {
    // equalAreaRadius = r = sqrt(A/PI)
    double r = Math.sqrt(area / Math.PI);
    // equalAreaPerimeter = C = 2pi * r (Circumference)
    double equalAreaPerimeter = 2 * Math.PI * r;
    // Schwartzberg score = 1 / (Perimeter of district / C )
    return 1 / (perimeter / equalAreaPerimeter);

  }

  public boolean maintainsConstraints() {
    return isContiguous();
  }

  public boolean isContiguous() {
//    for (District d : redistricter.getWorkingDistricts()) {
//      for (District e : redistricter.getWorkingDistricts()) {
//        if (d.getPolygon().crosses(e.getPolygon())) {
//          return false;
//
//        }
//      }
//    }
    return true;
  }
}
