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
import java.util.Optional;
import net.minecraft.command.CommandSource;
import org.lwjgl.glfw.GLFW;

public class ConfigCommand extends Command {
   public ConfigCommand() {
      super("bind");
   }

   @Override
   public void execute(LiteralArgumentBuilder<CommandSource> builder) {
      builder.then(
         this.literal("add")
            .then(
               this.arg("module", StringArgumentType.word())
                  .suggests(
                     (context, suggestionsBuilder) -> {
                        String remaining = suggestionsBuilder.getRemaining().toLowerCase();
                        ModuleManager.INSTANCE
                           .getObject()
                           .stream()
                           .map(Module::getName)
                           .filter(name -> name.toLowerCase().startsWith(remaining))
                           .forEach(suggestionsBuilder::suggest);
                        return suggestionsBuilder.buildFuture();
                     }
                  )
                  .then(this.arg("key", StringArgumentType.word()).suggests((context, suggestionsBuilder) -> {
                     String remaining = suggestionsBuilder.getRemaining().toUpperCase();
                     Field[] var3 = GLFW.class.getDeclaredFields();
                     int var4 = var3.length;

                     for (int var5 = 0; var5 < var4; var5++) {
                        Field field = var3[var5];
                        String fieldName = field.getName();
                        if (fieldName.startsWith("GLFW_KEY_")) {
                           String keyName = fieldName.replace("GLFW_KEY_", "");
                           if (keyName.startsWith(remaining)) {
                              suggestionsBuilder.suggest(keyName);
                           }
                        }
                     }

                     if ("NONE".startsWith(remaining)) {
                        suggestionsBuilder.suggest("NONE");
                     }

                     return suggestionsBuilder.buildFuture();
                  }).executes(ctx -> {
                     String moduleName = (String)ctx.getArgument("module", String.class);
                     Optional<Module> optionalModule = this.findModuleByName(moduleName);
                     if (optionalModule.isEmpty()) {
                        ObsidianComponent_hatUtil.sendMessage("Модуль " + moduleName + " не найден");
                        return 1;
                     } else {
                        Module module = optionalModule.get();
                        String keyName = ((String)ctx.getArgument("key", String.class)).toUpperCase();
                        int keyCode = this.getKeyCode(keyName);
                        if (keyCode == -1) {
                           ObsidianComponent_hatUtil.sendMessage("Клавиша " + keyName + " не найдена");
                        } else {
                           module.setKey(keyCode);
                           ObsidianComponent_hatUtil.sendMessage("Модуль " + module.getName() + " привязан к клавише " + keyName);
                        }

                        return 1;
                     }
                  }))
            )
      );
      builder.then(this.literal("remove").then(this.arg("module", StringArgumentType.word()).executes(ctx -> {
         String moduleName = (String)ctx.getArgument("module", String.class);
         Optional<Module> optionalModule = this.findModuleByName(moduleName);
         if (optionalModule.isEmpty()) {
            ObsidianComponent_hatUtil.sendMessage("Модуль " + moduleName + " не найден");
            return 1;
         } else {
            Module module = optionalModule.get();
            module.setKey(-1);
            ObsidianComponent_hatUtil.sendMessage("Привязка клавиши для модуля " + module.getName() + " удалена");
            return 1;
         }
      })));
      builder.then(this.literal("clear").executes(ctx -> {
         ModuleManager.INSTANCE.getObject().forEach(module -> module.setKey(-1));
         ObsidianComponent_hatUtil.sendMessage("Все привязки клавиш удалены");
         return 1;
      }));
      builder.then(
         this.literal("list")
            .executes(
               ctx -> {
                  StringBuilder bindingsList = new StringBuilder("Список привязанных модулей: ");
                  boolean hasBinds = ModuleManager.INSTANCE
                     .getObject()
                     .stream()
                     .filter(
                        module -> (module.getKey() != -1)
                     )
                     .peek(module -> bindingsList.append("Модуль: ").append(module.getName()).append(" -> Клавиша: ").append(module.getKey()).append("\n"))
                     .findAny()
                     .isPresent();
                  if (!hasBinds) {
                     ObsidianComponent_hatUtil.sendMessage("Нет привязанных модулей");
                  } else {
                     ObsidianComponent_hatUtil.sendMessage(bindingsList.toString());
                  }

                  return 1;
               }
            )
      );
   }
   public Optional<Module> findModuleByName(String moduleName) {
      return ModuleManager.INSTANCE.getObject().stream().filter(module -> module.getName().equalsIgnoreCase(moduleName)).findFirst();
   }
   public int getKeyCode(String keyName) {
      if ("NONE".equalsIgnoreCase(keyName)) {
         return -1;
      } else {
         try {
            return GLFW.class.getField("GLFW_KEY_" + keyName).getInt(null);
         } catch (IllegalAccessException | NoSuchFieldException var3) {
            return -1;
         }
      }
   }
}
