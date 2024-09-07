package com.ccs.gencorelite.compiler;

public class Rewriter {

    static {
       System.loadLibrary("gencorelite");
    }
    public native void generateScript(String inputPath, String outputPath);

}
