package redistr.app.messaging;

import redistr.app.algorithm.Abbreviation;

public class UserParameters {
  private double compactness;
  private double efficiencyGap;
  private double partisanFairness;
  private double populationEquality;
  private int numSeedDistricts;
  private String approach;
  private boolean done;
  private Abbreviation stateAbbr;
  private int year;

  @Override
  public String toString() {
    return "UserParameters{" +
           "compactness=" + compactness +
           ", efficiencyGap=" + efficiencyGap +
           ", partisanFairness=" + partisanFairness +
           ", populationEquality=" + populationEquality +
           ", numSeedDistricts=" + numSeedDistricts +
           ", approach='" + approach + '\'' +
           ", stateAbbr=" + stateAbbr +
           ", year=" + year +
           '}';
  }

  public double getCompactness() {
    return compactness;
  }

  public void setCompactness(final double compactness) {
    this.compactness = compactness;
  }

  public double getEfficiencyGap() {
    return efficiencyGap;
  }

  public void setEfficiencyGap(final double efficiencyGap) {
    this.efficiencyGap = efficiencyGap;
  }

  public double getPartisanFairness() {
    return partisanFairness;
  }

  public void setPartisanFairness(final double partisanFairness) {
    this.partisanFairness = partisanFairness;
  }

  public double getPopulationEquality() {
    return populationEquality;
  }

  public void setPopulationEquality(final double populationEquality) {
    this.populationEquality = populationEquality;
  }

  public int getNumSeedDistricts() {
    return numSeedDistricts;
  }

  public void setNumSeedDistricts(final int numSeedDistricts) {
    this.numSeedDistricts = numSeedDistricts;
  }

  public String getApproach() {
    return approach;
  }

  public void setApproach(final String approach) {
    this.approach = approach;
  }

  public boolean isDone() {
    return done;
  }

  public void setDone(final boolean done) {
    this.done = done;
  }

  public Abbreviation getStateAbbr() {
    return stateAbbr;
  }

  public void setStateAbbr(final Abbreviation stateAbbr) {
    this.stateAbbr = stateAbbr;
  }

  public int getYear() {
    return year;
  }

  public void setYear(final int year) {
    this.year = year;
  }
}
