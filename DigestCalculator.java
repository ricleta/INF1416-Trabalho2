/*
  Lívia Lutz dos Santos, 2211055
  Ricardo Bastos Leta Vieira, 2110526
*/

import java.security.*;
import java.util.ArrayList;
import java.util.Map;

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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

class FileEntry {
  private String fileName;
  private Map<String, String> digestMap;

  public FileEntry(String fileName) {
      this.fileName = fileName;
  }

  public FileEntry(String fileName, String digestType, String digestHex) {
      this.fileName = fileName;
      addDigest(digestType, digestHex);
  }

  public void addDigest(String digestType, String digestHex){
      if (digestMap == null) {
          digestMap = new java.util.HashMap<>();
      }
      
      if (digestHex == null || digestHex.isEmpty()) {
          return;
      }
      if (digestType == null || digestType.isEmpty()) {
          return;
      }
      if (digestMap.containsValue(digestHex)) {
          return;
      }

      digestMap.put(digestType, digestHex);
  }

  public String getFileName() {
      return fileName;
  }

  public String getDigestHex(String digestType) {
      return digestMap.get(digestType);
  }
}

public class DigestCalculator {
  private static final int DIGEST_BUFFER_SIZE = 8192;

  public static void main (String[] args) throws Exception {
    
    // check number os args
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

      // Check if the file is empty
      if (xmlFile.length() == 0) {
        // System.out.println("XML file is empty. Returning an empty list.");
        return fileEntries; // Return an empty list
      }
      

      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

      Document doc = dBuilder.parse(xmlFile);

      // System.out.println("Root element :" + doc.getDocumentElement().getNodeName());

      NodeList fileEntryList = doc.getElementsByTagName("FILE_ENTRY");
      for (int temp = 0; temp < fileEntryList.getLength(); temp++) {
        org.w3c.dom.Node fileEntryNode = fileEntryList.item(temp);
        
        if (fileEntryNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
          Element fileEntryElement = (Element) fileEntryNode;
          
          NodeList fileNameList = fileEntryElement.getElementsByTagName("FILE_NAME");
          String fileName = fileNameList.item(0).getTextContent();
          
          FileEntry fileEntry = new FileEntry(fileName);
          
          // System.out.println("File Name: " + fileName);

          NodeList digestEntryList = fileEntryElement.getElementsByTagName("DIGEST_ENTRY");
          for (int i = 0; i < digestEntryList.getLength(); i++) {
            org.w3c.dom.Node digestEntryNode = digestEntryList.item(i);
            Element digestEntryElement = (Element) digestEntryNode;
            
            NodeList digestTypeList = digestEntryElement.getElementsByTagName("DIGEST_TYPE");
            for (int j = 0; j < digestTypeList.getLength(); j++) {
              org.w3c.dom.Node digestTypeNode = digestTypeList.item(j);
              String digestType = digestTypeNode.getTextContent();
              // System.out.println("Digest Type: " + digestType);
              
              NodeList digestHexList = digestEntryElement.getElementsByTagName("DIGEST_HEX");
              org.w3c.dom.Node digestHexNode = digestHexList.item(0);
              String digestHex = digestHexNode.getTextContent();

              // System.out.println("Digest Hex: " + digestHex);

              // Add the digest to the file entry
              fileEntry.addDigest(digestType, digestHex);
            }
          }
          fileEntries.add(fileEntry);
        }
      }
      return fileEntries;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new ArrayList<>(); // Return an empty list in case of an exception
  }
  
  private static void writeDigestListToXml(File xmlFile, FileEntry entry, String digestType) {
    try {
      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      Document doc;

      // If the XML file is empty, create the first entry
      // Otherwise, check if the current entry already exists
      if (xmlFile.length() == 0) {
        doc = createFirstEntry(entry, digestType);
      } else {
        doc = dBuilder.parse(xmlFile);

        NodeList fileEntryList = doc.getElementsByTagName("FILE_ENTRY");

        if (isfileAlreadyRegistered(fileEntryList, entry.getFileName())) {
          // System.out.println(entry.getFileName() + "already registered in the XML file.");
          
          // only add the digest
          addDigestEntry(doc, entry, digestType);
        } else {
          // System.out.println(entry.getFileName() + " not registered. Adding new entry.");
          
          doc = addEntry(doc, entry, digestType);
        }
      }

      // Write the updated document back to the XML file
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      DOMSource source = new DOMSource(doc);
      StreamResult result = new StreamResult(xmlFile);
      transformer.transform(source, result);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  private static Document createFirstEntry(FileEntry entry, String digestType) {
    try {
      // Create the root element
      Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

      Element rootElement = doc.createElement("CATALOG");
      doc.appendChild(rootElement);

      doc = addEntry(doc, entry, digestType);
  
      return doc;

    } catch (Exception e) {
      e.printStackTrace();
    }
    return null; // Return null in case of an exception
  }

  private static Document addEntry(Document doc, FileEntry entry, String digestType) {
    try {
      // get the root element
      Element rootElement = doc.getDocumentElement();

      // Create the file entry element
      Element fileEntryElement = doc.createElement("FILE_ENTRY");
      rootElement.appendChild(fileEntryElement);

      // Create the file name element
      Element fileNameElement = doc.createElement("FILE_NAME");
      fileNameElement.appendChild(doc.createTextNode(entry.getFileName()));
      fileEntryElement.appendChild(fileNameElement);

      doc = addDigestEntry(doc, entry, digestType);

      return doc;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null; // Return null in case of an exception
  }

  private static Document addDigestEntry(Document doc, FileEntry entry, String digestType) {
    try {
      NodeList fileEntryList = doc.getElementsByTagName("FILE_ENTRY");
      for (int i = 0; i < fileEntryList.getLength(); i++) {
        Element fileEntryElement = (Element) fileEntryList.item(i);
        NodeList fileNameList = fileEntryElement.getElementsByTagName("FILE_NAME");
        String existingFileName = fileNameList.item(0).getTextContent();
        
        if (existingFileName.equals(entry.getFileName())) {
           // Create the digest entry element
          Element digestEntryElement = doc.createElement("DIGEST_ENTRY");
          fileEntryElement.appendChild(digestEntryElement);

          // Create the digest type element
          Element digestTypeElement = doc.createElement("DIGEST_TYPE");
          digestTypeElement.appendChild(doc.createTextNode(digestType));
          digestEntryElement.appendChild(digestTypeElement);

          // Create the digest hex element
          Element digestHexElement = doc.createElement("DIGEST_HEX");
          digestHexElement.appendChild(doc.createTextNode(entry.getDigestHex(digestType)));
          digestEntryElement.appendChild(digestHexElement);

          return doc;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null; // Return null in case of an exception
  }

  private static boolean isfileAlreadyRegistered(NodeList fileEntryList, String fileName) {
    for (int i = 0; i < fileEntryList.getLength(); i++) {
      Element fileEntryElement = (Element) fileEntryList.item(i);
      NodeList fileNameList = fileEntryElement.getElementsByTagName("FILE_NAME");
      String existingFileName = fileNameList.item(0).getTextContent();
      if (existingFileName.equals(fileName)) {
        return true;
      }
    }
    return false;
  }

  private static void checkStatus(ArrayList<FileEntry> newEntries, ArrayList<FileEntry> existingEntries, File arqListaDigest, String digestType) {
    System.out.println("Nome_Arq1 Tipo_Digest Digest_Hex_Arq1 (STATUS)");

    for (FileEntry newEntry : newEntries) {
      boolean found = false;

      if (isThereCollision(newEntry, newEntries, existingEntries, digestType)) {
        // System.out.println("Checking for collisions... newEntry: " + newEntry.getFileName() + " existingEntry: " + existingEntry.getFileName());
        System.out.println(newEntry.getFileName() + " " + digestType + " " + newEntry.getDigestHex(digestType) + " (COLLISION)");
        continue;
      }

      for (FileEntry existingEntry : existingEntries) {
        if (newEntry.getFileName().equals(existingEntry.getFileName())) {

          if (existingEntry.getDigestHex(digestType) != null)
          {
            found = true;

            if (newEntry.getDigestHex(digestType).equals(existingEntry.getDigestHex(digestType))) {
              System.out.println(newEntry.getFileName() + " " + digestType + " " + newEntry.getDigestHex(digestType) + " (OK)");
            } else {
              System.out.println(newEntry.getFileName() + " " + digestType + " " + newEntry.getDigestHex(digestType) + " (NOT OK)");
            }
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

  private static boolean isThereCollision(FileEntry currentEntry, ArrayList<FileEntry> newEntries, ArrayList<FileEntry> existingEntries, String digestType) {
    ArrayList<FileEntry> allEntries = new ArrayList<>();
    allEntries.addAll(newEntries);
    allEntries.addAll(existingEntries);
    
    for (FileEntry entry : allEntries) {
      if (!currentEntry.getFileName().equals(entry.getFileName())) {
        if (currentEntry.getDigestHex(digestType).equals(entry.getDigestHex(digestType))) 
        {
          return true;
        }
      }
  }
    return false;
  }
}
