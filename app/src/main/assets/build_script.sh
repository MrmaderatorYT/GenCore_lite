#!/data/data/com.termux/files/usr/bin/sh

# Додайте PATH
export PATH=$PATH:/data/data/com.termux/files/usr/bin

echo "=== Starting script ==="

pkg update -y
pkg upgrade -y

echo "=== Install required packages ==="
pkg install -y openjdk-17 dx ecj aapt2 termux-api wget

echo "=== Download lib++ file ==="
chmod 777 /storage/emulated/0/Download/
wget -P /storage/emulated/0/Documents/GenCoreLite/scripts https://packages-cf.termux.dev/apt/termux-main/pool/main/libc/libc%2B%2B/libc%2B%2B_26b_aarch64.deb

dpkg -i /storage/emulated/0/Documents/GenCoreLite/scripts/libc++_26b_aarch64.deb

echo "=== Update packages again ==="
pkg update -y
pkg upgrade -y

# Параметри
APP_NAME="MyApp"
PACKAGE_NAME="com.example.myapp"
MAIN_ACTIVITY="MainActivity"
SOURCE_DIR="/storage/emulated/0/Documents/GenCoreLite/scripts/src"
BUILD_DIR="/storage/emulated/0/Documents/GenCoreLite/scripts/build"
OUTPUT_DIR="/storage/emulated/0/Documents/GenCoreLite/scripts/output"
RES_DIR="/storage/emulated/0/Documents/GenCoreLite/scripts/res"
MANIFEST_FILE="/storage/emulated/0/Documents/GenCoreLite/scripts/AndroidManifest.xml"

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

IC_LAUNCHER="@mipmap/ic_launcher"
IC_LAUNCHER_ROUND="@mipmap/ic_launcher_round"
THEME="@style/Theme.MyApp"

MANIFEST_CONTENT="
<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"
    package=\"$PACKAGE_NAME\">
    <application
        android:allowBackup=\"true\"
        android:icon=\"$IC_LAUNCHER\"
        android:label=\"$APP_NAME\"
        android:roundIcon=\"$IC_LAUNCHER_ROUND\"
        android:supportsRtl=\"true\"
        android:theme=\"$THEME\">
        <activity android:name=\".$MAIN_ACTIVITY\">
            <intent-filter>
                <action android:name=\"android.intent.action.MAIN\" />
                <category android:name=\"android.intent.category.LAUNCHER\" />
            </intent-filter>
        </activity>
    </application>
</manifest>
"

THEME_CONTENT="
<resources>
    <style name=\"Theme.MyApp\" parent=\"android:Theme.Material\">
        <item name=\"android:colorPrimary\">@android:color/holo_blue_bright</item>
        <item name=\"android:colorPrimaryDark\">@android:color/holo_blue_dark</item>
        <item name=\"android:colorAccent\">@android:color/holo_blue_light</item>
    </style>
</resources>
"

COLOR_CONTENT="<?xml version=\"1.0\" encoding=\"utf-8\"?>
<resources>
    <color name=\"myapp_purple_200\">#FFBB86FC</color>
    <color name=\"myapp_purple_500\">#FF6200EE</color>
    <color name=\"myapp_purple_700\">#FF3700B3</color>
    <color name=\"myapp_teal_200\">#FF03DAC5</color>
    <color name=\"myapp_teal_700\">#FF018786</color>
    <color name=\"myapp_black\">#FF000000</color>
    <color name=\"myapp_white\">#FFFFFFFF</color>
</resources>"

# Проста XML картинка для лого
IC_LAUNCHER_XML="<?xml version=\"1.0\" encoding=\"utf-8\"?>
<vector xmlns:android=\"http://schemas.android.com/apk/res/android\"
    android:width=\"108dp\"
    android:height=\"108dp\"
    android:viewportWidth=\"108\"
    android:viewportHeight=\"108\">
    <path
        android:fillColor=\"#FF6200EE\"
        android:pathData=\"M54,0c29.8,0 54,24.2 54,54s-24.2,54 -54,54S0,83.8 0,54 24.2,0 54,0z\"/>
</vector>"

IC_LAUNCHER_ROUND_XML="<?xml version=\"1.0\" encoding=\"utf-8\"?>
<vector xmlns:android=\"http://schemas.android.com/apk/res/android\"
    android:width=\"108dp\"
    android:height=\"108dp\"
    android:viewportWidth=\"108\"
    android:viewportHeight=\"108\">
    <path
        android:fillColor=\"#FF3700B3\"
        android:pathData=\"M54,0c29.8,0 54,24.2 54,54s-24.2,54 -54,54S0,83.8 0,54 24.2,0 54,0z\"/>
</vector>"

echo "=== Directories and files setup ==="

# Створення необхідних директорій
mkdir -p $SOURCE_DIR/$PACKAGE_NAME
chmod 777 $SOURCE_DIR/$PACKAGE_NAME

mkdir -p $BUILD_DIR
chmod 777 $BUILD_DIR

mkdir -p $OUTPUT_DIR
chmod 777 $OUTPUT_DIR

mkdir -p $RES_DIR/mipmap
chmod 777 $RES_DIR/mipmap

mkdir -p $RES_DIR/values
chmod 777 $RES_DIR/values

echo "=== Directories created ==="

# Створення основного Java файлу
echo "$MAIN_ACTIVITY_CODE" > $SOURCE_DIR/$PACKAGE_NAME/$MAIN_ACTIVITY.java

# Створення AndroidManifest.xml
echo "$MANIFEST_CONTENT" > $MANIFEST_FILE

# Створення файлів теми
echo "$THEME_CONTENT" > $RES_DIR/values/themes.xml
echo "$COLOR_CONTENT" > $RES_DIR/values/colors.xml

# Створення placeholder іконок
echo "$IC_LAUNCHER_XML" > $RES_DIR/mipmap/ic_launcher.xml
echo "$IC_LAUNCHER_ROUND_XML" > $RES_DIR/mipmap/ic_launcher_round.xml

echo "=== Files created ==="

# Перевірка наявності файлів
echo "=== Checking files ==="
ls -R $SOURCE_DIR
ls -R $RES_DIR
cat $MANIFEST_FILE

echo "=== Компліяція Java файлів ==="
ecj -d $BUILD_DIR $SOURCE_DIR/$PACKAGE_NAME/$MAIN_ACTIVITY.java
if [ $? -ne 0 ]; then
    echo "Помилка компіляції Java файлів"
    exit 1
fi

echo "=== Перетворення класів в DEX файл ==="
dx --dex --output=$BUILD_DIR/classes.dex $BUILD_DIR
if [ $? -ne 0 ]; then
    echo "Помилка перетворення класів в DEX файл"
    exit 1
fi

echo "=== Створення .flat файлів ресурсів ==="
aapt2 compile --dir $RES_DIR -o $BUILD_DIR/res.zip
if [ $? -ne 0 ];then
    echo "Помилка компіляції ресурсів"
    exit 1
fi

echo "=== Лінкування ресурсів та створення APK файлу ==="
aapt2 link -o $OUTPUT_DIR/$APP_NAME.apk -I $PREFIX/share/aapt/android.jar --manifest $MANIFEST_FILE -R $BUILD_DIR/res.zip --auto-add-overlay
if [ $? -ne 0 ]; then
    echo "Помилка створення APK файлу"
    exit 1
fi

echo "=== Додавання Dex файлу в APK ==="
aapt add $OUTPUT_DIR/$APP_NAME.apk $BUILD_DIR/classes.dex
if [ $? -ne 0 ]; then
    echo "Помилка додавання DEX файлу в APK"
    exit 1
fi

# Очищення
rm -rf $BUILD_DIR

echo "APK створено: $OUTPUT_DIR/$APP_NAME.apk"
