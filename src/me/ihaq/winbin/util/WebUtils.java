package me.ihaq.winbin.util;

import me.ihaq.winbin.WinBin;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.function.Consumer;

public class WebUtils {

    public static void makeNewPaste(Consumer<String> consumer, String contents) {
        WinBin.INSTANCE.executorService.submit(() -> {
            try {

                HttpURLConnection connection = (HttpURLConnection) new URL("https://pastebin.com/api/api_post.php").openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
                connection.setDoOutput(true);
                connection.connect();

                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                wr.writeBytes("api_option=" + "paste" +
                        "&api_dev_key=" + WinBin.INSTANCE.pasteBinKey +
                        "&api_paste_private=" + 1 +
                        "&api_paste_code=" + contents +
                        "&api_expire_date=" + "N");
                wr.flush();
                wr.close();

                BufferedReader r = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    sb.append(line);
                }
                r.close();

                consumer.accept(sb.toString());

            } catch (IOException e) {
                e.printStackTrace();
            }

        });

    }
}