package redistr.app.algorithm.metrics;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * @author Thomas Povinelli
 * Created 2018-Dec-11
 * In API-Section1
 */
public class CongressionalElection {
 public Map<Party, Integer> votes = new HashMap<>();
  public int getVotes(Party party) {
    return votes.get(party);
  }
  public void putVotes(Party party, int votes) {
    this.votes.put(party, votes);
  }
  public void computeVotes(Party p, BiFunction<Party, Integer, Integer> mapper) {
    this.votes.compute(p, mapper);
  }
}
