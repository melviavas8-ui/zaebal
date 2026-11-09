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
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.command.CommandSource;

public class StaffCommand extends Command {
   public StaffCommand() {
      super("staff");
   }

   @Override
   public void execute(LiteralArgumentBuilder<CommandSource> builder) {
      ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)builder.then(
                  this.literal("add").then(this.arg("player", StringArgumentType.word()).suggests((context, builder1) -> {
                     for (PlayerListEntry entry : mc.getNetworkHandler().getPlayerList()) {
                        String name = entry.getProfile().getName();
                        if (name.toLowerCase().startsWith(builder1.getRemaining().toLowerCase())) {
                           builder1.suggest(name);
                        }
                     }

                     return builder1.buildFuture();
                  }).executes(context -> {
                     String player = (String)context.getArgument("player", String.class);
                     if (!Obsidian.INSTANCE.staffStorage.isStaff(player)) {
                        Obsidian.INSTANCE.staffStorage.add(player);
                        ObsidianComponent_hatUtil.sendMessage("Игрок " + player + " добавлен в список стаффов!");
                     } else {
                        ObsidianComponent_hatUtil.sendMessage("Игрок " + player + " уже в списке стаффов!");
                     }

                     return 1;
                  }))
               ))
               .then(
                  this.literal("remove")
                     .then(
                        this.arg("player", StringArgumentType.word())
                           .suggests(
                              (context, builder1) -> {
                                 Obsidian.INSTANCE
                                    .staffStorage
                                    .getStaffs()
                                    .stream()
                                    .sorted(String::compareTo)
                                    .filter(name -> name.startsWith(builder1.getRemaining()))
                                    .forEach(builder1::suggest);
                                 return builder1.buildFuture();
                              }
                           )
                           .executes(context -> {
                              String player = (String)context.getArgument("player", String.class);
                              if (Obsidian.INSTANCE.staffStorage.isStaff(player)) {
                                 Obsidian.INSTANCE.staffStorage.remove(player);
                                 ObsidianComponent_hatUtil.sendMessage("Игрок " + player + " удалён из списка стаффов!");
                              } else {
                                 ObsidianComponent_hatUtil.sendMessage("Игрок " + player + " не найден в списке стаффов!");
                              }

                              return 1;
                           })
                     )
               ))
            .then(this.literal("list").executes(context -> {
               StringBuilder builder1 = new StringBuilder();
               if (Obsidian.INSTANCE.staffStorage.getStaffs().isEmpty()) {
                  ObsidianComponent_hatUtil.sendMessage("Список стаффов пуст!");
               } else {
                  for (int i = 0; i < Obsidian.INSTANCE.staffStorage.getStaffs().size(); i++) {
                     builder1.append(Obsidian.INSTANCE.staffStorage.getStaffs().get(i));
                     if (i < Obsidian.INSTANCE.staffStorage.getStaffs().size() - (1)) {
                        builder1.append(", ");
                     }
                  }

                  builder1.append(".");
                  ObsidianComponent_hatUtil.sendMessage("Стаффы: " + builder1);
               }

               return 1;
            })))
         .then(this.literal("clear").executes(context -> {
            if (!Obsidian.INSTANCE.staffStorage.isEmpty()) {
               Obsidian.INSTANCE.staffStorage.clear();
               ObsidianComponent_hatUtil.sendMessage("Список стаффов очищен!");
            } else {
               ObsidianComponent_hatUtil.sendMessage("Список стаффов пуст!");
            }

            return 1;
         }));
   }
}
