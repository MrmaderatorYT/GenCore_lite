##############################################################################
# about buildozer.spec : https://buildozer.readthedocs.io/en/latest/specifications.html
##############################################################################
[app]

# (str) Title of your application
title = induperator

# (str) Package name
package.name = induperator

# (str) Package domain (needed for android/ios packaging)
package.domain = org.induperator.edu

# (str) Source code where the main.py live
source.dir = .

# (list) Source files to include (let empty to include all the files)
source.include_exts = py,png,jpg,kv,atlas,ogg, wav, mp3

# (list) List of inclusions using pattern matching
source.include_patterns = image/*.png, raw/*.ogg, raw/*.wav, raw/*.mp3

version = 0.1


presplash.filename = %(source.dir)s/data/presplash.png

# (str) Icon of the application
icon.filename = %(source.dir)s/data/icon512x512.png

orientation = portrait


osx.python_version = 3

# Kivy version to use
osx.kivy_version = 2.3.0

fullscreen = 0

android.api = 27

# (int) Minimum API required
android.minapi = 19

# (int) Android SDK version to use
android.sdk = 23

# (str) Android NDK directory (if empty, it will be automatically downloaded.)
android.ndk_path = ~/Desktop/crystax-ndk-10.3.2

android.arch = armeabi-v7a



[buildozer]

log_level = 2

warn_on_root = 1
