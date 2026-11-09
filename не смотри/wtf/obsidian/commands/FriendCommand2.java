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

public class FriendCommand2 extends Command {
   public FriendCommand2() {
      super("friend");
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
                     if (!Obsidian.INSTANCE.friendStorage.isFriend(player)) {
                        Obsidian.INSTANCE.friendStorage.add(player);
                        ObsidianComponent_hatUtil.sendMessage("Игрок " + player + " добавлен в друзья!");
                     } else {
                        ObsidianComponent_hatUtil.sendMessage("Игрок " + player + " уже в списке друзей!");
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
                                    .friendStorage
                                    .getFriends()
                                    .stream()
                                    .sorted(String::compareTo)
                                    .filter(name -> name.startsWith(builder1.getRemaining()))
                                    .forEach(builder1::suggest);
                                 return builder1.buildFuture();
                              }
                           )
                           .executes(context -> {
                              String player = (String)context.getArgument("player", String.class);
                              if (Obsidian.INSTANCE.friendStorage.isFriend(player)) {
                                 Obsidian.INSTANCE.friendStorage.remove(player);
                                 ObsidianComponent_hatUtil.sendMessage("Игрок " + player + " удалён из друзей!");
                              } else {
                                 ObsidianComponent_hatUtil.sendMessage("Игрок " + player + " не найден в списке друзей!");
                              }

                              return 1;
                           })
                     )
               ))
            .then(this.literal("list").executes(context -> {
               if (Obsidian.INSTANCE.friendStorage.getFriends().isEmpty()) {
                  ObsidianComponent_hatUtil.sendMessage("Список друзей пуст!");
               } else {
                  StringBuilder builder1 = new StringBuilder();

                  for (int i = 0; i < Obsidian.INSTANCE.friendStorage.getFriends().size(); i++) {
                     builder1.append(Obsidian.INSTANCE.friendStorage.getFriends().get(i));
                     if (i < Obsidian.INSTANCE.friendStorage.getFriends().size() - (1)) {
                        builder1.append(", ");
                     }
                  }

                  ObsidianComponent_hatUtil.sendMessage("Друзья: " + builder1);
               }

               return 1;
            })))
         .then(this.literal("clear").executes(context -> {
            if (!Obsidian.INSTANCE.friendStorage.isEmpty()) {
               Obsidian.INSTANCE.friendStorage.clear();
               ObsidianComponent_hatUtil.sendMessage("Список друзей очищен!");
            } else {
               ObsidianComponent_hatUtil.sendMessage("Список друзей пуст!");
            }

            return 1;
         }));
   }
}
