#!/data/data/com.termux/files/usr/bin/sh

# Додайте PATH
export PATH=$PATH:/data/data/com.termux/files/usr/bin

echo "=== Starting script ==="

pkg update -y
pkg upgrade -y

echo "=== Install required packages ==="
pkg install -y openjdk-17 ecj aapt2 termux-api wget

# Встановлення d8 замість dx
if ! command -v d8 &> /dev/null; then
  pkg install -y d8
fi

echo "=== Download lib++ file ==="
if [ -f "/storage/emulated/0/Documents/GenCoreLite/scripts/libc++_26b_aarch64.deb" ]; then
  echo "файл lib++26b_aarch64.deb присутній"
else
  chmod 777 /storage/emulated/0/Download/
  wget -P /storage/emulated/0/Documents/GenCoreLite/scripts https://packages-cf.termux.dev/apt/termux-main/pool/main/libc/libc%2B%2B/libc%2B%2B_26b_aarch64.deb
  dpkg -i /storage/emulated/0/Documents/GenCoreLite/scripts/libc++_26b_aarch64.deb
fi

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
MIN_SDK_VERSION="21" # Мінімальна версія SDK
TARGET_SDK_VERSION="30" # Цільова версія SDK
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
    <uses-sdk android:minSdkVersion=\"$MIN_SDK_VERSION\" android:targetSdkVersion=\"$TARGET_SDK_VERSION\" />
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
mkdir -p $SOURCE_DIR/$(echo $PACKAGE_NAME | tr '.' '/')
chmod 777 $SOURCE_DIR/$(echo $PACKAGE_NAME | tr '.' '/')

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
echo "$MAIN_ACTIVITY_CODE" > $SOURCE_DIR/$(echo $PACKAGE_NAME | tr '.' '/')/$MAIN_ACTIVITY.java

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
ecj -d $BUILD_DIR $SOURCE_DIR/$(echo $PACKAGE_NAME | tr '.' '/')/$MAIN_ACTIVITY.java
if [ $? -ne 0 ]; then
    echo "Помилка компіляції Java файлів"
    exit 1
fi

echo "=== Перетворення класів в DEX файл ==="
d8 $BUILD_DIR/$(echo $PACKAGE_NAME | tr '.' '/')/*.class --output $BUILD_DIR
if [ $? -ne 0 ]; then
    echo "Помилка перетворення класів в DEX файл"
    exit 1
fi

# Переконатися, що classes.dex створений
if [ ! -f "$BUILD_DIR/classes.dex" ]; then
    echo "Помилка: classes.dex не створено"
    exit 1
fi

echo "=== Створення .flat файлів ресурсів ==="
aapt2 compile --dir $RES_DIR -o $BUILD_DIR/res.zip
if [ $? -ne 0 ]; then
    echo "Помилка компіляції ресурсів"
    exit 1
fi

echo "=== Лінкування ресурсів та створення APK файлу ==="
aapt2 link -o $OUTPUT_DIR/$APP_NAME-unsigned.apk -I $PREFIX/share/aapt/android.jar --manifest $MANIFEST_FILE -R $BUILD_DIR/res.zip --min-sdk-version $MIN_SDK_VERSION --target-sdk-version $TARGET_SDK_VERSION --auto-add-overlay
if [ $? -ne 0 ]; then
    echo "Помилка створення APK файлу"
    exit 1
fi

echo "=== Перевірка наявності APK та DEX файлів ==="
ls -l $OUTPUT_DIR
ls -l $BUILD_DIR

echo "=== Додавання DEX файлу в APK ==="
cd $BUILD_DIR
if [ -f "$OUTPUT_DIR/$APP_NAME-unsigned.apk" ]; then
    echo "=== Додавання нового DEX файла до APK ==="
    zip -j $OUTPUT_DIR/$APP_NAME-unsigned.apk "$BUILD_DIR/classes.dex"
    if [ $? -ne 0 ]; then
        echo "Помилка додавання DEX файла в APK"
        exit 1
    fi
else
    echo "Помилка: APK файл не знайдено"
    exit 1
fi

# Підписання APK файла
SIGNED_APK="$OUTPUT_DIR/$APP_NAME.apk"
if [ -f $SIGNED_APK ]; then
    echo "Видалення старого підписаного APK файла"
    rm $SIGNED_APK
fi

# Генерація ключа для підписання APK
KEYSTORE="$OUTPUT_DIR/my-release-key.jks"
KEY_ALIAS="my-key-alias"
KEYSTORE_PASSWORD="password"
KEY_PASSWORD="password"

if [ ! -f $KEYSTORE ]; then
    echo "=== Генерація ключа для підписання APK ==="
    keytool -genkey -v -keystore $KEYSTORE -keyalg RSA -keysize 2048 -validity 10000 -alias $KEY_ALIAS -storepass $KEYSTORE_PASSWORD -keypass $KEY_PASSWORD -dname "CN=MyApp, OU=MyApp, O=MyApp, L=MyApp, S=MyApp, C=US"
fi

echo "=== Підписання APK файлу ==="
jarsigner -keystore $KEYSTORE -storepass $KEYSTORE_PASSWORD -keypass $KEY_PASSWORD -signedjar $SIGNED_APK $OUTPUT_DIR/$APP_NAME-unsigned.apk $KEY_ALIAS
if [ $? -ne 0 ]; then
    echo "Помилка підписання APK файлу"
    exit 1
fi

rm -rf $OUTPUT_DIR/$APP_NAME-aligned.apk

# Вирівнювання APK файлу
echo "=== Вирівнювання APK файлу ==="
zipalign -v 4 $SIGNED_APK $OUTPUT_DIR/$APP_NAME-aligned.apk
if [ $? -ne 0 ]; then
    echo "Помилка вирівнювання APK файлу"
    exit 1
fi

echo "=== APK файл готовий: $OUTPUT_DIR/$APP_NAME-aligned.apk ==="
