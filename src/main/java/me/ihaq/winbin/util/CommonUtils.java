package me.ihaq.winbin.util;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.async.Callback;
import me.ihaq.winbin.WinBin;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;

import java.awt.*;

import static me.ihaq.winbin.WinBin.INSTANCE;

public class CommonUtils {

    private CommonUtils() {
        
    }

    public static void shutdown(int code) {
        try {
            GlobalScreen.unregisterNativeHook();
        } catch (NativeHookException e) {
            e.printStackTrace();
        }
        showNotification(INSTANCE.NAME + " " + (code == 0 ? "stopped" : "crashed") + ".");
        System.exit(code);
    }

    public static void showNotification(String message) {
        INSTANCE.trayIcon.displayMessage(INSTANCE.NAME, message, TrayIcon.MessageType.INFO);
    }


    public static void makeNewPaste(Callback<String> callback, String contents) {
        Unirest.post("https://pastebin.com/api/api_post.php")
                .header("User-Agent", "Mozilla/5.0")
                .field("api_option", "paste")
                .field("api_dev_key", WinBin.INSTANCE.pasteBinKey)
                .field("api_paste_private", 0)
                .field("api_paste_code", contents)
                .field("api_expire_date", "N")
                .asStringAsync(callback);
    }
}
