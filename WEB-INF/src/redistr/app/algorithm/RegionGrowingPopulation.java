package redistr.app.algorithm;

import redistr.app.messaging.UserParameters;

/**
 * @author Thomas Povinelli
 * Created 11/28/18
 * In CSE308
 */
public class RegionGrowingPopulation extends RegionGrowing {
  private int iteration = 0;

  public RegionGrowingPopulation(UserParameters parameters, redistr.app.algorithm.State state) {
    this(parameters, state, 5);
  }

  public RegionGrowingPopulation(UserParameters parameters, redistr.app.algorithm.State state, int stepsPerRequest) {
    super(parameters, state, stepsPerRequest);

  }


  @Override
  public String[] getSeedIds() {
    return new String[0];
  }

  @Override
  protected boolean runWhileCondition() {
    return unassigned.getPrecincts().size() != 0 || iteration < RegionGrowing.MAX_ITERATIONS;
  }

  @Override
  public void stepAlgorithm() {
    Move move = getNextMove();
    iteration++;
    if (move == null) {
      //      done = true;
      iteration += npeIterSkip;
      return;
    }
    moves.add(move);
    double newValue = function.calculateValue();
    move.makeMove();
    move.setObjFnValue(newValue);
    if (!function.maintainsConstraints() || newValue - currentBestObjValue < 0) {
      Move inverted = move.inverted(currentBestObjValue);
      inverted.makeMove();
      moves.add(inverted);
    } else {
      currentBestObjValue = newValue;
    }
  }


  @Override
  public Move getNextMove() {
    try {
//      District destination = workingDistricts.stream().reduce((r, a) -> r.getTotalPopulation() > a.getTotalPopulation() ? r : a).get();
      int workingDistrictIdx = (int) (Math.random() * workingDistricts.size());
      District destination = workingDistricts.get(workingDistrictIdx);
      Precinct borderPrecinct = randomFromSet(destination.getBorderPrecincts());
      Precinct newPrecinct = randomFromSet(borderPrecinct.getNeighbors(), (p) -> !p.isSeed() && p.getContainerDistrict().getId().equals("-1"));
      District source = newPrecinct.getContainerDistrict();
      return new Move(source, destination, newPrecinct);
    } catch (NullPointerException e) {
      return null;
    }

  }

}
