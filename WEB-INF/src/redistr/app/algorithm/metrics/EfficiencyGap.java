package redistr.app.algorithm.metrics;

import redistr.app.algorithm.District;
import redistr.app.algorithm.State;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Thomas Povinelli
 * Created 2018-Dec-11
 * In CSE308
 */

public class EfficiencyGap  {

  public StateMetricResult calculate(State state, Year year) {
    List<District> districts = state.getDistricts(year);
    if (districts.isEmpty()) {
      throw new DataNotFoundException();
    }
    if (districts.size() == 1) {
      throw new NotEnoughDistrictsException();
    }
    int demWastedVotes = 0;
    int repWastedVotes = 0;
    int totalVotes = 0;
    List<DistrictMetricResult> districtResults = new ArrayList<>();
    for (District d : districts) {
      DistrictMetricResult districtResult = calculate(d, year);
      districtResults.add(districtResult);
      demWastedVotes += (Integer) (districtResult.getProperty("DEM_WASTED"));
      repWastedVotes += (Integer) (districtResult.getProperty("REP_WASTED"));
      totalVotes += (Integer) (districtResult.getProperty("TOTAL_VOTES"));
    }
    StateMetricResult result = new StateMetricResult(state, year);
    if(demWastedVotes <= repWastedVotes)
      result.setScore(((double) Math.abs(demWastedVotes - repWastedVotes)) / totalVotes);
    else
      result.setScore(((double) Math.abs(repWastedVotes - demWastedVotes)) / totalVotes);
    result.setDistrictResults(districtResults);
    result.addProperty("DEM_WASTED", demWastedVotes);
    result.addProperty("REP_WASTED", repWastedVotes);
    return result;
  }


  protected DistrictMetricResult calculate(District district, Year year) {
    CongressionalElection electResult = district.getCongressionalElection(year);
    if (electResult == null) {
      throw new DataNotFoundException();
    }
    Party losing = getLosingParty(electResult);
    int totalVotesLosingParty = electResult.getVotes(losing);
    int totalVotesCast = electResult.getVotes(Party.DEMOCRATIC) + electResult.getVotes(Party.REPUBLICAN);
    int totalVotesMargin = electResult.getVotes(losing.getOpposite()) - electResult.getVotes(losing);
    int wastedVotesWinning = (int)Math.abs(0.5 * (totalVotesCast) - electResult.getVotes(losing.getOpposite()));
    DistrictMetricResult result = new DistrictMetricResult(district);
    result.setScore(((double) Math.abs(totalVotesLosingParty - wastedVotesWinning)) / totalVotesCast);
    if(losing == Party.DEMOCRATIC) {
      result.addProperty("DEM_WASTED", totalVotesLosingParty);
      result.addProperty("REP_WASTED", wastedVotesWinning);
    }else{
      result.addProperty("REP_WASTED", totalVotesLosingParty);
      result.addProperty("DEM_WASTED", wastedVotesWinning);
    }
    result.addProperty("LOSING_PARTY", losing);
    result.addProperty("LOSING_VOTES", totalVotesLosingParty);
    result.addProperty("TOTAL_VOTES", totalVotesCast);
    result.addProperty("VOTE_MARGIN", totalVotesMargin);
    return result;
  }

  private Party getLosingParty(CongressionalElection electResult) {
    int demVotes = electResult.getVotes(Party.DEMOCRATIC);
    int repVotes = electResult.getVotes(Party.REPUBLICAN);
    if (demVotes >= repVotes) {
      return Party.REPUBLICAN;
    } else {
      return Party.DEMOCRATIC;
    }
  }

}
