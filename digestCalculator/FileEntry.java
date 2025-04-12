/*
  Lívia Lutz dos Santos, 2211055
  Ricardo Bastos Leta Vieira, 2110526
*/
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