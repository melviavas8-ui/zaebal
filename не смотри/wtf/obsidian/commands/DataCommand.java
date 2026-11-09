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
import java.util.List;
import net.minecraft.command.CommandSource;

public class DataCommand extends Command {
   public DataCommand() {
      super("data");
   }

   @Override
   public void execute(LiteralArgumentBuilder<CommandSource> builder) {
      ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)builder.executes(
                              context -> {
                                 this.sendStatus();
                                 return 1;
                              }
                           ))
                           .then(this.literal("record").executes(context -> {
                              KillAuraModule aura = this.getAura();
                              if (aura == null) {
                                 return 1;
                              } else {
                                 aura.getDataSystem().startRecording();
                                 ObsidianComponent_hatUtil.sendMessage("Data: запись начата, старые паттерны в памяти очищены");
                                 return 1;
                              }
                           })))
                        .then(((LiteralArgumentBuilder)this.literal("stop").executes(context -> {
                           this.stopRecording("data_" + System.currentTimeMillis());
                           return 1;
                        })).then(this.arg("name", StringArgumentType.greedyString()).executes(context -> {
                           this.stopRecording((String)context.getArgument("name", String.class));
                           return 1;
                        }))))
                     .then(
                        this.literal("play")
                           .then(
                              this.arg("name", StringArgumentType.word())
                                 .suggests(
                                    (context, suggestions) -> {
                                       this.getAuraPatterns()
                                          .stream()
                                          .filter(name -> name.toLowerCase().startsWith(suggestions.getRemaining().toLowerCase()))
                                          .forEach(suggestions::suggest);
                                       return suggestions.buildFuture();
                                    }
                                 )
                                 .executes(context -> {
                                    this.playProfile((String)context.getArgument("name", String.class));
                                    return 1;
                                 })
                           )
                     ))
                  .then(
                     this.literal("delete")
                        .then(
                           this.arg("name", StringArgumentType.word())
                              .suggests(
                                 (context, suggestions) -> {
                                    this.getAuraPatterns()
                                       .stream()
                                       .filter(name -> name.toLowerCase().startsWith(suggestions.getRemaining().toLowerCase()))
                                       .forEach(suggestions::suggest);
                                    return suggestions.buildFuture();
                                 }
                              )
                              .executes(context -> {
                                 this.deleteProfile((String)context.getArgument("name", String.class));
                                 return 1;
                              })
                        )
                  ))
               .then(this.literal("list").executes(context -> {
                  this.listProfiles();
                  return 1;
               })))
            .then(this.literal("clear").executes(context -> {
               KillAuraModule aura = this.getAura();
               if (aura == null) {
                  return 1;
               } else {
                  aura.getDataSystem().clearPatterns();
                  ObsidianComponent_hatUtil.sendMessage("Data: паттерны очищены");
                  return 1;
               }
            })))
         .then(this.literal("status").executes(context -> {
            this.sendStatus();
            return 1;
         }));
   }
   public void stopRecording(String name) {
      KillAuraModule aura = this.getAura();
      if (aura != null) {
         if (!aura.getDataSystem().isRecording()) {
            ObsidianComponent_hatUtil.sendMessage("Data: запись не запущена");
         } else if (!aura.getDataSystem().savePatterns(name)) {
            ObsidianComponent_hatUtil.sendMessage("Data: нечего сохранять");
         } else {
            aura.getDataSystem().stopRecording();
            ObsidianComponent_hatUtil.sendMessage("Data: запись остановлена и сохранена как " + name);
         }
      }
   }
   public void playProfile(String name) {
      KillAuraModule aura = this.getAura();
      if (aura != null) {
         if (!aura.getDataSystem().loadPatterns(name)) {
            ObsidianComponent_hatUtil.sendMessage("Data: профиль " + name + " не найден или поврежден");
         } else {
            aura.getDataSystem().setRecording(false);
            aura.getDataSystem().setUsingNeuro(true);
            aura.getDataSystem().resetState();
            ObsidianComponent_hatUtil.sendMessage("Data: загружен профиль " + name + " (" + aura.getDataSystem().getPatternCount() + " паттернов)");
            ObsidianComponent_hatUtil.sendMessage("Data: выбери режим ротации Data в Aura");
         }
      }
   }
   public void deleteProfile(String name) {
      KillAuraModule aura = this.getAura();
      if (aura != null) {
         if (aura.getDataSystem().deletePatterns(name)) {
            ObsidianComponent_hatUtil.sendMessage("Data: профиль " + name + " удален");
         } else {
            ObsidianComponent_hatUtil.sendMessage("Data: профиль " + name + " не найден");
         }
      }
   }
   public void listProfiles() {
      List<String> patterns = this.getAuraPatterns();
      if (patterns.isEmpty()) {
         ObsidianComponent_hatUtil.sendMessage("Data: нет сохраненных профилей");
      } else {
         ObsidianComponent_hatUtil.sendMessage("Data: сохраненные профили (" + patterns.size() + "):");

         for (String name : patterns) {
            ObsidianComponent_hatUtil.sendMessage("  - " + name);
         }
      }
   }
   public void sendStatus() {
      KillAuraModule aura = this.getAura();
      if (aura != null) {
         ObsidianComponent_hatUtil.sendMessage(aura.getDataSystem().getStatusString());
         if (aura.getDataSystem().getPatternCount() > 0) {
            ObsidianComponent_hatUtil.sendMessage("Новых в сессии: " + aura.getDataSystem().getRecordedThisSession());
         }
      }
   }
   public KillAuraModule getAura() {
      KillAuraModule aura = ModuleManager.INSTANCE == null ? null : ModuleManager.aura;
      if (aura == null) {
         ObsidianComponent_hatUtil.sendMessage("Data: модуль Aura не найден");
      }

      return aura;
   }
   public List<String> getAuraPatterns() {
      KillAuraModule aura = ModuleManager.INSTANCE == null ? null : ModuleManager.aura;
      return aura == null ? List.of() : aura.getDataSystem().getPatternNames();
   }
}
