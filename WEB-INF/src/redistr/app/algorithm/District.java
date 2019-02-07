package redistr.app.algorithm;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import redistr.app.algorithm.metrics.CongressionalElection;
import redistr.app.interfaces.JSONSerializable;
import redistr.app.interfaces.RDCopying;

import java.io.Serializable;
import java.io.StringReader;
import java.time.Year;
import java.util.*;

/**
 * @author Thomas Povinelli
 * Created 10/27/18
 * In CSE308
 */
public class District implements Serializable, RDCopying<District> {
  private String id;
  private int databaseId;
  private State containingState;
  private Set<Precinct> precincts;
  private Set<Precinct> borderPrecincts;
  private String boundaryData;
  // Used for calculating compactness metrics
  private transient GeometryCollection gc;
  // used for calculating compactness to keep track of when
  // geometry needs recalculating
  private transient int lastCalculatedOnIteration = -1;
  private boolean initializedGeometry = false;
  private HashMap<Precinct, Geometry> precinctGeometries;
  private HashMap<Year, CongressionalElection> congressionalElections = new HashMap<>();

  public District(gerrymandering.model.District d) {
    this.precincts = new HashSet<>();
    this.borderPrecincts = new HashSet<>();
    this.boundaryData = d.getBoundary();
    this.id = d.getExternalId();
    this.databaseId = d.getDistrictId();
  }

  public District() {
    this.precincts = new HashSet<>();
    this.borderPrecincts = new HashSet<>();
    this.precinctGeometries = new HashMap<>();
  }

  public District(final int id) {
    this.id = String.valueOf(id);
    this.precincts = new HashSet<>();
    this.borderPrecincts = new HashSet<>();
    this.precinctGeometries = new HashMap<>();
  }

  public void addCongressionalElectionData(Year year, CongressionalElection election) {
    congressionalElections.put(year, election);
  }

  public boolean isInitializedGeometry() {
    return initializedGeometry;
  }

  public double getArea(int iteration) {
    try {
      if (!initializedGeometry) {
        gc = initializeGeometry();
      }
      if (iteration > lastCalculatedOnIteration) {
        gc = updateGeometry();
        lastCalculatedOnIteration = iteration;
      }
      return gc.getArea();

    } catch (Exception e) {
      e.printStackTrace();
      return 0.0;
    }
    //    return Math.random() * 10000;
  }

  public GeometryCollection initializeGeometry() throws ParseException {
    GeoJsonReader r = new GeoJsonReader();
    GeometryFactory gf = new GeometryFactory();

    precinctGeometries = new HashMap<>();
    // TODO: Maybe only use border precincts since the border gives us perimeter and area and is all we need
    // TODO: cache precinctGeometries list and add and remove it so that we only have to update the
    //       geometry collection rather than re-parsing all the geojson
    for (Precinct p : precincts) {
      addPrecinctToGeometry(p);
    }
    initializedGeometry = true;
    GeometryCollection geometryCollection = gf.createGeometryCollection(precinctGeometries.values().toArray(new Geometry[0]));
    if (geometryCollection == null) {
      throw new NullPointerException("Geometry collection created by factory was null");
    }
    //    gc = geometryCollection;
    //
    //    ArrayList<Polygon> dpolys = new ArrayList<>();
    //
    //    for (int i = 0; i < getGeometry().getNumGeometries(); i++) {
    //      dpolys.add(gf.createPolygon(gf.createLinearRing(getGeometry().getGeometryN(i).getCoordinates())));
    //    }
    //    System.out.println("Calculating district polygons");
    //
    //    System.out.println("Start unioning!!!!!");
    //    polygon = dpolys.get(0);
    //    for (Polygon p : dpolys) {
    //      polygon = polygon.union(p);
    //    }
    //    System.out.println("Done unioning!!!!!");
    return gc;

  }


  public GeometryCollection updateGeometry() {
    GeometryFactory gf = new GeometryFactory();
    return gf.createGeometryCollection(precinctGeometries.values().toArray(new Geometry[0]));
  }

  public void addPrecinctToGeometry(Precinct p) throws ParseException {
    String boundaryData = p.getBoundaryData();
    GeometryFactory gf = new GeometryFactory();
    Gson gson = new GsonBuilder().create();
    try {
      MultiPolygonFeature precinctMultiPolygonFeature = gson.fromJson(new StringReader(boundaryData), MultiPolygonFeature.class);
      MultiPolygonGeometry multiPolygonGeometry = precinctMultiPolygonFeature.geometry;
      Geometry geo = new GeoJsonReader().read(multiPolygonGeometry.toJSONString());
      precinctGeometries.put(p, geo);
      //      Polygon ply = gf.createPolygon(gf.createLinearRing(geo.getCoordinates()));
      //      polygon = polygon.union(ply);
    } catch (JsonSyntaxException e) {
      PolygonFeature precinctPolygonFeature = gson.fromJson(new StringReader(boundaryData), PolygonFeature.class);
      PolygonGeometry PolygonGeometry = precinctPolygonFeature.geometry;
      Geometry geo = new GeoJsonReader().read(PolygonGeometry.toJSONString());
      precinctGeometries.put(p, geo);
      //      Polygon ply = gf.createPolygon(gf.createLinearRing(geo.getCoordinates()));
      //      polygon = polygon.union(ply);
    }
  }

  //  public void initPrecinctInGeometry(Precinct p) throws ParseException {
  //    String boundaryData = p.getBoundaryData();
  //    GeometryFactory gf = new GeometryFactory();
  //    Gson gson = new GsonBuilder().create();
  //    try {
  //      MultiPolygonFeature precinctMultiPolygonFeature = gson.fromJson(new StringReader(boundaryData), MultiPolygonFeature.class);
  //      MultiPolygonGeometry multiPolygonGeometry = precinctMultiPolygonFeature.geometry;
  //      Geometry geo = new GeoJsonReader().read(multiPolygonGeometry.toJSONString());
  //      precinctGeometries.put(p, geo);
  //    } catch (JsonSyntaxException e) {
  //      PolygonFeature precinctPolygonFeature = gson.fromJson(new StringReader(boundaryData), PolygonFeature.class);
  //      PolygonGeometry PolygonGeometry = precinctPolygonFeature.geometry;
  //      Geometry geo = new GeoJsonReader().read(PolygonGeometry.toJSONString());
  //      precinctGeometries.put(p, geo);
  //    }
  //  }


  public GeometryCollection getGeometry() {
    gc = updateGeometry();
    return gc;
  }

  public double getPerimeter(int iteration) {
    try {
      if (!initializedGeometry) {
        gc = initializeGeometry();
      } else if (iteration > lastCalculatedOnIteration) {
        gc = updateGeometry();
        lastCalculatedOnIteration = iteration;
      }
      return gc.getLength();
    } catch (Exception e) {
      e.printStackTrace();
      return 0.0;
    }
  }

  public void removePrecinctFromGeometry(Precinct p) {
    precinctGeometries.remove(p);
  }

  public int getDatabaseId() {
    return databaseId;
  }

  public void setDatabaseId(int databaseId) {
    this.databaseId = databaseId;
  }

  public State getContainingState() {
    return containingState;
  }

  public void setContainingState(State containingState) {
    this.containingState = containingState;
  }

  @Override
  public String toString() {
    return id;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Set<Precinct> getPrecincts() {
    return precincts;
  }

  public void setPrecincts(Set<Precinct> precincts) {
    this.precincts = precincts;
  }

  public Set<Precinct> getBorderPrecincts() {
    return borderPrecincts;
  }

  public void setBorderPrecincts(Set<Precinct> borderPrecincts) {
    this.borderPrecincts = borderPrecincts;
  }

  public int getTotalPopulation() {
    return precincts.stream().mapToInt(Precinct::getPopulation).sum();
  }

  public String getBoundaryData() {
    return boundaryData;
  }

  public void setBoundaryData(final String boundaryData) {
    this.boundaryData = boundaryData;
  }

  public CongressionalElection getCongressionalElection(final Year year) {
    return congressionalElections.get(year);
  }

  public District copy() {
    District ret = new District();
    ret.id = this.id;
    ret.databaseId = this.databaseId;
    ret.containingState = this.containingState;
    ret.precincts = copyOfList(this.precincts);
    ret.borderPrecincts = this.copyOfList(borderPrecincts);
    ret.boundaryData = this.boundaryData;
    ret.gc = (GeometryCollection) new GeometryFactory().createGeometry(gc);
    ret.lastCalculatedOnIteration = lastCalculatedOnIteration;
    ret.initializedGeometry = initializedGeometry;
    ret.precinctGeometries = copyOfList(precinctGeometries);
    ret.congressionalElections = new HashMap<>();
    return ret;
  }

  private <T extends RDCopying<T>> Set<T> copyOfList(final Set<T> precincts) {
    Set<T> ret = new HashSet<>();
    for (T val : precincts) {
      ret.add(val.copy());
    }
    return ret;
  }

  private <K extends RDCopying<K>, V > HashMap<K, V> copyOfList(final HashMap<K,V> precinctGeometries) {
    HashMap<K, V> ret = new HashMap<>();
    for (Map.Entry<K, V> entry : precinctGeometries.entrySet()) {
      ret.put(entry.getKey().copy(), entry.getValue());
    }
    return ret;
  }


  private class MultiPolygonFeature implements JSONSerializable {
    String type;
    MultiPolygonGeometry geometry;
    Object properties;

    @Override
    public String toJSONString() {
      return String.format("\"type\": \"%s\", \"geometry\": %s", type, (geometry == null ? "null" : geometry.toJSONString()));
    }
  }

  private class MultiPolygonGeometry implements JSONSerializable {
    String type;
    List<List<List<List<Double>>>> coordinates;


    @Override
    public String toJSONString() {
      return String.format("{\"type\": \"%s\", \"coordinates\":%s}", type, coordinates.toString());
    }
  }

  private class PolygonFeature implements JSONSerializable {
    String type;
    PolygonGeometry geometry;
    Object properties;

    @Override
    public String toJSONString() {
      return String.format("\"type\": \"%s\", \"geometry\": %s", type, (geometry == null ? "null" : geometry.toJSONString()));
    }
  }

  private class PolygonGeometry implements JSONSerializable {
    String type;
    List<List<List<Double>>> coordinates;


    @Override
    public String toJSONString() {
      return String.format("{\"type\": \"%s\", \"coordinates\":%s}", type, coordinates.toString());
    }
  }

}
