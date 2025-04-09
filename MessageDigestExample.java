import java.security.*;
import javax.crypto.*;
//
// Generate a Message Digest
public class MessageDigestExample {

  public static void main (String[] args) throws Exception {
    //
    // check args and get plaintext
    if (args.length != 1) {
      System.err.println("Usage: java MessageDigestExample text");
      System.exit(1);
    }
    byte[] plainText = args[0].getBytes("UTF8");
    //
    // get a message digest object using the MD5 algorithm
    //MessageDigest messageDigest = MessageDigest.getInstance("MD5");
    //MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
    MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
    //MessageDigest messageDigest = MessageDigest.getInstance("SHA-512");
    //
    // print out the provider used
    System.out.println( "\n" + messageDigest.getProvider().getInfo() );
    //
    // calculate the digest and print it out
    messageDigest.update( plainText);
    byte [] digest = messageDigest.digest();
    System.out.println( "\nDigest length: " + digest.length * 8 + "bits" );

    // converte o digist para hexadecimal
    StringBuffer buf = new StringBuffer();
    for(int i = 0; i < digest.length; i++) {
       String hex = Integer.toHexString(0x0100 + (digest[i] & 0x00FF)).substring(1);
       buf.append((hex.length() < 2 ? "0" : "") + hex);
    }

    // imprime o digest em hexadecimal
    System.out.println( "\nDigest(hex): " );
    System.out.println( buf.toString() );
  }
}
