package me.ihaq.windowsbin.file.files;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.ihaq.windowsbin.WindowsBin;
import me.ihaq.windowsbin.file.CustomFile;

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
    public void loadFile() throws IOException {
        FileReader fr = new FileReader(getFile());
        JsonObject jsonObject = getGson().fromJson(fr, JsonObject.class);
        fr.close();

        if (jsonObject == null)
            return;

        if (jsonObject.has(KEY))
            WindowsBin.INSTANCE.pasteBinKey = jsonObject.get(KEY).getAsString();
    }

    @Override
    public void saveFile() throws IOException {
        FileWriter fw = new FileWriter(getFile());

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(KEY, WindowsBin.INSTANCE.pasteBinKey);

        fw.write(getGson().toJson(jsonObject));
        fw.close();
    }
}
