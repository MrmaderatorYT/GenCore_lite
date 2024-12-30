package com.ccs.gencorelite.compiler;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.ccs.gencorelite.data.PreferenceConfig;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class RewriterConstClasses {

    // Метод для генерації маніфесту
    public static void generateManifest(String outputPath, Context context) {
        Log.d("ManifestGenerator", "Starting generating Manifest");
        String package_project = PreferenceConfig.getPackage(context);

        try (PrintWriter output = new PrintWriter(new FileWriter(outputPath))) {
            output.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
            output.println("<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"");
            output.println("    xmlns:tools=\"http://schemas.android.com/tools\"");
            output.println("    package=\"" + package_project + "\">");
            output.println("    <uses-sdk");
            output.println("        android:minSdkVersion=\"26\"");
            output.println("        android:targetSdkVersion=\"31\" />");
            output.println("    <application");
            output.println("        android:allowBackup=\"true\"");
            output.println("        android:fullBackupContent=\"@xml/backup_rules\"");
            output.println("        android:icon=\"@mipmap/ic_launcher\"");
            output.println("        android:label=\"@string/app_name\"");
            output.println("        android:largeHeap=\"true\"");
            output.println("        android:supportsRtl=\"true\"");
            output.println("        android:theme=\"@style/AppTheme\"");
            output.println("        tools:targetApi=\"31\">");
            output.println("        <activity");
            output.println("            android:name=\".Info\"");
            output.println("            android:exported=\"false\" />");
            output.println("        <activity");
            output.println("            android:name=\".Settings\"");
            output.println("            android:exported=\"false\"");
            output.println("            android:screenOrientation=\"landscape\" />");
            output.println("        <activity");
            output.println("            android:name=\".Game_First_Activity\"");
            output.println("            android:exported=\"false\"");
            output.println("            android:screenOrientation=\"landscape\" />");
            output.println("        <activity");
            output.println("            android:name=\".MainActivity\"");
            output.println("            android:exported=\"true\"");
            output.println("            android:screenOrientation=\"landscape\">");
            output.println("            <intent-filter>");
            output.println("                <action android:name=\"android.intent.action.MAIN\" />");
            output.println("                <category android:name=\"android.intent.category.LAUNCHER\" />");
            output.println("            </intent-filter>");
            output.println("        </activity>");
            output.println("    </application>");
            output.println("</manifest>");

            Log.d("ManifestGenerator", "Manifest file was generated. Generated file: [" + outputPath + "]");
            System.out.println("Маніфест успішно згенеровано та записано у файл: " + outputPath);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("ManifestGenerator", "Error generating manifest: " + e.getMessage());
        }
    }

    // Метод для перезапису класу ExitConfirmationDialog
    public static void rewriteExitConfirmationDialog(String outputPath, String package_project) {
        try (PrintWriter output = new PrintWriter(new FileWriter(outputPath))) {
            output.println("package " + package_project + ".system;");
            output.println();
            output.println("import android.app.AlertDialog;");
            output.println("import android.content.Context;");
            output.println("import android.content.DialogInterface;");
            output.println();
            output.println("import " + package_project + ".MainActivity;");
            output.println();
            output.println("public class ExitConfirmationDialog {");
            output.println();
            output.println("    public static void showExitConfirmationDialog(Context context) {");
            output.println("        AlertDialog.Builder builder = new AlertDialog.Builder(context);");
            output.println("        builder.setMessage(\"Ви впевнені, що хочете вийти?\");");
            output.println("        builder.setPositiveButton(\"Так\", new DialogInterface.OnClickListener() {");
            output.println("            @Override");
            output.println("            public void onClick(DialogInterface dialog, int which) {");
            output.println("                if (context instanceof MainActivity) {");
            output.println("                    ((MainActivity) context).exit();");
            output.println("                }");
            output.println("            }");
            output.println("        });");
            output.println("        builder.setNegativeButton(\"Відмна\", new DialogInterface.OnClickListener() {");
            output.println("            @Override");
            output.println("            public void onClick(DialogInterface dialog, int which) {");
            output.println("                dialog.dismiss();");
            output.println("            }");
            output.println("        });");
            output.println("        builder.show();");
            output.println("    }");
            output.println("}");

            Log.d("RewriterConstClasses", "ExitConfirmationDialog rewritten. Output path: [" + outputPath + "]");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("RewriterConstClasses", "Error rewriting ExitConfirmationDialog: " + e.getMessage());
        }
    }

    // Метод для перезапису класу WebAppInterface
    public static void rewriteWebAppInterface(String outputPath, String package_project) {
        try (PrintWriter output = new PrintWriter(new FileWriter(outputPath))) {
            output.println("package " + package_project + ".data;");
            output.println();
            output.println("import android.webkit.JavascriptInterface;");
            output.println();
            output.println("public class WebAppInterface {");
            output.println("    private boolean type;");
            output.println("    private int value;");
            output.println();
            output.println("    public WebAppInterface(boolean type, int value) {");
            output.println("        this.type = type;");
            output.println("        this.value = value;");
            output.println("    }");
            output.println();
            output.println("    @JavascriptInterface");
            output.println("    public boolean getValue() {");
            output.println("        return type;");
            output.println("    }");
            output.println();
            output.println("    @JavascriptInterface");
            output.println("    public int indexFromJS(int value) {");
            output.println("        this.value = value;");
            output.println("        return value;");
            output.println("    }");
            output.println("}");

            Log.d("RewriterConstClasses", "WebAppInterface rewritten. Output path: [" + outputPath + "]");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("RewriterConstClasses", "Error rewriting WebAppInterface: " + e.getMessage());
        }
    }

    // Метод для перезапису класу FileManager
    public static void rewriteFileManager(String outputPath, String package_project) {
        try (PrintWriter output = new PrintWriter(new FileWriter(outputPath))) {
            output.println("package " + package_project + ".data;");
            output.println();
            output.println("import java.io.File;");
            output.println("import java.io.FileWriter;");
            output.println("import java.io.FileReader;");
            output.println("import java.io.BufferedReader;");
            output.println("import java.io.IOException;");
            output.println();
            output.println("public class FileManager {");
            output.println();
            output.println("    public static void saveToFile(String filename, String data) {");
            output.println("        try {");
            output.println("            FileWriter writer = new FileWriter(filename);");
            output.println("            writer.write(data);");
            output.println("            writer.close();");
            output.println("            System.out.println(\"Data has been saved to \" + filename);");
            output.println("        } catch (IOException e) {");
            output.println("            e.printStackTrace();");
            output.println("        }");
            output.println("    }");
            output.println();
            output.println("    public static String readFromFile(String filename) {");
            output.println("        StringBuilder content = new StringBuilder();");
            output.println("        try {");
            output.println("            BufferedReader reader = new BufferedReader(new FileReader(filename));");
            output.println("            String line;");
            output.println("            while ((line = reader.readLine()) != null) {");
            output.println("                content.append(line);");
            output.println("                content.append(\"\\n\");");
            output.println("            }");
            output.println("            reader.close();");
            output.println("        } catch (IOException e) {");
            output.println("            e.printStackTrace();");
            output.println("        }");
            output.println("        return content.toString();");
            output.println("    }");
            output.println("}");

            Log.d("RewriterConstClasses", "FileManager rewritten. Output path: [" + outputPath + "]");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("RewriterConstClasses", "Error rewriting FileManager: " + e.getMessage());
        }
    }

    // Метод для перезапису класу PreferenceConfig
    public static void rewritePreferenceConfig(String outputPath, String package_project) {
        try (PrintWriter output = new PrintWriter(new FileWriter(outputPath))) {
            output.println("package " + package_project + ".data;");
            output.println();
            output.println("import android.content.Context;");
            output.println("import android.content.SharedPreferences;");
            output.println();
            output.println("public class PreferenceConfig {");
            output.println("    public static final String REFERENCE = \"reference\";");
            output.println();
            output.println("    public static final String CHOOSE = \"choose\";");
            output.println("    public static final String VOLUME_LEVEL = \"volume_level\";");
            output.println();
            output.println("    public static final String IVAN = \"ivan\";");
            output.println("    public static final String KATYA = \"katya\";");
            output.println("    public static final String ANTONIYA = \"antoniya\";");
            output.println("    public static final String EVGENIY_ANATOLIEVICH = \"evgeniy_anatolievich\";");
            output.println("    public static final String IGOR = \"igor\";");
            output.println("    public static final String VADYM = \"vadym\";");
            output.println("    public static final String SONYA = \"sonya\";");
            output.println("    public static final String BEAR = \"bear\";");
            output.println("    public static final String ANIM_SWITCH_VALUE = \"anim_switch_value\";");
            output.println("    public static final String VALUEFORLOAD = \"value for loading\";");
            output.println();
            output.println("    public static void registerPref(Context context, SharedPreferences.OnSharedPreferenceChangeListener listener) {");
            output.println("        SharedPreferences pref = context.getSharedPreferences(REFERENCE, Context.MODE_PRIVATE);");
            output.println("        pref.registerOnSharedPreferenceChangeListener(listener);");
            output.println("    }");
            output.println();
            output.println("    public static void setChoose(Context context, int value) {");
            output.println("        SharedPreferences pref = context.getSharedPreferences(REFERENCE, Context.MODE_PRIVATE);");
            output.println("        SharedPreferences.Editor editor = pref.edit();");
            output.println("        editor.putInt(CHOOSE, value);");
            output.println("        editor.apply();");
            output.println("    }");
            output.println();
            output.println("    public static int getChoose(Context context) {");
            output.println("        SharedPreferences pref = context.getSharedPreferences(REFERENCE, Context.MODE_PRIVATE);");
            output.println("        return pref.getInt(CHOOSE, 0);");
            output.println("    }");
            output.println();
            output.println("    public static void setVolumeLevel(Context context, float value) {");
            output.println("        SharedPreferences pref = context.getSharedPreferences(REFERENCE, Context.MODE_PRIVATE);");
            output.println("        SharedPreferences.Editor editor = pref.edit();");
            output.println("        editor.putFloat(VOLUME_LEVEL, value);");
            output.println("        editor.apply();");
            output.println("    }");
            output.println();
            output.println("    public static float getVolumeLevel(Context context) {");
            output.println("        SharedPreferences pref = context.getSharedPreferences(REFERENCE, Context.MODE_PRIVATE);");
            output.println("        return pref.getFloat(VOLUME_LEVEL, 100.0f);");
            output.println("    }");
            output.println();
            output.println("    public static void setIvanValue(Context context, int value) {");
            output.println("        SharedPreferences pref = context.getSharedPreferences(REFERENCE, Context.MODE_PRIVATE);");
            output.println("        SharedPreferences.Editor editor = pref.edit();");
            output.println("        editor.putInt(IVAN, value);");
            output.println("        editor.apply();");
            output.println("    }");
            output.println();
            output.println("    public static int getIvanValue(Context context) {");
            output.println("        SharedPreferences pref = context.getSharedPreferences(REFERENCE, Context.MODE_PRIVATE);");
            output.println("        return pref.getInt(IVAN, 0);");
            output.println("    }");
            output.println();
            output.println("    public static void setKatyaValue(Context context, int value) {");
            output.println("        SharedPreferences pref = context.getSharedPreferences(REFERENCE, Context.MODE_PRIVATE);");
            output.println("        SharedPreferences.Editor editor = pref.edit();");
            output.println("        editor.putInt(KATYA, value);");
            output.println("        editor.apply();");
            output.println("    }");
            output.println();
            output.println("    public static int getKatyaValue(Context context) {");
            output.println("        SharedPreferences pref = context.getSharedPreferences(REFERENCE, Context.MODE_PRIVATE);");
            output.println("        return pref.getInt(KATYA, 0);");
            output.println("    }");
            output.println();
            output.println("    public static void setAntonyaValue(Context context, int value) {");
            output.println("        SharedPreferences pref = context.getSharedPreferences(REFERENCE, Context.MODE_PRIVATE);");
            output.println("        SharedPreferences.Editor editor = pref.edit();");
            output.println("        editor.putInt(ANTONIYA, value);");
            output.println("        editor.apply();");
            output.println("    }");
            output.println();
            output.println("    public static int getAntonyaValue(Context context) {");
            output.println("        SharedPreferences pref = context.getSharedPreferences(REFERENCE, Context.MODE_PRIVATE);");
            output.println("        return pref.getInt(ANTONIYA, 0);");
            output.println("    }");
            output.println();
            output.println("    public static void setEvgeniyAnatolievichValue(Context context, int value) {");
            output.println("        SharedPreferences pref = context.getSharedPreferences(REFERENCE, Context.MODE_PRIVATE);");
            output.println("        SharedPreferences.Editor editor = pref.edit();");
            output.println("        editor.putInt(EVGENIY_ANATOLIEVICH, value);");
            output.println("        editor.apply();");
            output.println("    }");
            output.println();
            output.println("    public static int getEvgeniyAnatolievichValue(Context context) {");
            output.println("        SharedPreferences pref = context.getSharedPreferences(REFERENCE, Context.MODE_PRIVATE);");
            output.println("        return pref.getInt(EVGENIY_ANATOLIEVICH, 0);");
            output.println("    }");
            output.println();
            output.println("    public static void setIgorValue(Context context, int value) {");
            output.println("        SharedPreferences pref = context.getSharedPreferences(REFERENCE, Context.MODE_PRIVATE);");
            output.println("        SharedPreferences.Editor editor = pref.edit();");
            output.println("        editor.putInt(IGOR, value);");
            output.println("        editor.apply();");
            output.println("    }");
            output.println();
            output.println("    public static int getIgorValue(Context context) {");
            output.println("        SharedPreferences pref = context.getSharedPreferences(REFERENCE, Context.MODE_PRIVATE);");
            output.println("        return pref.getInt(IGOR, 0);");
            output.println("    }");
            output.println();
            output.println("    public static void setVadymValue(Context context, int value) {");
            output.println("        SharedPreferences pref = context.getSharedPreferences(REFERENCE, Context.MODE_PRIVATE);");
            output.println("        SharedPreferences.Editor editor = pref.edit();");
            output.println("        editor.putInt(VADYM, value);");
            output.println("        editor.apply();");
            output.println("    }");
            output.println();
            output.println("    public static int getVadymValue(Context context) {");
            output.println("        SharedPreferences pref = context.getSharedPreferences(REFERENCE, Context.MODE_PRIVATE);");
            output.println("        return pref.getInt(VADYM, 0);");
            output.println("    }");
            output.println();
            output.println("    public static void setSonyaValue(Context context, int value) {");
            output.println("        SharedPreferences pref = context.getSharedPreferences(REFERENCE, Context.MODE_PRIVATE);");
            output.println("        SharedPreferences.Editor editor = pref.edit();");
            output.println("        editor.putInt(SONYA, value);");
            output.println("        editor.apply();");
            output.println("    }");
            output.println();
            output.println("    public static int getSonyaValue(Context context) {");
            output.println("        SharedPreferences pref = context.getSharedPreferences(REFERENCE, Context.MODE_PRIVATE);");
            output.println("        return pref.getInt(SONYA, 0);");
            output.println("    }");
            output.println();
            output.println("    public static void setBearValue(Context context, int value) {");
            output.println("        SharedPreferences pref = context.getSharedPreferences(REFERENCE, Context.MODE_PRIVATE);");
            output.println("        SharedPreferences.Editor editor = pref.edit();");
            output.println("        editor.putInt(BEAR, value);");
            output.println("        editor.apply();");
            output.println("    }");
            output.println();
            output.println("    public static int getBearValue(Context context) {");
            output.println("        SharedPreferences pref = context.getSharedPreferences(REFERENCE, Context.MODE_PRIVATE);");
            output.println("        return pref.getInt(BEAR, 0);");
            output.println("    }");
            output.println();
            output.println("    public static void setAnimSwitchValue(Context context, boolean value) {");
            output.println("        SharedPreferences pref = context.getSharedPreferences(REFERENCE, Context.MODE_PRIVATE);");
            output.println("        SharedPreferences.Editor editor = pref.edit();");
            output.println("        editor.putBoolean(ANIM_SWITCH_VALUE, value);");
            output.println("        editor.apply();");
            output.println("    }");
            output.println();
            output.println("    public static boolean getAnimSwitchValue(Context context) {");
            output.println("        SharedPreferences pref = context.getSharedPreferences(REFERENCE, Context.MODE_PRIVATE);");
            output.println("        return pref.getBoolean(ANIM_SWITCH_VALUE, true);");
            output.println("    }");
            output.println();
            output.println("    public static void setValue(Context context, int value) {");
            output.println("        SharedPreferences pref = context.getSharedPreferences(REFERENCE, Context.MODE_PRIVATE);");
            output.println("        SharedPreferences.Editor editor = pref.edit();");
            output.println("        editor.putInt(VALUEFORLOAD, value);");
            output.println("        editor.apply();");
            output.println("    }");
            output.println();
            output.println("    public static int getValue(Context context) {");
            output.println("        SharedPreferences pref = context.getSharedPreferences(REFERENCE, Context.MODE_PRIVATE);");
            output.println("        return pref.getInt(VALUEFORLOAD, 0);");
            output.println("    }");
            output.println("}");

            Log.d("RewriterConstClasses", "PreferenceConfig rewritten. Output path: [" + outputPath + "]");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("RewriterConstClasses", "Error rewriting PreferenceConfig: " + e.getMessage());
        }
    }

    // AsyncTask для виконання операції генерації маніфесту у фоновому потоці
    public static class FileOperationTask extends AsyncTask<Void, Void, Void> {
        private String outputPath;
        private String inputPath;
        private Context context;

        public FileOperationTask(String outputPath, Context context) {
            this.outputPath = outputPath;
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            generateManifest(outputPath, context);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // Оновіть UI після завершення операції
            Log.d("ManifestGenerationTask", "Manifest generation completed.");
        }
    }
}