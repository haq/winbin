package me.ihaq.winbin.util;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import me.ihaq.winbin.WinBin;

import java.util.function.Consumer;

public class WebUtils {

    public static void makeNewPaste(Consumer<String> consumer, String contents) {
        WinBin.INSTANCE.executorService.submit(() -> {
            try {
                consumer.accept(
                        Unirest.post("https://pastebin.com/api/api_post.php")
                                .header("User-Agent", "Mozilla/5.0")
                                .field("api_option", "paste")
                                .field("api_dev_key", WinBin.INSTANCE.pasteBinKey)
                                .field("api_paste_private", 1)
                                .field("api_paste_code", contents)
                                .field("api_expire_date", "N")
                                .asString().getBody()
                );
            } catch (UnirestException e) {
                e.printStackTrace();
            }
        });

    }
}