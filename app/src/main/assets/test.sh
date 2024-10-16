#!/bin/bash

export PATH=$PATH:/data/data/com.termux/files/usr/bin


pkg install -y openjdk-17 aapt2 dx zip apksigner keytool

# will stop there.
function catch_error() {
  local error_code="$?"
  echo "Error: $error_code"
}
trap catch_error ERR
set -e

dir=/storage/emulated/0/Documents/GenCoreLite/scripts


project_package=com/ccs/romanticadventure
title=
sdk=
version=

output_dir=/storage/emulated/Documents/GenCoreLite/output


echo "Work Dir: $dir"

if [ ! -d "$dir" ]; then
  echo "Directory does not exist"; exit 1;
else
  echo "Directory exists, continuing..."; fi

cd $dir

#its likely these arent executable at first download

export JAVA_HOME="/data/data/com.termux/files/usr/lib/jvm/java-17-openjdk/bin/javac"
export PATH="$PATH:$JAVA_HOME/bin"
export PATH="$BUILD_TOOLS:$PATH"

# Clean up junk from last build:
rm -rf build
mkdir build
mkdir build/classes

# Begin compilation!

echo "---------------aapt2 compile: "
aapt2 compile -v\
  --dir res \
  -o build/resources.zip


echo "---------------aapt2 link: "

aapt2 link -v \
  -I $dir/android.jar \
  --manifest $dir/AndroidManifest.xml \
  --java $dir/build/ \
  -o $dir/build/link.apk \
   $dir/build/resources.zip \
   --auto-add-overlay


echo "---------------- Using `$JAVA_HOME --version` ---------------"
$JAVA_HOME --release=9 -verbose \
 -d build/classes \
 --class-path \
    $dir/android.jar \
 $dir/src/$project_package/MainActivity.java \
 $dir/build/$project_package/R.java

echo "---------------d8: "
cd $dir/build/classes
dx --dex --verbose --debug\
                 --output=classes.dex \
                $project_package/*.class \


echo "---------------zip: "
zip -v -u ../link.apk classes.dex


echo "---------------zipalign: "
zipalign -v -f -p 4 ../link.apk ../zipout.apk


echo  "--------------- keytool"

keytool -genkeypair -keystore key.keystore -keyalg RSA

chmod +x $dir/apksigner.jar
echo "---------------apksigner: "
java -jar $dir/apksigner.jar sign \
  --verbose \
  --ks $dir/key.keystore \
  --ks-pass pass:123456 \
  --out ../final.apk ../zipout.apk

echo
echo
echo "...if success, result is $dir/build/final.apk"