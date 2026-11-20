package org.firstinspires.ftc.teamcode.autonomus;

import android.content.Context;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
public class JsonLoader {
    public static AutonomaConfig load(Context context, String fileName) {
        try {
            InputStream is = context.getAssets().open(fileName);
            InputStreamReader reader = new InputStreamReader(is);
            return new Gson().fromJson(reader, AutonomaConfig.class);
        } catch (IOException e) {
            throw new RuntimeException("Impossibile aprire " + fileName + ": " + e.getMessage());
        }
    }
}
