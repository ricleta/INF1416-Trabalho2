import java.security.*;
import javax.crypto.*;
//
// Generate a Message Digest
public class MessageDigestExample {

  public static void main (String[] args) throws Exception {
    //
    // check args and get plaintext
    if (args.length != 3) {
      System.err.println("Usage: java MessageDigestExample text");
      System.exit(1);
    }

    //getting each argument from command prompt
    byte[] tipo_digest = args[0].getBytes("UTF8");
    byte[] caminho_da_Pasta_dos_Arquivos = args[1].getBytes("UTF8");
    byte[] caminho_ArqListaDigest = args[2].getBytes("UTF8");
    
    // get a message digest object using the algorithm from command prompt
    MessageDigest messageDigest = MessageDigest.getInstance(tipo_digest);
    
    
    //use file content instead of plaintext
    messageDigest.update( plainText);
    byte [] digest = messageDigest.digest();
    //System.out.println( "\nDigest length: " + digest.length * 8 + "bits" );

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
