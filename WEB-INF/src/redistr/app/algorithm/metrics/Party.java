package redistr.app.algorithm.metrics;

/**
 * @author Thomas Povinelli
 * Created 2018-Dec-11
 * In API-Section1
 */
public class Party {
  public static final Party DEMOCRATIC = new Party("DEMOCRATIC");
  public static final Party REPUBLICAN = new Party("REPUBLICAN");
  public static final Party GREEN = new Party("GREEN");
  public static final Party LIBERTARIAN = new Party("LIBERTARIAN");
  public static final Party THIRD_PARTY = new Party("THIRD_PARTY");
  public static final Party WRITE_IN = new Party("WRITE_IN");

  public static Party valueOf(final String s) {
    switch ( s.toLowerCase() ) {
      case "democrat":
        return DEMOCRATIC;
      case "republican":
        return REPUBLICAN;
      case "green":
        return GREEN;
      case "libertarian":
        return LIBERTARIAN;
      case "none":
        return WRITE_IN;
      default:
        return THIRD_PARTY;
    }
  }

  private final String partyName;

  private Party( ) { partyName = null; }


  private Party(final String name) {
    this.partyName = name;
  }

  public Party getOpposite( ) {
    return this == DEMOCRATIC ? REPUBLICAN : DEMOCRATIC;
  }
}
