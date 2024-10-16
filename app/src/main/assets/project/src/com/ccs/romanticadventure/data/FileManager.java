package com.ccs.romanticadventure.data;

import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

public class FileManager {

    public static void saveToFile(String filename, String data) {
        try {
            FileWriter writer = new FileWriter(filename);
            writer.write(data);
            writer.close();
            System.out.println("Data has been saved to " + filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readFromFile(String filename) {
        StringBuilder content = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
                content.append("\n");
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content.toString();
    }
}
