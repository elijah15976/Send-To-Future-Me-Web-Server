import java.security.*;
import java.nio.charset.*;

//Main functions in this class are hash(password) and compareHash(original, input password)
public class PasswordHasher{
  public static String hash(String password) throws NoSuchAlgorithmException{
    String pwhash = "";
    String saltStr = "";
    
    SecureRandom random = new SecureRandom();
    byte[] salt = new byte[16];
    random.nextBytes(salt);

    MessageDigest md = MessageDigest.getInstance("SHA-512");
    md.update(salt);

    byte[] hashedPassword = md.digest(password.getBytes(StandardCharsets.UTF_8));

    for(int i = 0; i<hashedPassword.length; i++){
      pwhash += hashedPassword[i] + " ";
    }
    for(int i = 0; i<salt.length; i++){
      saltStr += salt[i] + " ";
    }
    //System.out.println(saltStr);
    //System.out.println("--------");

    //compareHash(saltStr + pwhash, saltStr + pwhash);

    return saltStr + pwhash;
  }

  public static String hashWithSeed(String password, String saltStr) throws NoSuchAlgorithmException{
    String pwhash = "";
    String saltStrTemp = saltStr;
    byte[] salt = new byte[16];
    for(int i = 0; i<salt.length; i++){
      //System.out.println(i);
      //System.out.println(saltStrTemp);
      try{
        salt[i] = (byte)(Integer.parseInt(saltStrTemp.substring(0, saltStrTemp.indexOf(' '))));
        saltStrTemp = saltStrTemp.substring(saltStrTemp.indexOf(' ')+1);
      }
      catch(Exception e){
        salt[i] = (byte)(Integer.parseInt(saltStrTemp));
      }
      //System.out.println(salt[i]);
    }

    MessageDigest md = MessageDigest.getInstance("SHA-512");
    md.update(salt);

    byte[] hashedPassword = md.digest(password.getBytes(StandardCharsets.UTF_8));

    for(byte i : hashedPassword){
      pwhash += i + " ";
    }
    //System.out.println(saltStr);
    //System.out.println("--------");

    //compareHash(saltStr + pwhash, saltStr + pwhash);

    return pwhash;
  }

  public static boolean compareHash(String originalHash, String password) throws NoSuchAlgorithmException{
    String salt = originalHash.substring(0, findNthCharacter(originalHash, ' ', 16));
    String ogPassHash = originalHash.substring(findNthCharacter(originalHash, ' ', 16)+1);

    //Input Password should be hashed here too
    String inputPWHash = hashWithSeed(password, salt);

    //System.out.println(salt);
    //System.out.println(ogPassHash);
    //System.out.println(inputPWHash);

    if(ogPassHash.equals(inputPWHash)){
      return true;
    }
    else{
      return false;
    }
  }

  public static int findNthCharacter(String str, char character, int iteration){
    int finalNum = 0;
    for(int i = 0; i<str.length(); i++){
      if(str.charAt(i) == character){
        finalNum++;
      }
      if(finalNum >= iteration){
        return i;
      }
    }
    return -1;
  }
}