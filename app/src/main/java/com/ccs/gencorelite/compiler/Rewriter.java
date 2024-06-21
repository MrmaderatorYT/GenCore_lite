package com.ccs.gencorelite.compiler;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Rewriter {

    void readFiles(String title) throws FileNotFoundException {
        try (BufferedReader reader = new BufferedReader(new FileReader(title)))  {
            String value = reader.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    void changeToJava(String title){
        String[] a = {};
    }
}
