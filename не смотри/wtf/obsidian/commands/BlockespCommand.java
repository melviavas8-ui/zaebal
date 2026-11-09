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
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.command.CommandSource;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class BlockespCommand extends Command {
   public BlockespCommand() {
      super("blockesp");
   }

   @Override
   public void execute(LiteralArgumentBuilder<CommandSource> builder) {
      ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)builder.then(
                  this.literal("add")
                     .then(
                        this.arg("block", StringArgumentType.word())
                           .suggests(
                              (context, builder1) -> {
                                 String input = builder1.getRemaining().toLowerCase();
                                 Registries.BLOCK
                                    .stream()
                                    .map(Registries.BLOCK::getId)
                                    .<String>map(Identifier::getPath)
                                    .filter(name -> name.startsWith(input))
                                    .limit(20L)
                                    .forEach(builder1::suggest);
                                 return builder1.buildFuture();
                              }
                           )
                           .executes(context -> {
                              String blockName = (String)context.getArgument("block", String.class);
                              if (BlockESPModule.INSTANCE.isTracking(blockName)) {
                                 ObsidianComponent_hatUtil.sendMessage("§cБлок §e" + blockName + "§c уже отслеживается!");
                                 return 1;
                              } else {
                                 boolean exists = Registries.BLOCK.stream().anyMatch(block -> {
                                    String name = Registries.BLOCK.getId(block).getPath();
                                    return name.equalsIgnoreCase(blockName);
                                 });
                                 if (!exists) {
                                    ObsidianComponent_hatUtil.sendMessage("§cБлок §e" + blockName + "§c не найден!");
                                    return 1;
                                 } else {
                                    BlockESPModule.INSTANCE.addBlock(blockName);
                                    ObsidianComponent_hatUtil.sendMessage("§aБлок §e" + blockName + "§a добавлен в отслеживание!");
                                    return 1;
                                 }
                              }
                           })
                     )
               ))
               .then(
                  this.literal("remove")
                     .then(
                        this.arg("block", StringArgumentType.word())
                           .suggests(
                              (context, builder1) -> {
                                 BlockESPModule.INSTANCE
                                    .getTrackedBlocks()
                                    .stream()
                                    .sorted(String::compareTo)
                                    .filter(name -> name.startsWith(builder1.getRemaining().toLowerCase()))
                                    .forEach(builder1::suggest);
                                 return builder1.buildFuture();
                              }
                           )
                           .executes(context -> {
                              String blockName = (String)context.getArgument("block", String.class);
                              if (!BlockESPModule.INSTANCE.isTracking(blockName)) {
                                 ObsidianComponent_hatUtil.sendMessage("§cБлок §e" + blockName + "§c не отслеживается!");
                                 return 1;
                              } else {
                                 BlockESPModule.INSTANCE.removeBlock(blockName);
                                 ObsidianComponent_hatUtil.sendMessage("§aБлок §e" + blockName + "§a удалён из отслеживания!");
                                 return 1;
                              }
                           })
                     )
               ))
            .then(this.literal("list").executes(context -> {
               Set<String> blocks = BlockESPModule.INSTANCE.getTrackedBlocks();
               if (blocks.isEmpty()) {
                  ObsidianComponent_hatUtil.sendMessage("§cСписок отслеживаемых блоков пуст!");
                  return 1;
               } else {
                  String blockList = blocks.stream().sorted().collect(Collectors.joining("§7, §e"));
                  ObsidianComponent_hatUtil.sendMessage("§aОтслеживаемые блоки §7(§e" + blocks.size() + "§7)§a: §e" + blockList);
                  return 1;
               }
            })))
         .then(this.literal("clear").executes(context -> {
            if (BlockESPModule.INSTANCE.getTrackedBlocks().isEmpty()) {
               ObsidianComponent_hatUtil.sendMessage("§cСписок отслеживаемых блоков уже пуст!");
               return 1;
            } else {
               BlockESPModule.INSTANCE.clearBlocks();
               ObsidianComponent_hatUtil.sendMessage("§aСписок отслеживаемых блоков очищен!");
               return 1;
            }
         }));
   }
}
