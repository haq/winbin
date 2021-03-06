package me.ihaq.winbin.file.files;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.ihaq.winbin.WinBin;
import me.ihaq.winbin.file.CustomFile;

import javax.swing.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ConfigFile extends CustomFile {

    private final String KEY = "key";

    public ConfigFile(Gson gson, File file) {
        super(gson, file);
    }

    @Override
    public void makeDirectory() {
        if (getFile() != null && !getFile().exists()) {
            WinBin.INSTANCE.pasteBinKey = JOptionPane.showInputDialog("Enter your Developer API Key:");
        }
        super.makeDirectory();
    }

    @Override
    public void loadFile() throws IOException {
        FileReader fr = new FileReader(getFile());
        JsonObject jsonObject = getGson().fromJson(fr, JsonObject.class);
        fr.close();

        if (jsonObject == null) {
            return;
        }

        if (jsonObject.has(KEY)) {
            WinBin.INSTANCE.pasteBinKey = jsonObject.get(KEY).getAsString();
        }
    }

    @Override
    public void saveFile() throws IOException {
        FileWriter fw = new FileWriter(getFile());

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(KEY, WinBin.INSTANCE.pasteBinKey);

        fw.write(getGson().toJson(jsonObject));
        fw.close();
    }
}
