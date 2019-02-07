import gerrymandering.HibernateManager;
import gerrymandering.model.Precinct;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Thomas Povinelli
 * Created 12/8/18
 * In CSE308
 */
public class DatabaseModificationTest {

  public static void main(String[] args) throws Throwable {
    HibernateManager m = HibernateManager.getInstance();
    List<Precinct> allPrecincts = m.getAllRecords(Precinct.class)
                                   .stream()
                                   .map(Precinct.class::cast)
                                   .collect(Collectors.toList());
    Precinct p = allPrecincts.stream()
                             .filter(i -> i.getRemarks() != null)
                             .filter(i -> i.getRemarks().contains("UT"))
                             .findFirst()
                             .get();

    System.out.println(pToS(p));
    p.setRemarks("P-BGL-UT");
    m.persistToDB(p);

    allPrecincts = m.getAllRecords(Precinct.class)
                    .stream()
                    .map(Precinct.class::cast)
                    .collect(Collectors.toList());
    p = allPrecincts.stream()
                    .filter(i -> i.getPrecinctId() == 3948)
                    .findFirst()
                    .get();
  }

  public static String pToS(Precinct p) {
    return "Precinct(id=" + p.getPrecinctId() + ", exID=" + p.getExternalId() +
           ", dID=" + p.getDistrictId() + ", remarks=" + p.getRemarks() + ")";
  }

}
