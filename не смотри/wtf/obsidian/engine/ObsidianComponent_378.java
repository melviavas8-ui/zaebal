package wtf.obsidian.engine;

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
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.command.CommandSource;

public class ObsidianComponent_378 {
   private final CommandDispatcher<CommandSource> dispatcher = new CommandDispatcher();
   private final List<Command> commands = new ArrayList<>();
   public String prefix = ".";

   public ObsidianComponent_378() {
      this.registry();
   }
   public void registry() {
      Command[] var10001 = new Command[13];
      var10001[0] = new AutoLesCommand();
      var10001[1] = new FriendCommand2();
      var10001[2] = new ConfigCommand2();
      var10001[3] = new MacroCommand();
      var10001[4] = new BotCommand();
      var10001[5] = new BlockespCommand();
      var10001[6] = new NukerCommand();
      var10001[7] = new NukerCommand("nuk");
      var10001[8] = new GpsCommand2();
      var10001[9] = new ConfigCommand();
      var10001[10] = new StaffCommand();
      var10001[11] = new VclipCommand();
      var10001[12] = new DataCommand();
      this.addCommands(var10001);
   }

   public CommandSource getSource() {
      return new ClientCommandSource(null, MinecraftClient.getInstance());
   }
   public void addCommands(Command... command) {
      Command[] var2 = command;
      int var3 = command.length;

      for (int var4 = 0; var4 < var3; var4++) {
         Command cmd = var2[var4];
         cmd.register(this.dispatcher);
         this.commands.add(cmd);
      }
   }

   @Generated
   public CommandDispatcher<CommandSource> getDispatcher() {
      return this.dispatcher;
   }

   @Generated
   public List<Command> getCommands() {
      return this.commands;
   }

   @Generated
   public String getPrefix() {
      return this.prefix;
   }

   @Generated
   public void setPrefix(String prefix) {
      this.prefix = prefix;
   }
}
