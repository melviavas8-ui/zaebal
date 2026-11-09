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
























import java.util.HashMap;
import java.util.Map;

public class ObsidianComponent_345 {
   private final Map<String, String> en = new HashMap<>();
   private final Map<String, String> uk = new HashMap<>();
   public Language language = Language.RUSSIAN;

   public ObsidianComponent_345() {
      this.add("Combat", "Combat", "Бій");
      this.add("Movement", "Movement", "Рух");
      this.add("Render", "Render", "Візуал");
      this.add("Misc", "Misc", "Різне");
      this.add("Player", "Player", "Гравець");
      this.add("Язык", "Language", "Мова");
      this.add("Русский", "Russian", "Російська");
      this.add("Английский", "English", "Англійська");
      this.add("Украинский", "Ukrainian", "Українська");
      this.add("Sprint", "Sprint", "Спринт");
      this.add("AutoTotem", "Auto Totem", "Авто тотем");
      this.add("Interface", "Interface", "Інтерфейс");
      this.add("InventoryWalk", "Inventory Walk", "Хода з інвентарем");
      this.add("NoVignette", "No Vignette", "Без віньєтки");
      this.add("Aura", "Aura", "Аура");
      this.add("ElytraBoost", "Elytra Boost", "Елітра буст");
      this.add("ElytraTarget", "Elytra Target", "Таргет елітри");
      this.add("FullBright", "Full Bright", "Повна яскравість");
      this.add("ElytraSwap", "Elytra Swap", "Свап елітр");
      this.add("PlayerFakeLags", "Fake Lag", "Фейк лаг");
      this.add("Chams", "Chams", "Чамси");
      this.add("ClientSounds", "Client Sounds", "Звуки клієнта");
      this.add("Cosmetics", "Cosmetics", "Косметика");
      this.add("ServerHelper", "Server Helper", "Сервер Хелпер");
      this.add("KTLeave", "KT Leave", "КТ лів");
      this.add("NoClip", "No Clip", "Без кліпу");
      this.add("Particles", "Particles", "Частинки");
      this.add("ElytraMotion", "Elytra Motion", "Рух елітри");
      this.add("HitBubbles", "Hit Bubbles", "Бульбашки удару");
      this.add("RPSpoofer", "RP Spoofer", "RP спуфер");
      this.add("Projectile", "Projectile", "Снаряд");
      this.add("AutoExplosion", "Auto Explosion", "Авто вибух");
      this.add("PacketCriticals", "Packet Criticals", "Пакетні кріти");
      this.add("EntityESP", "Entity ESP", "ESP сутностей");
      this.add("NoPush", "No Push", "Без штовхання");
      this.add("TPLoot", "TP Loot", "ТП лут");
      this.add("CutDetector", "Cut Detector", "Детектор кату");
      this.add("AirStuck", "Air Stuck", "Зависання в повітрі");
      this.add("Sonar", "Sonar", "Сонар");
      this.add("NoWeb", "No Web", "Без павутини");
      this.add("Cubes", "Cubes", "Куби");
      this.add("Removals", "Removals", "Видалення");
      this.add("SwingAnimations", "Swing Animations", "Анімації свінгу");
      this.add("ViewModel", "View Model", "Модель рук");
      this.add("TargetESP", "Target ESP", "ESP цілі");
      this.add("JumpCircle", "Jump Circle", "Коло стрибка");
      this.add("CustomWorld", "Custom World", "Світ");
      this.add("InterpolateF5", "Interpolate F5", "Плавний F5");
      this.add("BlockOverlay", "Block Overlay", "Оверлей блоку");
      this.add("ShaderHands", "Shader Hands", "Шейдер рук");
      this.add("Режим", "Mode", "Режим");
      this.add("Мод", "Mode", "Режим");
      this.add("Стиль", "Style", "Стиль");
      this.add("Скорость", "Speed", "Швидкість");
      this.add("Скорость анимации", "Animation Speed", "Швидкість анімації");
      this.add("Скорость вращения", "Rotation Speed", "Швидкість обертання");
      this.add("Скорость волн", "Wave Speed", "Швидкість хвиль");
      this.add("Скорость нитей", "Thread Speed", "Швидкість ниток");
      this.add("Дистанция", "Distance", "Дистанція");
      this.add("Размер", "Size", "Розмір");
      this.add("Прозрачность", "Opacity", "Прозорість");
      this.add("Свечение", "Glow", "Світіння");
      this.add("Сила свечения", "Glow Strength", "Сила світіння");
      this.add("Сила анимации", "Animation Strength", "Сила анімації");
      this.add("Плавность", "Smoothness", "Плавність");
      this.add("Анимация", "Animation", "Анімація");
      this.add("Анимация крыльев", "Wing Animation", "Анімація крил");
      this.add("Анимация свинга", "Swing Animation", "Анімація свінгу");
      this.add("Плавная анимация", "Smooth Animation", "Плавна анімація");
      this.add("Тип частиц", "Particle Type", "Тип частинок");
      this.add("Количество", "Count", "Кількість");
      this.add("Приоритет", "Priority", "Пріоритет");
      this.add("Ротация", "Rotation", "Ротація");
      this.add("Обход", "Bypass", "Обхід");
      this.add("Сервер", "Server", "Сервер");
      this.add("После лута", "After Loot", "Після луту");
      this.add("Элементы", "Elements", "Елементи");
      this.add("Ватермарка", "Watermark", "Ватермарка");
      this.add("Аррай лист", "Array List", "Список модулів");
      this.add("Горячие клавиши", "Key Binds", "Гарячі клавіші");
      this.add("Зелья", "Potions", "Зілля");
      this.add("Таргет худ", "Target HUD", "Таргет HUD");
      this.add("Уведомления", "Notifications", "Сповіщення");
      this.add("Стафф", "Staff", "Стаф");
      this.add("Сессия", "Session", "Сесія");
      this.add("КейСтроки", "Key Strokes", "Кейстроки");
      this.add("Информация", "Information", "Інформація");
      this.add("Обычный", "Default", "Звичайний");
      this.add("Красивый", "Fancy", "Гарний");
      this.add("Шейдер", "Shader", "Шейдер");
      this.add("Нитки", "Threads", "Нитки");
      this.add("Разлет", "Scatter", "Розліт");
      this.add("Падение", "Fall", "Падіння");
      this.add("Возвращаться", "Return", "Повертатися");
      this.add("Тепаться на спавн", "Teleport to Spawn", "Телепортуватись на спавн");
      this.add("Картинка 1", "Image 1", "Картинка 1");
      this.add("Картинка 2", "Image 2", "Картинка 2");
      this.add("Призраки", "Ghosts", "Привиди");
      this.add("Райдер", "Rider", "Райдер");
      this.add("Души", "Souls", "Души");
      this.add("Кристаллы", "Crystals", "Кристали");
      this.add("Коллизия", "Collision", "Колізія");
      this.add("Тест", "Test", "Тест");
      this.add("Дистанция атаки", "Attack Range", "Дистанція атаки");
      this.add("Только движение", "Only Movement", "Тільки рух");
      this.add("Только при Aura", "Only with Aura", "Тільки з Aura");
      this.add("Только с аурой", "Only with Aura", "Тільки з аурою");
      this.add("Правая рука X", "Right Hand X", "Права рука X");
      this.add("Правая рука Y", "Right Hand Y", "Права рука Y");
      this.add("Правая рука Z", "Right Hand Z", "Права рука Z");
      this.add("Левая рука X", "Left Hand X", "Ліва рука X");
      this.add("Левая рука Y", "Left Hand Y", "Ліва рука Y");
      this.add("Левая рука Z", "Left Hand Z", "Ліва рука Z");
      this.add("Авто-взлёт", "Auto Takeoff", "Авто зліт");
      this.add("Обходить Grim", "Bypass Grim", "Обходити Grim");
      this.add("Крылья", "Wings", "Крила");
      this.add("Крылья 2", "Wings 2", "Крила 2");
      this.add("Китайская шляпа", "China Hat", "Китайський капелюх");
   }
   public void add(String key, String english, String ukrainian) {
      this.en.put(key, english);
      this.uk.put(key, ukrainian);
   }

   public Language getLanguage() {
      return this.language;
   }

   public void setLanguage(Language language) {
      this.language = language == null ? Language.RUSSIAN : language;
   }

   public void cycleLanguage() {
      this.language = this.language.next();
   }

   public String translateCategory(Category category) {
      return this.translate(category.getName());
   }

   public String translate(String key) {
      if (key != null && !key.isEmpty()) {
         return switch (this.language) {
            case RUSSIAN -> key;
            case ENGLISH -> (String)this.en.getOrDefault(key, this.fallbackEnglish(key));
            case UKRAINIAN -> (String)this.uk.getOrDefault(key, this.fallbackUkrainian(key));
         };
      } else {
         return key;
      }
   }
   public String fallbackEnglish(String key) {
      return key.chars()
            .allMatch(ch -> (ch < 128))
         ? this.humanizeAscii(key)
         : key;
   }
   public String fallbackUkrainian(String key) {
      if (this.uk.containsKey(key)) {
         return this.uk.get(key);
      } else {
         return key.chars()
               .allMatch(
                  ch -> (ch < 128)
               )
            ? this.humanizeAscii(key)
            : key;
      }
   }
   public String humanizeAscii(String key) {
      if (key.indexOf(32) >= 0) {
         return key;
      } else {
         String humanized = key.replaceAll("([a-z])([A-Z])", "$1 $2");
         humanized = humanized.replaceAll("([A-Z]+)([A-Z][a-z])", "$1 $2");
         return humanized.trim();
      }
   }
}
