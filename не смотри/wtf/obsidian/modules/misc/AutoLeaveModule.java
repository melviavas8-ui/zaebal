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

























import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;

public class AutoLeaveModule extends Module {
   public static final AutoLeaveModule INSTANCE = new AutoLeaveModule();
   public static final Set<String> STAFF_PREFIXES;
   private final SliderSetting leaveDistance = new SliderSetting(
      "Дистанция срабатывания",
      5.0f,
      3.0f,
      50.0f,
      1.0f
   );
   public final MultiBooleanSetting leaveIfSeen;
   public final ModeSetting leaveType;
   public final BooleanSetting stopBaritone;
   public final BooleanSetting leaveDisable;
   public int cooldownTicks;

   public AutoLeaveModule() {
      super("AutoLeave", "Выходит с сервера, когда замечает поблизости игрока", Category.MISC);
      BooleanSetting[] var10004 = new BooleanSetting[2];
      var10004[0] = new BooleanSetting("Игрок", true);
      var10004[1] = new BooleanSetting("Модератор", false);
      this.leaveIfSeen = new MultiBooleanSetting("Выходить если замечен", var10004);
      String[] var10005 = new String[4];
      var10005[0] = "В мейн меню";
      var10005[1] = "/hub";
      var10005[2] = "/home";
      var10005[3] = "/spawn";
      this.leaveType = new ModeSetting("Тип выхода", "В мейн меню", var10005);
      this.stopBaritone = new BooleanSetting("Выключать баритон", false);
      this.leaveDisable = new BooleanSetting("Выключать после выхода", true);
      Setting[] var10001 = new Setting[5];
      var10001[0] = this.leaveDistance;
      var10001[1] = this.leaveIfSeen;
      var10001[2] = this.leaveType;
      var10001[3] = this.stopBaritone;
      var10001[4] = this.leaveDisable;
      this.addSettings(var10001);
   }

   @Override
   public void onEnable() {
      this.cooldownTicks = 0;
      super.onEnable();
   }

   @Override
   public void onDisable() {
      this.cooldownTicks = 0;
      super.onDisable();
   }

   @EventTarget
   public void onUpdate(EventTick event) {
      if (mc.player != null && mc.world != null) {
         if (this.cooldownTicks > 0) {
            this.cooldownTicks -= 1;
         } else {
            float maxDistance = this.leaveDistance.get();

            for (PlayerEntity player : mc.world.getPlayers()) {
               if (player != null && player != mc.player && mc.player.distanceTo(player) <= maxDistance && this.shouldLeaveFor(player)) {
                  this.triggerLeave();
                  break;
               }
            }
         }
      }
   }
   public boolean shouldLeaveFor(PlayerEntity player) {
      return this.isModerator(player) ? this.leaveIfSeen.is("Модератор") : this.leaveIfSeen.is("Игрок");
   }
   public boolean isModerator(PlayerEntity player) {
      if (player == null) {
         return false;
      } else {
         String name = player.getName().getString();
         if (Obsidian.INSTANCE != null && Obsidian.INSTANCE.staffStorage != null && Obsidian.INSTANCE.staffStorage.isStaff(name)) {
            return true;
         } else {
            Team team = player.getScoreboardTeam();
            if (team == null) {
               return false;
            } else {
               String prefix = team.getPrefix().getString().toLowerCase(Locale.ROOT);

               for (String candidate : STAFF_PREFIXES) {
                  if (prefix.contains(candidate)) {
                     return true;
                  }
               }

               return false;
            }
         }
      }
   }
   public void triggerLeave() {
      this.tryStopBaritone();
      String var1 = this.leaveType.getCurrent();
      int var2 = -1;
      switch (var1.hashCode()) {
         case 1503846:
            if (var1.equals("/hub")) {
               var2 = 1;
            }
            break;
         case 46613902:
            if (var1.equals("/home")) {
               var2 = 2;
            }
            break;
         case 193977407:
            if (var1.equals("В мейн меню")) {
               var2 = 0;
            }
            break;
         case 1455208620:
            if (var1.equals("/spawn")) {
               var2 = 3;
            }
      }

      switch (var2) {
         case 0:
            this.disconnectLeave();
            break;
         case 1:
            this.commandLeave("hub");
            break;
         case 2:
            this.commandLeave("home home");
            break;
         case 3:
            this.commandLeave("spawn");
      }
   }
   public void tryStopBaritone() {
      if (this.stopBaritone.isState() && mc.getNetworkHandler() != null) {
         mc.getNetworkHandler().sendChatMessage("#stop");
      }
   }
   public void disconnectLeave() {
      if (mc.getNetworkHandler() == null) {
         ObsidianComponent_hatUtil.sendMessage("Модуль не работает в одиночном мире");
      } else {
         mc.getNetworkHandler().getConnection().disconnect(Text.literal("AutoLeave"));
         if (this.leaveDisable.isState()) {
            this.toggle();
         }
      }
   }
   public void commandLeave(String command) {
      if (mc.getNetworkHandler() == null) {
         ObsidianComponent_hatUtil.sendMessage("AutoLeave нельзя использовать в одиночной игре!");
      } else {
         mc.getNetworkHandler().sendChatCommand(command);
         this.cooldownTicks = this.leaveDisable.isState() ? 10 : 30;
         if (this.leaveDisable.isState()) {
            this.toggle();
         }
      }
   }

   static {
      String[] var10002 = new String[17];
      var10002[0] = "supp";
      var10002[1] = "mod";
      var10002[2] = "der";
      var10002[3] = "adm";
      var10002[4] = "wne";
      var10002[5] = "curat";
      var10002[6] = "dev";
      var10002[7] = "yt";
      var10002[8] = "мод";
      var10002[9] = "помо";
      var10002[10] = "адм";
      var10002[11] = "владе";
      var10002[12] = "курато";
      var10002[13] = "сапп";
      var10002[14] = "ютуб";
      var10002[15] = "стажер";
      var10002[16] = "сотрудник";
      STAFF_PREFIXES = new HashSet<>(Arrays.asList(var10002));
   }
}
