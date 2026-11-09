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

























import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.command.CommandSource;

public class AutoLesCommand extends Command {
   public AutoLesCommand() {
      super("autoles");
   }

   @Override
   public void execute(LiteralArgumentBuilder<CommandSource> builder) {
      builder.executes(ctx -> {
         this.sendStatus();
         return 1;
      });
      builder.then(this.literal("enable").executes(ctx -> {
         if (!this.module().isCurrentSessionEnabled()) {
            this.module().enableForCurrentSession();
         }

         ObsidianComponent_hatUtil.sendMessage("АвтоЛес включён");
         return 1;
      }));
      builder.then(this.literal("disable").executes(ctx -> {
         if (this.module().isCurrentSessionEnabled()) {
            this.module().disableForCurrentSession();
         }

         ObsidianComponent_hatUtil.sendMessage("АвтоЛес выключен");
         return 1;
      }));
      builder.then(this.literal("mode").then(this.arg("value", StringArgumentType.word()).suggests((ctx, suggestions) -> {
         this.module().getModeSuggestions().forEach(suggestions::suggest);
         return suggestions.buildFuture();
      }).executes(ctx -> {
         String value = (String)ctx.getArgument("value", String.class);
         if (!this.module().setModeAlias(value)) {
            ObsidianComponent_hatUtil.sendMessage("Неизвестный режим. Доступно: normal, fast");
            return 1;
         } else {
            ObsidianComponent_hatUtil.sendMessage("Режим: " + this.module().getModeAlias());
            return 1;
         }
      })));
      builder.then(this.booleanSetting("swing", value -> this.module().setSwingEnabled(value), () -> this.module().isSwingEnabled()));
      builder.then(this.booleanSetting("autosell", value -> this.module().setAutoSellEnabled(value), () -> this.module().isAutoSellEnabled()));
      builder.then(this.booleanSetting("autopay", value -> this.module().setAutoPayEnabled(value), () -> this.module().isAutoPayEnabled()));
      builder.then(this.booleanSetting("visuals", value -> this.module().setPreserveVisualsEnabled(value), () -> this.module().isPreserveVisualsEnabled()));
      builder.then(this.floatSetting("pps", value -> this.module().setPacketsPerSecond(value), () -> this.module().getPacketsPerSecond()));
      builder.then(this.floatSetting("radius", value -> this.module().setBreakRadius(value), () -> this.module().getBreakRadius()));
      builder.then(this.floatSetting("payamount", value -> this.module().setPayAmount(value), () -> this.module().getPayAmount()));
      builder.then(this.floatSetting("interval", value -> this.module().setIntervalSeconds(value), () -> this.module().getIntervalSeconds()));
      builder.then(((LiteralArgumentBuilder)this.literal("pay").then(this.literal("clear").executes(ctx -> {
         this.module().clearPayTarget();
         ObsidianComponent_hatUtil.sendMessage("Ник для перевода очищен");
         return 1;
      }))).then(this.arg("nick", StringArgumentType.word()).executes(ctx -> {
         String nick = (String)ctx.getArgument("nick", String.class);
         if (!this.module().setPayTarget(nick)) {
            ObsidianComponent_hatUtil.sendMessage("Ник не может быть пустым");
            return 1;
         } else {
            ObsidianComponent_hatUtil.sendMessage("Ник для перевода: " + this.module().getPayTarget());
            return 1;
         }
      })));
      builder.then(this.literal("status").executes(ctx -> {
         this.sendStatus();
         return 1;
      }));
   }
   public LiteralArgumentBuilder<CommandSource> booleanSetting(String name, Consumer<Boolean> setter, Supplier<Boolean> getter) {
      return (LiteralArgumentBuilder<CommandSource>)this.literal(name).then(this.arg("value", BoolArgumentType.bool()).suggests((ctx, suggestions) -> {
         suggestions.suggest("true");
         suggestions.suggest("false");
         return suggestions.buildFuture();
      }).executes(ctx -> {
         boolean value = BoolArgumentType.getBool(ctx, "value");
         setter.accept(value);
         ObsidianComponent_hatUtil.sendMessage(this.settingLabel(name) + ": " + (getter.get() ? "включено" : "выключено"));
         return 1;
      }));
   }
   public LiteralArgumentBuilder<CommandSource> floatSetting(String name, Consumer<Float> setter, Supplier<Float> getter) {
      return (LiteralArgumentBuilder<CommandSource>)this.literal(name).then(this.arg("value", FloatArgumentType.floatArg()).executes(ctx -> {
         float value = FloatArgumentType.getFloat(ctx, "value");
         setter.accept(value);
         ObsidianComponent_hatUtil.sendMessage(this.settingLabel(name) + ": " + getter.get());
         return 1;
      }));
   }
   public void sendStatus() {
      AutoForestModule module = this.module();
      ObsidianComponent_hatUtil.sendMessage("АвтоЛес: " + (module.isCurrentSessionEnabled() ? "включён" : "выключен"));
      ObsidianComponent_hatUtil.sendMessage(
         "Режим="
            + module.getModeAlias()
            + ", Мах рукой="
            + this.booleanText(module.isSwingEnabled())
            + ", Автопродажа="
            + this.booleanText(module.isAutoSellEnabled())
            + ", AutoPay="
            + this.booleanText(module.isAutoPayEnabled())
            + ", Визуализация="
            + this.booleanText(module.isPreserveVisualsEnabled())
      );
      ObsidianComponent_hatUtil.sendMessage(
         "Пакетов в секунду="
            + module.getPacketsPerSecond()
            + ", Радиус="
            + module.getBreakRadius()
            + ", Сумма перевода="
            + module.getPayAmount()
            + ", Задержка="
            + module.getIntervalSeconds()
            + ", Ник перевода="
            + (module.getPayTarget().isBlank() ? "<пусто>" : module.getPayTarget())
      );
   }
   public AutoForestModule module() {
      return ModuleManager.autoForest;
   }
   public String booleanText(boolean value) {
      return value ? "включено" : "выключено";
   }
   public String settingLabel(String name) {
      int var3 = -1;
      switch (name.hashCode()) {
         case -938578798:
            if (name.equals("radius")) {
               var3 = 5;
            }
            break;
         case -646299495:
            if (name.equals("autopay")) {
               var3 = 2;
            }
            break;
         case -422649088:
            if (name.equals("payamount")) {
               var3 = 6;
            }
            break;
         case 111219:
            if (name.equals("pps")) {
               var3 = 4;
            }
            break;
         case 109854462:
            if (name.equals("swing")) {
               var3 = 0;
            }
            break;
         case 467099955:
            if (name.equals("visuals")) {
               var3 = 3;
            }
            break;
         case 570418373:
            if (name.equals("interval")) {
               var3 = 7;
            }
            break;
         case 1439645057:
            if (name.equals("autosell")) {
               var3 = 1;
            }
      }
      return switch (var3) {
         case 0 -> "Мах рукой";
         case 1 -> "Автопродажа";
         case 2 -> "AutoPay";
         case 3 -> "Визуализация";
         case 4 -> "Пакетов в секунду";
         case 5 -> "Радиус";
         case 6 -> "Сумма перевода";
         case 7 -> "Задержка";
         default -> name;
      };
   }
}
