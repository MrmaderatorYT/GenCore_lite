//package com.ccs.gencorelite.compiler;
//
//import android.content.Context;
//import android.os.Environment;
//import android.os.Handler;
//import android.os.Looper;
//import android.widget.Toast;
//
//import com.android.tools.r8.D8;
//import com.android.tools.r8.D8Command;
//import com.android.tools.r8.OutputMode;
//
//import org.eclipse.jdt.core.compiler.batch.BatchCompiler;
//
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.io.PrintWriter;
//import java.nio.file.Paths;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.zip.ZipEntry;
//import java.util.zip.ZipOutputStream;
//
//public class TestCompiler {
//    private static final int REQUEST_CODE = 123;
//
//    private Context context;
//    private Handler handler;
//
//    public TestCompiler(Context context) {
//        this.context = context;
//        this.handler = new Handler(Looper.getMainLooper());
//    }
//
//    public boolean compileApk() {
//        try {
//            File projectDir = createProjectStructure();
//            if (projectDir == null) {
//                showToast("Failed to create project structure");
//                return false;
//            }
//
//            boolean success = compileToDex(projectDir);
//            if (!success) {
//                showToast("Failed to compile to DEX");
//                return false;
//            }
//
//            success = packIntoApk(projectDir);
//            if (!success) {
//                showToast("Failed to pack into APK");
//                return false;
//            }
//
//            File apkFile = new File(projectDir, "app.apk");
//            if (apkFile.exists()) {
//                File outputApk = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "output.apk");
//                try (InputStream in = new FileInputStream(apkFile);
//                     OutputStream out = new FileOutputStream(outputApk)) {
//                    byte[] buffer = new byte[1024];
//                    int length;
//                    while ((length = in.read(buffer)) > 0) {
//                        out.write(buffer, 0, length);
//                    }
//                }
//                showToast("APK compiled successfully");
//                return true;
//            } else {
//                showToast("Failed to find APK file");
//                return false;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            showToast("Failed to compile APK: " + e.getMessage());
//            return false;
//        }
//    }
//
//    private void showToast(String message) {
//        handler.post(() -> Toast.makeText(context, message, Toast.LENGTH_LONG).show());
//    }
//
//    private File createProjectStructure() throws IOException {
//        File projectDir = new File(context.getCacheDir(), "MyApp");
//        if (projectDir.exists()) {
//            deleteRecursive(projectDir);
//        }
//
//        if (!projectDir.mkdirs()) {
//            return null;
//        }
//
//        File appDir = new File(projectDir, "app");
//        if (!appDir.mkdirs()) {
//            return null;
//        }
//
//        File srcDir = new File(appDir, "src/main/java/com/example/myapp");
//        if (!srcDir.mkdirs()) {
//            return null;
//        }
//
//        File resDir = new File(appDir, "src/main/res");
//        if (!resDir.mkdirs()) {
//            return null;
//        }
//
//        File manifestFile = new File(appDir, "src/main/AndroidManifest.xml");
//        try (FileWriter writer = new FileWriter(manifestFile)) {
//            writer.write("<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
//                    "    package=\"com.example.myapp\">\n" +
//                    "    <application\n" +
//                    "        android:allowBackup=\"true\"\n" +
//                    "        android:label=\"MyApp\"\n" +
//                    "        android:supportsRtl=\"true\"\n" +
//                    "        android:theme=\"@android:style/Theme.Material.Light\">\n" +
//                    "        <activity android:name=\".MainActivity\">\n" +
//                    "            <intent-filter>\n" +
//                    "                <action android:name=\"android.intent.action.MAIN\" />\n" +
//                    "                <category android:name=\"android.intent.category.LAUNCHER\" />\n" +
//                    "            </intent-filter>\n" +
//                    "        </activity>\n" +
//                    "    </application>\n" +
//                    "</manifest>");
//        }
//
//        File mainActivityFile = new File(srcDir, "MainActivity.java");
//        try (FileWriter writer = new FileWriter(mainActivityFile)) {
//            writer.write("package com.example.myapp;\n\n" +
//                    "import android.app.Activity;\n" +
//                    "import android.os.Bundle;\n" +
//                    "import android.widget.TextView;\n\n" +
//                    "public class MainActivity extends Activity {\n" +
//                    "    @Override\n" +
//                    "    protected void onCreate(Bundle savedInstanceState) {\n" +
//                    "        super.onCreate(savedInstanceState);\n" +
//                    "        TextView textView = new TextView(this);\n" +
//                    "        textView.setText(\"Hello, World!\");\n" +
//                    "        setContentView(textView);\n" +
//                    "    }\n" +
//                    "}");
//        }
//
//        File valuesDir = new File(resDir, "values");
//        if (!valuesDir.mkdirs()) {
//            return null;
//        }
//
//        File stringsFile = new File(valuesDir, "strings.xml");
//        try (FileWriter writer = new FileWriter(stringsFile)) {
//            writer.write("<resources>\n" +
//                    "    <string name=\"app_name\">MyApp</string>\n" +
//                    "</resources>");
//        }
//
//        return projectDir;
//    }
//
//    private void deleteRecursive(File fileOrDirectory) {
//        if (fileOrDirectory.isDirectory()) {
//            for (File child : fileOrDirectory.listFiles()) {
//                deleteRecursive(child);
//            }
//        }
//        fileOrDirectory.delete();
//    }
//
//    private boolean compileToDex(File projectDir) {
//        try {
//            File classesOutputDir = new File(context.getCacheDir(), "classes");
//            if (classesOutputDir.exists()) {
//                deleteRecursive(classesOutputDir);
//            }
//            classesOutputDir.mkdirs();
//
//            File srcDir = new File(projectDir, "app/src/main/java");
//            List<File> sourceFiles = getJavaFiles(srcDir);
//            List<String> sourceFilePaths = new ArrayList<>();
//            for (File file : sourceFiles) {
//                sourceFilePaths.add(file.getAbsolutePath());
//            }
//
//            String[] compileOptions = new String[2 + sourceFilePaths.size()];
//            compileOptions[0] = "-d";
//            compileOptions[1] = classesOutputDir.getAbsolutePath();
//            for (int i = 0; i < sourceFilePaths.size(); i++) {
//                compileOptions[i + 2] = sourceFilePaths.get(i);
//            }
//
//            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
//            PrintWriter writer = new PrintWriter(outStream);
//
//            BatchCompiler.compile(compileOptions, writer, writer, null);
//            writer.flush();
//
//            String result = outStream.toString();
//            if (!result.isEmpty()) {
//                showToast("Java Compilation failed: " + result);
//                return false;
//            }
//
//            File dexOutputDir = new File(context.getCacheDir(), "dex");
//            if (dexOutputDir.exists()) {
//                deleteRecursive(dexOutputDir);
//            }
//            dexOutputDir.mkdirs();
//
//            File dexFile = new File(dexOutputDir, "classes.dex");
//
//            D8.run(D8Command.builder()
//                    .addProgramFiles(Paths.get(classesOutputDir.getAbsolutePath()))
//                    .setOutput(Paths.get(dexFile.getAbsolutePath()), OutputMode.DexIndexed)
//                    .build());
//
//            return true;
//        } catch (Exception e) {
//            e.printStackTrace();
//            showToast("Failed to compile to DEX: " + e.getMessage());
//            return false;
//        }
//    }
//
//    private List<File> getJavaFiles(File dir) {
//        List<File> javaFiles = new ArrayList<>();
//        File[] files = dir.listFiles();
//        if (files != null) {
//            for (File file : files) {
//                if (file.isDirectory()) {
//                    javaFiles.addAll(getJavaFiles(file));
//                } else if (file.getName().endsWith(".java")) {
//                    javaFiles.add(file);
//                }
//            }
//        }
//        return javaFiles;
//    }
//
//    private boolean packIntoApk(File projectDir) {
//        try {
//            File dexFile = new File(context.getCacheDir(), "dex/classes.dex");
//            if (!dexFile.exists()) {
//                return false;
//            }
//
//            File apkFile = new File(projectDir, "app.apk");
//            try (FileOutputStream fos = new FileOutputStream(apkFile);
//                 ZipOutputStream zos = new ZipOutputStream(fos)) {
//
//                ZipEntry manifestEntry = new ZipEntry("AndroidManifest.xml");
//                zos.putNextEntry(manifestEntry);
//                try (InputStream in = new FileInputStream(new File(projectDir, "app/src/main/AndroidManifest.xml"))) {
//                    byte[] buffer = new byte[1024];
//                    int length;
//                    while ((length = in.read(buffer)) > 0) {
//                        zos.write(buffer, 0, length);
//                    }
//                }
//                zos.closeEntry();
//
//                ZipEntry dexEntry = new ZipEntry("classes.dex");
//                zos.putNextEntry(dexEntry);
//                try (InputStream in = new FileInputStream(dexFile)) {
//                    byte[] buffer = new byte[1024];
//                    int length;
//                    while ((length = in.read(buffer)) > 0) {
//                        zos.write(buffer, 0, length);
//                    }
//                }
//                zos.closeEntry();
//            }
//
//            return true;
//        } catch (IOException e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//}
