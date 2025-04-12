if [ -z "$1" ]; then
  echo "Usage: $0 <digest_type>"
  echo "digest_type: MD5, SHA1, SHA-256, SHA-512"
  exit 1
fi

javac digestCalculator/FileEntry.java
javac digestCalculator/DigestCalculator.java

java digestCalculator/DigestCalculator $1 ./files ./arq_lista_digest.xml