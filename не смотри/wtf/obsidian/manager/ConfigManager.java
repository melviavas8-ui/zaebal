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























import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class ConfigManager {
   public String currentConfig = "default";
   public final String extension = ".obs";

   public ConfigManager() {
      this.loadAll();
      Runtime.getRuntime().addShutdownHook(new Thread(this::saveAll));
   }
   public void loadAll() {
      try {
         this.loadGlobals();
         this.loadConfig(this.currentConfig);
      } catch (Exception var2) {
         var2.printStackTrace(System.err);
      }
   }
   public void saveAll() {
      try {
         this.saveGlobals();
         this.saveConfig(this.currentConfig);
      } catch (Exception var2) {
         var2.printStackTrace(System.err);
      }
   }

   public void saveConfig(String config) throws Exception {
      File file = new File(Obsidian.INSTANCE.configsDir, config + ".obs");
      JsonObject object = new JsonObject();
      object.add("config", new JsonPrimitive(config));
      object.add("theme", new JsonPrimitive(Obsidian.INSTANCE.themeStorage.getThemes().name()));
      object.add("language", new JsonPrimitive(Obsidian.INSTANCE.localizationStorage.getLanguage().name()));
      object.add("modules", this.serializeModules());
      object.add("draggables", this.serializeDraggables());
      object.add("hud", this.serializeHudState());

      try (Writer writer = new OutputStreamWriter(new FileOutputStream(file, false), StandardCharsets.UTF_8)) {
         writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(object));
      }

      this.currentConfig = config;
   }

   public void loadConfig(String config) throws Exception {
      if (ObsidianComponent_59.exists(Obsidian.INSTANCE.configsDir + "/" + config + ".obs")) {
         JsonObject object;
         try (
            InputStream stream = Files.newInputStream(Paths.get(Obsidian.INSTANCE.configsDir + "/" + config + ".obs"));
            Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
         ) {
            object = JsonParser.parseReader(reader).getAsJsonObject();
         }

         if (object.has("theme")) {
            String themeName = object.get("theme").getAsString();
            Theme[] var14 = Theme.values();
            int var5 = var14.length;

            for (int var6 = 0; var6 < var5; var6++) {
               Theme theme = var14[var6];
               if (theme.name().equals(themeName)) {
                  Obsidian.INSTANCE.themeStorage.setThemes(theme);
                  break;
               }
            }
         }

         if (object.has("language")) {
            try {
               Obsidian.INSTANCE.localizationStorage.setLanguage(Language.valueOf(object.get("language").getAsString()));
            } catch (Exception var10) {
            }
         }

         if (object.has("draggables")) {
            this.deserializeDraggables(object.get("draggables").getAsJsonObject());
         }

         if (object.has("modules")) {
            this.deserializeModules(object.get("modules").getAsJsonObject());
         }

         if (object.has("hud")) {
            this.deserializeHudState(object.get("hud").getAsJsonObject());
         }

         this.currentConfig = config;
      }
   }

   public void saveGlobals() throws Exception {
      File file = new File(Obsidian.INSTANCE.globalsDir, "globals.obs");
      JsonObject object = new JsonObject();
      object.add("config", new JsonPrimitive(this.currentConfig));
      object.add("theme", new JsonPrimitive(Obsidian.INSTANCE.themeStorage.getThemes().name()));
      object.add("language", new JsonPrimitive(Obsidian.INSTANCE.localizationStorage.getLanguage().name()));
      object.add("draggables", this.serializeDraggables());
      object.add("hud", this.serializeHudState());
      JsonArray friendsArray = new JsonArray();
      Obsidian.INSTANCE.friendStorage.getFriends().forEach(friendsArray::add);
      object.add("friends", friendsArray);
      JsonArray staffsArray = new JsonArray();
      Obsidian.INSTANCE.staffStorage.getStaffs().forEach(staffsArray::add);
      object.add("staffs", staffsArray);
      JsonArray macrosArray = new JsonArray();
      Obsidian.INSTANCE.macroStorage.getMacros().forEach(macro -> {
         JsonObject macroObject = new JsonObject();
         macroObject.addProperty("name", macro.getName());
         macroObject.addProperty("command", macro.getCommand());
         macroObject.addProperty("key", macro.getBind().getKey());
         macrosArray.add(macroObject);
      });
      object.add("macros", macrosArray);

      try (Writer writer = new OutputStreamWriter(new FileOutputStream(file, false), StandardCharsets.UTF_8)) {
         writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(object));
      }
   }

   public void loadGlobals() throws Exception {
      if (ObsidianComponent_59.exists(Obsidian.INSTANCE.globalsDir + "/globals.obs")) {
         JsonObject object;
         try (
            InputStream stream = Files.newInputStream(Paths.get(Obsidian.INSTANCE.globalsDir + "/globals.obs"));
            Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
         ) {
            object = JsonParser.parseReader(reader).getAsJsonObject();
         }

         if (object.has("config")) {
            this.currentConfig = object.get("config").getAsString();
         }

         if (object.has("theme")) {
            String themeName = object.get("theme").getAsString();
            Theme[] var18 = Theme.values();
            int name = var18.length;

            for (int command = 0; command < name; command++) {
               Theme theme = var18[command];
               if (theme.name().equals(themeName)) {
                  Obsidian.INSTANCE.themeStorage.setThemes(theme);
                  break;
               }
            }
         }

         if (object.has("language")) {
            try {
               Obsidian.INSTANCE.localizationStorage.setLanguage(Language.valueOf(object.get("language").getAsString()));
            } catch (Exception var10) {
            }
         }

         if (object.has("draggables")) {
            this.deserializeDraggables(object.get("draggables").getAsJsonObject());
         }

         if (object.has("hud")) {
            this.deserializeHudState(object.get("hud").getAsJsonObject());
         }

         if (object.has("friends")) {
            for (JsonElement element : object.get("friends").getAsJsonArray()) {
               if (!Obsidian.INSTANCE.friendStorage.isFriend(element.getAsString())) {
                  Obsidian.INSTANCE.friendStorage.add(element.getAsString());
               }
            }
         }

         if (object.has("staffs")) {
            for (JsonElement elementx : object.get("staffs").getAsJsonArray()) {
               if (!Obsidian.INSTANCE.staffStorage.isStaff(elementx.getAsString())) {
                  Obsidian.INSTANCE.staffStorage.add(elementx.getAsString());
               }
            }
         }

         if (object.has("macros")) {
            for (JsonElement elementxx : object.get("macros").getAsJsonArray()) {
               try {
                  String name;
                  String commandx;
                  int key;
                  if (elementxx.isJsonObject()) {
                     JsonObject macroObject = elementxx.getAsJsonObject();
                     name = macroObject.has("name") ? macroObject.get("name").getAsString() : "";
                     commandx = macroObject.has("command") ? macroObject.get("command").getAsString() : "";
                     key = macroObject.has("key") ? macroObject.get("key").getAsInt() : -1;
                  } else {
                     String[] split = elementxx.getAsString().split(":", 3);
                     if (split.length < (3)) {
                        continue;
                     }

                     name = split[0];
                     commandx = split[1];
                     key = Integer.parseInt(split[2]);
                  }

                  if (!name.isBlank() && Obsidian.INSTANCE.macroStorage.getMacro(name) == null) {
                     Obsidian.INSTANCE.macroStorage.add(new ObsidianComponent_203(name, commandx, new ObsidianComponent_261("bind", key)));
                  }
               } catch (Exception var13) {
               }
            }
         }
      }
   }
   public JsonObject serializeModules() {
      JsonObject modules = new JsonObject();
      ObjectListIterator var2 = ModuleManager.INSTANCE.getObject().iterator();

      while (var2.hasNext()) {
         Module module = (Module)var2.next();

         try {
            JsonObject object = new JsonObject();
            object.add("toggled", new JsonPrimitive(module.isEnable()));
            object.add("bind", new JsonPrimitive(module.getKey()));
            JsonObject settings = new JsonObject();

            for (Setting s : module.getSettings()) {
               try {
                  if (s instanceof BooleanSetting bool) {
                     settings.add(s.name(), new JsonPrimitive(bool.isState()));
                  } else if (s instanceof SliderSetting num) {
                     settings.add(s.name(), new JsonPrimitive(num.getValue().floatValue()));
                  } else if (s instanceof ModeSetting mode) {
                     settings.add(s.name(), new JsonPrimitive(mode.getCurrent()));
                  } else if (s instanceof ObsidianComponent_341 text) {
                     settings.add(s.name(), new JsonPrimitive(text.get()));
                  } else if (s instanceof ObsidianComponent_261 bind) {
                     settings.add(s.name(), new JsonPrimitive(bind.getKey()));
                  } else if (s instanceof MultiBooleanSetting list) {
                     JsonObject listObj = new JsonObject();

                     for (BooleanSetting setting : list.getSettings()) {
                        listObj.add(setting.name(), new JsonPrimitive(setting.isState()));
                     }

                     settings.add(list.name(), listObj);
                  }
               } catch (Exception var17) {
               }
            }

            object.add("settings", settings);
            modules.add(module.getName(), object);
         } catch (Exception var18) {
         }
      }

      return modules;
   }
   public void deserializeModules(JsonObject modules) {
      Map<Module, Boolean> targetStates = new LinkedHashMap<>();
      ObjectListIterator var3 = ModuleManager.INSTANCE.getObject().iterator();

      while (var3.hasNext()) {
         Module module = (Module)var3.next();

         try {
            JsonObject object = modules.has(module.getName()) ? modules.get(module.getName()).getAsJsonObject() : null;
            boolean toggled = (object != null && object.has("toggled") && object.get("toggled").getAsBoolean());
            targetStates.put(module, toggled);
            if (module.isEnable()) {
               module.setEnabled(false);
            }
         } catch (Exception var22) {
            targetStates.put(module, Boolean.valueOf(false));
         }
      }

      var3 = ModuleManager.INSTANCE.getObject().iterator();

      while (var3.hasNext()) {
         Module module = (Module)var3.next();

         try {
            if (modules.has(module.getName())) {
               JsonObject object = modules.get(module.getName()).getAsJsonObject();
               if (object.has("bind")) {
                  module.setKey(object.get("bind").getAsInt());
               }

               if (object.has("settings")) {
                  JsonObject settings = object.get("settings").getAsJsonObject();

                  for (Setting s : module.getSettings()) {
                     try {
                        if (settings.has(s.name())) {
                           JsonElement element = settings.get(s.name());
                           if (s instanceof BooleanSetting bool) {
                              bool.setState(element.getAsBoolean());
                           } else if (s instanceof SliderSetting num) {
                              num.setValue(element.getAsFloat());
                           } else if (s instanceof ModeSetting mode) {
                              mode.set(element.getAsString());
                           } else if (s instanceof ObsidianComponent_341 text) {
                              text.setText(element.getAsString());
                           } else if (s instanceof ObsidianComponent_261 bind) {
                              bind.setKey(element.getAsInt());
                           } else if (s instanceof MultiBooleanSetting list) {
                              JsonObject listObj = element.getAsJsonObject();

                              for (BooleanSetting setting : list.getSettings()) {
                                 if (listObj.has(setting.name())) {
                                    setting.setState(listObj.get(setting.name()).getAsBoolean());
                                 }
                              }
                           }
                        }
                     } catch (Exception var20) {
                     }
                  }
               }
            }
         } catch (Exception var21) {
         }
      }

      for (Entry<Module, Boolean> entry : targetStates.entrySet()) {
         try {
            entry.getKey().setEnabled(entry.getValue());
         } catch (Exception var19) {
         }
      }
   }
   public JsonObject serializeHudState() {
      JsonObject hud = new JsonObject();
      HudInterfaceModule interfaceModule = ModuleManager.interfaceModule;
      if (interfaceModule == null) {
         return hud;
      } else {
         for (Entry<String, DraggableHudElement> entry : interfaceModule.getConfigurableHudElements().entrySet()) {
            DraggableHudElement element = entry.getValue();
            if (element != null) {
               JsonObject object = new JsonObject();
               object.add("unusualRectType", new JsonPrimitive(element.isUnusualRectType()));
               if (element instanceof KeybindsHudElement2 waterMark) {
                  object.add("showFps", new JsonPrimitive(waterMark.isShowFps()));
                  object.add("showMs", new JsonPrimitive(waterMark.isShowMs()));
                  object.add("showServer", new JsonPrimitive(waterMark.isShowServer()));
                  object.add("showTps", new JsonPrimitive(waterMark.isShowTps()));
               } else if (element instanceof TargetHudElement targetHud) {
                  object.add("headParticlesEnabled", new JsonPrimitive(targetHud.isHeadParticlesEnabled()));
               }

               hud.add(entry.getKey(), object);
            }
         }

         return hud;
      }
   }
   public void deserializeHudState(JsonObject hud) {
      HudInterfaceModule interfaceModule = ModuleManager.interfaceModule;
      if (interfaceModule != null) {
         for (Entry<String, DraggableHudElement> entry : interfaceModule.getConfigurableHudElements().entrySet()) {
            if (hud.has(entry.getKey())) {
               try {
                  JsonObject object = hud.get(entry.getKey()).getAsJsonObject();
                  DraggableHudElement element = entry.getValue();
                  if (object.has("unusualRectType")) {
                     element.setUnusualRectType(object.get("unusualRectType").getAsBoolean());
                  }

                  if (element instanceof KeybindsHudElement2 waterMark) {
                     if (object.has("showFps")) {
                        waterMark.setShowFps(object.get("showFps").getAsBoolean());
                     }

                     if (object.has("showMs")) {
                        waterMark.setShowMs(object.get("showMs").getAsBoolean());
                     }

                     if (object.has("showServer")) {
                        waterMark.setShowServer(object.get("showServer").getAsBoolean());
                     }

                     if (object.has("showTps")) {
                        waterMark.setShowTps(object.get("showTps").getAsBoolean());
                     }
                  } else if (element instanceof TargetHudElement targetHud && object.has("headParticlesEnabled")) {
                     targetHud.setHeadParticlesEnabled(object.get("headParticlesEnabled").getAsBoolean());
                  }
               } catch (Exception var9) {
               }
            }
         }
      }
   }
   public JsonObject serializeDraggables() {
      JsonObject draggables = new JsonObject();

      for (ObsidianComponent_280 drag : ObsidianComponent_148.draggables.values()) {
         JsonObject object = new JsonObject();
         object.add("x", new JsonPrimitive(drag.getX()));
         object.add("y", new JsonPrimitive(drag.getY()));
         draggables.add(drag.getName(), object);
      }

      return draggables;
   }
   public void deserializeDraggables(JsonObject draggables) {
      for (String name : draggables.keySet()) {
         ObsidianComponent_280 drag = ObsidianComponent_148.draggables.get(name);
         if (drag != null) {
            JsonObject object = draggables.get(name).getAsJsonObject();
            if (object.has("x")) {
               drag.setX(object.get("x").getAsFloat());
            }

            if (object.has("y")) {
               drag.setY(object.get("y").getAsFloat());
            }
         }
      }
   }
}
