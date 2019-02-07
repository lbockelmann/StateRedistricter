package redistr.app.http;

import redistr.app.algorithm.State;
import redistr.app.interfaces.JSONSerializable;
import redistr.util.IOUtil;

import java.util.List;

/**
 * @author Thomas Povinelli
 * Created 11/27/18
 * In CSE308
 */
public class AllStatesResponse implements JSONSerializable {
  public List<State> states;

  public AllStatesResponse(List<State> states) {
    this.states = states;
  }

  public List<State> getStates() {
    return states;
  }

  public void setStates(List<State> states) {
    this.states = states;
  }

  @Override
  public String toJSONString() {
    String jsonTemplate = "{\"states\": %s}";
    return String.format(jsonTemplate, IOUtil.listToJSONString(states));
  }
}
