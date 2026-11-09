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
























import java.util.List;
import lombok.Generated;

public class ObsidianComponent_160 {
   protected final ObsidianComponent_269 topLeftColor;
   protected final ObsidianComponent_269 bottomLeftColor;
   protected final ObsidianComponent_269 topRightColor;
   protected final ObsidianComponent_269 bottomRightColor;

   protected ObsidianComponent_160(ObsidianComponent_269 topLeftColor, ObsidianComponent_269 bottomLeftColor, ObsidianComponent_269 topRightColor, ObsidianComponent_269 bottomRightColor) {
      this.topLeftColor = topLeftColor;
      this.bottomLeftColor = bottomLeftColor;
      this.topRightColor = topRightColor;
      this.bottomRightColor = bottomRightColor;
   }

   public static ObsidianComponent_160 of(ObsidianComponent_269 topLeftColor, ObsidianComponent_269 bottomLeftColor, ObsidianComponent_269 topRightColor, ObsidianComponent_269 bottomRightColor) {
      return new ObsidianComponent_160(topLeftColor, bottomLeftColor, topRightColor, bottomRightColor);
   }

   public static ObsidianComponent_160 of(List<ObsidianComponent_269> colors) {
      return new ObsidianComponent_160(
         colors.get(0),
         colors.get(1),
         colors.get(2),
         colors.get(3)
      );
   }

   public ObsidianComponent_160 rotate() {
      return this;
   }

   public ObsidianComponent_160 mulAlpha(float alphaMultiplier) {
      return new ObsidianComponent_160(
         this.topLeftColor.mulAlpha(alphaMultiplier),
         this.bottomLeftColor.mulAlpha(alphaMultiplier),
         this.topRightColor.mulAlpha(alphaMultiplier),
         this.bottomRightColor.mulAlpha(alphaMultiplier)
      );
   }

   @Generated
   public ObsidianComponent_269 getTopLeftColor() {
      return this.topLeftColor;
   }

   @Generated
   public ObsidianComponent_269 getBottomLeftColor() {
      return this.bottomLeftColor;
   }

   @Generated
   public ObsidianComponent_269 getTopRightColor() {
      return this.topRightColor;
   }

   @Generated
   public ObsidianComponent_269 getBottomRightColor() {
      return this.bottomRightColor;
   }
}
