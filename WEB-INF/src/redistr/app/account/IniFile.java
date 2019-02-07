package redistr.app.account;

import org.ini4j.Ini;
import redistr.app.algorithm.Redistricter;

public class IniFile {


  //write new account
  public static boolean writeAccount(int accountID, String password, Redistricter redistricter, String email, String isAdministrator, Ini accountsIni) {
    String accountSection = Integer.toString(accountID);
    accountsIni.put(accountSection, "email", email);
    accountsIni.put(accountSection, "password", password);
    accountsIni.put(accountSection, "isAdministrator", isAdministrator);
    //accountsIni.put(accountSection,"redistricter",email);


    return true;
  }

  //update account
  public static boolean writeAccount(int accountID, String password, Redistricter redistricter, Ini accountsIni) {
    String accountSection = Integer.toString(accountID);

    return true;
  }
}
