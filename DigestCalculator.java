/*
 * Lívia Lutz dos Santos, 2211055
  Ricardo Bastos Leta Vieira, 2110526
 */
import java.security.*;
import javax.crypto.*;
import java.io.File;
//
// Generate a Message Digest
public class DigestCalculator {

  public static void main (String[] args) throws Exception {
    //
    // check number os args
    if (args.length != 3) {
      //if not enough arguments, print instructions and exit
      System.err.println("Argumentos da linha de comando omitidos ou insuficientes para a execução do programa \n" +
                         "Uso: java DigestCalculator<SP>Tipo_Digest<SP>Caminho_da_Pasta_dos_Arquivos<SP>Caminho_ArqListaDigest");
      System.exit(1);
    }

    //getting each argument from command prompt
    byte[] tipo_digest = args[0].getBytes("UTF8");
    byte[] caminho_da_Pasta_dos_Arquivos = args[1].getBytes("UTF8");
    byte[] caminho_ArqListaDigest = args[2].getBytes("UTF8");
    
    // get a message digest object using the algorithm from command prompt
    MessageDigest messageDigest = MessageDigest.getInstance(tipo_digest.toString());

    //path to file with list of digests
    File path = new File(caminho_da_Pasta_dos_Arquivos.toString() + caminho_ArqListaDigest.toString());

    //maybe check if path is valid with try and catch?

    //Create a file array containing every file in path
    File[] listOfFiles = path.listFiles();

    //maybe use try and catch here?
    for (File file : listOfFiles) {
      if(file.isFile()){
        //TODO
       /*read xml file
        * calculate digest of the content of each file
        * convert digest to hexadecimal format
        * compare the digest with the one in the file and update status
        * print Nome_ArqN<SP>Tipo_Digest<SP>Digest_Hex_ArqN<SP>(STATUS)
       */

       /*Os digests calculados para os arquivos com status NOT FOUND devem ser acrescentados no
        registro de um nome de arquivo existente ou como uma nova entrada de um novo arquivo no final
        do arquivo de lista de digests, mantendo seu formato padrão. Os digests calculados para os
        arquivos com status COLISION não devem ser acrescentados no arquivo de lista de digests.
        */

      }
    }
    
    //use file content instead of plaintext
    /*messageDigest.update( plainText);
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
    System.out.println( buf.toString() );*/
  }
}
