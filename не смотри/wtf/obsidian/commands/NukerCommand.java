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

public class NukerCommand extends Command {
   public NukerCommand() {
      super("nuker");
   }

   public NukerCommand(String command) {
      super(command);
   }

   @Override
   public void execute(LiteralArgumentBuilder<CommandSource> builder) {
      ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)builder.then(
                  this.literal("add")
                     .then(
                        this.arg("block", StringArgumentType.word())
                           .suggests(
                              (context, builder1) -> {
                                 String input = NukerModule.normalizeBlockName(builder1.getRemaining());
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
                              String blockName = NukerModule.normalizeBlockName((String)context.getArgument("block", String.class));
                              if (NukerModule.INSTANCE.isTargetBlock(blockName)) {
                                 ObsidianComponent_hatUtil.sendMessage("§cБлок §e" + blockName + "§c уже в списке Nuker!");
                                 return 1;
                              } else if (!this.blockExists(blockName)) {
                                 ObsidianComponent_hatUtil.sendMessage("§cБлок §e" + blockName + "§c не найден!");
                                 return 1;
                              } else {
                                 NukerModule.INSTANCE.addBlock(blockName);
                                 ObsidianComponent_hatUtil.sendMessage("§aБлок §e" + blockName + "§a добавлен в Nuker!");
                                 return 1;
                              }
                           })
                     )
               ))
               .then(this.literal("remove").then(this.arg("block", StringArgumentType.word()).suggests((context, builder1) -> {
                  String input = NukerModule.normalizeBlockName(builder1.getRemaining());
                  NukerModule.INSTANCE.getTargetBlocks().stream().sorted(String::compareTo).filter(name -> name.startsWith(input)).forEach(builder1::suggest);
                  return builder1.buildFuture();
               }).executes(context -> {
                  String blockName = NukerModule.normalizeBlockName((String)context.getArgument("block", String.class));
                  if (!NukerModule.INSTANCE.isTargetBlock(blockName)) {
                     ObsidianComponent_hatUtil.sendMessage("§cБлока §e" + blockName + "§c нет в списке Nuker!");
                     return 1;
                  } else {
                     NukerModule.INSTANCE.removeBlock(blockName);
                     ObsidianComponent_hatUtil.sendMessage("§aБлок §e" + blockName + "§a удален из Nuker!");
                     return 1;
                  }
               }))))
            .then(this.literal("list").executes(context -> {
               Set<String> blocks = NukerModule.INSTANCE.getTargetBlocks();
               if (blocks.isEmpty()) {
                  ObsidianComponent_hatUtil.sendMessage("§cСписок Nuker пуст!");
                  return 1;
               } else {
                  String blockList = blocks.stream().sorted().collect(Collectors.joining("§7, §e"));
                  ObsidianComponent_hatUtil.sendMessage("§aБлоки Nuker §7(§e" + blocks.size() + "§7)§a: §e" + blockList);
                  return 1;
               }
            })))
         .then(this.literal("clear").executes(context -> {
            if (NukerModule.INSTANCE.getTargetBlocks().isEmpty()) {
               ObsidianComponent_hatUtil.sendMessage("§cСписок Nuker уже пуст!");
               return 1;
            } else {
               NukerModule.INSTANCE.clearBlocks();
               ObsidianComponent_hatUtil.sendMessage("§aСписок Nuker очищен!");
               return 1;
            }
         }));
   }
   public boolean blockExists(String blockName) {
      return Registries.BLOCK.stream().anyMatch(block -> Registries.BLOCK.getId(block).getPath().equalsIgnoreCase(blockName));
   }
}
