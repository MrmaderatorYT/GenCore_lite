#!/bin/bash

export PATH=$PATH:/data/data/com.termux/files/usr/bin
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
project_package=com/ccs
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
export BUILD_TOOLS="$dir/toolz"
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

# -I gives the path to the android platform’s android.jar,
# --manifest specifies the android manifest,
# --java specifies the path to generate the R.java file.
# --o specifies the output path.
echo "---------------aapt2 link: "

aapt2 link -v \
  -I $dir/android.jar \
  --manifest $dir/AndroidManifest.xml \
  --java $dir/build/ \
  -o $dir/build/link.apk \
   $dir/build/resources.zip \
   --auto-add-overlay

# This will compile our code to java bytecode
# and place the .class files in build/classes
# directory. Take note of the R.java file which
# is the one that was generated in the previous step.
# Without --release=9 nothing will work and
# puppies will die

echo "---------------- Using `$JAVA_HOME --version` ---------------"
$JAVA_HOME --release=9 -verbose \
 -d build/classes \
 --class-path \
    $dir/android.jar \
 $dir/src/$project_package/MainActivity.java \
 $dir/build/$project_package/R.java

# Once we have java bytecode we now convert it to
# DEX bytecode that runs on android devices.
# This is done using android’s d8 commandline tool.

# d8 uses '/bin/ls' which is not where it is, in termux.
# this..i found out you could get termux-ready versions of
# both these tools so these arent being used anymore.
#sed -i 's/\/bin\/ls/ls/g' $BUILD_TOOLS/dx
#sed -i 's/\/bin\/ls/ls/g' $BUILD_TOOLS/d8

# IF you dont include that 'cd' below, the fails will be monumental.
# it took me 60% of the time i spent on this to figure out
# that this tool WILL NOT FUNCTION unless the directory structure
# matches the package name, ie com/helloworld com.helloworld.
# if it even catches a glimpse that its actually src/com/helloworld
# itll uncompromisingly refuse to communicate or function

# To convert into dex :
echo "---------------d8: "
cd $dir/build/classes
dx --dex --verbose --debug\
                 --output=classes.dex \
                $project_package/*.class \

# This is the same step as above (you only need
# but one of them) but uses d8 instead of dx.
# I chose dx only because its debug output is better
# (d8 is completely silent and impossible to troubleshoot,
# however in the end I got them both to work.)
#$BUILD_TOOLS/d8 --classpath $ANDROID_HOME/platforms/$TARGET_PLATFORM/android.jar \
#       --output build/dex/ \
#       $(ls -1 build/classes/com/helloworld |\
#        xargs -I{} printf "%s "\
#        "build/classes/com/helloworld/{}")


# The output will be a file called classes.dex.
# We then need to add this file into our link.apk
# that was generated in the linking stage:
# (notice that zip comes from your system and isn't
# in android sdk):

echo "---------------zip: "
zip -v -u ../link.apk classes.dex


# Next we need to zip align our apk using the
# zipalign tool and then sign the apk using the
# apksigner tool.

echo "---------------zipalign: "
zipalign -v -f -p 4 ../link.apk ../zipout.apk

# (To sign the application you will need to have a
# public-private key pair. You can generate one
# using java’s keytool. This you only do once, so
# if its your first time thru, uncomment the 'keytool'
# line and answer the questions:
# I used 'password' when asked for one
echo  "--------------- keytool"
#keytool -genkeypair -v -keystore $dir/key.keystore -keyalg RSA -keysize 2048 -validity 10000 -alias key0

keytool -genkeypair -keystore key.keystore -keyalg RSA

chmod +x $dir/apksigner.jar
# And sign! --ks-pass pass:<YOUR PASS HERE> if you change it.
echo "---------------apksigner: "
java -jar $dir/apksigner.jar sign \
  --verbose \
  --ks $dir/key.keystore \
  --ks-pass pass:123456 \
  --out ../final.apk ../zipout.apk

# The output of this command is an apk final.apk.
echo
echo
echo "...if success, result is $dir/build/final.apk"