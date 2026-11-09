package wtf.obsidian.hud;

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
























import net.minecraft.client.network.ServerInfo;

public class SessionHudElement extends DraggableHudElement {
   private long sessionStartTime = System.currentTimeMillis();

   public SessionHudElement(ObsidianComponent_280 draggable) {
      super(draggable);
   }

   @Override
   public void onRender(EventRender2D eventRender) {
      float x = this.draggable.getX();
      float y = this.draggable.getY();
      long now = System.currentTimeMillis();
      float height = 18.0f;
      String serverName = "local";
      if (mc != null) {
         ServerInfo info = mc.getCurrentServerEntry();
         if (info != null && info.address != null && !info.address.isEmpty()) {
            serverName = info.address;
         }
      }

      String playerName = "unknown";
      if (mc != null && mc.player != null) {
         playerName = mc.player.getName().getString();
      } else if (mc != null && mc.getSession() != null) {
         playerName = mc.getSession().getUsername();
      }

      long elapsed = now - this.sessionStartTime;
      long totalSeconds = elapsed / (1000L);
      long hours = totalSeconds / (3600L);
      long minutes = totalSeconds
         % (3600L)
         / (60L);
      long seconds = totalSeconds % (60L);
      String playTime = hours + "h " + minutes + "m " + seconds + "s";
      String titleText = "sessioninfo";
      String serverText = "server: " + serverName;
      String nameText = "name: " + playerName;
      String playTimeText = "playtime: " + playTime;
      FontRenderer font = ObsidianComponent_201.getFont("suisse", 15);
      float titleWidth = font.getWidth(titleText);
      float serverWidth = font.getWidth(serverText);
      float nameWidth = font.getWidth(nameText);
      float playTimeWidth = font.getWidth(playTimeText);
      float maxTextWidth = Math.max(titleWidth, Math.max(serverWidth, Math.max(nameWidth, playTimeWidth)));
      float width = maxTextWidth + 10.0f;
      int time = (int)(
         (float)(now % (2000L))
            / 2000.0f
            * 360.0f
      );
      int leftTop = ObsidianComponent_olorUtil.getThemeColor(time);
      int leftBottom = ObsidianComponent_olorUtil.getThemeColor(time + (30));
      int centerTop = ObsidianComponent_olorUtil.getThemeColor(time + (90));
      int centerBottom = ObsidianComponent_olorUtil.getThemeColor(time + 120);
      int rightTop = ObsidianComponent_olorUtil.getThemeColor(time + 180);
      int rightBottom = ObsidianComponent_olorUtil.getThemeColor(time + (210));
      RenderUtil.drawWaveHudPanel(
         eventRender.getContext().getMatrices(),
         x,
         y,
         width,
         height + 25.0f,
         ObsidianComponent_olorUtil.rgba(
            25, 25, 25, 150
         ),
         height - 3.0f,
         0.0f,
         10.0f,
         10.0f,
         leftTop,
         leftBottom,
         centerTop,
         centerBottom,
         rightTop,
         rightBottom
      );
      font.drawStringWithShadow(
         eventRender.getContext().getMatrices(),
         titleText,
         x + 3.0f,
         y + 5.0f,
         -1
      );
      font.drawStringWithShadow(
         eventRender.getContext().getMatrices(),
         serverText,
         x + 3.0f,
         y + 18.0f,
         -1
      );
      font.drawStringWithShadow(
         eventRender.getContext().getMatrices(),
         nameText,
         x + 3.0f,
         y + 25.5f,
         -1
      );
      font.drawStringWithShadow(
         eventRender.getContext().getMatrices(),
         playTimeText,
         x + 3.0f,
         y + 33.5f,
         -1
      );
      this.draggable.setHeight(height + 25.0f);
      this.draggable.setWidth(width);
      super.onRender(eventRender);
   }
}
