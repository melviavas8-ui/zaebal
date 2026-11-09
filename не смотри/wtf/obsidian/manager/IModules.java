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























public interface IModules {
   public AirStuckModule airStuck = AirStuckModule.INSTANCE;
   public ArrowsModule arrows = ArrowsModule.INSTANCE;
   public KillAuraModule aura = KillAuraModule.INSTANCE;
   public AntiBotModule antibot = AntiBotModule.INSTANCE;
   public AimAssistModule aimassist = AimAssistModule.INSTANCE;
   public AutoAcceptModule autoAccept = AutoAcceptModule.INSTANCE;
   public AutoArmorModule autoArmor = AutoArmorModule.INSTANCE;
   public AutoFishModule autoFish = AutoFishModule.INSTANCE;
   public AutoForestModule autoForest = AutoForestModule.INSTANCE;
   public DeathCoordModule deathCoord = DeathCoordModule.INSTANCE;
   public LeaveTrackerModule leavetracker = LeaveTrackerModule.INSTANCE;
   public EcOpenModule ecopen = EcOpenModule.INSTANCE;
   public AutoBuyModule autoBuy = AutoBuyModule.INSTANCE;
   public AutoDuelModule autoDuel = AutoDuelModule.INSTANCE;
   public AutoEatModule autoEat = AutoEatModule.INSTANCE;
   public AutoLeaveModule autoLeave = AutoLeaveModule.INSTANCE;
   public AutoExplosionModule autoExplosion = AutoExplosionModule.INSTANCE;
   public AutoJoinModule autoJoin = AutoJoinModule.INSTANCE;
   public AutoPvPModule autoPvp = AutoPvPModule.INSTANCE;
   public NameProtectModule nameProtect = NameProtectModule.INSTANCE;
   public AutoJumpModule autoJump = AutoJumpModule.INSTANCE;
   public ChestStealerModule chestStealer = ChestStealerModule.INSTANCE;
   public ClientSoundsModule clientSounds = ClientSoundsModule.INSTANCE;
   public AutoToolModule autoTool = AutoToolModule.INSTANCE;
   public AutoTotemModule autoTotem = AutoTotemModule.INSTANCE;
   public AutoTrapModule autoTrap = AutoTrapModule.INSTANCE;
   public BlockOverlayModule blockOverlay = BlockOverlayModule.INSTANCE;
   public ClickPearlModule clickPearl = ClickPearlModule.INSTANCE;
   public CosmeticsModule cosmetics = CosmeticsModule.INSTANCE;
   public CubesModule cubes = CubesModule.INSTANCE;
   public KillEffectModule killEffect = KillEffectModule.INSTANCE;
   public BlockESPModule blockesp = BlockESPModule.INSTANCE;
   public ElytraBoostModule elytraBoost = ElytraBoostModule.INSTANCE;
   public ElytraMotionModule elytraMotion = ElytraMotionModule.INSTANCE;
   public ElytraSwapModule elytraSwap = ElytraSwapModule.INSTANCE;
   public ElytraSampleModule elytraTarget = ElytraSampleModule.INSTANCE;
   public ElytraResolverModule elytraresolver = ElytraResolverModule.INSTANCE;
   public EntityESPModule entityESP = EntityESPModule.INSTANCE;
   public FlightModule flight = FlightModule.INSTANCE;
   public FreeCamModule freeCam = FreeCamModule.INSTANCE;
   public FullBrightModule fullBright = FullBrightModule.INSTANCE;
   public NoFallModule grimNoFall = NoFallModule.INSTANCE;
   public GrimGlideModule grimGlide = GrimGlideModule.INSTANCE;
   public HighJumpModule highJump = HighJumpModule.INSTANCE;
   public HelpMessageModule helpMessage = HelpMessageModule.INSTANCE;
   public HitBubblesModule hitBubbles = HitBubblesModule.INSTANCE;
   public HudInterfaceModule interfaceModule = HudInterfaceModule.INSTANCE;
   public CinematicCameraModule interpolateF5 = CinematicCameraModule.INSTANCE;
   public InventoryWalkModule inventoryWalk = InventoryWalkModule.INSTANCE;
   public ItemAimModule itemAim = ItemAimModule.INSTANCE;
   public ItemScrollerModule itemScroller = ItemScrollerModule.INSTANCE;
   public ItemReleaseModule itemRelease = ItemReleaseModule.INSTANCE;
   public JumpCircleModule jumpCircle = JumpCircleModule.INSTANCE;
   public KTLeaveModule kTLeave = KTLeaveModule.INSTANCE;
   public LineGlyphesModule lineGlyphes = LineGlyphesModule.INSTANCE;
   public LockSlotModule lockSlot = LockSlotModule.INSTANCE;
   public NoClipModule noClip = NoClipModule.INSTANCE;
   public NoJumpDelayModule noJumpDelay = NoJumpDelayModule.INSTANCE;
   public NukerModule nuker = NukerModule.INSTANCE;
   public NoPushModule noPush = NoPushModule.INSTANCE;
   public NoVelocityModule noVelocity = NoVelocityModule.INSTANCE;
   public NoVignetteModule noVignette = NoVignetteModule.INSTANCE;
   public NoControllerWebModule noControllerWeb = NoControllerWebModule.INSTANCE;
   public NoWebModule noWeb = NoWebModule.INSTANCE;
   public PacketCriticalsModule packetCriticals = PacketCriticalsModule.INSTANCE;
   public ParticlesModule particle = ParticlesModule.INSTANCE;
   public PlayerFakeLagsModule playerFakeLags = PlayerFakeLagsModule.INSTANCE;
   public ProjectileModule projectile = ProjectileModule.INSTANCE;
   public FireworkESPModule fireworkESP = FireworkESPModule.INSTANCE;
   public FastBreakModule fastBreak = FastBreakModule.INSTANCE;
   public FastExpModule fastExp = FastExpModule.INSTANCE;
   public RemovalsModule removals = RemovalsModule.INSTANCE;
   public RPSpooferModule rPSpoofer = RPSpooferModule.INSTANCE;
   public SeeInvisiblesModule seeInvisibles = SeeInvisiblesModule.INSTANCE;
   public ShaderEspModule shaderEsp = ShaderEspModule.INSTANCE;
   public ShaderHandsModule shaderHands = ShaderHandsModule.INSTANCE;
   public SatelliteModule sattelite = SatelliteModule.INSTANCE;
   public ShulkerPreviewModule shulkerPreview = ShulkerPreviewModule.INSTANCE;
   public SonarModule sonar = SonarModule.INSTANCE;
   public SpeedModule speed = SpeedModule.INSTANCE;
   public SprintModule sprint = SprintModule.INSTANCE;
   public SwingAnimationsModule swingAnimations = SwingAnimationsModule.INSTANCE;
   public TargetESPModule targetESP = TargetESPModule.INSTANCE;
   public TargetPearlModule targetPearl = TargetPearlModule.INSTANCE;
   public TimerModule timer = TimerModule.INSTANCE;
   public TotemPopModule totemAngel = TotemPopModule.INSTANCE;
   public TpBackModule tpBack = TpBackModule.INSTANCE;
   public TpsSyncModule tpsSync = TpsSyncModule.INSTANCE;
   public TPLootModule tpLoot = TPLootModule.INSTANCE;
   public ViewModelModule viewModel = ViewModelModule.INSTANCE;
   public CustomWorldModule worldTweaks = CustomWorldModule.INSTANCE;
   public XCarryModule xCarry = XCarryModule.INSTANCE;
   public AutoSwapModule autoSwap = AutoSwapModule.INSTANCE;
   public AimBotModule aimBot = AimBotModule.INSTANCE;
   public TriggerBotModule triggerBot = TriggerBotModule.INSTANCE;
   public NoSlowModule noSlow = NoSlowModule.INSTANCE;
   public ScoreboardHPModule scoreboardHP = ScoreboardHPModule.INSTANCE;
   public StepModule step = StepModule.INSTANCE;
   public ServerHelperModule serverHelper = ServerHelperModule.INSTANCE;
   public SpiderModule spider = SpiderModule.INSTANCE;
   public LootTrackerModule lootTracker = LootTrackerModule.INSTANCE;
   public HitMarkerModule hitMarker = HitMarkerModule.INSTANCE;
   public TrailsModule trails = TrailsModule.INSTANCE;
   public TrajectoriesModule trajectories = TrajectoriesModule.INSTANCE;
}
