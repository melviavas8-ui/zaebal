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

























import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import lombok.Generated;
import net.minecraft.command.CommandSource;

public abstract class Command implements IInstance {
   public final String command;

   public Command(String command) {
      this.command = command;
   }

   public abstract void execute(LiteralArgumentBuilder<CommandSource> var1);

   public void register(CommandDispatcher<CommandSource> dispatcher) {
      LiteralArgumentBuilder<CommandSource> builder = LiteralArgumentBuilder.literal(this.command);
      this.execute(builder);
      dispatcher.register(builder);
   }

   protected <T> RequiredArgumentBuilder<CommandSource, T> arg(String name, ArgumentType<T> type) {
      return RequiredArgumentBuilder.argument(name, type);
   }

   protected LiteralArgumentBuilder<CommandSource> literal(String name) {
      return LiteralArgumentBuilder.literal(name);
   }

   @Generated
   public String getCommand() {
      return this.command;
   }
}
