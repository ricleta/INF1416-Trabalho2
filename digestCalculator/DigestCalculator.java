/*
  Lívia Lutz dos Santos, 2211055
  Ricardo Bastos Leta Vieira, 2110526
*/
package digestCalculator;

import digestCalculator.FileEntry;
import java.security.*;
import java.util.ArrayList;

import javax.crypto.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

//
// Generate a Message Digest
public class DigestCalculator {
  private static final int DIGEST_BUFFER_SIZE = 8192;

  public static void main (String[] args) throws Exception {
    
    // check number of args
    if (args.length != 3) {
      //if not enough arguments, print instructions and exit
      System.err.println("Usage: java DigestCalculator Tipo_Digest Caminho_da_Pasta_dos_Arquivos Caminho_ArqListaDigest");
      System.exit(1);
    }

    //getting each argument from command prompt
    String tipoDigest = args[0];
    String folderPath = args[1];
    String arqListaDigestPath = args[2];
  
    //path to folder with all digests
    File folder = null;
    try {
      folder = new File(folderPath);
    }
    catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
      System.exit(2);
    }

    //Create a file array containing every file in path
    File []listOfFiles = null;
    int numFiles = 0;
    try {
      listOfFiles = folder.listFiles();
      numFiles = listOfFiles.length;
    }
    catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
      System.exit(2);
    }

    File arqListaDigest = null;
    ArrayList<FileEntry> existingEntries = null;
    try {
      arqListaDigest = new File(arqListaDigestPath);
      existingEntries = readDigestListFromXml(arqListaDigest);
    }
    catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
      System.exit(2);
    }
  
    ArrayList<FileEntry> newEntries = new ArrayList<>();
    
    for (int i = 0; i < numFiles; i++) {
      File file = listOfFiles[i];
      String fileName = file.getName();
      String digestHex = getDigestStr(getDigest(file, tipoDigest));
      FileEntry fileEntry = new FileEntry(fileName, tipoDigest, digestHex);
      newEntries.add(fileEntry);
    }

    // Compare the new entries with the existing entries
    checkStatus(newEntries, existingEntries, arqListaDigest, tipoDigest);
  }

  private static byte[] getDigest(File file, String tipoDigest) throws Exception {
    // Create a MessageDigest object for the specified algorithm
    MessageDigest messageDigest = MessageDigest.getInstance(tipoDigest);

    // Read the file and update the digest
    try (FileInputStream fis = new FileInputStream(file)) {
      byte[] buffer = new byte[DIGEST_BUFFER_SIZE];
      int bytesRead;

      while ((bytesRead = fis.read(buffer)) != -1) {
        messageDigest.update(buffer, 0, bytesRead);
      }
    }
    
    return messageDigest.digest();
  }

  private static String getDigestStr(byte [] digest){
    StringBuffer buf = new StringBuffer();

    for(int i = 0; i < digest.length; i++) {
        String hex = Integer.toHexString(0x0100 + (digest[i] & 0x00FF)).substring(1);
        buf.append((hex.length() < 2 ? "0" : "") + hex);
    }
    
    return buf.toString();
  }

  private static ArrayList<FileEntry> readDigestListFromXml(File xmlFile) {
    try {
      ArrayList<FileEntry> fileEntries = new ArrayList<>();
      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      Document doc = dBuilder.parse(xmlFile);
        
      System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
      
      NodeList fileEntryList = doc.getElementsByTagName("FILE_ENTRY");
      
      for (int temp = 0; temp < fileEntryList.getLength(); temp++) {
        org.w3c.dom.Node fileEntryNode = fileEntryList.item(temp);
        
        if (fileEntryNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
          Element fileEntryElement = (Element) fileEntryNode;
          
          NodeList fileNameList = fileEntryElement.getElementsByTagName("FILE_NAME");
          String fileName = fileNameList.item(0).getTextContent();
          
          FileEntry fileEntry = new FileEntry(fileName);
          
          System.out.println("File Name: " + fileName);

          NodeList digestEntryList = fileEntryElement.getElementsByTagName("DIGEST_ENTRY");

          for (int i = 0; i < digestEntryList.getLength(); i++) {
            org.w3c.dom.Node digestEntryNode = digestEntryList.item(i);
            Element digestEntryElement = (Element) digestEntryNode;
            NodeList digestTypeList = digestEntryElement.getElementsByTagName("DIGEST_TYPE");

            for (int j = 0; j < digestTypeList.getLength(); j++) {
              org.w3c.dom.Node digestTypeNode = digestTypeList.item(j);
              String digestType = digestTypeNode.getTextContent();
              System.out.println("Digest Type: " + digestType);
              
              NodeList digestHexList = digestEntryElement.getElementsByTagName("DIGEST_HEX");
              org.w3c.dom.Node digestHexNode = digestHexList.item(0);
              String digestHex = digestHexNode.getTextContent();

              System.out.println("Digest Hex: " + digestHex);

              // Add the digest to the file entry
              if (fileEntry.addDigest(digestType, digestHex)) {
                System.out.println("Digest added successfully.");
              } 
              
              else {
                System.out.println("Failed to add digest.");
              }
            }
          }
          fileEntries.add(fileEntry);
        }
      }
      return fileEntries;
    } 
    
    catch (Exception e) {
      e.printStackTrace();
    }

    return new ArrayList<>(); // Return an empty list in case of an exception
  }
  
  private static void writeDigestListToXml(File xmlFile, FileEntry entry, String digestType) {
    try {
      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      Document doc;

      // Check if the XML file exists
      if (xmlFile.exists()) {
        doc = dBuilder.parse(xmlFile);
      } 
        
      else {
        // Create a new XML document if the file does not exist
        doc = dBuilder.newDocument();
        Element rootElement = doc.createElement("DIGEST_LIST");
        doc.appendChild(rootElement);
      }

      // Get the root element
      Element rootElement = doc.getDocumentElement();

      Element fileEntryElement = null;
      boolean fileEntryExists = false;

      // Check if the FILE_ENTRY element already exists
      if (doesElementExist(doc, "FILE_ENTRY")) {
        // If there are existing FILE_ENTRY elements, check if the file name already exists
        NodeList fileEntryList = doc.getElementsByTagName("FILE_ENTRY");

        for (int i = 0; i < fileEntryList.getLength(); i++) {
          Element existingFileEntry = (Element) fileEntryList.item(i);
          NodeList fileNameList = existingFileEntry.getElementsByTagName("FILE_NAME");
          String existingFileName = fileNameList.item(0).getTextContent();

          if (existingFileName.equals(entry.getFileName())) {
            // If the file name already exists, add the new digest to the existing FILE_ENTRY
            Element digestEntryElement = doc.createElement("DIGEST_ENTRY");

            Element digestTypeElement = doc.createElement("DIGEST_TYPE");
            digestTypeElement.appendChild(doc.createTextNode(digestType));
            digestEntryElement.appendChild(digestTypeElement);

            Element digestHexElement = doc.createElement("DIGEST_HEX");
            digestHexElement.appendChild(doc.createTextNode(entry.getDigestHex(digestType)));
            digestEntryElement.appendChild(digestHexElement);

            existingFileEntry.appendChild(digestEntryElement);
            fileEntryExists = true;
            break;
          }
        }
      }

      // If the file name does not exist, create a new FILE_ENTRY element
      if (!fileEntryExists) {
        fileEntryElement = doc.createElement("FILE_ENTRY");

        // Add FILE_NAME element
        Element fileNameElement = doc.createElement("FILE_NAME");
        fileNameElement.appendChild(doc.createTextNode(entry.getFileName()));
        fileEntryElement.appendChild(fileNameElement);

        // Add DIGEST_ENTRY element
        Element digestEntryElement = doc.createElement("DIGEST_ENTRY");
        Element digestTypeElement = doc.createElement("DIGEST_TYPE");
        digestTypeElement.appendChild(doc.createTextNode(digestType));
        digestEntryElement.appendChild(digestTypeElement);

        Element digestHexElement = doc.createElement("DIGEST_HEX");
        digestHexElement.appendChild(doc.createTextNode(entry.getDigestHex(digestType)));
        fileEntryElement.appendChild(digestHexElement);

        // Append the new FILE_ENTRY to the root element
        rootElement.appendChild(fileEntryElement);
      }

      // Write the updated document back to the XML file
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      DOMSource source = new DOMSource(doc);
      StreamResult result = new StreamResult(xmlFile);
      transformer.transform(source, result);

    } 
    
    catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  private static Boolean doesElementExist(Document doc, String elementName) {
    NodeList nodeList = doc.getElementsByTagName(elementName);
    return nodeList.getLength() > 0;
  }

  private static void checkStatus(ArrayList<FileEntry> newEntries, ArrayList<FileEntry> existingEntries, File arqListaDigest, String digestType) {
    System.out.println("Nome_Arq1 Tipo_Digest Digest_Hex_Arq1 (STATUS)");

    for (FileEntry newEntry : newEntries) {
      boolean found = false;
      for (FileEntry existingEntry : existingEntries) {
        if (newEntry.getFileName().equals(existingEntry.getFileName())) {
          found = true;
          if (newEntry.getDigestHex(digestType).equals(existingEntry.getDigestHex(digestType))) {
            System.out.println(newEntry.getFileName() + " " + digestType + " " + newEntry.getDigestHex(digestType) + " (OK)");
          } 
          else {
            System.out.println(newEntry.getFileName() + " " + digestType + " " + newEntry.getDigestHex(digestType) + " (NOT OK)");
          }
          break;
        }
        else{
          if (newEntry.getDigestHex(digestType).equals(existingEntry.getDigestHex(digestType))) {
            found = true;
            System.out.println(newEntry.getFileName() + " " + digestType + " " + newEntry.getDigestHex(digestType) + " (COLLISION)");
            break;
          }
        }
      }
      if (!found) {
        System.out.println(newEntry.getFileName() + " " + digestType + " " + newEntry.getDigestHex(digestType) + " (NOT FOUND)");
        // Write the new entry to the XML file
        writeDigestListToXml(arqListaDigest, newEntry, digestType);
      }
    }
  }
}
