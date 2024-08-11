#!/bin/bash

# Перевірка, чи встановлено buildozer, і встановлення, якщо ні
if ! [ -x "$(command -v buildozer)" ]; then
  echo 'Buildozer is not installed. Installing buildozer and dependencies...'
  pip install --upgrade pip setuptools
  pip install buildozer
  sudo apt-get update
  sudo apt-get install -y python3-pip build-essential git
  sudo apt-get install -y cython
  sudo apt-get install -y openjdk-8-jdk
  sudo apt-get install -y unzip
  sudo apt-get install -y zlib1g-dev
  sudo apt-get install -y libncurses5-dev libffi-dev libssl-dev
  sudo apt-get install -y liblzma-dev libgdbm-dev
  sudo apt-get install -y libreadline-dev
fi

# Ініціалізація buildozer, якщо buildozer.spec не існує
if [ ! -f buildozer.spec ]; then
  echo 'Initializing buildozer...'
  buildozer init
fi

# Оновлення buildozer.spec для включення необхідних файлів
sed -i 's/^source.include_exts =.*/source.include_exts = py,png,jpg,kv,atlas,json/' buildozer.spec

# Запуск компіляції APK файлу
echo 'Building APK file...'
buildozer -v android debug

echo 'Build process finished. Check the bin directory for the APK file.'
