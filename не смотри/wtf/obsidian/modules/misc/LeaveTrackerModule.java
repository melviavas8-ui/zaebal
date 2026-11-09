package wtf.obsidian.modules.misc;

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

























import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Formatting;

public class LeaveTrackerModule extends Module {
   public static LeaveTrackerModule INSTANCE = new LeaveTrackerModule();
   private final Map<UUID, ObsidianComponent_159> trackedPlayers = new HashMap<>();
   public ClientWorld lastWorld;
   public boolean initialized;

   public LeaveTrackerModule() {
      super("LeaveTracker", "Пишет координаты ливнутых игроков из прогрузки", Category.MISC);
   }

   @Override
   public void onDisable() {
      this.trackedPlayers.clear();
      this.initialized = false;
      super.onDisable();
   }

   @EventTarget
   public void onUpdate(EventTick event) {
      if (mc.player != null && mc.world != null) {
         if (mc.world != this.lastWorld) {
            this.lastWorld = mc.world;
            this.trackedPlayers.clear();
            this.initialized = false;
         }

         if (!this.initialized) {
            this.snapshotPlayers();
            this.initialized = true;
         } else {
            Set<UUID> seenPlayers = new HashSet<>();

            for (PlayerEntity player : mc.world.getPlayers()) {
               if (player != mc.player && player.isAlive()) {
                  UUID uuid = player.getUuid();
                  seenPlayers.add(uuid);
                  this.trackedPlayers.put(uuid, new ObsidianComponent_159(player.getName().getString(), player.getBlockPos()));
               }
            }

            Iterator<Entry<UUID, ObsidianComponent_159>> iterator = this.trackedPlayers.entrySet().iterator();

            while (iterator.hasNext()) {
               Entry<UUID, ObsidianComponent_159> entry = iterator.next();
               if (!seenPlayers.contains(entry.getKey())) {
                  ObsidianComponent_159 tracked = entry.getValue();
                  double distSq = mc.player.squaredDistanceTo((double)tracked.pos.getX(), (double)tracked.pos.getY(), (double)tracked.pos.getZ());
                  if (distSq < 65536.0) {
                     ObsidianComponent_hatUtil.sendMessage(
                        Formatting.GRAY
                           + tracked.name
                           + Formatting.WHITE
                           + " ливнул на "
                           + Formatting.GRAY
                           + tracked.pos.getX()
                           + " "
                           + tracked.pos.getY()
                           + " "
                           + tracked.pos.getZ()
                     );
                  }

                  iterator.remove();
               }
            }
         }
      }
   }
   public void snapshotPlayers() {
      this.trackedPlayers.clear();

      for (PlayerEntity player : mc.world.getPlayers()) {
         if (player != mc.player && player.isAlive()) {
            this.trackedPlayers.put(player.getUuid(), new ObsidianComponent_159(player.getName().getString(), player.getBlockPos()));
         }
      }
   }
}
