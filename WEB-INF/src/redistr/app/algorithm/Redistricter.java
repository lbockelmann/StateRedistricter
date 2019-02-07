package redistr.app.algorithm;

import redistr.app.algorithm.metrics.ObjectiveFunction;
import redistr.app.messaging.MapUpdate;
import redistr.app.messaging.UserParameters;
import redistr.util.Properties;
import redistr.util.PropertyManager;
import redistr.util.RDLogger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static redistr.util.Properties.STEPS_PER_REQUEST;

public abstract class Redistricter extends Thread implements Serializable {
  protected final int npeIterSkip = PropertyManager.getInstance().get(Properties.NPE_ITER_SKIP, Integer.class);
  protected boolean done;
  protected UserParameters parameters;
  protected int stepsPerRequest = PropertyManager.getInstance()
                                                 .get(STEPS_PER_REQUEST, Integer.class);
  protected transient redistr.app.algorithm.State state;
  protected int iteration = 0;
  protected List<District> workingDistricts;
  protected ObjectiveFunction function;
  protected List<Move> moves = new ArrayList<>();
  protected int updateStart = 0;
  protected double currentBestObjValue;
  protected boolean timedOut;
  private String stateAbbr;
  private transient Lock lock = new ReentrantLock();
  private transient Condition condition = lock.newCondition();
  private int year;

  public Redistricter(UserParameters parameters, redistr.app.algorithm.State state) {
    this(parameters, state, 5);
  }

  public Redistricter(UserParameters parameters, redistr.app.algorithm.State state, int stepsPerRequest) {
    super.setDaemon(true);
    this.function = new ObjectiveFunction(this, parameters);
    this.parameters = parameters;
    this.stepsPerRequest = stepsPerRequest;
    this.state = state;
    this.stateAbbr = state.getAbbr();
    this.year = parameters.getYear();
    this.workingDistricts = state.getDistricts();
  }

  public List<District> getWorkingDistricts() {
    return workingDistricts;
  }

  public final void signalRequest(boolean keepGoing) {
    this.done = !keepGoing;
    lock.lock();
    try {
      condition.signal();
    } finally {
      lock.unlock();
    }
  }

  public final MapUpdate getUpdates() {
    lock.lock();
    MapUpdate ret;
    try {
      ret = composeUpdate();
    } finally {
      lock.unlock();
    }
    return ret;
  }

  protected MapUpdate composeUpdate() {
    List<Move> ret = new ArrayList<>(moves.size() - updateStart);
    int i;
    for (i = updateStart; i < moves.size(); i++) {
      ret.add(moves.get(i));
    }
    updateStart = i;
    return new MapUpdate(done, ret);
  }

  public <T> T randomFromSet(Set<T> things) {
    int size = things.size();
    int rnd = (int) (Math.random() * size);
    return things.stream().skip(rnd).findFirst().orElse(null);
  }

  public <T> T randomFromSet(Set<T> things, Predicate<T> predicate) {
    List<T> filtered = things.stream()
                             .filter(predicate)
                             .collect(Collectors.toList());
    int rnd = (int) (Math.random() * filtered.size());
    return filtered.stream().skip(rnd).findFirst().orElse(null);
  }


  @Override
  public void run() {
    while (runWhileCondition() && !done && !timedOut) {
      lock.lock();
      try {
        for (int i = 0; i < stepsPerRequest && runWhileCondition() && !done; i++) {
          stepAlgorithm();
        }
        waitForRequest();
      } catch (Exception e) {
        RDLogger.getLogger().throwing(getClass().getName(), "", e);
      } finally {
        lock.unlock();
      }

    }
    workingDistricts.stream()
                    .filter(x -> x.getId().equals("-1"))
                    .findFirst()
                    .ifPresent(d -> System.out.println(d.getPrecincts()));
    done = true;
  }

  protected abstract boolean runWhileCondition();

  public void stepAlgorithm() {
    Move move = getNextMove();
    iteration++;
    if (move == null) {
      //      done = true;
      iteration += npeIterSkip;
      return;
    }
    moves.add(move);
    move.makeMove();
    double newValue = function.calculateValue();
    move.setObjFnValue(newValue);
    if (!function.maintainsConstraints() || newValue - currentBestObjValue < 0) {
      Move inverted = move.inverted(currentBestObjValue);
      inverted.makeMove();
      moves.add(inverted);
    } else {
      currentBestObjValue = newValue;
    }
  }

  private void waitForRequest() {
    lock.lock();
    try {
      boolean requested = condition.await(2, TimeUnit.MINUTES);
      timedOut = !requested;
    } catch (Exception e) {
      RDLogger.getLogger().throwing(getClass().getName(), "", e);
    } finally {
      lock.unlock();
    }
  }

  public abstract Move getNextMove();

  public boolean isDone() {
    return done;
  }

  public int getYear() {

    return year;
  }

  public void setYear(final int year) {
    this.year = year;
  }

  public redistr.app.algorithm.State getCurrentState() {
    return this.state;
  }

  public int getCurrentIteration() {
    return iteration;
  }

  public List<Move> getMoves() {
    return moves;
  }

  public void resetWorkingDistricts() {

    this.workingDistricts = this.state.copyOfCurrentDistricts(this.year);
  }


}
