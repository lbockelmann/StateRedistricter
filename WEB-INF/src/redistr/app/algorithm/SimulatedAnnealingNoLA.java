package redistr.app.algorithm;

import redistr.app.messaging.UserParameters;
import redistr.util.PropertyManager;
import redistr.util.RDLogger;

import static redistr.util.Properties.SA_MAX_ITERATIONS;

/**
 * @author Thomas Povinelli
 * Created 10/28/18
 * In CSE308
 */
public class SimulatedAnnealingNoLA extends Redistricter {

  public SimulatedAnnealingNoLA(UserParameters parameters, redistr.app.algorithm.State state) {
    this(parameters, state, 5);
  }

  public SimulatedAnnealingNoLA(UserParameters parameters, redistr.app.algorithm.State state, int stepsPerRequest) {
    super(parameters, state, stepsPerRequest);
  }

  @Override
  protected boolean runWhileCondition() {
    return iteration <
           PropertyManager.getInstance().get(SA_MAX_ITERATIONS, Integer.class);
  }

  /**
   * Retrieve a random source and destination district and the precinct that moves
   * between them
   *
   * @return a move of a random precinct from one random district to a random neighbor
   */
  @Override
  public Move getNextMove() {
    try {
      int workingDistrictIdx = (int) (Math.random() * workingDistricts.size());
      District destDistrict = workingDistricts.get(workingDistrictIdx);
      Precinct borderPrecinct = randomFromSet(destDistrict.getBorderPrecincts());
      Precinct movingPrecinct = randomFromSet(borderPrecinct.getNeighbors(), p -> p.getContainerDistrict() != destDistrict);
      District sourceDistrict = movingPrecinct.getContainerDistrict();
      return new Move(sourceDistrict, destDistrict, movingPrecinct);
    } catch ( NullPointerException e ) {
      RDLogger.getLogger().throwing(getClass().getName(), "getNextMove", e);
      return null;
    }
  }
}
