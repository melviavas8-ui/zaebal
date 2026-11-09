package wtf.obsidian.commands;

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

























import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.io.File;
import java.util.Arrays;
import net.minecraft.command.CommandSource;

public class ConfigCommand2 extends Command {
   public ConfigCommand2() {
      super("config");
   }

   @Override
   public void execute(LiteralArgumentBuilder<CommandSource> builder) {
      ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)builder.then(
                  this.literal("save").then(this.arg("config", StringArgumentType.word()).suggests((context, builder1) -> {
                     if (Obsidian.INSTANCE.configsDir.exists() && Obsidian.INSTANCE.configsDir.isDirectory()) {
                        File[] files = Obsidian.INSTANCE.configsDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".obs"));
                        if (files != null) {
                           Arrays.stream(files).map(File::getName).map(name -> name.replace(".obs", "")).forEach(builder1::suggest);
                        }
                     }

                     return builder1.buildFuture();
                  }).executes(context -> {
                     String config = (String)context.getArgument("config", String.class);

                     try {
                        Obsidian.INSTANCE.configStorage.saveConfig(config);
                        ObsidianComponent_hatUtil.sendMessage("Конфиг " + config + " успешно сохранён!");
                     } catch (Exception var3) {
                        ObsidianComponent_hatUtil.sendMessage("Ошибка при сохранении конфига " + config + "!");
                        var3.printStackTrace();
                     }

                     return 1;
                  }))
               ))
               .then(this.literal("load").then(this.arg("config", StringArgumentType.word()).suggests((context, builder1) -> {
                  if (Obsidian.INSTANCE.configsDir.exists() && Obsidian.INSTANCE.configsDir.isDirectory()) {
                     File[] files = Obsidian.INSTANCE.configsDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".obs"));
                     if (files != null) {
                        Arrays.stream(files).map(File::getName).map(name -> name.replace(".obs", "")).forEach(builder1::suggest);
                     }
                  }

                  return builder1.buildFuture();
               }).executes(context -> {
                  String config = (String)context.getArgument("config", String.class);

                  try {
                     Obsidian.INSTANCE.configStorage.loadConfig(config);
                     ObsidianComponent_hatUtil.sendMessage("Конфиг " + config + " успешно загружен!");
                  } catch (Exception var3) {
                     ObsidianComponent_hatUtil.sendMessage("Ошибка при загрузке конфига " + config + "!");
                     var3.printStackTrace();
                  }

                  return 1;
               }))))
            .then(this.literal("list").executes(context -> {
               File[] files = Obsidian.INSTANCE.configsDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".obs"));
               if (files != null && files.length != 0) {
                  StringBuilder builder1 = new StringBuilder();

                  for (int i = 0; i < files.length; i++) {
                     String fileName = files[i].getName().replace(".obs", "");
                     builder1.append(fileName);
                     if (i < files.length - 1) {
                        builder1.append(", ");
                     }
                  }

                  ObsidianComponent_hatUtil.sendMessage("Конфиги: " + builder1);
               } else {
                  ObsidianComponent_hatUtil.sendMessage("Список конфигов пуст!");
               }

               return 1;
            })))
         .then(this.literal("dir").executes(context -> {
            try {
               File configsDir = new File(Obsidian.INSTANCE.globalsDir, "configs");
               if (!configsDir.exists()) {
                  configsDir.mkdirs();
               }

               String[] var10002 = new String[2];
               var10002[0] = "explorer.exe";
               var10002[1] = configsDir.getAbsolutePath();
               new ProcessBuilder(var10002).start();
               ObsidianComponent_hatUtil.sendMessage("Папка с конфигами открыта!");
            } catch (Exception var2) {
               ObsidianComponent_hatUtil.sendMessage("Ошибка при открытии папки с конфигами!");
               var2.printStackTrace();
            }

            return 1;
         }));
   }
}
