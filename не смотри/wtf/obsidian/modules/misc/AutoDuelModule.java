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

























import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.Vec3d;

public class AutoDuelModule extends Module {
   public static AutoDuelModule INSTANCE = new AutoDuelModule();
   public ModeSetting mode;
   private static final Pattern NAME_PATTERN = Pattern.compile("^\\w{3,16}$");
   public final List<String> sent;
   public final ObsidianComponent_372 duelT;
   public final ObsidianComponent_372 clrT;
   public final ObsidianComponent_372 pickT;
   public final ObsidianComponent_372 setT;
   public Vec3d lastPos;
   public boolean inDuel;

   public AutoDuelModule() {
      super("AutoDuel", "Автоматически кидает дуель", Category.MISC);
      String[] var10005 = new String[9];
      var10005[0] = "Щит";
      var10005[1] = "Шипы";
      var10005[2] = "Лук";
      var10005[3] = "Тотемы";
      var10005[4] = "Нодебафф";
      var10005[5] = "Шары";
      var10005[6] = "Классик";
      var10005[7] = "Читер";
      var10005[8] = "Незер";
      this.mode = new ModeSetting("Режим", "Шары", var10005);
      this.sent = new ArrayList<>();
      this.duelT = new ObsidianComponent_372();
      this.clrT = new ObsidianComponent_372();
      this.pickT = new ObsidianComponent_372();
      this.setT = new ObsidianComponent_372();
      Setting[] var10001 = new Setting[1];
      var10001[0] = this.mode;
      this.addSettings(var10001);
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.sent.clear();
      this.inDuel = false;
      if (mc.player != null) {
         this.lastPos = mc.player.getPos();
      }

      this.duelT.reset();
      this.clrT.reset();
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.sent.clear();
      this.inDuel = false;
   }

   @EventTarget
   public void onUpdate(EventTick e) {
      if (mc.player != null && mc.world != null && !this.inDuel) {
         if (this.lastPos != null
            && mc.player.getPos().distanceTo(this.lastPos) > 500.0) {
            this.toggle();
         } else {
            this.lastPos = mc.player.getPos();
            if (this.clrT.getElapsedTime() >= (30000L)) {
               this.sent.clear();
               this.clrT.reset();
            }

            if (this.duelT.getElapsedTime() >= (1000L)) {
               this.sendDuel();
               this.duelT.reset();
            }

            this.handleGui();
         }
      }
   }

   @EventTarget
   public void onPacket(PacketEvent e) {
      if (mc.player != null && mc.world != null) {
         if (e.getType() == PacketType.RECEIVE && e.getPacket() instanceof GameMessageS2CPacket p) {
            String msg = p.content().getString().toLowerCase();
            if (msg.contains("начало") && msg.contains("через") && msg.contains("секунд")
               || msg.contains("поединок начался")
               || msg.contains("во время поединка")) {
               this.inDuel = true;
               this.toggle();
            }
         }
      }
   }
   public void sendDuel() {
      for (String p : this.getPlayers()) {
         if (!this.sent.contains(p) && !p.equals(mc.player.getName().getString())) {
            mc.getNetworkHandler().sendChatCommand("duel " + p);
            this.sent.add(p);
            break;
         }
      }
   }
   public void handleGui() {
      if (mc.currentScreen instanceof GenericContainerScreen s) {
         int var4 = ((GenericContainerScreenHandler)s.getScreenHandler()).syncId;
         String t = s.getTitle().getString();
         if (t.contains("Выбор набора") && this.pickT.getElapsedTime() >= (150L)) {
            mc.interactionManager.clickSlot(var4, this.getModeSlot(), 0, SlotActionType.QUICK_MOVE, mc.player);
            this.pickT.reset();
         } else if (t.contains("Настройка поединка") && this.setT.getElapsedTime() >= (150L)) {
            mc.interactionManager
               .clickSlot(var4, 0, 0, SlotActionType.QUICK_MOVE, mc.player);
            this.setT.reset();
         }
      }
   }
   public int getModeSlot() {
      if (this.mode.is("Щит")) {
         return 0;
      } else if (this.mode.is("Шипы")) {
         return 1;
      } else if (this.mode.is("Лук")) {
         return 2;
      } else if (this.mode.is("Тотемы")) {
         return 3;
      } else if (this.mode.is("Нодебафф")) {
         return 4;
      } else if (this.mode.is("Шары")) {
         return 5;
      } else if (this.mode.is("Классик")) {
         return 6;
      } else if (this.mode.is("Читер")) {
         return 7;
      } else {
         return this.mode.is("Незер") ? 8 : 5;
      }
   }
   public List<String> getPlayers() {
      List<String> list = new ArrayList<>();
      if (mc.getNetworkHandler() == null) {
         return list;
      } else {
         for (PlayerListEntry e : mc.getNetworkHandler().getPlayerList()) {
            String n = e.getProfile().getName();
            if (NAME_PATTERN.matcher(n).matches()) {
               list.add(n);
            }
         }

         return list;
      }
   }
}
