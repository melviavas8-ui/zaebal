package wtf.obsidian.modules.render;

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

























import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.WaterAnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Rarity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public class EntityESPModule extends Module {
   public static EntityESPModule INSTANCE = new EntityESPModule();
   public static final float TAG_FROM_ENTITY_GAP = 0.0F;
   public static final int TAG_FONT_SIZE = 13;
   public static final int TAG_TEXT_COLOR = -1;
   public static final int TAG_HEALTH_COLOR = -43691;
   public static final int TAG_FRIEND_COLOR = -11141291;
   public static final float TAG_HUD_RADIUS = 1.1F;
   public static final int TAG_HUD_ALPHA = 204;
   public static final float ARMOR_CELL_SIZE = 8.4F;
   public static final float ARMOR_ITEM_SCALE = 0.46F;
   public static final float ARMOR_CELL_GAP = 1.0F;
   public static final float PLAYER_HEAD_SIZE = 7.5F;
   public static final float PLAYER_HEAD_GAP = 3.0F;
   public static final float BOX_LINE_WIDTH = 1.5F;
   public static final float FILL_ALPHA = 0.23F;
   public static final float EPSILON = 0.001F;
   public static final long DONATE_CACHE_TTL_MS = 1000L;
   public static final long DONATE_CACHE_CLEANUP_MS = 2000L;
   public static final int MAX_ITEM_TAGS_PER_FRAME = 48;
   public final MultiBooleanSetting elements;
   public final BooleanSetting show3DBox;
   public final BooleanSetting boxFilled;
   public final ModeSetting boxFillMode;
   public final SliderSetting waveSpeed;
   public final SliderSetting waveScale;
   public final SliderSetting lineSpeed;
   public final SliderSetting lineJitter;
   public final SliderSetting outline;
   public final SliderSetting glow;
   public final SliderSetting fill;
   public final SliderSetting alpha;
   public final BooleanSetting hurtTint;
   public final Matrix4f lastProjectionMatrix;
   public final Quaternionf lastCameraRotation;
   public final Quaternionf lastInverseCameraRotation;
   public Vec3d lastCameraPos;
   public float lastTickDelta;
   public int lastScaledWidth;
   public int lastScaledHeight;
   public boolean hasProjection;
   public Framebuffer maskBuffer;
   public final List<Framebuffer> bloomBuffers;
   public final Map<UUID, DonateSegmentCache> donateCache;
   public final Map<Integer, Float> entityHurtTintProgress;
   public long nextDonateCacheCleanupAt;
   public int maskWidth;
   public int maskHeight;
   public boolean hasShaderMask;
   public final Vector3f projectionScratch;
   public final Vector4f clipScratch;
   public final ObsidianComponent_96 projectedPoint;
   public final ItemStack[] armorStacksScratch;
   public final boolean[] armorHandScratch;
   public int frameThemeColor;
   public final BooleanSetting targetPlayers;
   public final BooleanSetting targetMobs;
   public final BooleanSetting targetAnimals;
   public final BooleanSetting targetItems;
   public final MultiBooleanSetting targets;

   public EntityESPModule() {
      super("EntityESP", "Показывает игроков через стену", Category.RENDER);
      BooleanSetting[] var10004 = new BooleanSetting[2];
      var10004[0] = new BooleanSetting("Теги", true);
      var10004[1] = new BooleanSetting("Броня", true);
      this.elements = new MultiBooleanSetting("Элементы", var10004);
      this.show3DBox = new BooleanSetting("Боксы", true);
      this.boxFilled = new BooleanSetting("Заполнить бокс", true);
      String[] var10005 = new String[3];
      var10005[0] = "Обычный";
      var10005[1] = "Волны";
      var10005[2] = "Нитки";
      this.boxFillMode = new ModeSetting("Мод заливки", "Обычный", var10005);
      this.waveSpeed = new SliderSetting(
            "Скорость волн",
            1.2000000476837158f,
            0.10000000149011612f,
            5.0f,
            0.10000000149011612f
         )
         .visible(() -> this.boxFillMode.is("Волны"));
      this.waveScale = new SliderSetting(
            "Размер волн",
            1.0f,
            1.0f,
            3.0f,
            0.10000000149011612f
         )
         .visible(() -> this.boxFillMode.is("Волны"));
      this.lineSpeed = new SliderSetting(
            "Скорость линий",
            1.399999976158142f,
            0.10000000149011612f,
            5.0f,
            0.10000000149011612f
         )
         .visible(
            () -> Boolean.valueOf(
                  (this.boxFillMode.getIndex() == 2)
               )
         );
      this.lineJitter = new SliderSetting(
            "Прыжки линий",
            0.550000011920929f,
            0.0f,
            1.5f,
            0.009999999776482582f
         )
         .visible(
            () -> Boolean.valueOf(
                  (this.boxFillMode.getIndex() == (2))
               )
         );
      this.outline = new SliderSetting(
            "Обводка",
            1.100000023841858f,
            0.10000000149011612f,
            5.0f,
            0.10000000149011612f
         )
         .visible(this::isPostBoxMode);
      this.glow = new SliderSetting(
            "Свечение",
            1.0f,
            0.0f,
            5.0f,
            0.10000000149011612f
         )
         .visible(this::isPostBoxMode);
      this.fill = new SliderSetting(
            "Сила заливки",
            0.6000000238418579f,
            0.0f,
            1.0f,
            0.009999999776482582f
         )
         .visible(this::isPostBoxMode);
      this.alpha = new SliderSetting(
            "Прозрачность",
            1.0f,
            0.0f,
            4.0f,
            0.009999999776482582f
         )
         .visible(this::isPostBoxMode);
      this.hurtTint = new BooleanSetting("Краснеть при ударе", true);
      this.lastProjectionMatrix = new Matrix4f();
      this.lastCameraRotation = new Quaternionf();
      this.lastInverseCameraRotation = new Quaternionf();
      this.lastCameraPos = Vec3d.ZERO;
      this.bloomBuffers = new ArrayList<>();
      this.donateCache = new HashMap<>();
      this.entityHurtTintProgress = new HashMap<>();
      this.maskWidth = -1;
      this.maskHeight = -1;
      this.projectionScratch = new Vector3f();
      this.clipScratch = new Vector4f();
      this.projectedPoint = new ObsidianComponent_96();
      this.armorStacksScratch = new ItemStack[6];
      this.armorHandScratch = new boolean[6];
      this.frameThemeColor = -1;
      this.targetPlayers = new BooleanSetting("Игроки", true);
      this.targetMobs = new BooleanSetting("Мобы", true);
      this.targetAnimals = new BooleanSetting("Животные", true);
      this.targetItems = new BooleanSetting("Предметы", true);
      var10004 = new BooleanSetting[4];
      var10004[0] = this.targetPlayers;
      var10004[1] = this.targetMobs;
      var10004[2] = this.targetAnimals;
      var10004[3] = this.targetItems;
      this.targets = new MultiBooleanSetting("Отображать", var10004);
      Setting[] var10001 = new Setting[2];
      var10001[0] = this.targets;
      var10001[1] = this.elements;
      this.addSettings(var10001);
      var10001 = new Setting[3];
      var10001[0] = this.show3DBox;
      var10001[1] = this.boxFilled;
      var10001[2] = this.hurtTint;
      this.addSettings(var10001);
   }

   @Override
   public void onDisable() {
      this.hasProjection = false;
      this.hasShaderMask = false;
      this.donateCache.clear();
      this.entityHurtTintProgress.clear();
      this.nextDonateCacheCleanupAt = 0L;
      if (this.maskBuffer != null) {
         this.maskBuffer.delete();
         this.maskBuffer = null;
      }

      for (Framebuffer fb : this.bloomBuffers) {
         fb.delete();
      }

      this.bloomBuffers.clear();
      super.onDisable();
   }

   @EventTarget(
      priority = 100
   )
   public void onRender3D(RenderEvent event) {
      this.hasProjection = true;
      this.lastProjectionMatrix.set(event.getProjectionMatrix());
      this.lastCameraPos = event.getCamera().getPos();
      this.lastCameraRotation.set(event.getCamera().getRotation());
      this.lastInverseCameraRotation.set(this.lastCameraRotation).conjugate();
      this.lastTickDelta = event.getTickDelta();
      this.lastScaledWidth = mc.getWindow().getScaledWidth();
      this.lastScaledHeight = mc.getWindow().getScaledHeight();
      this.frameThemeColor = this.getStableThemeColor();
      this.hasShaderMask = false;
      if (this.show3DBox.isState() && mc.world != null && mc.player != null) {
         MatrixStack matrices = event.getMatrices();
         float tickDelta = event.getTickDelta();
         boolean postMode = this.isPostBoxMode();
         boolean threadMode = this.isThreadMode();
         if (postMode) {
            this.ensureMaskBuffer();
            if (this.maskBuffer != null) {
               this.maskBuffer
                  .setClearColor(
                     0.0f,
                     0.0f,
                     0.0f,
                     0.0f
                  );
               this.maskBuffer.clear();
               this.copyMainDepthToMask();
               this.maskBuffer.beginWrite(false);
               RenderSystem.disableBlend();
               RenderSystem.enableDepthTest();
               RenderSystem.depthMask(false);
               RenderSystem.disableCull();
               RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            }
         }

         for (Entity entity : mc.world.getEntities()) {
            if (this.shouldProcess3DEntity(entity)) {
               if (postMode && this.maskBuffer != null) {
                  this.drawPlayerMaskBox(matrices, entity, tickDelta);
                  this.hasShaderMask = true;
               } else {
                  this.render3DBox(matrices, entity, tickDelta);
               }
            }
         }

         if (postMode && this.maskBuffer != null) {
            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
            mc.getFramebuffer().beginWrite(true);
            if (this.show3DBox.isState()) {
               this.renderShaderBoxesWorldPass();
            }
         }

         if (threadMode) {
            for (Entity entityx : mc.world.getEntities()) {
               if (this.shouldProcess3DEntity(entityx)) {
                  this.renderThreadWeb(matrices, entityx, tickDelta);
               }
            }
         }
      }
   }

   @EventTarget(
      priority = 100
   )
   public void onRender2D(EventRender2D event) {
      if (this.hasProjection && mc.world != null && mc.player != null) {
         this.frameThemeColor = this.getStableThemeColor();
         boolean tagsEnabled = (!this.elements.getSettings().isEmpty()
               && this.elements.getSettings().get(0).isState());
         boolean armorEnabled = (this.elements.getSettings().size() > (1)
               && this.elements.getSettings().get(1).isState());
         if (tagsEnabled || armorEnabled) {
            FontRenderer font = tagsEnabled ? ObsidianComponent_201.getFont("sf_regular", 13) : null;
            int renderedItemTags = 0;

            for (Entity entity : mc.world.getEntities()) {
               if (entity instanceof PlayerEntity) {
                  PlayerEntity player = (PlayerEntity)entity;
                  if (this.shouldProcess2DPlayer(player)) {
                     Box interpolatedBox = this.getInterpolatedBox(player, this.lastTickDelta);
                     ObsidianComponent_255 rect = this.projectBox(interpolatedBox);
                     if (rect != null) {
                        if (tagsEnabled && font != null) {
                           this.drawTag(event, player, rect, font);
                        }

                        if (armorEnabled) {
                           this.drawArmor(event, player, rect, tagsEnabled);
                        }
                     }
                  }
               } else if (tagsEnabled && font != null) {
                  if (entity instanceof ItemEntity) {
                     ItemEntity itemEntity = (ItemEntity)entity;
                     if (this.shouldProcessItem2D(itemEntity)
                        && renderedItemTags < 48
                        && this.projectEntityAnchor(
                           itemEntity,
                           (double)itemEntity.getHeight() + 0.25,
                           this.projectedPoint
                        )) {
                        this.drawDroppedItemTag(event, itemEntity, this.projectedPoint.x, this.projectedPoint.y, font);
                        renderedItemTags++;
                     }
                  } else if (entity instanceof LivingEntity) {
                     LivingEntity livingEntity = (LivingEntity)entity;
                     if (this.shouldProcessLiving2D(livingEntity)) {
                        Box interpolatedBox = this.getInterpolatedBox(livingEntity, this.lastTickDelta);
                        ObsidianComponent_255 rect = this.projectBox(interpolatedBox);
                        if (rect != null) {
                           this.drawLivingTag(event, livingEntity, rect, font);
                        }
                     }
                  }
               }
            }
         }
      }
   }
   public void drawTag(EventRender2D event, PlayerEntity player, ObsidianComponent_255 rect, FontRenderer font) {
      MatrixStack matrices = event.getContext().getMatrices();
      List<ObsidianComponent_173> donateSegments = this.getDonateSegmentsFromTab(player);
      String nameText = this.getProtectedName(player.getNameForScoreboard());
      float hp = ScoreboardHPModule.getHealthWithAbsorption(player);
      String leftBracket = "";
      String hpText = Math.round(hp) + " hp";
      String rightBracket = "";
      boolean isFriend = (Obsidian.INSTANCE.friendStorage != null && Obsidian.INSTANCE.friendStorage.isFriend(player.getName().getString()));
      String friendSuffix = isFriend ? " [F]" : "";
      float donateWidth = 0.0f;

      for (ObsidianComponent_173 segment : donateSegments) {
         donateWidth += font.getStringWidth(segment.text());
      }

      float totalWidth = donateWidth
         + font.getStringWidth(nameText)
         + font.getStringWidth(leftBracket)
         + font.getStringWidth(hpText)
         + font.getStringWidth(rightBracket)
         + font.getStringWidth(friendSuffix)
         + 7.5f
         + 3.0f
         + 2.0f;
      float boxHeight = 16.0f;
      float x = rect.centerX() - totalWidth * 0.5f;
      float y = this.getTagTopY(rect, boxHeight);
      this.drawDefaultTagPanel(
         matrices,
         x - 1.0f,
         y - 0.5f,
         totalWidth + 2.0f,
         boxHeight - 4.0f
      );
      float headY = y + 1.7000000476837158f;
      RenderUtil.drawPlayerHead(
         matrices,
         player.getUuid(),
         x + 1.0f,
         headY,
         7.5f,
         1.0f,
         1.0f,
         0.0f
      );
      float drawX = x + 1.5f + 7.5F + 3.0f;

      for (ObsidianComponent_173 segment : donateSegments) {
         font.drawString(matrices, segment.text(), drawX, y + 4.0f, segment.color());
         drawX += font.getStringWidth(segment.text());
      }

      font.drawString(matrices, nameText, drawX, y + 4.0f, -1);
      drawX += font.getStringWidth(nameText);
      font.drawString(matrices, leftBracket, drawX, y + 4.0f, -1);
      drawX += font.getStringWidth(leftBracket);
      font.drawString(matrices, hpText, drawX, y + 4.0f, -43691);
      drawX += font.getStringWidth(hpText);
      font.drawString(
         matrices, rightBracket, drawX, y + 4.0f, -1
      );
      drawX += font.getStringWidth(rightBracket);
      if (isFriend) {
         font.drawString(matrices, friendSuffix, drawX, y + 4.0f, -11141291);
      }
   }
   public void drawArmor(EventRender2D event, PlayerEntity player, ObsidianComponent_255 rect, boolean tagsEnabled) {
      MatrixStack matrices = event.getContext().getMatrices();
      int count = 0;
      ItemStack offHand = player.getOffHandStack();
      if (!offHand.isEmpty()) {
         this.armorStacksScratch[count] = offHand;
         this.armorHandScratch[count++] = true;
      }

      for (ItemStack stack : player.getArmorItems()) {
         if (!stack.isEmpty()) {
            this.armorStacksScratch[count] = stack;
            this.armorHandScratch[count++] = false;
         }
      }

      ItemStack mainHand = player.getMainHandStack();
      if (!mainHand.isEmpty()) {
         this.armorStacksScratch[count] = mainHand;
         this.armorHandScratch[count++] = true;
      }

      if (count != 0) {
         float step = 9.399999618530273f;
         float rowWidth = (float)count * 8.399999618530273f
            + (float)Math.max(0, count - 1)
               * 1.0f;
         float x = rect.centerX() - rowWidth * 0.5f;
         float y = tagsEnabled
            ? this.getTagTopY(rect, 14.0f) - 13.0f
            : rect.minY() - 13.0f;

         for (int i = 0; i < count; i++) {
            float drawX = x + (float)i * step;
            this.drawDefaultTagPanel(
               matrices,
               drawX,
               y,
               8.399999618530273f,
               8.399999618530273f
            );
         }

         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableDepthTest();
         RenderSystem.depthMask(false);

         for (int i = 0; i < count; i++) {
            float drawX = x + (float)i * step;
            int stackIndex = count - (1) - i;
            ItemStack stackx = this.armorStacksScratch[stackIndex];
            boolean handStack = this.armorHandScratch[stackIndex];
            matrices.push();
            float itemSize = 7.360000133514404f;
            float itemX = drawX
               + (8.399999618530273f - itemSize) * 0.5f;
            float itemY = y + (8.399999618530273f - itemSize) * 0.5f;
            matrices.translate(itemX, itemY, 0.0f);
            matrices.scale(
               0.46000000834465027f,
               0.46000000834465027f,
               1.0f
            );
            event.getContext().drawItem(stackx, 0, 0);
            if (!handStack) {
               event.getContext().drawStackOverlay(mc.textRenderer, stackx, 0, 0, null);
            }

            matrices.pop();
         }

         RenderSystem.depthMask(true);
         RenderSystem.enableDepthTest();
         RenderSystem.disableBlend();

         for (int i = 0; i < count; i++) {
            this.armorStacksScratch[i] = ItemStack.EMPTY;
            this.armorHandScratch[i] = false;
         }
      }
   }
   public void drawLivingTag(EventRender2D event, LivingEntity entity, ObsidianComponent_255 rect, FontRenderer font) {
      MatrixStack matrices = event.getContext().getMatrices();
      String nameText = entity instanceof PlayerEntity player
         ? this.getProtectedName(player.getDisplayName().getString())
         : entity.getDisplayName().getString();
      String hpText = Math.round(ScoreboardHPModule.getHealthWithAbsorption(entity)) + " hp";
      float totalWidth = font.getStringWidth(nameText) + font.getStringWidth(" ") + font.getStringWidth(hpText);
      float boxHeight = 14.0f;
      float x = rect.centerX() - totalWidth * 0.5f;
      float y = this.getTagTopY(rect, boxHeight);
      this.drawDefaultTagPanel(
         matrices,
         x - 1.0F,
         y - 0.5f,
         totalWidth + 2.0f,
         boxHeight - 4.0f
      );
      font.drawString(matrices, nameText, x, y + 3.0f, -1);
      font.drawString(
         matrices,
         hpText,
         x + font.getStringWidth(nameText) + font.getStringWidth(" "),
         y + 3.0f,
         -43691
      );
   }
   public void drawDroppedItemTag(EventRender2D event, ItemEntity itemEntity, float anchorX, float anchorY, FontRenderer font) {
      MatrixStack matrices = event.getContext().getMatrices();
      ItemStack stack = itemEntity.getStack();
      String countText = stack.getCount() + "x";
      List<ObsidianComponent_173> itemSegments = this.getStyledTextSegments(stack.getName(), this.getDroppedItemTextColor(stack));
      int countColor = ObsidianComponent_olorUtil.rgba(
         155, 155, 155, 255
      );
      float itemNameWidth = 0.0f;

      for (ObsidianComponent_173 segment : itemSegments) {
         itemNameWidth += font.getStringWidth(segment.text());
      }

      float spaceWidth = font.getStringWidth(" ");
      float totalWidth = itemNameWidth + spaceWidth + font.getStringWidth(countText);
      float boxHeight = 14.0f;
      float x = anchorX - totalWidth * 0.5f;
      float y = anchorY - boxHeight - 2.0f;
      this.drawDefaultTagPanel(
         matrices,
         x - 2.0f,
         y - 0.5f,
         totalWidth + 4.0f,
         boxHeight - 3.0f
      );
      float drawX = x;

      for (ObsidianComponent_173 segment : itemSegments) {
         font.drawString(matrices, segment.text(), drawX, y + 3.5f, segment.color());
         drawX += font.getStringWidth(segment.text());
      }

      font.drawString(matrices, countText, drawX + spaceWidth, y + 3.5f, countColor);
   }
   public int getMinecraftItemNameColor(ItemStack stack) {
      Text name = stack.getName();
      if (name != null) {
         int[] var10000 = new int[1];
         var10000[0] = 0;
         int[] discoveredColor = var10000;
         boolean[] var5 = new boolean[1];
         var5[0] = false;
         boolean[] found = var5;
         name.visit((style, string) -> {
            if (!found[0] && style != null && style.getColor() != null) {
               discoveredColor[0] = -16777216 | style.getColor().getRgb();
               found[0] = true;
            }

            return found[0] ? Optional.of(string) : Optional.empty();
         }, Style.EMPTY);
         if (found[0]) {
            return discoveredColor[0];
         }
      }
      return switch (stack.getRarity()) {
         case COMMON -> ObsidianComponent_olorUtil.rgba(
         255, 255, 85, 255
      );
         case UNCOMMON -> ObsidianComponent_olorUtil.rgba(
         85, 255, 255, 255
      );
         case RARE -> ObsidianComponent_olorUtil.rgba(255, 85, 255, 255);
         case EPIC -> -1;
         default -> throw new MatchException(null, null);
      };
   }
   public int getDroppedItemTextColor(ItemStack stack) {
      return this.getMinecraftItemNameColor(stack);
   }
   public boolean isNetheriteItem(Item item) {
      return Registries.ITEM.getId(item).getPath().contains("netherite");
   }
   public void drawDefaultTagPanel(MatrixStack matrices, float x, float y, float width, float height) {
      int themeColor = this.frameThemeColor;
      RenderUtil.drawDefaultHudPanel(
         matrices,
         x,
         y,
         width,
         height,
         1.100000023841858f,
         1.100000023841858f,
         ObsidianComponent_olorUtil.rgba(
            50,
            50,
            50,
            204
         ),
         ObsidianComponent_olorUtil.setAlphaColor(ObsidianComponent_olorUtil.darken(themeColor, 0.15000000596046448f), 204),
         ObsidianComponent_olorUtil.setAlphaColor(ObsidianComponent_olorUtil.darken(themeColor, 0.05000000074505806f), 204)
      );
   }

   public boolean shouldHideVanillaTags() {
      return (this.isEnable()
            && !this.elements.getSettings().isEmpty()
            && this.elements.getSettings().get(0).isState());
   }
   public float getTagTopY(ObsidianComponent_255 rect, float tagHeight) {
      return rect.minY() - tagHeight - 0.0f;
   }
   public String[] getNameVariants(PlayerEntity player) {
      String profileName = player.getGameProfile() != null ? player.getGameProfile().getName() : "";
      String scoreboardName = player.getNameForScoreboard();
      String protectedScoreboardName = this.getProtectedName(scoreboardName);
      String protectedProfileName = this.getProtectedName(profileName);
      String protectedPlainName = this.getProtectedName(player.getName().getString());
      String[] var10000 = new String[6];
      var10000[0] = player.getName().getString();
      var10000[1] = protectedPlainName;
      var10000[2] = scoreboardName;
      var10000[3] = protectedScoreboardName;
      var10000[4] = profileName;
      var10000[5] = protectedProfileName;
      return var10000;
   }
   public String getProtectedName(String input) {
      NameProtectModule nameProtect = ModuleManager.INSTANCE != null ? ModuleManager.nameProtect : null;
      return nameProtect != null && nameProtect.isEnable() ? nameProtect.patch(input) : input;
   }
   public int findAnyNameIndex(String text, String[] names) {
      if (text != null && !text.isEmpty() && names != null) {
         int best = -1;
         String[] var4 = names;
         int var5 = names.length;

         for (int var6 = 0; var6 < var5; var6++) {
            String name = var4[var6];
            if (name != null && !name.isEmpty()) {
               int idx = this.indexOfIgnoreCase(text, name);
               if (idx >= 0 && (best == -1 || idx < best)) {
                  best = idx;
               }
            }
         }

         return best;
      } else {
         return -1;
      }
   }
   public int indexOfIgnoreCase(String text, String search) {
      if (text != null && search != null && !search.isEmpty()) {
         int limit = text.length() - search.length();

         for (int i = 0; i <= limit; i++) {
            if (text.regionMatches(true, i, search, 0, search.length())) {
               return i;
            }
         }

         return -1;
      } else {
         return -1;
      }
   }
   public void trimSegmentsToLength(List<ObsidianComponent_173> segments, int maxLength) {
      int remaining = Math.max(0, maxLength);
      List<ObsidianComponent_173> trimmed = new ArrayList<>();

      for (ObsidianComponent_173 seg : segments) {
         if (remaining <= 0) {
            break;
         }

         String text = seg.text();
         if (text.length() <= remaining) {
            trimmed.add(seg);
            remaining -= text.length();
         } else {
            trimmed.add(new ObsidianComponent_173(text.substring(0, remaining), seg.color()));
            remaining = 0;
         }
      }

      segments.clear();
      segments.addAll(trimmed);
   }
   public List<ObsidianComponent_173> getDonateSegmentsFromTab(PlayerEntity player) {
      long now = System.currentTimeMillis();
      DonateSegmentCache cache = this.donateCache.computeIfAbsent(player.getUuid(), uuid -> new DonateSegmentCache());
      if (now < cache.nextUpdateAt) {
         return cache.segments;
      } else {
         List<ObsidianComponent_173> segments = new ArrayList<>();
         if (mc.getNetworkHandler() == null) {
            cache.segments = Collections.emptyList();
            cache.nextUpdateAt = now + (1000L);
            return cache.segments;
         } else {
            PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(player.getUuid());
            if (entry == null) {
               cache.segments = Collections.emptyList();
               cache.nextUpdateAt = now + (1000L);
               return cache.segments;
            } else {
               Text displayName = entry.getDisplayName();
               if (displayName == null) {
                  displayName = player.getDisplayName();
               }

               if (displayName == null) {
                  cache.segments = Collections.emptyList();
                  cache.nextUpdateAt = now + (1000L);
                  return cache.segments;
               } else {
                  String[] nameVariants = this.getNameVariants(player);
                  boolean[] var10000 = new boolean[1];
                  var10000[0] = false;
                  boolean[] foundName = var10000;
                  displayName.visit(
                     (style, string) -> {
                        if (!foundName[0] && string != null && !string.isEmpty()) {
                           String part = string.replace((char)(10), (char)(32))
                              .replace((char)(13), (char)(32));
                           int nameIndex = this.findAnyNameIndex(part, nameVariants);
                           String donatePart = nameIndex >= 0 ? part.substring(0, nameIndex) : part;
                           if (!donatePart.isEmpty()) {
                              int baseColor = style.getColor() != null ? style.getColor().getRgb() : 16777215;
                              this.appendColoredSegments(segments, donatePart, baseColor);
                           }

                           if (nameIndex >= 0) {
                              foundName[0] = true;
                           }

                           return Optional.empty();
                        } else {
                           return Optional.empty();
                        }
                     },
                     Style.EMPTY
                  );
                  if (!foundName[0]) {
                     segments.clear();
                     Team team = player.getScoreboardTeam();
                     if (team != null && team.getPrefix() != null) {
                        this.appendTextSegments(segments, team.getPrefix());
                     }
                  }

                  if (segments.isEmpty()) {
                     cache.segments = Collections.emptyList();
                     cache.nextUpdateAt = now + (1000L);
                     this.cleanupDonateCache(now);
                     return cache.segments;
                  } else {
                     StringBuilder combined = new StringBuilder();

                     for (ObsidianComponent_173 seg : segments) {
                        combined.append(seg.text());
                     }

                     int donateNameIndex = this.findAnyNameIndex(combined.toString(), nameVariants);
                     if (donateNameIndex >= 0) {
                        if (donateNameIndex == 0) {
                           cache.segments = Collections.emptyList();
                           cache.nextUpdateAt = now + (1000L);
                           this.cleanupDonateCache(now);
                           return cache.segments;
                        }

                        this.trimSegmentsToLength(segments, donateNameIndex);
                     }

                     if (segments.isEmpty()) {
                        cache.segments = Collections.emptyList();
                        cache.nextUpdateAt = now + (1000L);
                        this.cleanupDonateCache(now);
                        return cache.segments;
                     } else {
                        StringBuilder textCheck = new StringBuilder();

                        for (ObsidianComponent_173 seg : segments) {
                           textCheck.append(seg.text());
                        }

                        if (textCheck.toString().trim().isEmpty()) {
                           cache.segments = Collections.emptyList();
                           cache.nextUpdateAt = now + (1000L);
                           this.cleanupDonateCache(now);
                           return cache.segments;
                        } else {
                           ObsidianComponent_173 last = segments.get(segments.size() - 1);
                           if (!last.text().endsWith(" ")) {
                              segments.set(segments.size() - 1, new ObsidianComponent_173(last.text() + " ", last.color()));
                           }

                           cache.segments = List.copyOf(segments);
                           cache.nextUpdateAt = now + (1000L);
                           this.cleanupDonateCache(now);
                           return cache.segments;
                        }
                     }
                  }
               }
            }
         }
      }
   }
   public void appendTextSegments(List<ObsidianComponent_173> out, Text text) {
      text.visit(
         (style, string) -> {
            if (string != null && !string.isEmpty()) {
               int baseColor = style.getColor() != null ? style.getColor().getRgb() : 16777215;
               this.appendColoredSegments(
                  out,
                  string.replace((char)(10), (char)(32))
                     .replace((char)(13), (char)(32)),
                  baseColor
               );
               return Optional.empty();
            } else {
               return Optional.empty();
            }
         },
         Style.EMPTY
      );
   }
   public List<ObsidianComponent_173> getStyledTextSegments(Text text, int fallbackColor) {
      List<ObsidianComponent_173> segments = new ArrayList<>();
      if (text != null) {
         this.appendTextSegments(segments, text);
      }

      if (segments.isEmpty() && text != null && !text.getString().isEmpty()) {
         segments.add(new ObsidianComponent_173(text.getString(), fallbackColor));
      }

      return segments;
   }
   public void appendColoredSegments(List<ObsidianComponent_173> out, String text, int baseColor) {
      if (text != null && !text.isEmpty()) {
         int currentColor = baseColor;
         StringBuilder chunk = new StringBuilder();
         int chunkColor = baseColor;
         int offset = 0;

         while (offset < text.length()) {
            int codePoint = text.codePointAt(offset);
            int charCount = Character.charCount(codePoint);
            if (codePoint == (167) && offset + charCount < text.length()) {
               this.flushSegment(out, chunk, chunkColor);
               char code = Character.toLowerCase(text.charAt(offset + charCount));
               Integer mappedColor = this.sectionColorToRgb(code);
               if (mappedColor != null) {
                  currentColor = mappedColor;
               } else if (code == (114)) {
                  currentColor = baseColor;
               }

               chunkColor = currentColor;
               offset += charCount + 1;
            } else {
               String replacement = ObsidianComponent_404.replaceCodePoint(codePoint);
               if (replacement == null) {
                  if (chunk.length() > 0 && chunkColor != currentColor) {
                     this.flushSegment(out, chunk, chunkColor);
                  }

                  chunkColor = currentColor;
                  chunk.appendCodePoint(codePoint);
                  offset += charCount;
               } else {
                  this.flushSegment(out, chunk, chunkColor);
                  int totalChars = Math.max(1, replacement.length());

                  for (int i = 0; i < replacement.length(); i++) {
                     int gradientColor = ObsidianComponent_404.getGradientColorForReplacement(
                        codePoint, i, totalChars, 1.0f, currentColor
                     );
                     if (chunk.length() > 0 && chunkColor != gradientColor) {
                        this.flushSegment(out, chunk, chunkColor);
                     }

                     chunkColor = gradientColor;
                     chunk.append(replacement.charAt(i));
                  }

                  offset += charCount;
               }
            }
         }

         this.flushSegment(out, chunk, chunkColor);
      }
   }
   public void flushSegment(List<ObsidianComponent_173> out, StringBuilder chunk, int color) {
      if (!chunk.isEmpty()) {
         out.add(new ObsidianComponent_173(chunk.toString(), color));
         chunk.setLength(0);
      }
   }
   public Integer sectionColorToRgb(char code) {
      return switch (code) {
         case '0' -> 0;
         case '1' -> 170;
         case '2' -> 43520;
         case '3' -> 43690;
         case '4' -> 11141120;
         case '5' -> 11141290;
         case '6' -> 16755200;
         case '7' -> 11184810;
         case '8' -> 5592405;
         case '9' -> 5592575;
         default -> null;
         case 'a' -> 5635925;
         case 'b' -> 5636095;
         case 'c' -> 16733525;
         case 'd' -> 16733695;
         case 'e' -> 16777045;
         case 'f' -> 16777215;
      };
   }
   public void cleanupDonateCache(long now) {
      if (now >= this.nextDonateCacheCleanupAt && mc.world != null) {
         this.nextDonateCacheCleanupAt = now + (2000L);
         this.donateCache
            .entrySet()
            .removeIf(entry -> (mc.world.getPlayerByUuid(entry.getKey()) == null));
      }
   }
   public Box getInterpolatedBox(Entity entity, float tickDelta) {
      double x = MathHelper.lerp((double)tickDelta, entity.lastRenderX, entity.getX());
      double y = MathHelper.lerp((double)tickDelta, entity.lastRenderY, entity.getY());
      double z = MathHelper.lerp((double)tickDelta, entity.lastRenderZ, entity.getZ());
      double ox = x - entity.getX();
      double oy = y - entity.getY();
      double oz = z - entity.getZ();
      return entity.getBoundingBox().offset(ox, oy, oz).expand(0.05);
   }
   public ObsidianComponent_255 projectBox(Box box) {
      double minX = Double.POSITIVE_INFINITY;
      double minY = Double.POSITIVE_INFINITY;
      double maxX = Double.longBitsToDouble(-4503599627370496L);
      double maxY = Double.longBitsToDouble(-4503599627370496L);
      boolean projectedAny = false;

      for (int xi = 0; xi < (2); xi++) {
         for (int yi = 0; yi < (2); yi++) {
            for (int zi = 0; zi < (2); zi++) {
               if (this.projectToScreen(xi == 0 ? box.minX : box.maxX, yi == 0 ? box.minY : box.maxY, zi == 0 ? box.minZ : box.maxZ, this.projectedPoint)) {
                  projectedAny = true;
                  minX = Math.min(minX, (double)this.projectedPoint.x);
                  minY = Math.min(minY, (double)this.projectedPoint.y);
                  maxX = Math.max(maxX, (double)this.projectedPoint.x);
                  maxY = Math.max(maxY, (double)this.projectedPoint.y);
               }
            }
         }
      }

      if (!projectedAny) {
         return null;
      } else if (minX > (double)(mc.getWindow().getScaledWidth() + (300))
         || maxX < Double.longBitsToDouble(-4579386764849840128L)) {
         return null;
      } else if (minY > (double)(mc.getWindow().getScaledHeight() + (300))
         || maxY < Double.longBitsToDouble(-4579386764849840128L)) {
         return null;
      } else {
         return !(maxX - minX < 2.0)
               && !(maxY - minY < 2.0)
            ? new ObsidianComponent_255((float)minX, (float)minY, (float)maxX, (float)maxY)
            : null;
      }
   }
   public boolean projectToScreen(double worldX, double worldY, double worldZ, ObsidianComponent_96 out) {
      this.projectionScratch.set((float)(worldX - this.lastCameraPos.x), (float)(worldY - this.lastCameraPos.y), (float)(worldZ - this.lastCameraPos.z));
      this.projectionScratch.rotate(this.lastInverseCameraRotation);
      this.clipScratch.set(this.projectionScratch.x, this.projectionScratch.y, this.projectionScratch.z, 1.0f);
      this.lastProjectionMatrix.transform(this.clipScratch);
      float w = this.clipScratch.w;
      if (w <= 9.999999747378752e-06f) {
         return false;
      } else {
         float ndcX = this.clipScratch.x / w;
         float ndcY = this.clipScratch.y / w;
         float ndcZ = this.clipScratch.z / w;
         float screenX = (ndcX * 0.5f + 0.5f)
            * (float)this.lastScaledWidth;
         float screenY = (
               1.0f
                  - (ndcY * 0.5f + 0.5f)
            )
            * (float)this.lastScaledHeight;
         if (Float.isNaN(screenX) || Float.isNaN(screenY)) {
            return false;
         } else if (!Float.isInfinite(screenX) && !Float.isInfinite(screenY)) {
            out.x = screenX;
            out.y = screenY;
            out.z = ndcZ;
            return true;
         } else {
            return false;
         }
      }
   }
   public boolean projectEntityAnchor(Entity entity, double yOffset, ObsidianComponent_96 out) {
      double x = MathHelper.lerp((double)this.lastTickDelta, entity.lastRenderX, entity.getX());
      double y = MathHelper.lerp((double)this.lastTickDelta, entity.lastRenderY, entity.getY()) + yOffset;
      double z = MathHelper.lerp((double)this.lastTickDelta, entity.lastRenderZ, entity.getZ());
      return this.projectToScreen(x, y, z, out);
   }
   public boolean isInFirstPerson() {
      return (mc != null && mc.gameRenderer != null && !mc.gameRenderer.getCamera().isThirdPerson());
   }
   public boolean shouldProcess3DEntity(Entity entity) {
      if (entity == null || entity.isRemoved() || entity instanceof ArmorStandEntity) {
         return false;
      } else if (entity instanceof PlayerEntity player) {
         return this.shouldProcessPlayer(player, false);
      } else if (!(entity instanceof ItemEntity itemEntity)) {
         if (!(entity instanceof LivingEntity livingEntity) || !livingEntity.isAlive()) {
            return false;
         }

         if (this.isAnimalEntity(entity)) {
            return this.targetAnimals.isState();
         } else {
            return (this.isMobEntity(entity) && this.targetMobs.isState());
         }
      } else {
         return (this.targetItems.isState() && itemEntity.isAlive());
      }
   }
   public boolean shouldProcess2DPlayer(PlayerEntity player) {
      return this.shouldProcessPlayer(player, true);
   }
   public boolean shouldProcessLiving2D(LivingEntity entity) {
      return this.shouldProcess3DEntity(entity);
   }
   public boolean shouldProcessItem2D(ItemEntity itemEntity) {
      return (this.targetItems.isState() && itemEntity.isAlive());
   }
   public boolean shouldProcessPlayer(PlayerEntity player, boolean skipInvisible) {
      if (!this.targetPlayers.isState()) {
         return false;
      } else if (player == null || !player.isAlive()) {
         return false;
      } else if (player == mc.player && this.isInFirstPerson()) {
         return false;
      } else {
         return (!(skipInvisible && player.isInvisible() && !this.canRenderInvisiblePlayer(player)));
      }
   }
   public boolean isTargetEnabled(int index) {
      return (this.targets.getSettings().size() > index && this.targets.getSettings().get(index).isState());
   }
   public boolean isAnimalEntity(Entity entity) {
      return (!(!(entity instanceof AnimalEntity) && !(entity instanceof WaterAnimalEntity) && !(entity instanceof AmbientEntity)));
   }
   public boolean isMobEntity(Entity entity) {
      return (entity instanceof MobEntity && !this.isAnimalEntity(entity) && !(entity instanceof PlayerEntity));
   }
   public boolean canRenderInvisiblePlayer(PlayerEntity player) {
      SeeInvisiblesModule seeInvisibles = ModuleManager.seeInvisibles;
      return (seeInvisibles != null && seeInvisibles.shouldRenderInvisible(player));
   }
   public boolean isOutsideRenderDistance(Entity entity) {
      int viewDistanceChunks = (Integer)mc.options.getViewDistance().getValue();
      double maxDistance = Math.max(
         48.0,
         (double)viewDistanceChunks * 16.0
            + 16.0
      );
      return (entity.squaredDistanceTo(this.lastCameraPos) > maxDistance * maxDistance);
   }
   public void render3DBox(MatrixStack matrices, Entity entity, float tickDelta) {
      Box box;
      int var10000;
      label23: {
         Vec3d camera = mc.gameRenderer.getCamera().getPos();
         double x = MathHelper.lerp((double)tickDelta, entity.lastRenderX, entity.getX()) - camera.x;
         double y = MathHelper.lerp((double)tickDelta, entity.lastRenderY, entity.getY()) - camera.y;
         double z = MathHelper.lerp((double)tickDelta, entity.lastRenderZ, entity.getZ()) - camera.z;
         box = entity.getBoundingBox().offset(-entity.getX(), -entity.getY(), -entity.getZ());
         matrices.push();
         matrices.translate(x, y, z);
         if (entity instanceof PlayerEntity player
            && Obsidian.INSTANCE.friendStorage != null
            && Obsidian.INSTANCE.friendStorage.isFriend(player.getName().getString())) {
            var10000 = 1;
            break label23;
         }

         var10000 = 0;
      }

      boolean isFriend = (var10000 != 0);
      int boxColor;
      if (isFriend) {
         boxColor = ObsidianComponent_olorUtil.rgba(
            84,
            255,
            84,
            255
         );
      } else {
         boxColor = this.getStableThemeColor();
      }

      boxColor = this.applyEntityHurtTint(entity, boxColor);
      float r = ObsidianComponent_olorUtil.redf(boxColor);
      float g = ObsidianComponent_olorUtil.greenf(boxColor);
      float b = ObsidianComponent_olorUtil.bluef(boxColor);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableCull();
      RenderSystem.enableDepthTest();
      RenderSystem.depthMask(false);
      RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
      RenderSystem.lineWidth(1.5f);
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      Tessellator tessellator = Tessellator.getInstance();
      if (this.boxFilled.isState()) {
         this.drawFilledBox(tessellator, matrix, box, r, g, b, 0.23000000417232513f);
      }

      this.drawBoxOutline(tessellator, matrix, box, r, g, b, 1.0f);
      RenderSystem.enableCull();
      RenderSystem.enableDepthTest();
      RenderSystem.depthMask(true);
      RenderSystem.disableBlend();
      matrices.pop();
   }
   public int applyEntityHurtTint(Entity entity, int baseColor) {
      if (entity instanceof LivingEntity livingEntity && this.hurtTint.isState()) {
         float target = MathHelper.clamp(
            (float)livingEntity.hurtTime / 10.0f,
            0.0f,
            1.0f
         );
         float current = this.entityHurtTintProgress.getOrDefault(entity.getId(), 0.0f);
         float speed = target > current
            ? 0.3799999952316284f
            : 0.1599999964237213f;
         current += (target - current) * speed;
         if (current <= 0.003000000026077032f
            && target <= 0.0f) {
            this.entityHurtTintProgress.remove(entity.getId());
            return baseColor;
         }

         this.entityHurtTintProgress.put(entity.getId(), current);
         int hitColor = ObsidianComponent_olorUtil.rgba(
            255,
            70,
            70,
            255
         );
         return ObsidianComponent_olorUtil.interpolateColor(baseColor, hitColor, current);
      }

      this.entityHurtTintProgress.remove(entity.getId());
      return baseColor;
   }
   public void drawPlayerMaskBox(MatrixStack matrices, Entity entity, float tickDelta) {
      Vec3d camera = mc.gameRenderer.getCamera().getPos();
      double x = MathHelper.lerp((double)tickDelta, entity.lastRenderX, entity.getX()) - camera.x;
      double y = MathHelper.lerp((double)tickDelta, entity.lastRenderY, entity.getY()) - camera.y;
      double z = MathHelper.lerp((double)tickDelta, entity.lastRenderZ, entity.getZ()) - camera.z;
      Box box = entity.getBoundingBox().offset(-entity.getX(), -entity.getY(), -entity.getZ());
      matrices.push();
      matrices.translate(x, y, z);
      this.drawMaskBox(Tessellator.getInstance(), matrices.peek().getPositionMatrix(), box);
      matrices.pop();
   }
   public void drawMaskBox(Tessellator tessellator, Matrix4f matrix, Box box) {
      BufferBuilder b = tessellator.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
      float minX = (float)box.minX;
      float minY = (float)box.minY;
      float minZ = (float)box.minZ;
      float maxX = (float)box.maxX;
      float maxY = (float)box.maxY;
      float maxZ = (float)box.maxZ;
      int white = -1;
      b.vertex(matrix, minX, minY, minZ).color(white);
      b.vertex(matrix, maxX, minY, minZ).color(white);
      b.vertex(matrix, maxX, minY, maxZ).color(white);
      b.vertex(matrix, minX, minY, maxZ).color(white);
      b.vertex(matrix, minX, maxY, minZ).color(white);
      b.vertex(matrix, minX, maxY, maxZ).color(white);
      b.vertex(matrix, maxX, maxY, maxZ).color(white);
      b.vertex(matrix, maxX, maxY, minZ).color(white);
      b.vertex(matrix, minX, minY, minZ).color(white);
      b.vertex(matrix, minX, maxY, minZ).color(white);
      b.vertex(matrix, maxX, maxY, minZ).color(white);
      b.vertex(matrix, maxX, minY, minZ).color(white);
      b.vertex(matrix, minX, minY, maxZ).color(white);
      b.vertex(matrix, maxX, minY, maxZ).color(white);
      b.vertex(matrix, maxX, maxY, maxZ).color(white);
      b.vertex(matrix, minX, maxY, maxZ).color(white);
      b.vertex(matrix, minX, minY, minZ).color(white);
      b.vertex(matrix, minX, minY, maxZ).color(white);
      b.vertex(matrix, minX, maxY, maxZ).color(white);
      b.vertex(matrix, minX, maxY, minZ).color(white);
      b.vertex(matrix, maxX, minY, minZ).color(white);
      b.vertex(matrix, maxX, maxY, minZ).color(white);
      b.vertex(matrix, maxX, maxY, maxZ).color(white);
      b.vertex(matrix, maxX, minY, maxZ).color(white);
      BufferRenderer.drawWithGlobalProgram(b.end());
   }
   public void renderShaderBoxes() {
      if (this.hasShaderMask && this.maskBuffer != null) {
         boolean lineMode = this.isThreadMode();
         ShaderProgram shader = mc.getShaderLoader().getOrCreateProgram(ObsidianComponent_257.blockOverlay);
         if (shader != null) {
            int color1 = this.getStableThemeColor();
            int color2 = this.isRainbowTheme() ? ObsidianComponent_olorUtil.getThemeColor(180) : color1;
            mc.getFramebuffer().beginWrite(false);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableDepthTest();
            RenderSystem.setShader(ObsidianComponent_257.blockOverlay);
            RenderSystem.setShaderTexture(0, this.maskBuffer.getColorAttachment());
            this.setUniform(
               shader,
               "texelSize",
               1.0f
                  / (float)Math.max(1, mc.getWindow().getFramebufferWidth()),
               1.0f
                  / (float)Math.max(1, mc.getWindow().getFramebufferHeight())
            );
            this.setUniform(shader, "color", ObsidianComponent_olorUtil.redf(color1), ObsidianComponent_olorUtil.greenf(color1), ObsidianComponent_olorUtil.bluef(color1));
            this.setUniform(shader, "color2", ObsidianComponent_olorUtil.redf(color2), ObsidianComponent_olorUtil.greenf(color2), ObsidianComponent_olorUtil.bluef(color2));
            this.setUniform(
               shader,
               "time",
               (float)(System.currentTimeMillis() % (100000L))
                  / 1000.0f
            );
            this.setUniform(shader, "speed", this.waveSpeed.get());
            this.setUniform(shader, "scale", this.waveScale.get());
            this.setUniform(shader, "outline", this.outline.get());
            this.setUniform(shader, "glow", lineMode ? 0.0f : this.glow.get());
            this.setUniform(shader, "fill", lineMode ? 0.0f : this.fill.get());
            this.setUniform(shader, "alpha", lineMode ? 1.0f : this.alpha.get());
            this.setUniform(
               shader,
               "outlineOnly",
               lineMode ? 1.0f : 0.0f
            );
            this.drawFullscreenQuad();
            if (this.glow.get() > 0.0010000000474974513f) {
               int blurredMask = this.runKawaseBloom(
                  Math.max(
                     3,
                     Math.min(
                        8,
                        (4) + Math.round(this.outline.get() * 0.699999988079071f)
                     )
                  )
               );
               ShaderProgram glowShader = mc.getShaderLoader().getOrCreateProgram(ObsidianComponent_257.shaderHandsGlow);
               if (glowShader != null) {
                  RenderSystem.blendFuncSeparate(SrcFactor.SRC_ALPHA, DstFactor.ONE, SrcFactor.ZERO, DstFactor.ONE);
                  RenderSystem.setShader(ObsidianComponent_257.shaderHandsGlow);
                  RenderSystem.setShaderTexture(0, blurredMask);
                  RenderSystem.setShaderTexture(1, this.maskBuffer.getColorAttachment());
                  this.setUniform(glowShader, "color", ObsidianComponent_olorUtil.redf(color1), ObsidianComponent_olorUtil.greenf(color1), ObsidianComponent_olorUtil.bluef(color1));
                  this.setUniform(glowShader, "color2", ObsidianComponent_olorUtil.redf(color2), ObsidianComponent_olorUtil.greenf(color2), ObsidianComponent_olorUtil.bluef(color2));
                  this.setUniform(
                     glowShader,
                     "exposure",
                     1.0f + this.glow.get() * 1.7999999523162842f
                  );
                  this.drawFullscreenQuad();
               }
            }

            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderTexture(0, 0);
            RenderSystem.setShaderTexture(1, 0);
            mc.getFramebuffer().beginWrite(true);
         }
      }
   }
   public void renderShaderBoxesWorldPass() {
      if (this.isPostBoxMode()) {
         Matrix4f savedProjection = new Matrix4f(RenderSystem.getProjectionMatrix());
         float width = (float)Math.max(mc.getWindow().getScaledWidth(), 1);
         float height = (float)Math.max(mc.getWindow().getScaledHeight(), 1);
         Matrix4f ortho = new Matrix4f()
            .setOrtho(
               0.0f,
               width,
               height,
               0.0f,
               -1000.0f,
               1000.0f
            );
         RenderSystem.setProjectionMatrix(ortho, ProjectionType.ORTHOGRAPHIC);

         try {
            this.renderShaderBoxes();
         } finally {
            RenderSystem.setProjectionMatrix(savedProjection, ProjectionType.ORTHOGRAPHIC);
         }
      }
   }
   public int runKawaseBloom(int iterations) {
      this.ensureBloomBuffers(iterations);
      if (this.bloomBuffers.isEmpty()) {
         return this.maskBuffer.getColorAttachment();
      } else {
         int currentTexture = this.maskBuffer.getColorAttachment();
         ShaderProgram downShader = mc.getShaderLoader().getOrCreateProgram(ObsidianComponent_257.shaderHandsKawaseDown);
         ShaderProgram upShader = mc.getShaderLoader().getOrCreateProgram(ObsidianComponent_257.shaderHandsKawaseUp);
         if (downShader != null && upShader != null) {
            for (int i = 0; i < iterations; i++) {
               Framebuffer dst = this.bloomBuffers.get(i);
               dst.setClearColor(
                  0.0f,
                  0.0f,
                  0.0f,
                  0.0f
               );
               dst.clear();
               dst.beginWrite(true);
               RenderSystem.setShader(ObsidianComponent_257.shaderHandsKawaseDown);
               RenderSystem.setShaderTexture(0, currentTexture);
               this.setHandsKawaseUniforms(downShader, dst.textureWidth, dst.textureHeight, 1.0f + (float)i);
               this.drawFullscreenQuad();
               currentTexture = dst.getColorAttachment();
            }

            for (int i = iterations - (1); i >= 1; i--) {
               Framebuffer dst = this.bloomBuffers.get(i - (1));
               dst.setClearColor(
                  0.0f,
                  0.0f,
                  0.0f,
                  0.0f
               );
               dst.clear();
               dst.beginWrite(true);
               RenderSystem.setShader(ObsidianComponent_257.shaderHandsKawaseUp);
               RenderSystem.setShaderTexture(0, currentTexture);
               this.setHandsKawaseUniforms(
                  upShader, dst.textureWidth, dst.textureHeight, 1.0f + (float)i
               );
               this.setUniform(
                  upShader,
                  "color",
                  1.0f,
                  1.0f,
                  1.0f
               );
               this.drawFullscreenQuad();
               currentTexture = dst.getColorAttachment();
            }

            mc.getFramebuffer().beginWrite(true);
            return currentTexture;
         } else {
            return currentTexture;
         }
      }
   }
   public void ensureMaskBuffer() {
      int w = mc.getWindow().getFramebufferWidth();
      int h = mc.getWindow().getFramebufferHeight();
      if (this.maskBuffer == null || this.maskWidth != w || this.maskHeight != h) {
         if (this.maskBuffer != null) {
            this.maskBuffer.delete();
         }

         this.maskBuffer = new SimpleFramebuffer(w, h, true);
         this.maskWidth = w;
         this.maskHeight = h;

         for (Framebuffer fb : this.bloomBuffers) {
            fb.delete();
         }

         this.bloomBuffers.clear();
      }
   }
   public void ensureBloomBuffers(int iterations) {
      while (this.bloomBuffers.size() > iterations) {
         int last = this.bloomBuffers.size() - (1);
         this.bloomBuffers.get(last).delete();
         this.bloomBuffers.remove(last);
      }

      for (int i = 0; i < iterations; i++) {
         int w = Math.max(2, this.maskWidth >> i + 1);
         int h = Math.max(2, this.maskHeight >> i + 1);
         if (i >= this.bloomBuffers.size()) {
            this.bloomBuffers.add(new SimpleFramebuffer(w, h, false));
         } else {
            Framebuffer fb = this.bloomBuffers.get(i);
            if (fb.textureWidth != w || fb.textureHeight != h) {
               fb.delete();
               this.bloomBuffers.set(i, new SimpleFramebuffer(w, h, false));
            }
         }
      }
   }
   public void copyMainDepthToMask() {
      if (this.maskBuffer != null) {
         int readFbo = GL11.glGetInteger(36010);
         int drawFbo = GL11.glGetInteger(36006);
         int w = mc.getWindow().getFramebufferWidth();
         int h = mc.getWindow().getFramebufferHeight();
         GL30.glBindFramebuffer(36008, mc.getFramebuffer().fbo);
         GL30.glBindFramebuffer(36009, this.maskBuffer.fbo);
         GL30.glBlitFramebuffer(
            0,
            0,
            w,
            h,
            0,
            0,
            w,
            h,
            256,
            9728
         );
         GL30.glBindFramebuffer(36008, readFbo);
         GL30.glBindFramebuffer(36009, drawFbo);
      }
   }
   public void setUniform(ShaderProgram shader, String name, float value) {
      GlUniform uniform = shader.getUniform(name);
      if (uniform != null) {
         uniform.set(value);
      }
   }
   public void setUniform(ShaderProgram shader, String name, float x, float y) {
      GlUniform uniform = shader.getUniform(name);
      if (uniform != null) {
         uniform.set(x, y);
      }
   }
   public void setUniform(ShaderProgram shader, String name, float x, float y, float z) {
      GlUniform uniform = shader.getUniform(name);
      if (uniform != null) {
         uniform.set(x, y, z);
      }
   }
   public void setHandsKawaseUniforms(ShaderProgram shader, int texWidth, int texHeight, float offset) {
      this.setUniform(shader, "uSize", (float)Math.max(1, texWidth), (float)Math.max(1, texHeight));
      this.setUniform(shader, "uOffset", offset, offset);
      this.setUniform(
         shader,
         "uHalfPixel",
         0.5f / (float)Math.max(1, texWidth),
         0.5f / (float)Math.max(1, texHeight)
      );
   }
   public void drawFullscreenQuad() {
      float width = (float)Math.max(mc.getWindow().getScaledWidth(), 1);
      float height = (float)Math.max(mc.getWindow().getScaledHeight(), 1);
      BufferBuilder b = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
      b.vertex(
            0.0f,
            0.0f,
            0.0f
         )
         .texture(0.0f, 1.0f)
         .color(
            1.0f,
            1.0f,
            1.0f,
            1.0f
         );
      b.vertex(0.0f, height, 0.0f)
         .texture(0.0f, 0.0f)
         .color(
            1.0f,
            1.0f,
            1.0f,
            1.0f
         );
      b.vertex(width, height, 0.0f)
         .texture(1.0f, 0.0f)
         .color(
            1.0f,
            1.0f,
            1.0f,
            1.0f
         );
      b.vertex(width, 0.0f, 0.0f)
         .texture(1.0f, 1.0f)
         .color(
            1.0f,
            1.0f,
            1.0f,
            1.0f
         );
      BufferRenderer.drawWithGlobalProgram(b.end());
   }
   public boolean isPostBoxMode() {
      return false;
   }
   public boolean isThreadMode() {
      return false;
   }
   public boolean isRainbowTheme() {
      if (Obsidian.INSTANCE != null && Obsidian.INSTANCE.themeStorage != null && Obsidian.INSTANCE.themeStorage.getThemes() != null) {
         ObsidianComponent_olorPalette theme = Obsidian.INSTANCE.themeStorage.getThemes().getTheme();
         return (theme != null && "Rainbow".equals(theme.getName()));
      } else {
         return false;
      }
   }
   public int getStableThemeColor() {
      if (Obsidian.INSTANCE != null && Obsidian.INSTANCE.themeStorage != null && Obsidian.INSTANCE.themeStorage.getThemes() != null) {
         ObsidianComponent_olorPalette theme = Obsidian.INSTANCE.themeStorage.getThemes().getTheme();
         return theme != null && theme.color != null && theme.color.length != 0
            ? theme.color[0]
            : ObsidianComponent_olorUtil.getThemeColor(0);
      } else {
         return ObsidianComponent_olorUtil.getThemeColor(0);
      }
   }
   public void renderThreadWeb(MatrixStack matrices, Entity entity, float tickDelta) {
      Vec3d camera = mc.gameRenderer.getCamera().getPos();
      double x = MathHelper.lerp((double)tickDelta, entity.lastRenderX, entity.getX()) - camera.x;
      double y = MathHelper.lerp((double)tickDelta, entity.lastRenderY, entity.getY()) - camera.y;
      double z = MathHelper.lerp((double)tickDelta, entity.lastRenderZ, entity.getZ()) - camera.z;
      Box box = entity.getBoundingBox().offset(-entity.getX(), -entity.getY(), -entity.getZ());
      matrices.push();
      matrices.translate(x, y, z);
      this.drawAnimatedWeb(matrices.peek().getPositionMatrix(), box, (long)entity.getId());
      matrices.pop();
   }
   public void drawAnimatedWeb(Matrix4f matrix, Box box, long seedBase) {
      int strandsPerFace = 5;
      int samples = 18;
      float t = (float)(System.currentTimeMillis() % (100000L))
         / 1000.0f
         * this.lineSpeed.get();
      float lineWidth = 0.0024999999441206455f;
      float bendBase = 0.05999999865889549f + this.lineJitter.get() * 0.2F;
      int baseAlpha = Math.max(
         20,
         Math.min(255, (int)(this.alpha.get() * 210.0f))
      );
      int themeColor = this.getStableThemeColor();
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableCull();
      RenderSystem.enableDepthTest();
      RenderSystem.depthMask(false);
      RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
      this.drawFilledBoxInt(
         matrix,
         box,
         ObsidianComponent_olorUtil.setAlphaColor(themeColor, (int)(this.alpha.get() * this.fill.get() * 170.0f))
      );

      for (int face = 0; face < (6); face++) {
         int[] neighbors = this.faceNeighbors(face);

         for (int strand = 0; strand < strandsPerFace; strand++) {
            int key = face * 1000 + strand * 53;
            int adj = neighbors[strand % neighbors.length];
            double phase = (double)t
                  * (
                     0.95
                        + this.rand01(seedBase, key + 1)
                           * 0.55
                  )
               + (double)strand * 0.83
               + (double)face * 1.11;
            double edgeT = this.clamp01(
               0.5
                  + Math.sin(
                        phase * 1.37
                           + this.rand01(seedBase, key + (2))
                              * 6.2831853
                     )
                     * 0.38
            );
            Vec3d pivot = this.edgePoint(box, face, adj, edgeT, 0.0015);
            Vec3d start = this.facePoint(
               box,
               face,
               this.clamp01(
                  0.5
                     + (
                           this.rand01(seedBase, key + 3)
                              - 0.5
                        )
                        * 0.46
               ),
               this.clamp01(
                  0.5
                     + (
                           this.rand01(seedBase, key + (4))
                              - 0.5
                        )
                        * 0.46
               ),
               0.0015
            );
            Vec3d end = this.facePoint(
               box,
               adj,
               this.clamp01(
                  0.5
                     + (
                           this.rand01(seedBase, key + (5))
                              - 0.5
                        )
                        * 0.46
               ),
               this.clamp01(
                  0.5
                     + (
                           this.rand01(seedBase, key + (6))
                              - 0.5
                        )
                        * 0.46
               ),
               0.0015
            );
            Vec3d[] basisA = this.faceBasis(face);
            Vec3d[] basisB = this.faceBasis(adj);
            Vec3d normalA = this.faceNormal(face);
            Vec3d normalB = this.faceNormal(adj);
            double bendA = (double)bendBase
               * (
                  0.7
                     + this.rand01(seedBase, key + (7))
               )
               * Math.sin(
                  phase * 1.9
                     + this.rand01(seedBase, key + 8)
                        * 6.2831853
               );
            double bendB = (double)bendBase
               * (
                  0.7
                     + this.rand01(seedBase, key + (9))
               )
               * Math.cos(
                  phase * 1.7
                     + this.rand01(seedBase, key + 10)
                        * 6.2831853
               );
            Vec3d dirA = pivot.subtract(start);
            Vec3d c1a = start.add(dirA.multiply(0.38))
               .add(basisA[0].multiply(bendA))
               .add(
                  basisA[1]
                     .multiply(-bendA * 0.55)
               );
            Vec3d c2a = start.add(dirA.multiply(0.76))
               .add(
                  basisA[0]
                     .multiply(-bendA * 0.65)
               )
               .add(
                  basisA[1]
                     .multiply(bendA * 0.4)
               );
            Vec3d dirB = end.subtract(pivot);
            Vec3d c1b = pivot.add(dirB.multiply(0.24))
               .add(basisB[0].multiply(bendB))
               .add(
                  basisB[1]
                     .multiply(bendB * 0.45)
               );
            Vec3d c2b = pivot.add(dirB.multiply(0.62))
               .add(
                  basisB[0]
                     .multiply(-bendB * 0.7)
               )
               .add(
                  basisB[1]
                     .multiply(-bendB * 0.35)
               );
            int alphaLine = Math.max(
               18,
               Math.min(
                  255,
                  (int)(
                     (double)baseAlpha
                        * (
                           0.74
                              + 0.26
                                 * Math.sin(phase * 2.6)
                        )
                  )
               )
            );
            int color = ObsidianComponent_olorUtil.setAlphaColor(themeColor, alphaLine);
            this.drawBezierRibbon(matrix, start, c1a, c2a, pivot, normalA, samples, color, lineWidth);
            this.drawBezierRibbon(matrix, pivot, c1b, c2b, end, normalB, samples, color, lineWidth);
         }
      }

      RenderSystem.depthMask(true);
      RenderSystem.enableCull();
      RenderSystem.disableBlend();
   }
   public Vec3d cubicBezier(Vec3d p0, Vec3d p1, Vec3d p2, Vec3d p3, float t) {
      double it = 1.0 - (double)t;
      double it2 = it * it;
      double t2 = (double)(t * t);
      return p0.multiply(it2 * it)
         .add(p1.multiply(3.0 * it2 * (double)t))
         .add(p2.multiply(3.0 * it * t2))
         .add(p3.multiply(t2 * (double)t));
   }
   public void drawBezierRibbon(Matrix4f matrix, Vec3d p0, Vec3d p1, Vec3d p2, Vec3d p3, Vec3d faceNormal, int samples, int color, float halfWidth) {
      Vec3d[] points = new Vec3d[samples + 1];

      for (int s = 0; s <= samples; s++) {
         float u = (float)s / (float)samples;
         points[s] = this.cubicBezier(p0, p1, p2, p3, u);
      }

      BufferBuilder quads = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);

      for (int i = 0; i < samples; i++) {
         Vec3d a = points[i];
         Vec3d b = points[i + (1)];
         Vec3d dir = b.subtract(a);
         if (!(dir.lengthSquared() < 1e-06)) {
            Vec3d perp = faceNormal.crossProduct(dir).normalize().multiply((double)halfWidth);
            Vec3d aL = a.add(perp);
            Vec3d aR = a.subtract(perp);
            Vec3d bL = b.add(perp);
            Vec3d bR = b.subtract(perp);
            quads.vertex(matrix, (float)aL.x, (float)aL.y, (float)aL.z).color(color);
            quads.vertex(matrix, (float)aR.x, (float)aR.y, (float)aR.z).color(color);
            quads.vertex(matrix, (float)bR.x, (float)bR.y, (float)bR.z).color(color);
            quads.vertex(matrix, (float)bL.x, (float)bL.y, (float)bL.z).color(color);
         }
      }

      BufferRenderer.drawWithGlobalProgram(quads.end());
   }
   public int[] faceNeighbors(int face) {
      int[] var10000;
      switch (face) {
         case 0:
         case 1:
            var10000 = new int[4];
            var10000[0] = 2;
            var10000[1] = 3;
            var10000[2] = 4;
            var10000[3] = 5;
            break;
         case 2:
         case 3:
            var10000 = new int[4];
            var10000[0] = 0;
            var10000[1] = 1;
            var10000[2] = 4;
            var10000[3] = 5;
            break;
         default:
            var10000 = new int[4];
            var10000[0] = 0;
            var10000[1] = 1;
            var10000[2] = 2;
            var10000[3] = 3;
      }

      return var10000;
   }
   public Vec3d[] faceBasis(int face) {
      Vec3d[] var10000;
      switch (face) {
         case 0:
         case 1:
            var10000 = new Vec3d[2];
            var10000[0] = new Vec3d(
               1.0,
               0.0,
               0.0
            );
            var10000[1] = new Vec3d(
               0.0,
               0.0,
               1.0
            );
            break;
         case 2:
         case 3:
            var10000 = new Vec3d[2];
            var10000[0] = new Vec3d(
               1.0,
               0.0,
               0.0
            );
            var10000[1] = new Vec3d(
               0.0,
               1.0,
               0.0
            );
            break;
         default:
            var10000 = new Vec3d[2];
            var10000[0] = new Vec3d(
               0.0,
               0.0,
               1.0
            );
            var10000[1] = new Vec3d(
               0.0,
               1.0,
               0.0
            );
      }

      return var10000;
   }
   public Vec3d faceNormal(int face) {
      return switch (face) {
         case 0 -> new Vec3d(
         0.0,
         1.0,
         0.0
      );
         case 1 -> new Vec3d(
         0.0,
         Double.longBitsToDouble(-4616189618054758400L),
         0.0
      );
         case 2 -> new Vec3d(
         0.0,
         0.0,
         Double.longBitsToDouble(-4616189618054758400L)
      );
         case 3 -> new Vec3d(
         0.0,
         0.0,
         1.0
      );
         case 4 -> new Vec3d(
         Double.longBitsToDouble(-4616189618054758400L),
         0.0,
         0.0
      );
         default -> new Vec3d(
         1.0,
         0.0,
         0.0
      );
      };
   }
   public Vec3d edgePoint(Box box, int faceA, int faceB, double t, double inset) {
      double x = Double.NaN;
      double y = Double.NaN;
      double z = Double.NaN;
      double[] fixedA = this.faceFixedCoords(box, faceA, inset);
      if (!Double.isNaN(fixedA[0])) {
         x = fixedA[0];
      }

      if (!Double.isNaN(fixedA[1])) {
         y = fixedA[1];
      }

      if (!Double.isNaN(fixedA[2])) {
         z = fixedA[2];
      }

      double[] fixedB = this.faceFixedCoords(box, faceB, inset);
      if (!Double.isNaN(fixedB[0])) {
         x = fixedB[0];
      }

      if (!Double.isNaN(fixedB[1])) {
         y = fixedB[1];
      }

      if (!Double.isNaN(fixedB[2])) {
         z = fixedB[2];
      }

      double tt = this.clamp01(t);
      if (Double.isNaN(x)) {
         x = this.lerp(box.minX, box.maxX, tt);
      }

      if (Double.isNaN(y)) {
         y = this.lerp(box.minY, box.maxY, tt);
      }

      if (Double.isNaN(z)) {
         z = this.lerp(box.minZ, box.maxZ, tt);
      }

      return new Vec3d(x, y, z);
   }
   public double[] faceFixedCoords(Box box, int face, double inset) {
      double[] var10000;
      switch (face) {
         case 0:
            var10000 = new double[3];
            var10000[0] = Double.NaN;
            var10000[1] = box.maxY - inset;
            var10000[2] = Double.NaN;
            break;
         case 1:
            var10000 = new double[3];
            var10000[0] = Double.NaN;
            var10000[1] = box.minY + inset;
            var10000[2] = Double.NaN;
            break;
         case 2:
            var10000 = new double[3];
            var10000[0] = Double.NaN;
            var10000[1] = Double.NaN;
            var10000[2] = box.minZ + inset;
            break;
         case 3:
            var10000 = new double[3];
            var10000[0] = Double.NaN;
            var10000[1] = Double.NaN;
            var10000[2] = box.maxZ - inset;
            break;
         case 4:
            var10000 = new double[3];
            var10000[0] = box.minX + inset;
            var10000[1] = Double.NaN;
            var10000[2] = Double.NaN;
            break;
         default:
            var10000 = new double[3];
            var10000[0] = box.maxX - inset;
            var10000[1] = Double.NaN;
            var10000[2] = Double.NaN;
      }

      return var10000;
   }
   public Vec3d facePoint(Box box, int face, double u, double v, double inset) {
      u = this.clamp01(u);
      v = this.clamp01(v);

      return switch (face) {
         case 0 -> new Vec3d(this.lerp(box.minX, box.maxX, u), box.maxY - inset, this.lerp(box.minZ, box.maxZ, v));
         case 1 -> new Vec3d(this.lerp(box.minX, box.maxX, u), box.minY + inset, this.lerp(box.minZ, box.maxZ, v));
         case 2 -> new Vec3d(this.lerp(box.minX, box.maxX, u), this.lerp(box.minY, box.maxY, v), box.minZ + inset);
         case 3 -> new Vec3d(this.lerp(box.minX, box.maxX, u), this.lerp(box.minY, box.maxY, v), box.maxZ - inset);
         case 4 -> new Vec3d(box.minX + inset, this.lerp(box.minY, box.maxY, v), this.lerp(box.minZ, box.maxZ, u));
         default -> new Vec3d(box.maxX - inset, this.lerp(box.minY, box.maxY, v), this.lerp(box.minZ, box.maxZ, u));
      };
   }
   public double rand01(long seed, int salt) {
      long x = seed
         + (-7046029254386353131L)
            * ((long)salt + (1L));
      x ^= x >>> (30);
      x *= -4658895280553007687L;
      x ^= x >>> (27);
      x *= -7723592293110705685L;
      x ^= x >>> 31;
      return (double)(x & (16777215L))
         / 16777216.0;
   }
   public double lerp(double a, double b, double t) {
      return a + (b - a) * t;
   }
   public double clamp01(double v) {
      return Math.max(
         0.0,
         Math.min(1.0, v)
      );
   }
   public void drawFilledBoxInt(Matrix4f matrix, Box box, int color) {
      BufferBuilder b = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
      b.vertex(matrix, (float)box.minX, (float)box.minY, (float)box.minZ).color(color);
      b.vertex(matrix, (float)box.minX, (float)box.minY, (float)box.maxZ).color(color);
      b.vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.maxZ).color(color);
      b.vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.minZ).color(color);
      b.vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.minZ).color(color);
      b.vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.minZ).color(color);
      b.vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.maxZ).color(color);
      b.vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.maxZ).color(color);
      b.vertex(matrix, (float)box.minX, (float)box.minY, (float)box.minZ).color(color);
      b.vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.minZ).color(color);
      b.vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.minZ).color(color);
      b.vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.minZ).color(color);
      b.vertex(matrix, (float)box.minX, (float)box.minY, (float)box.maxZ).color(color);
      b.vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.maxZ).color(color);
      b.vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.maxZ).color(color);
      b.vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.maxZ).color(color);
      b.vertex(matrix, (float)box.minX, (float)box.minY, (float)box.minZ).color(color);
      b.vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.minZ).color(color);
      b.vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.maxZ).color(color);
      b.vertex(matrix, (float)box.minX, (float)box.minY, (float)box.maxZ).color(color);
      b.vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.minZ).color(color);
      b.vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.maxZ).color(color);
      b.vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.maxZ).color(color);
      b.vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.minZ).color(color);
      BufferRenderer.drawWithGlobalProgram(b.end());
   }
   public void drawFilledBox(Tessellator tessellator, Matrix4f matrix, Box box, float r, float g, float b, float a) {
      RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
      BufferBuilder buffer = tessellator.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
      float minX = (float)box.minX;
      float minY = (float)box.minY;
      float minZ = (float)box.minZ;
      float maxX = (float)box.maxX;
      float maxY = (float)box.maxY;
      float maxZ = (float)box.maxZ;
      buffer.vertex(matrix, minX, minY, minZ).color(r, g, b, a);
      buffer.vertex(matrix, maxX, minY, minZ).color(r, g, b, a);
      buffer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a);
      buffer.vertex(matrix, minX, minY, maxZ).color(r, g, b, a);
      buffer.vertex(matrix, minX, maxY, minZ).color(r, g, b, a);
      buffer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a);
      buffer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a);
      buffer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a);
      buffer.vertex(matrix, minX, minY, minZ).color(r, g, b, a);
      buffer.vertex(matrix, minX, maxY, minZ).color(r, g, b, a);
      buffer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a);
      buffer.vertex(matrix, maxX, minY, minZ).color(r, g, b, a);
      buffer.vertex(matrix, minX, minY, maxZ).color(r, g, b, a);
      buffer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a);
      buffer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a);
      buffer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a);
      buffer.vertex(matrix, minX, minY, minZ).color(r, g, b, a);
      buffer.vertex(matrix, minX, minY, maxZ).color(r, g, b, a);
      buffer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a);
      buffer.vertex(matrix, minX, maxY, minZ).color(r, g, b, a);
      buffer.vertex(matrix, maxX, minY, minZ).color(r, g, b, a);
      buffer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a);
      buffer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a);
      buffer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a);
      BufferRenderer.drawWithGlobalProgram(buffer.end());
   }
   public void drawBoxOutline(Tessellator tessellator, Matrix4f matrix, Box box, float r, float g, float b, float a) {
      RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
      RenderSystem.lineWidth(1.5f);
      BufferBuilder buffer = tessellator.begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
      float minX = (float)box.minX;
      float minY = (float)box.minY;
      float minZ = (float)box.minZ;
      float maxX = (float)box.maxX;
      float maxY = (float)box.maxY;
      float maxZ = (float)box.maxZ;
      buffer.vertex(matrix, minX, minY, minZ).color(r, g, b, a);
      buffer.vertex(matrix, maxX, minY, minZ).color(r, g, b, a);
      buffer.vertex(matrix, maxX, minY, minZ).color(r, g, b, a);
      buffer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a);
      buffer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a);
      buffer.vertex(matrix, minX, minY, maxZ).color(r, g, b, a);
      buffer.vertex(matrix, minX, minY, maxZ).color(r, g, b, a);
      buffer.vertex(matrix, minX, minY, minZ).color(r, g, b, a);
      buffer.vertex(matrix, minX, maxY, minZ).color(r, g, b, a);
      buffer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a);
      buffer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a);
      buffer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a);
      buffer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a);
      buffer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a);
      buffer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a);
      buffer.vertex(matrix, minX, maxY, minZ).color(r, g, b, a);
      buffer.vertex(matrix, minX, minY, minZ).color(r, g, b, a);
      buffer.vertex(matrix, minX, maxY, minZ).color(r, g, b, a);
      buffer.vertex(matrix, maxX, minY, minZ).color(r, g, b, a);
      buffer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a);
      buffer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a);
      buffer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a);
      buffer.vertex(matrix, minX, minY, maxZ).color(r, g, b, a);
      buffer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a);
      BufferRenderer.drawWithGlobalProgram(buffer.end());
   }
}
