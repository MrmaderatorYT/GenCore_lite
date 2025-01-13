GenCore Lite - is a Android-based game engine which can help with creating novel app for android OS using termux environment. It includes basic tools for compiling resources, Java code, and packaging APK files.

## Features

- **Termux support**: Can be used on Android devices via Termux.
- **Minimal Dependencies**: Uses only essential tools such as `aapt2`, `dx`, `javac`, `zip`, `zipalign`, and `apksigner`.
- **Automatic Build**: The script automatically compiles resources, Java code, creates DEX files, and packages the APK.
- **APK Signing**: Generates a key for signing the APK and automatically signs the final file.
- **Own Script Language**: own script language which makes development more comfortable. This script will be rewrited on Java


## Script Syntax

The game core has its own scripting language, which is rewritten into Java code during compilation. Below are the keywords that will help you develop your own visual novel:

### Keywords

- **`SCENE`**: Marks the beginning of a new scene. Each scene has a unique number (e.g., `SCENE 1`, `SCENE 2`, etc.).
- **`BACKGROUND`**: Specifies the image that will be used as the background for the current scene. For example, `BACKGROUND hall.png` sets the background to the image `hall.png`.
- **`MUSIC`**: Specifies the music file that will play during the scene. For example, `MUSIC intro.mp3` plays the music from the file `intro.mp3`.
- **`DIALOG`**: Marks a dialogue between characters. After this keyword, specify the character’s name and their line. For example, `DIALOG John: Hello?` means the character named John says, "Hello?".
- **`GOTO`**: Indicates a transition to another scene. For example, `GOTO 2` means that after the current scene ends, the game will transition to Scene 2.
- **`SOUND`**: Specifies a sound file that will play during the scene. For example, `SOUND intro.wav` plays the sound from the file `intro.wav`.
- **`END`**: Marks the end of the current scene. After this keyword, the scene concludes, and the game moves to the next scene or ends if it’s the final scene.

### Comments and Reserved Keywords

- **`#`**: A standard comment that will remain as a comment in the code.
- **`CHOICE`**: Represents a choice the player must make. After this keyword, a list of options is provided. *(Currently, this keyword is reserved and acts as a comment in version 0.1.)*

---

### Example Script

Here’s an example of how to structure a script using the above keywords:

```plaintext
SCENE 1
BACKGROUND hall.png
MUSIC intro.mp3
DIALOG John: Hello
GOTO 2
END

SCENE 2
BACKGROUND hall.png
DIALOG Alice: You came!
SOUND intro.wav
GOTO 3
END

SCENE 3
BACKGROUND hall.png
DIALOG John: Yes, so what will we do now?
END
```


Main page, edit page
Few screenshots:
![photo_6_2025-01-05_21-49-28](https://github.com/user-attachments/assets/f95e8cd5-bed2-4a04-9205-0859441ac6c2)
![photo_5_2025-01-05_21-49-28](https://github.com/user-attachments/assets/68494a30-6c79-45b1-bac5-4ab2eda47e08)
![photo_4_2025-01-05_21-49-28](https://github.com/user-attachments/assets/ab31e3fc-2e2a-4518-a26c-43eac04b583f)
![photo_3_2025-01-05_21-49-28](https://github.com/user-attachments/assets/8c9100ab-bb92-4e4c-8fb5-302236472aad)
![photo_2_2025-01-05_21-49-28](https://github.com/user-attachments/assets/dc5d6304-f9bd-4f25-99de-0b2e9df91b8d)
![photo_1_2025-01-05_21-49-28](https://github.com/user-attachments/assets/9c89bb24-9042-405a-b4eb-1e9a8834aa01)
