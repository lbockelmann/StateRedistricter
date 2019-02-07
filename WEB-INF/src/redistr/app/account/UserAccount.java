package redistr.app.account;

import org.ini4j.Ini;
import redistr.app.algorithm.Move;
import redistr.app.algorithm.State;
import redistr.util.RDLogger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class UserAccount {

  private static String servletPath;
  private static Ini propertiesFile;
  private static String propertiesFileName;
  private static Ini accountsFile;
  private static Ini weightsFile;

  public static String getServletPath() { return servletPath; }

  public static void setServletPath(final String servletPath) {
    UserAccount.servletPath = servletPath;
  }

  public static void initializeAccountIni() {
    try {
      File accountsFile = new File(accountsFileName());
      accountsFile.createNewFile();
      UserAccount.accountsFile = new Ini(accountsFile);
      UserAccount.accountsFile.put("AccountNumber", "Key", "Value");
    } catch (IOException e) {
      RDLogger.getLogger().throwing(UserAccount.class.getName(), "", e);
    }
  }

  private static String accountsFileName() {
    return Paths.get(servletPath, "accounts.ini").toString();
  }

  public static boolean validateLogin(String username, String password) {
    boolean success = false;
    try {
      File input = new File(accountsFileName());
      input.createNewFile();
      accountsFile = new Ini(input);
      Map<String, String> map = accountsFile.get(username);
      if (map != null && !map.isEmpty()) {
        success = map.get("password").equals(password);
      }


    } catch (IOException e) {
      RDLogger.getLogger().throwing(UserAccount.class.getName(), "validateLogin", e);
    }
    return success;
  }

  public static boolean createAccount(String username, String password) {
    boolean success = false;
    try {
      File input = new File(accountsFileName());
      input.createNewFile();
      accountsFile = new Ini(input);
      Ini.Section section = accountsFile.get(username);
      if (section == null) {
        accountsFile.put(username, "password", password);
        accountsFile.put(username, "isAdmin", username.equalsIgnoreCase("admin"));
        accountsFile.store();
        success = true;
      }
    } catch (IOException e) {
      RDLogger.getLogger().throwing(UserAccount.class.getName(), "createAccount", e);
    }
    return success;
  }

  public static String getAccounts() {
    StringBuilder accounts = new StringBuilder();
    try {
      File input = new File(accountsFileName());
      input.createNewFile();

      accountsFile = new Ini(input);
      Set<String> keyset = accountsFile.keySet();
      for (final String aKeyset : keyset) {
        accounts.append(aKeyset).append("\n");
      }

    } catch (IOException e) {
      RDLogger.getLogger().throwing(UserAccount.class.getName(), "getAccounts", e);
    }

    return accounts.toString();
  }

  public static boolean deleteAccount(String username) {
    boolean success = false;

    try {
      File input = new File(accountsFileName());
      input.createNewFile();
      accountsFile = new Ini(input);

      if (accountsFile.containsKey(username)) {
        Ini.Section section = accountsFile.get(username);
        accountsFile.remove(section);
        accountsFile.store();
        success = true;
      }
    } catch (IOException e) {
      RDLogger.getLogger().throwing(UserAccount.class.getName(), "deleteAccount", e);
    }
    return success;
  }

  public static boolean saveWeights(String username) {
    String[] values = username.split(",");
    String user = values[0];
    String compactness = values[1];
    String efficiencyGap = values[2];
    String partisianFairness = values[3];
    String populationEquality = values[4];
    String numSeedDistricts = values[5];
    String approach = values[6];
    String year = values[7];
    try {
      File input = new File(weightsFileName());
      input.createNewFile();
      weightsFile = new Ini(input);
      Ini.Section section = weightsFile.get(user);
      weightsFile.put(user, "compactness", compactness);
      weightsFile.put(user, "efficiencyGap", efficiencyGap);
      weightsFile.put(user, "partisianFairness", partisianFairness);
      weightsFile.put(user, "populationEquality", populationEquality);
      weightsFile.put(user, "numSeedDistricts", numSeedDistricts);
      weightsFile.put(user, "approach", approach);
      weightsFile.put(user, "year", year);
      weightsFile.store();
      return true;

    } catch (ArrayIndexOutOfBoundsException | IOException e) {
      RDLogger.getLogger().throwing(UserAccount.class.getName(), "saveWeights", e);
      return false;
    }

  }

  private static String weightsFileName() { return Paths.get(servletPath, "weights.ini").toString(); }

  public static boolean saveMap(final String username, State state, int year, boolean done, final List<Move> moves) {
    File input = new File(mapsFileName());
    try {
      input.createNewFile();
      Ini mapsFile = new Ini(input);
      Ini.Section userSession = mapsFile.get(username);

      if (userSession == null) {
        userSession = mapsFile.add(username);
      }

      String s = mapsFile.fetch(username, "numSaves");
      int saves = 0;
      if (s != null) {
        saves = Integer.parseInt(s);
        userSession.remove("numSaves");
      }
      saves++;

      userSession.put("numSaves", saves);

      // update list

      String savesListString = userSession.get("savesList");
      String newSaveList = "1";
      int max = 0;
      if (savesListString != null) {
        String[] saveList = savesListString.split(",");
        max = Integer.parseInt(saveList[saveList.length - 1]);

        newSaveList = String.join(",", saveList) + "," + (max + 1);
      }
      userSession.remove("savesList");
      userSession.put("savesList", newSaveList);

      Ini.Section section = userSession.addChild("save" + (max + 1));

      section.add("state", state.getAbbr());
      section.add("year", year);
      section.add("done", done);


      for (int idx = 0; idx < moves.size(); idx++) {
        Ini.Section move = section.addChild("move" + idx);

        move.add("originalDistrict", moves.get(idx).getOriginalDistrict().getId());
        move.add("destinationDistrict", moves.get(idx).getDestinationDistrict().getId());
        move.add("precinct", moves.get(idx).getPrecinct().getId());
      }
      //      mapsFile.put(username, section);
      mapsFile.store();
      return true;
    } catch (IOException e) {
      RDLogger.getLogger().throwing(UserAccount.class.getName(), "saveMap", e);
      return false;
    }

  }

  private static String mapsFileName() { return Paths.get(servletPath, "maps.ini").toString(); }

  public static boolean deleteMap(String username) {
    boolean success = false;
    String[] values = username.split(",");
    String user = values[0];
    String saveNumToDelete = values[1];
    //File input = new File("/usr/local/Cellar/tomcat@8/8.5.34/libexec/webapps/CSE308/maps.ini");
    File input = new File(mapsFileName());
    try {
      input.createNewFile();
      Ini mapsFile = new Ini(input);
      Ini.Section userSession = mapsFile.get(user);
      if (userSession == null) {
        return false;
      }

      String numSaves = mapsFile.fetch(user, "numSaves");
      int saves = Integer.parseInt(numSaves);

      String[] saveList = mapsFile.fetch(user, "savesList").split(",");
      String newSaveList = Arrays.stream(saveList)
                                 .filter(s -> !s.equals(saveNumToDelete))
                                 .collect(Collectors.joining(","));

      userSession.remove("savesList");
      userSession.put("savesList", newSaveList);

      Set<String> keyset = mapsFile.keySet();
      ArrayList<String> keyList = new ArrayList<>();

      for (String key : keyset) {
        if (key.startsWith(user + "/save" + saveNumToDelete + "/move")) {
          keyList.add(key);
        }
      }

      mapsFile.remove(mapsFile.get(user + "/save" + saveNumToDelete));

      for (String key : keyList) {
        mapsFile.remove(mapsFile.get(key));
      }

      saves--;
      if (saves <= 0) {
        mapsFile.remove(user);
      } else {
        userSession.remove("numSaves");
        userSession.put("numSaves", saves);
      }

      mapsFile.store();
      success = true;

    } catch (IOException e) {
      RDLogger.getLogger().throwing(UserAccount.class.getName(), "deleteMap", e);
    }
    return success;
  }

  public static String getSavedMaps(String username) {
    String savedMaps = "";

    File input = new File(mapsFileName());
    try {
      input.createNewFile();
      Ini mapsFile = new Ini(input);
      Ini.Section userSession = mapsFile.get(username);

      if (userSession == null) {
        savedMaps = "no saves";
      }

      savedMaps = mapsFile.fetch(username, "savesList");

      if (savedMaps == null) {
        savedMaps = "";
        return savedMaps;
      }
    } catch (IOException e) {
      RDLogger.getLogger().throwing(UserAccount.class.getName(), "getSavedMap", e);
    }
    return savedMaps;
  }

  public static String loadWeights(String username) {
    String weights = "";

    String compactness;
    String efficiencyGap;
    String partisianFairness;
    String populationEquality;
    String numSeedDistricts;
    String approach;
    String year;

    try {
      File input = new File(weightsFileName());
      input.createNewFile();
      weightsFile = new Ini(input);
      Map<String, String> map = weightsFile.get(username);
      if (map == null || map.isEmpty()) {
        weights = "invalidload";
      } else {
        compactness = map.get("compactness");
        efficiencyGap = map.get("efficiencyGap");
        partisianFairness = map.get("partisianFairness");
        populationEquality = map.get("populationEquality");
        numSeedDistricts = map.get("numSeedDistricts");
        approach = map.get("approach");
        year = map.get("year");

        weights = "validload" + "," +
                  compactness + "," +
                  efficiencyGap + "," +
                  partisianFairness + "," +
                  populationEquality + "," +
                  numSeedDistricts + "," +
                  approach + "," +
                  year;

      }
    } catch (IOException e) {
      System.out.println("Exception");
      e.printStackTrace();
    }
    return weights;
  }

}
