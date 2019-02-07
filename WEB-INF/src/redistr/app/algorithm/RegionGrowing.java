package redistr.app.algorithm;

import redistr.app.messaging.UserParameters;
import redistr.util.Properties;
import redistr.util.PropertyManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Thomas Povinelli
 * Created 2018-Nov-29
 * In CSE308
 */
public abstract class RegionGrowing extends Redistricter {

  public static final int MAX_ITERATIONS = PropertyManager.getInstance().get(Properties.RG_MAX_ITERATIONS, Integer.class);
  protected District unassigned = new District(-1);
  String[] seedIds;


  public RegionGrowing(UserParameters parameters, redistr.app.algorithm.State state) {
    this(parameters, state, 5);
  }

  public RegionGrowing(UserParameters parameters, redistr.app.algorithm.State state, int stepsPerRequest) {
    super(parameters, state, stepsPerRequest);
    ArrayList<String> ids = new ArrayList<>();
    for (int i = 0; i < parameters.getNumSeedDistricts(); i++) {
      ids.add(randomFromSet(randomFromList(getWorkingDistricts()).getPrecincts()).getId());
    }
    seedIds = ids.toArray(new String[0]);
    for (District d : workingDistricts) {
      List<Precinct> removal = new ArrayList<>();
      for (Precinct p : d.getPrecincts()) {
        if (Arrays.stream(seedIds).noneMatch(id -> p.getId().equals(id))) {
          unassigned.getPrecincts().add(p);
          removal.add(p);
          p.setContainerDistrict(unassigned);
          d.removePrecinctFromGeometry(p);
        } else {
          d.getBorderPrecincts().add(p);
          p.setSeed(true);
        }
      }
      for (Precinct p : removal) {
        d.getPrecincts().remove(p);
        d.getBorderPrecincts().remove(p);
      }
    }
  }


  private <T> T randomFromList(final List<T> workingDistricts) {
    int i = (int) (Math.random() * workingDistricts.size());
    return workingDistricts.get(i);
  }


  public  String[] getSeedIds() {
    return seedIds;
  }
}
