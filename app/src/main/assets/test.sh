#!/bin/bash

# Update PATH to include necessary directories
export PATH=$PATH:/data/data/com.termux/files/usr/bin

# Install required packages
pkg install -y openjdk-17 aapt2 dx zip apksigner

# Error handling
function catch_error() {
  local error_code="$?"
  echo "Error: $error_code"
}
trap catch_error ERR
set -e

# Set directories and package names
dir=/storage/emulated/0/Documents/GenCoreLite/scripts
project_package=com/ccs
output_dir=/storage/emulated/0/Documents/GenCoreLite/output

echo "Work Dir: $dir"

# Check if the working directory exists
if [ ! -d "$dir" ]; then
  echo "Directory does not exist"; exit 1;
else
  echo "Directory exists, continuing..."
fi

cd $dir

# Set up environment variables for Java and build tools
export JAVA_HOME="/data/data/com.termux/files/usr/lib/jvm/java-17-openjdk"
export PATH="$JAVA_HOME/bin:$PATH"
export BUILD_TOOLS="$dir/toolz"
export PATH="$BUILD_TOOLS:$PATH"

# Clean up from the last build
rm -rf build
mkdir -p build/classes

# Compile resources with aapt2
echo "---------------aapt2 compile: "
aapt2 compile -v \
  --dir res \
  -o build/resources.zip

# Link resources and generate R.java
echo "---------------aapt2 link: "
aapt2 link -v \
  -I $dir/android.jar \
  --manifest $dir/AndroidManifest.xml \
  --java build/ \
  -o build/link.apk \
  build/resources.zip \
  --auto-add-overlay

# Compile all Java source files, including generated R.java
echo "---------------- Using `$JAVA_HOME/bin/javac --version` ---------------"
find $dir/java -name "*.java" > build/sources.txt
find build -name "R.java" >> build/sources.txt

$JAVA_HOME/bin/javac --release=9 -verbose \
  -d build/classes \
  -classpath $dir/android.jar \
  -sourcepath $dir/src:build \
  @build/sources.txt

# Convert .class files to DEX bytecode
echo "---------------dx: "
cd build/classes
dx --dex --verbose --debug \
  --min-sdk-version=26 \
  --output=classes.dex \
  $(find . -name "*.class")

# Add classes.dex to the APK
echo "---------------zip: "
cd $dir/build/classes
zip -v -u ../link.apk classes.dex

# Align the APK
echo "---------------zipalign: "
zipalign -v -f -p 4 ../link.apk ../zipout.apk

# Sign the APK
echo "---------------apksigner: "

# Повертаємося до каталогу з key.keystore
rm -f $dir/key.keystore
# Генеруємо ключове сховище з повним шляхом
$JAVA_HOME/bin/keytool -genkeypair -v \
  -keystore $dir/key.keystore \
  -alias key0 \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -storepass password \
  -keypass password \
  -dname "CN=Ваше Ім'я, OU=Ваш Підрозділ, O=Ваша Організація, L=Ваше Місто, S=Ваша Область, C=UA"

# Підписуємо APK
apksigner sign \
  --verbose \
  --ks $dir/key.keystore \
  --ks-pass pass:password \
  --key-pass pass:password \
  --out $output_dir/final.apk $dir/build/zipout.apk

echo
echo
echo "...if success, result is $output_dir/final.apk"