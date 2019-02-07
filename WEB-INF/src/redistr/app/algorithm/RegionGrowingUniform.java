package redistr.app.algorithm;

import redistr.app.messaging.UserParameters;

/**
 * @author Thomas Povinelli
 * Created 11/28/18
 * In CSE308
 */
public class RegionGrowingUniform extends RegionGrowing {

  private int iteration = 0;
  private int currentDistrictIndex = 0;

  public RegionGrowingUniform(UserParameters parameters, redistr.app.algorithm.State state) {
    this(parameters, state, 5);
  }

  public RegionGrowingUniform(UserParameters parameters, redistr.app.algorithm.State state, int stepsPerRequest) {
    super(parameters, state, stepsPerRequest);
  }


  @Override
  protected boolean runWhileCondition() {
    return iteration < RegionGrowing.MAX_ITERATIONS;
  }

  @Override
  public void stepAlgorithm() {
    for (int i = 0; i < super.workingDistricts.size(); i++) {
      Move move = getNextMove();
      iteration++;
      if (move == null) {
//        done = true;
        iteration += npeIterSkip;
        return;
      }
      moves.add(move);
      double newValue = function.calculateValue();
      move.setObjFnValue(newValue);
      move.makeMove();
      if (!function.maintainsConstraints() || newValue - currentBestObjValue < 0) {
        Move inverted = move.inverted(currentBestObjValue);
        inverted.makeMove();
        moves.add(inverted);
      } else {
        currentBestObjValue = newValue;
      }
    }
  }

  @Override
  public Move getNextMove() {
    try {
      int workingDistrictIdx = (int) (Math.random() * workingDistricts.size());
      District destination = workingDistricts.get(workingDistrictIdx);
      Precinct newPrecinct = randomFromSet(destination.getBorderPrecincts(), (b) -> b.getContainerDistrict() == unassigned);
      District source = newPrecinct.getContainerDistrict();
      return new Move(source, destination, newPrecinct);
    } catch ( NullPointerException e ) {
      return null;
    }
  }

}
