#!/bin/bash
GREEN='\033[0;32m'
RED='\033[41m'

# Перевірка, чи встановлено buildozer, і встановлення, якщо ні
if ! [ -x "$(command -v buildozer)" ]; then
  echo -e "${RED}Buildozer is not installed. Installing buildozer and dependencies..."
  pkg update -y
  pkg upgrade -y
  pkg install -y python build-essential git
  pip install buildozer
  pip install Cython
  pkg install -y openjdk-17
  pkg install -y unzip
  pkg install -y libffi openssl readline lzma gdbm
  pip install --upgrade pip setuptools
  pkg install dos2unix
  git clone https://github.com/madler/zlib
  cd zlib
  ./configure --prefix=$PREFIX
  make all
  make install
  cd ..
fi

# Завантаження і установка Android SDK
ANDROID_SDK_PATH="/storage/emulated/0/Documents/GenCoreLite/.buildozer/android/platform/android-sdk"
if [ ! -d "$ANDROID_SDK_PATH" ]; then
  echo -e "${GREEN}Downloading and installing Android SDK..."
  mkdir -p $ANDROID_SDK_PATH
  cd $ANDROID_SDK_PATH
  wget https://dl.google.com/android/repository/commandlinetools-linux-8512546_latest.zip -O cmdline-tools.zip
  unzip cmdline-tools.zip -d cmdline-tools
  mkdir -p cmdline-tools/latest
  mv cmdline-tools/cmdline-tools/* cmdline-tools/latest/
  rm -rf cmdline-tools cmdline-tools.zip
  cd cmdline-tools/latest/bin
  yes | ./sdkmanager --sdk_root=$ANDROID_SDK_PATH "platforms;android-29" "build-tools;30.0.3"
fi

# Перетворення файлів у формат Unix
dos2unix /storage/emulated/0/Documents/GenCoreLite/scripts/build_script.sh
dos2unix /storage/emulated/0/Documents/GenCoreLite/buildozer.spec

# Ініціалізація buildozer, якщо buildozer.spec не існує
if [ ! -f /storage/emulated/0/Documents/GenCoreLite/buildozer.spec ]; then
  echo -e "${GREEN}Initializing buildozer..."
  cd /storage/emulated/0/Documents/GenCoreLite
  buildozer init
fi

# Оновлення buildozer.spec для включення необхідних файлів
sed -i 's/^source.include_exts =.*/source.include_exts = py,png,jpg,kv,atlas,ogg,wav,mp3/' /storage/emulated/0/Documents/GenCoreLite/buildozer.spec

# Запуск компіляції APK файлу
echo -e "${GREEN}Building APK file..."
cd /storage/emulated/0/Documents/GenCoreLite
buildozer android debug

echo -e "${GREEN}Build process finished. Check the bin directory for the APK file."
