package redistr.app.algorithm;

import org.locationtech.jts.io.ParseException;
import redistr.app.algorithm.metrics.Party;
import redistr.app.interfaces.JSONSerializable;

import java.io.Serializable;

/**
 * @author Thomas Povinelli
 * Created 10/28/18
 * In CSE308
 */
public class Move implements JSONSerializable, Serializable {

  private District originalDistrict;
  private District destinationDistrict;
  private Precinct precinct;
  // for debugging
  private double objFnValue;

  public District getOriginalDistrict() {
    return originalDistrict;
  }

  public District getDestinationDistrict() {
    return destinationDistrict;
  }

  public Precinct getPrecinct() {
    return precinct;
  }

  public double getObjFnValue() {
    return objFnValue;
  }

  // end for debugging
  private boolean fromRevert;

  public Move(District originalDistrict, District destinationDistrict, Precinct precinct, double objFnValue) {
    this.originalDistrict = originalDistrict;
    this.destinationDistrict = destinationDistrict;
    this.precinct = precinct;
    this.objFnValue = objFnValue;
  }

  public Move(District originalDistrict, District destinationDistrict, Precinct precinct) {
    this.originalDistrict = originalDistrict;
    this.destinationDistrict = destinationDistrict;
    this.precinct = precinct;
    this.objFnValue = -1;
  }

  public boolean isFromRevert() {
    return fromRevert;
  }

  public void setFromRevert(final boolean fromRevert) {
    this.fromRevert = fromRevert;
  }

  public String toJSONString() {
    return String.format(
        "{\"originalDistrict\": \"%s\", \"destinationDistrict\": \"%s\", \"precinct\": \"%s\", \"functionValue\":\"%f\", \"fromRevert\": \"%s\"}",
        originalDistrict
            .getId(), destinationDistrict.getId(), precinct.getId(), objFnValue, String.valueOf(fromRevert)
                        );
  }

  public void makeMove() {

    originalDistrict.getPrecincts().remove(precinct);
    originalDistrict.getBorderPrecincts().remove(precinct);
    destinationDistrict.getPrecincts().add(precinct);
    destinationDistrict.getBorderPrecincts().add(precinct);

    Integer repVotes = precinct.getVotes().get(Party.REPUBLICAN);
    Integer demoVotes = precinct.getVotes().get(Party.DEMOCRATIC);

//    originalDistrict.getCongressionalElection(Year.of(2017)).computeVotes(Party.REPUBLICAN, (p, v) -> v - repVotes);
//    originalDistrict.getCongressionalElection(Year.of(2017)).computeVotes(Party.DEMOCRATIC, (p, v) -> v - demoVotes);
//
//    destinationDistrict.getCongressionalElection(Year.of(2017)).computeVotes(Party.REPUBLICAN, (p, v) -> v + repVotes);
//    destinationDistrict.getCongressionalElection(Year.of(2017)).computeVotes(Party.DEMOCRATIC, (p, v) -> v + demoVotes);


    originalDistrict.removePrecinctFromGeometry(precinct);
    try {
      destinationDistrict.addPrecinctToGeometry(precinct);
    } catch (ParseException e) {
      e.printStackTrace();
    }

    precinct.setContainerDistrict(destinationDistrict);

  }

  public Move inverted(double oldValue) {
    return new Move(destinationDistrict, originalDistrict, precinct, oldValue);
  }

  public void setObjFnValue(final double newValue) {
    this.objFnValue = newValue;
  }
}
