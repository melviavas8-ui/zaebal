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























public class ObsidianComponent_104 {
   public float deltaYaw;
   public float deltaPitch;
   public float signedYaw;
   public float signedPitch;
   public float rotationSpeed;
   public float jitterScore;
   public float jitterYawSpeed;
   public float jitterPitchSpeed;
   public long timeDeltaMs;
   public boolean isJittering;
   public boolean attacking;
   public boolean combatFrame;
   public boolean instantSnap;

   public ObsidianComponent_104() {
   }

   public boolean isCombatLike() {
      return (!(!this.attacking && !this.combatFrame && !this.instantSnap));
   }
}
