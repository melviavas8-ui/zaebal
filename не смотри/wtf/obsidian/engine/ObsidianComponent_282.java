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
























import com.sun.jna.Library;
import com.sun.jna.Native;

public interface ObsidianComponent_282 extends Library {
   public ObsidianComponent_282 INSTANCE = Native.loadLibrary("discord-rpc", ObsidianComponent_282.class);

   void Discord_UpdateHandlers(ObsidianComponent_401 var1);

   void Discord_UpdatePresence(DiscordRichPresence var1);

   void Discord_Respond(String var1, int var2);

   void Discord_Register(String var1, String var2);

   void Discord_Shutdown();

   void Discord_UpdateConnection();

   void Discord_RegisterSteamGame(String var1, String var2);

   void Discord_RunCallbacks();

   void Discord_Initialize(String var1, ObsidianComponent_401 var2, boolean var3, String var4);

   void Discord_ClearPresence();
}
