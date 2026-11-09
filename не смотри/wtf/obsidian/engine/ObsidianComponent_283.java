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
























import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ObsidianComponent_283 {
   public static final long DURATION_MS = 2500L;
   public static final long MODULE_SOUND_STARTUP_MUTE_MS = 4000L;
   private static final long INIT_TIME_MS = System.currentTimeMillis();
   private static final List<FontTextureAtlas> entries = new ArrayList<>();

   public static void push(String moduleName, String categoryIcon, boolean enabled) {
      if (moduleName != null && !moduleName.isEmpty()) {
         entries.add(new FontTextureAtlas(moduleName, categoryIcon, enabled, null, System.currentTimeMillis()));
         playModuleSound(enabled);
      }
   }

   public static void pushCustom(String text, String categoryIcon) {
      if (text != null && !text.isEmpty()) {
         entries.add(new FontTextureAtlas(text, categoryIcon, false, text, System.currentTimeMillis()));
      }
   }

   public static List<FontTextureAtlas> getActive() {
      long now = System.currentTimeMillis();
      Iterator<FontTextureAtlas> it = entries.iterator();

      while (it.hasNext()) {
         FontTextureAtlas e = it.next();
         if (now - e.startTime > (2500L)) {
            it.remove();
         }
      }

      return entries;
   }
   public static void playModuleSound(boolean enabled) {
      if (System.currentTimeMillis() - INIT_TIME_MS >= (4000L)) {
         ClientSoundsModule clientSounds = ClientSoundsModule.INSTANCE;
         if (clientSounds != null && clientSounds.isEnable()) {
            String soundName = clientSounds.stateSounds.getCurrent();
            if (!"Нет".equals(soundName)) {
               float pitch = enabled ? 1.0f : 0.949999988079071f;
               SoundManager.playSound(soundName + ".wav", (double)(clientSounds.volume.get() / clientSounds.volume.getMax()), pitch);
            }
         }
      }
   }
}
