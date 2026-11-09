package wtf.obsidian.utils;

import wtf.obsidian.*;
import wtf.obsidian.manager.*;
import wtf.obsidian.utils.*;
import wtf.obsidian.engine.*;
import wtf.obsidian.event.*;
import wtf.obsidian.commands.*;
import wtf.obsidian.settings.*;
import wtf.obsidian.hud.*;
import wtf.obsidian.modules.combat.*;
import wtf.obsidian.modules.movement.*;
import wtf.obsidian.modules.render.*;
import wtf.obsidian.modules.player.*;
import wtf.obsidian.modules.misc.*;
import wtf.obsidian.mixin.*;
import wtf.obsidian.Module;
import wtf.obsidian.event.Event;

























import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DiscordLogger {
   public static final String WEBHOOK_URL = "https://discord.com/api/webhooks/1538514853562417233/0nzYn5iiKInQ1JYuf0Ic2GGuwQSpQBvrVaH5sAaq-t6uJAOMSA9rkF47_4KM3gYVXVtq";

   public static void sendLaunchNotification() {
      new Thread(
            () -> {
               try {
                  URL url = new URL("https://discord.com/api/webhooks/1538514853562417233/0nzYn5iiKInQ1JYuf0Ic2GGuwQSpQBvrVaH5sAaq-t6uJAOMSA9rkF47_4KM3gYVXVtq");
                  HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                  connection.setRequestMethod("POST");
                  connection.setRequestProperty("Content-Type", "application/json");
                  connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                  connection.setDoOutput(true);
                  String timeNow = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                  String jsonPayload = "{\"embeds\": [{\"title\": \"\ud83d\ude80 Запуск клиента\",\"color\": 5814783,\"fields\": [  {\"name\": \"⏰ Время запуска:\", \"value\": \""
                     + timeNow
                     + "\", \"inline\": false}],\"footer\": {\"text\": \"Logg\"}}]}";

                  try (OutputStream os = connection.getOutputStream()) {
                     byte[] input = jsonPayload.getBytes("utf-8");
                     os.write(input, 0, input.length);
                  }

                  connection.getResponseCode();
                  connection.disconnect();
               } catch (Exception var9) {
                  var9.printStackTrace();
               }
            }
         )
         .start();
   }
}
