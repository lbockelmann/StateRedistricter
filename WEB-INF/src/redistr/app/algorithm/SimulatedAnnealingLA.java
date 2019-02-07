package redistr.app.algorithm;

import redistr.app.messaging.UserParameters;
import redistr.util.PropertyManager;

import java.util.LinkedList;

import static redistr.util.Properties.SA_MAX_ITERATIONS;
import static redistr.util.Properties.SA_MAX_LOOKAHEAD;

/**
 * @author Thomas Povinelli
 * Created 10/28/18
 * In CSE308
 */
public class SimulatedAnnealingLA extends Redistricter {

  private LinkedList<Move> lookAhead = new LinkedList<>();
  private boolean looking = false;
  private double lookaheadTarget;

  public SimulatedAnnealingLA(UserParameters parameters, redistr.app.algorithm.State state) {
    this(parameters, state, 5);
  }

  public SimulatedAnnealingLA(UserParameters parameters, redistr.app.algorithm.State state, int stepsPerRequest) {
    super(parameters, state, stepsPerRequest);
  }

  @Override
  protected boolean runWhileCondition() {
    return iteration <
           PropertyManager.getInstance().get(SA_MAX_ITERATIONS, Integer.class);
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
    double newValue = function.calculateValue();
    move.setObjFnValue(newValue);
    move.makeMove();
    moves.add(move);

    if (function.maintainsConstraints() && newValue > currentBestObjValue) {
      lookAhead.clear();
      currentBestObjValue = newValue;
    } else {
      lookAhead.add(move);
      if (lookAhead.size() == PropertyManager.getInstance().get(SA_MAX_LOOKAHEAD, Integer.class))
      {
        // be sure to go backwards
        for (int i = lookAhead.size() - 1; i >= 0; i--) {
          Move move1 = lookAhead.get(i);
          Move inverted = move1.inverted(currentBestObjValue);
          inverted.makeMove();
          moves.add(inverted);
          move1.setFromRevert(true);
        }
        lookAhead.clear();
      }
    }
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
      Precinct movingPrecinct = randomFromSet(borderPrecinct.getNeighbors(), (p) -> p.getContainerDistrict() != destDistrict);
      District sourceDistrict = movingPrecinct.getContainerDistrict();
      return new Move(sourceDistrict, destDistrict, movingPrecinct);
    } catch (NullPointerException e) {
      return null;
    }
  }

}
