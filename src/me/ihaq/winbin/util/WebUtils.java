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
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
                connection.connect();

                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                wr.writeBytes("api_option=" + "paste" +
                        "&api_dev_key=" + WinBin.INSTANCE.pasteBinKey +
                        "&api_paste_private=" + 1 +
                        "&api_paste_name=" + "Untitled" +
                        "&api_paste_format=" + "text" +
                        "&api_paste_code=" + contents +
                        "&api_expire_date=" + "1D");
                wr.flush();
                wr.close();

                BufferedReader r = new BufferedReader(new InputStreamReader(connection.getInputStream(), Charset.forName("UTF-8")));

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    sb.append(line);
                }

                consumer.accept(sb.toString());

            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }
}