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
import java.lang.reflect.Field;
import net.minecraft.command.CommandSource;
import org.lwjgl.glfw.GLFW;

public class MacroCommand extends Command {
   public MacroCommand() {
      super("macro");
   }

   @Override
   public void execute(LiteralArgumentBuilder<CommandSource> builder) {
      ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)builder.then(
                  this.literal("add")
                     .then(this.arg("name", StringArgumentType.word()).then(this.arg("bind", StringArgumentType.word()).suggests((context, builder1) -> {
                        Field[] var2 = GLFW.class.getDeclaredFields();
                        int var3 = var2.length;

                        for (int var4 = 0; var4 < var3; var4++) {
                           Field field = var2[var4];
                           String name = field.getName();
                           if (name.startsWith("GLFW_KEY_")) {
                              String bind = name.replace("GLFW_KEY_", "");
                              if (bind.startsWith(builder1.getRemaining())) {
                                 builder1.suggest(bind);
                              }
                           }
                        }

                        if ("NONE".startsWith(builder1.getRemaining().toUpperCase())) {
                           builder1.suggest("NONE");
                        }

                        return builder1.buildFuture();
                     }).then(this.arg("command", StringArgumentType.greedyString()).executes(context -> {
                        String name = (String)context.getArgument("name", String.class);
                        String bind = ((String)context.getArgument("bind", String.class)).toUpperCase();
                        String command = (String)context.getArgument("command", String.class);
                        if (Obsidian.INSTANCE.macroStorage.getMacro(name) != null) {
                           ObsidianComponent_hatUtil.sendMessage("Макрос " + name + " уже существует!");
                           return 1;
                        } else {
                           try {
                              int key = "NONE".equals(bind) ? -1 : GLFW.class.getField("GLFW_KEY_" + bind).getInt(null);
                              Obsidian.INSTANCE.macroStorage.add(new ObsidianComponent_203(name, command, new ObsidianComponent_261("bind", key)));
                              ObsidianComponent_hatUtil.sendMessage("Макрос " + name + " был добавлен!");
                           } catch (Exception var5) {
                              ObsidianComponent_hatUtil.sendMessage("Неверный бинд: " + bind);
                           }

                           return 1;
                        }
                     }))))
               ))
               .then(this.literal("remove").then(this.arg("name", StringArgumentType.word()).suggests((context, builder1) -> {
                  Obsidian.INSTANCE.macroStorage.getNames().stream().filter(name -> name.startsWith(builder1.getRemaining())).forEach(builder1::suggest);
                  return builder1.buildFuture();
               }).executes(context -> {
                  String name = (String)context.getArgument("name", String.class);
                  if (Obsidian.INSTANCE.macroStorage.isEmpty()) {
                     ObsidianComponent_hatUtil.sendMessage("Список макросов пуст!");
                     return 1;
                  } else {
                     ObsidianComponent_203 macro = Obsidian.INSTANCE.macroStorage.getMacro(name);
                     if (macro == null) {
                        ObsidianComponent_hatUtil.sendMessage("Макрос " + name + " не найден!");
                        return 1;
                     } else {
                        Obsidian.INSTANCE.macroStorage.remove(macro);
                        ObsidianComponent_hatUtil.sendMessage("Макрос " + name + " был удалён!");
                        return 1;
                     }
                  }
               }))))
            .then(this.literal("list").executes(context -> {
               StringBuilder builder1 = new StringBuilder();
               if (Obsidian.INSTANCE.macroStorage.getNames().isEmpty()) {
                  ObsidianComponent_hatUtil.sendMessage("Список макросов пуст!");
               } else {
                  for (int i = 0; i < Obsidian.INSTANCE.macroStorage.getNames().size(); i++) {
                     builder1.append(Obsidian.INSTANCE.macroStorage.getNames().get(i));
                     if (i < Obsidian.INSTANCE.macroStorage.getNames().size() - 1) {
                        builder1.append(", ");
                     }
                  }

                  builder1.append(".");
                  ObsidianComponent_hatUtil.sendMessage("Макросы: " + builder1);
               }

               return 1;
            })))
         .then(this.literal("clear").executes(context -> {
            if (!Obsidian.INSTANCE.macroStorage.isEmpty()) {
               Obsidian.INSTANCE.macroStorage.clear();
               ObsidianComponent_hatUtil.sendMessage("Все макросы были удалены!");
            } else {
               ObsidianComponent_hatUtil.sendMessage("Список макросов пуст!");
            }

            return 1;
         }));
   }
}
