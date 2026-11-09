package wtf.obsidian.manager;

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

public class ModuleManager extends Registry<Module> implements IModules {
   public static ModuleManager INSTANCE = new ModuleManager();
   public static ChamsModule chams = ChamsModule.INSTANCE;

   public void initialize() {
      Module[] var10001 = new Module[105];
      var10001[0] = antibot;
      var10001[1] = aimassist;
      var10001[2] = aimBot;
      var10001[3] = airStuck;
      var10001[4] = arrows;
      var10001[5] = aura;
      var10001[6] = autoAccept;
      var10001[7] = autoArmor;
      var10001[8] = autoBuy;
      var10001[9] = autoDuel;
      var10001[10] = autoEat;
      var10001[11] = autoLeave;
      var10001[12] = autoExplosion;
      var10001[13] = autoForest;
      var10001[14] = autoJoin;
      var10001[15] = autoPvp;
      var10001[16] = nameProtect;
      var10001[17] = autoFish;
      var10001[18] = autoJump;
      var10001[19] = autoBuy;
      var10001[20] = autoSwap;
      var10001[21] = autoTool;
      var10001[22] = autoTotem;
      var10001[23] = autoTrap;
      var10001[24] = blockesp;
      var10001[25] = blockOverlay;
      var10001[26] = chestStealer;
      var10001[27] = clientSounds;
      var10001[28] = clickPearl;
      var10001[29] = cosmetics;
      var10001[30] = cubes;
      var10001[31] = deathCoord;
      var10001[32] = ecopen;
      var10001[33] = elytraBoost;
      var10001[34] = elytraMotion;
      var10001[35] = elytraSwap;
      var10001[36] = elytraTarget;
      var10001[37] = elytraresolver;
      var10001[38] = entityESP;
      var10001[39] = fireworkESP;
      var10001[40] = fastBreak;
      var10001[41] = fastExp;
      var10001[42] = flight;
      var10001[43] = freeCam;
      var10001[44] = fullBright;
      var10001[45] = grimGlide;
      var10001[46] = grimNoFall;
      var10001[47] = highJump;
      var10001[48] = helpMessage;
      var10001[49] = hitBubbles;
      var10001[50] = hitMarker;
      var10001[51] = interfaceModule;
      var10001[52] = interpolateF5;
      var10001[53] = inventoryWalk;
      var10001[54] = itemAim;
      var10001[55] = itemRelease;
      var10001[56] = itemScroller;
      var10001[57] = jumpCircle;
      var10001[58] = killEffect;
      var10001[59] = kTLeave;
      var10001[60] = leavetracker;
      var10001[61] = lineGlyphes;
      var10001[62] = lockSlot;
      var10001[63] = lootTracker;
      var10001[64] = noClip;
      var10001[65] = noJumpDelay;
      var10001[66] = nuker;
      var10001[67] = noPush;
      var10001[68] = noSlow;
      var10001[69] = noVelocity;
      var10001[70] = noVignette;
      var10001[71] = noControllerWeb;
      var10001[72] = noWeb;
      var10001[73] = packetCriticals;
      var10001[74] = particle;
      var10001[75] = playerFakeLags;
      var10001[76] = projectile;
      var10001[77] = scoreboardHP;
      var10001[78] = removals;
      var10001[79] = rPSpoofer;
      var10001[80] = sattelite;
      var10001[81] = seeInvisibles;
      var10001[82] = shaderEsp;
      var10001[83] = shaderHands;
      var10001[84] = serverHelper;
      var10001[85] = shulkerPreview;
      var10001[86] = sonar;
      var10001[87] = speed;
      var10001[88] = spider;
      var10001[89] = sprint;
      var10001[90] = step;
      var10001[91] = swingAnimations;
      var10001[92] = targetESP;
      var10001[93] = targetPearl;
      var10001[94] = timer;
      var10001[95] = totemAngel;
      var10001[96] = tpsSync;
      var10001[97] = tpBack;
      var10001[98] = tpLoot;
      var10001[99] = trails;
      var10001[100] = trajectories;
      var10001[101] = triggerBot;
      var10001[102] = viewModel;
      var10001[103] = worldTweaks;
      var10001[104] = xCarry;
      this.add(var10001);
   }
   public void add(Module... mod) {
      this.getObject().addAll(List.of(mod));
   }
}
