if [ -z "$1" ]; then
  echo "Usage: $0 <digest_type>"
  echo "digest_type: md5, sha1, sha256, sha512"
  exit 1
fi

javac digestCalculator/FileEntry.java
javac digestCalculator/DigestCalculator.java

java digestCalculator/DigestCalculator $1 ./files ./arq_lista_digest.xml