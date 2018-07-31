package me.ihaq.winbin.util;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.async.Callback;
import me.ihaq.winbin.WinBin;

public class WebUtils {

    private WebUtils() {
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