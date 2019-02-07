package redistr.app.algorithm.metrics;

import redistr.app.algorithm.District;

public class DistrictMetricResult extends MetricResult {

  public DistrictMetricResult(District district) {
    this(district, false);
  }

  public DistrictMetricResult(District district, boolean stateLevelDetails) {
    super();
    addField("DISTRICT_NUM", district.getId());
    if (stateLevelDetails) {
//      addField("MAP_YEAR", district.getYear());
      addField("STATE_FIPS", district.getContainingState().getId());
      addField("STATE_NAME", district.getContainingState().getName());
    }
  }



  public String getStateName() { return (String) getValue("STATE_NAME"); }

  public int getDistrictNum() { return (int) getValue("DISTRICT_NUM"); }
}
