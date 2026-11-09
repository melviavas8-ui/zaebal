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
























import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.command.CommandSource;

public class GpsCommand2 extends Command {
   public GpsCommand2() {
      super("gps");
   }

   @Override
   public void execute(LiteralArgumentBuilder<CommandSource> builder) {
      ((LiteralArgumentBuilder)builder.then(
            this.arg("X", IntegerArgumentType.integer()).then(this.arg("Z", IntegerArgumentType.integer()).executes(context -> {
               int x = (Integer)context.getArgument("X", Integer.class);
               int z = (Integer)context.getArgument("Z", Integer.class);
               ObsidianComponent_153 waypoint = new ObsidianComponent_153((double)x, (double)z);
               Obsidian.INSTANCE.waypointStorage.set(waypoint);
               Object[] var10001 = new Object[2];
               var10001[0] = x;
               var10001[1] = z;
               ObsidianComponent_hatUtil.sendMessage(I18n.translate("Метка поставлена: ", var10001));
               return 1;
            }))
         ))
         .then(this.literal("remove").executes(context -> {
            if (!Obsidian.INSTANCE.waypointStorage.isEmpty()) {
               Obsidian.INSTANCE.waypointStorage.clear();
               ObsidianComponent_hatUtil.sendMessage(I18n.translate("Метка удалена!", new Object[0]));
            } else {
               ObsidianComponent_hatUtil.sendMessage(I18n.translate("Метки не было", new Object[0]));
            }

            return 1;
         }));
   }
}
