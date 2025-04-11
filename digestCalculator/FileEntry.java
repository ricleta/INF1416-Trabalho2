package digestCalculator;

import java.util.Map;

public class FileEntry {
    private String fileName;
    private Map<String, String> digestMap;

    public FileEntry(String fileName) {
        this.fileName = fileName;
    }

    public FileEntry(String fileName, String digestType, String digestHex) {
        this.fileName = fileName;
        addDigest(digestType, digestHex);
    }

    public Boolean addDigest(String digestType, String digestHex){
        if (digestMap == null) {
            digestMap = new java.util.HashMap<>();
        }
        
        if (digestHex == null || digestHex.isEmpty()) {
            return false;
        }
        if (digestType == null || digestType.isEmpty()) {
            return false;
        }
        if (digestMap.containsValue(digestHex)) {
            return false;
        }

        digestMap.put(digestType, digestHex);
        return true;
    }

    public String getFileName() {
        return fileName;
    }

    public String getDigestHex(String digestType) {
        return digestMap.get(digestType);
    }

    public String[] getDigestTypes() {
        return digestMap.keySet().toArray(new String[0]);
    }
}