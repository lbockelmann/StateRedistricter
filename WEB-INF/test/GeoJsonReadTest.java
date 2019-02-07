//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//import com.google.gson.annotations.Expose;
//import org.locationtech.jts.geom.Geometry;
//import org.locationtech.jts.geom.GeometryCollection;
//import org.locationtech.jts.geom.GeometryFactory;
//import org.locationtech.jts.io.ParseException;
//import org.locationtech.jts.io.geojson.GeoJsonReader;
//import redistr.app.algorithm.Precinct;
//import redistr.app.interfaces.JSONSerializable;
//
//import java.io.StringReader;
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * @author Thomas Povinelli
// * Created 2018-Dec-13
// * In CSE308
// */
//public class GeoJsonReadTest {
//  private GeometryCollection updateDistrictGeometry() throws ParseException {
//    GeoJsonReader r = new GeoJsonReader();
//    GeometryFactory gf = new GeometryFactory();
//
//    ArrayList<Geometry> precinctGeometries = new ArrayList<>();
//    // TODO: Border precincts?
//    for (Precinct p : precincts) {
//      String boundaryData = p.getBoundaryData();
//      Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
//      GeoJsonObject gj = gson.fromJson(new StringReader(boundaryData), GeoJsonObject.class);
//      List<Geometry> precinctPieces = new ArrayList<>();
//      for (FeatureObject gobj : gj.features) {
//        Geometry innerG = r.read(gobj.geometryObject.toJSONString());
//        precinctGeometries.add(innerG);
//      }
//      Geometry precinctGeo = gf.createGeometryCollection(GeometryFactory.toGeometryArray(precinctPieces));
//      precinctGeometries.add(precinctGeo);
//    }
//
//    return gf.createGeometryCollection(precinctGeometries.toArray(new Geometry[0]));
//    //    return null;
//  }
//
//  private class GeoJsonObject {
//    @Expose
//    String type;
//    @Expose
//    FeatureObject[] features;
//  }
//
//  private class FeatureObject {
//    @Expose
//    String type;
//    @Expose
//    GeometryObject geometryObject;
//    Object properties;
//  }
//
//  private class GeometryObject implements JSONSerializable {
//    @Expose
//    String type;
//    @Expose
//    Double[][][][] points;
//
//
//    @Override
//    public String toJSONString() {
//      return String.format("{\"type\": \"%s\", \"coordinates\":%s}", type, points);
//    }
//  }
//}

