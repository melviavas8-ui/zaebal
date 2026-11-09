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
























import lombok.Generated;
import net.minecraft.util.math.Vec3d;

public class ObsidianComponent_158 {
   public double serverX;
   public double serverY;
   public double serverZ;
   public double prevServerX;
   public double prevServerY;
   public double prevServerZ;
   public double backUpX;
   public double backUpY;
   public double backUpZ;
   public double lastSpeed;
   public double prevSpeed;
   public long lastUpdate;

   public Vec3d getResolvedPos() {
      return new Vec3d(this.serverX, this.serverY, this.serverZ);
   }

   public Vec3d getResolvedForward() {
      return new Vec3d(this.serverX - this.prevServerX, this.serverY - this.prevServerY, this.serverZ - this.prevServerZ);
   }

   public void update(double x, double y, double z) {
      this.backUpX = this.prevServerX;
      this.backUpY = this.prevServerY;
      this.backUpZ = this.prevServerZ;
      this.prevServerX = this.serverX;
      this.prevServerY = this.serverY;
      this.prevServerZ = this.serverZ;
      this.serverX = x;
      this.serverY = y;
      this.serverZ = z;
      this.prevSpeed = this.lastSpeed;
      this.lastSpeed = this.getResolvedForward().length() * 20.0;
      this.lastUpdate = System.currentTimeMillis();
   }

   public boolean isSpeedChanged() {
      return (!(!(this.lastSpeed >= 20.0)
            && (
               this.lastSpeed == this.prevSpeed
                  || this.lastSpeed != 0.0
            )));
   }

   @Generated
   public double getServerX() {
      return this.serverX;
   }

   @Generated
   public double getServerY() {
      return this.serverY;
   }

   @Generated
   public double getServerZ() {
      return this.serverZ;
   }

   @Generated
   public double getPrevServerX() {
      return this.prevServerX;
   }

   @Generated
   public double getPrevServerY() {
      return this.prevServerY;
   }

   @Generated
   public double getPrevServerZ() {
      return this.prevServerZ;
   }

   @Generated
   public double getBackUpX() {
      return this.backUpX;
   }

   @Generated
   public double getBackUpY() {
      return this.backUpY;
   }

   @Generated
   public double getBackUpZ() {
      return this.backUpZ;
   }

   @Generated
   public double getLastSpeed() {
      return this.lastSpeed;
   }

   @Generated
   public double getPrevSpeed() {
      return this.prevSpeed;
   }

   @Generated
   public long getLastUpdate() {
      return this.lastUpdate;
   }
}
