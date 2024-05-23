#!/data/data/com.termux/files/usr/bin/sh

# Додайте PATH
export PATH=$PATH:/data/data/com.termux/files/usr/bin

echo "=== Starting script ==="

pkg update -y
pkg upgrade -y
pkg install termux-api
pkg install -y openjdk-17 dx ecj aapt apksigner termux-api

echo "=== Packages installed ==="

# Параметри
APP_NAME=$1
PACKAGE_NAME=$2
MAIN_ACTIVITY=$3
SOURCE_DIR=$4
BUILD_DIR=$5
OUTPUT_DIR=$6
MANIFEST_FILE="AndroidManifest.xml"
MAIN_ACTIVITY_CODE="
package $PACKAGE_NAME;

import android.app.Activity;
import android.os.Bundle;

public class $MAIN_ACTIVITY extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Тут можна додати ваш код
    }
}
"

echo "=== Parameters set ==="

MANIFEST_CONTENT="
<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"
    package=\"$PACKAGE_NAME\">
    <application
        android:allowBackup=\"true\"
        android:icon=\"@mipmap/ic_launcher\"
        android:label=\"$APP_NAME\"
        android:roundIcon=\"@mipmap/ic_launcher_round\"
        android:supportsRtl=\"true\"
        android:theme=\"@style/Theme.MyApp\">
        <activity android:name=\".$MAIN_ACTIVITY\">
            <intent-filter>
                <action android:name=\"android.intent.action.MAIN\" />
                <category android:name=\"android.intent.category.LAUNCHER\" />
            </intent-filter>
        </activity>
    </application>
</manifest>
"

# Створення необхідних директорій
mkdir -p $SOURCE_DIR/$PACKAGE_NAME
chmod 777 $SOURCE_DIR/$PACKAGE_NAME

mkdir -p $BUILD_DIR
chmod 777 $BUILD_DIR

mkdir -p $OUTPUT_DIR
chmod 777 $OUTPUT_DIR

mkdir -p res/mipmap
chmod 777 res/mipmap


echo "=== Directories created ==="

# Створення основного Java файлу
echo "$MAIN_ACTIVITY_CODE" > $SOURCE_DIR/$PACKAGE_NAME/$MAIN_ACTIVITY.java

# Створення AndroidManifest.xml
echo "$MANIFEST_CONTENT" > $MANIFEST_FILE

# Створення placeholder іконок
touch res/mipmap/ic_launcher.png
touch res/mipmap/ic_launcher_round.png

echo "=== Files created ==="

# Перевірка наявності файлів
echo "=== Checking files ==="
ls -R $SOURCE_DIR
ls -R res
cat $MANIFEST_FILE

# Компіляція Java-коду
find $SOURCE_DIR -name "*.java" > sources.txt
ecj -d $BUILD_DIR @sources.txt
if [ $? -ne 0 ];then echo "Помилка компіляції Java файлів"
    exit 1
fi

# Перетворення класів в DEX файл
dx --dex --output=$BUILD_DIR/classes.dex $BUILD_DIR
if [ $? -ne 0 ];then echo "Помилка перетворення класів в DEX файл"
   exit 1
fi

# Створення APK файлу
aapt package -f -m -F $OUTPUT_DIR/$APP_NAME.apk -M $MANIFEST_FILE -S res -I $PREFIX/share/aapt/android.jar
if [ $? -ne 0 ];then echo "Помилка створення APK файлу"
    exit 1
fi

# Додавання DEX файлу в APK
aapt add $OUTPUT_DIR/$APP_NAME.apk $BUILD_DIR/classes.dex
if [ $? -ne 0 ];then echo "Помилка додавання DEX файлу в APK"
    exit 1
fi

# Підписання APK
if [ $? -ne 0 ];then echo "Помилка підписання APK файлу"
   exit 1
fi

# Очищення
rm sources.txt
rm -rf $BUILD_DIR

echo "APK створено: $OUTPUT_DIR/$APP_NAME.apk"
