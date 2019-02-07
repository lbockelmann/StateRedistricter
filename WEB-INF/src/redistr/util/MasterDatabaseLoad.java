package redistr.util;

import gerrymandering.HibernateManager;
import gerrymandering.model.District;
import gerrymandering.model.Precinct;
import gerrymandering.model.State;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Thomas Povinelli
 * Created 2018-Nov-29
 * In CSE308
 */


public class MasterDatabaseLoad {
  public static void main(String[] args) throws Throwable {
    HibernateManager m = HibernateManager.getInstance();
    //
//        int utahId = ((State) m.getRecordsBasedOnCriteria(State.class, new HashMap<String, Object>() {{
//          put("name", "Utah");
//        }}).get(0)).getStateId();
        int nmid = ((State) m.getRecordsBasedOnCriteria(State.class, new HashMap<String, Object>() {{
          put("name", "New Mexico");
        }}).get(0)).getStateId();
//    int scid = ((State) m.getRecordsBasedOnCriteria(State.class, new HashMap<String, Object>() {{
//      put("name", "South Carolina");
//    }}).get(0)).getStateId();
//
//    System.out.println("Starting SC Precincts");
//    addPrecincts(m, "SC", scid);
//    System.out.println("DONE SC PRECINCTS");

//    System.out.println("Starting UT Precincts");
//    addPrecincts(m, "UT", utahId);
//    System.out.println("DONE UT PRECINCTS");

    System.out.println("Starting NM Precincts");
    addPrecincts(m, "NM", nmid);
    System.out.println("DONE NM PRECINCTS");



    //    showMasterDataForModel(Precinct.class);


  }

  private static void addPrecincts(final HibernateManager m, String stateAbbr, int stateId) throws Throwable {
    FileReader reader = new FileReader(System.getProperty("user.dir") + "/js/Maps/" + stateAbbr + "/2017/" + stateAbbr + "_zoom7_0.geojson");

    String json = String.join("\n", (new BufferedReader(reader)).lines().collect(Collectors.toList()));
    JSONObject o = new JSONObject(json);
    JSONArray a = o.getJSONArray("features");
    for (int i = 0; i < a.length(); i++) {
      JSONObject feature = a.getJSONObject(i);
      String centerPoint = "[0.0, 0.0]";
      String pid = "p-1";
      int did = -1;
      try {


        JSONObject properties = feature.getJSONObject("properties");
        JSONObject geometry = feature.getJSONObject("geometry");

        pid = properties.getString("id");
        did = Integer.parseInt(properties.getString("containing_district_id"));
        centerPoint = properties.getJSONArray("center_point").toString();
        int population = 0;
        population = Integer.parseInt(properties.getString("population"));

      } catch (JSONException e) {
        System.out.println("Precinct error!");
        e.printStackTrace();
        continue;
      }
      List<Object> containerState = m.getRecordsBasedOnCriteria(District.class, new HashMap<String, Object>() {{
        put("stateId", stateId);
      }});


      final int finalDid = did;
      int container = containerState.stream()
                                    .map(District.class::cast)
                                    .filter(d -> Integer.parseInt(d.getExternalId()) == finalDid)
                                    .findFirst()
                                    .get()
                                    .getDistrictId();


      String teamName = "Bengals";
      String boundaryData = feature.toString();//geometry.toString();
      Precinct p = new Precinct(container, centerPoint, boundaryData, teamName, "BGL-" + stateAbbr);
      p.setExternalId(pid);

      m.persistToDB(p);

    }
  }

  private static void addDistricts(final HibernateManager m, String stateAbbr, int stateId) throws Throwable {
    FileReader reader = new FileReader(System.getProperty("user.dir") + "\\js\\Maps\\" + stateAbbr + "\\2017\\" + stateAbbr + "_2017_D.geojson");

    String json = String.join("\n", (new BufferedReader(reader)).lines().collect(Collectors.toList()));
    JSONObject o = new JSONObject(json);
    JSONArray a = o.getJSONArray("features");
    for (int i = 0; i < a.length(); i++) {
      JSONObject feature = a.getJSONObject(i);
      JSONObject properties = feature.getJSONObject("properties");
      JSONObject geometry = feature.getJSONObject("geometry");

      String did = properties.getString("id");
      //      String centerPoint = properties.getJSONArray("center_point").toString();


      String teamName = "Bengals";
      String boundaryData = feature.toString();//geometry.toString();
      District p = new District(stateId, "", boundaryData, teamName, "BGL-" + stateAbbr);
      p.setExternalId(did);

      m.persistToDB(p);

    }
  }

  public static <T> void showMasterDataForModel(Class<T> modelClass) {
    List<Object> o = null;
    try {
      o = HibernateManager.getInstance().getAllRecords(modelClass);
      for (Object a : o) {
        T s = modelClass.cast(a);
        System.out.print("Precinct(");
        for (Field f : modelClass.getDeclaredFields()) {
          f.setAccessible(true);
          String fieldName = f.getName();
          Object fieldValue = f.get(s);
          if (fieldName.contains("boundary")) {
            fieldValue = ((String) fieldValue).substring(0, 20) + "...";
          }
          System.out.print(fieldName + "=" + fieldValue + ", ");

        }
        RDLogger.getLogger().info("}");
      }
    } catch (Throwable throwable) {
      RDLogger.getLogger().throwing(MasterDatabaseLoad.class.getName(), "", throwable);
    }
  }
}
