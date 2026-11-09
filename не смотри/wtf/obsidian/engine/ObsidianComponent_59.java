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
























import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.Generated;

public final class ObsidianComponent_59 {
   public static void reset(String str) throws IOException {
      Path path = Paths.get(str);
      if (Files.exists(path)) {
         new File(str).delete();
      }

      Files.createFile(path);
   }

   public static boolean exists(String str) {
      return Files.exists(Paths.get(str));
   }

   @Generated
   public ObsidianComponent_59() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
