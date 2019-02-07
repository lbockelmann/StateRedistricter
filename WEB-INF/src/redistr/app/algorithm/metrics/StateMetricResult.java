package redistr.app.algorithm.metrics;

import redistr.app.algorithm.State;

import java.time.Year;
import java.util.List;

/**
 * @author Thomas Povinelli
 * Created 2018-Dec-11
 * In CSE308
 */
public class StateMetricResult extends MetricResult {

  public StateMetricResult(State state, Year year) {
    super();
    addField("STATE_NAME", state.getName());
    addField("STATE_FIPS", state.getId());
    if (year != null) {
      addField("MAP_YEAR", year.getValue());
    }
  }

  public List<DistrictMetricResult> getDistrictResults() { return (List<DistrictMetricResult>) getValue("DISTRICTS"); }

  public void setDistrictResults(List<DistrictMetricResult> results) {
    addField("DISTRICTS", results);
  }

  public String getStateName() { return (String) getValue("STATE_NAME"); }

  public void removeDistrictResults() {
    removeField("DISTRICTS");
  }

}
