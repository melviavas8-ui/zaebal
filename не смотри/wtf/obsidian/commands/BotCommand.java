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
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;

public class BotCommand extends Command {
   public BotCommand() {
      super("bot");
   }

   @Override
   public void execute(LiteralArgumentBuilder<CommandSource> builder) {
      ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)builder.then(
                              this.literal("connect")
                                 .then(this.arg("name", StringArgumentType.string()).then(this.arg("ip", StringArgumentType.string()).executes(context -> {
                                    MinecraftClient mc = MinecraftClient.getInstance();
                                    if (mc.player == null) {
                                       return 0;
                                    } else {
                                       String name = StringArgumentType.getString(context, "name");
                                       String ip = StringArgumentType.getString(context, "ip");
                                       BotManager.connect(name, ip);
                                       ObsidianComponent_hatUtil.sendMessage("§7[Bot] §fПодключение выполнено: " + name + " -> " + ip);
                                       return 1;
                                    }
                                 })))
                           ))
                           .then(this.literal("remove").then(this.arg("name", StringArgumentType.string()).suggests((context, suggestions) -> {
                              BotManager.getSessionNames(false).forEach(suggestions::suggest);
                              return suggestions.buildFuture();
                           }).executes(context -> {
                              MinecraftClient mc = MinecraftClient.getInstance();
                              if (mc.player == null) {
                                 return 0;
                              } else {
                                 String name = StringArgumentType.getString(context, "name");
                                 if (BotManager.remove(name)) {
                                    ObsidianComponent_hatUtil.sendMessage("§7[Bot] §fСессия отключена и удалена: " + name);
                                 } else {
                                    ObsidianComponent_hatUtil.sendMessage("§7[Bot] §fСессия не найдена: " + name);
                                 }

                                 return 1;
                              }
                           }))))
                        .then(this.literal("control").then(this.arg("name", StringArgumentType.string()).suggests((context, suggestions) -> {
                           BotManager.getSessionNames(false).forEach(suggestions::suggest);
                           return suggestions.buildFuture();
                        }).executes(context -> {
                           MinecraftClient mc = MinecraftClient.getInstance();
                           if (mc.player == null) {
                              return 0;
                           } else {
                              String name = StringArgumentType.getString(context, "name");
                              if (name.equalsIgnoreCase(BotManager.getCurrentSessionName())) {
                                 ObsidianComponent_hatUtil.sendMessage("§7[Bot] §fТы уже управляешь этой сессией: " + name);
                                 return 1;
                              } else {
                                 if (BotManager.control(name)) {
                                    ObsidianComponent_hatUtil.sendMessage("§7[Bot] §fПереключаю на сессию: " + name);
                                 } else {
                                    ObsidianComponent_hatUtil.sendMessage("§7[Bot] §fСессия не найдена: " + name);
                                 }

                                 return 1;
                              }
                           }
                        }))))
                     .then(this.literal("say").then(this.arg("name", StringArgumentType.string()).suggests((context, suggestions) -> {
                        BotManager.getSessionNames(false).forEach(suggestions::suggest);
                        return suggestions.buildFuture();
                     }).then(this.arg("message", StringArgumentType.greedyString()).executes(context -> {
                        MinecraftClient mc = MinecraftClient.getInstance();
                        if (mc.player == null) {
                           return 0;
                        } else {
                           String name = StringArgumentType.getString(context, "name");
                           String message = StringArgumentType.getString(context, "message");
                           if (BotManager.say(name, message)) {
                              ObsidianComponent_hatUtil.sendMessage("§7[Bot] §fСообщение отправлено от сессии " + name);
                           } else {
                              ObsidianComponent_hatUtil.sendMessage("§7[Bot] §fСессия не найдена: " + name);
                           }

                           return 1;
                        }
                     })))))
                  .then(this.literal("sayall").then(this.arg("message", StringArgumentType.greedyString()).executes(context -> {
                     MinecraftClient mc = MinecraftClient.getInstance();
                     if (mc.player == null) {
                        return 0;
                     } else {
                        String message = StringArgumentType.getString(context, "message");
                        BotManager.sayAll(message);
                        ObsidianComponent_hatUtil.sendMessage("§7[Bot] §fСообщение отправлено от всех ботов.");
                        return 1;
                     }
                  }))))
               .then(((LiteralArgumentBuilder)this.literal("return").executes(context -> {
                  MinecraftClient mc = MinecraftClient.getInstance();
                  if (mc.player == null) {
                     return 0;
                  } else {
                     if (BotManager.restore()) {
                        ObsidianComponent_hatUtil.sendMessage("§7[Bot] §fВозвращаю предыдущую сессию");
                     } else {
                        ObsidianComponent_hatUtil.sendMessage("§7[Bot] §fНет сохранённой сессии для возврата");
                     }

                     return 1;
                  }
               })).then(this.arg("name", StringArgumentType.string()).suggests((context, suggestions) -> {
                  BotManager.getSessionNames(true).forEach(suggestions::suggest);
                  return suggestions.buildFuture();
               }).executes(context -> {
                  MinecraftClient mc = MinecraftClient.getInstance();
                  if (mc.player == null) {
                     return 0;
                  } else {
                     String name = StringArgumentType.getString(context, "name");
                     if (name.equalsIgnoreCase(BotManager.getCurrentSessionName())) {
                        ObsidianComponent_hatUtil.sendMessage("§7[Bot] §fТы уже управляешь этой сессией: " + name);
                        return 1;
                     } else {
                        if (BotManager.restore(name)) {
                           ObsidianComponent_hatUtil.sendMessage("§7[Bot] §fПереключаю на сессию: " + name);
                        } else {
                           ObsidianComponent_hatUtil.sendMessage("§7[Bot] §fСессия не найдена: " + name);
                        }

                        return 1;
                     }
                  }
               }))))
            .then(this.literal("ignore").executes(context -> {
               MinecraftClient mc = MinecraftClient.getInstance();
               if (mc.player == null) {
                  return 0;
               } else {
                  boolean enabled = BotManager.toggleIgnoreBotMessages();
                  ObsidianComponent_hatUtil.sendMessage("§7[Bot] §fИгнор сообщений ботов: " + (enabled ? "§aвключен" : "§cвыключен"));
                  return 1;
               }
            })))
         .then(this.literal("list").executes(context -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null) {
               return 0;
            } else {
               List<BotConnection> connections = BotManager.getConnections();
               ObsidianComponent_hatUtil.sendMessage("§7[Bot] §fТекущая сессия: " + BotManager.getCurrentSessionName());
               if (connections.isEmpty()) {
                  ObsidianComponent_hatUtil.sendMessage("§7[Bot] §fСписок сохранённых сессий пуст");
               } else {
                  ObsidianComponent_hatUtil.sendMessage("§7[Bot] §fСохранённые сессии:");

                  for (BotConnection bot : connections) {
                     ObsidianComponent_hatUtil.sendMessage("§7- §f" + bot.name() + " (§7" + bot.address() + "§f)");
                  }
               }

               return 1;
            }
         }));
   }
}
