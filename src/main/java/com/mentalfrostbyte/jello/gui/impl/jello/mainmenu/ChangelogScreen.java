package com.mentalfrostbyte.jello.gui.impl.jello.mainmenu;

import com.google.gson.JsonArray;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.gui.base.animations.Animation;
import com.mentalfrostbyte.jello.gui.combined.CustomGuiScreen;
import com.mentalfrostbyte.jello.gui.base.elements.impl.Change;
import com.mentalfrostbyte.jello.gui.impl.jello.buttons.ScrollableContentPanel;
import com.mentalfrostbyte.jello.gui.impl.jello.mainmenu.changelog.ChangelogParser;
import com.mentalfrostbyte.jello.util.client.render.theme.ClientColors;
import com.mentalfrostbyte.jello.util.system.math.SmoothInterpolator;
import com.mentalfrostbyte.jello.util.client.render.ResourceRegistry;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil2;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil;
import org.newdawn.slick.TrueTypeFont;

public class ChangelogScreen extends CustomGuiScreen {
    public Animation animation = new Animation(380, 200, Animation.Direction.BACKWARDS);
    public ScrollableContentPanel scrollPanel;
    private static JsonArray cachedChangelog;

    public ChangelogScreen(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6) {
        super(var1, var2, var3, var4, var5, var6);
        this.setListening(false);
        this.scrollPanel = new ScrollableContentPanel(this, "scroll", 100, 200, var5 - 200, var6 - 200);
        this.scrollPanel.setScrollable(true);
        this.showAlert(this.scrollPanel);
        new Thread(() -> this.method13490(this.getChangelog())).start();
    }

    public void method13490(JsonArray var1) {
        if (var1 != null) {
            this.getParent().runThisOnDimensionUpdate(new ChangelogParser(this, var1));
        }
    }

    @Override
    public void updatePanelDimensions(int newHeight, int newWidth) {
        super.updatePanelDimensions(newHeight, newWidth);
        if (this.scrollPanel != null) {
            if (this.isHovered() && this.isSelfVisible()) {
                for (CustomGuiScreen var9 : this.scrollPanel.getButtonList().getChildren()) {
                    Change var10 = (Change) var9;
                    var10.animation2.changeDirection(Animation.Direction.FORWARDS);
                    if ((double) var10.animation2.calcPercent() < 0.5) {
                        break;
                    }
                }
            } else {
                for (CustomGuiScreen var6 : this.scrollPanel.getButtonList().getChildren()) {
                    Change var7 = (Change) var6;
                    var7.animation2.changeDirection(Animation.Direction.BACKWARDS);
                }
            }
        }
    }

    @Override
    public void draw(float partialTicks) {
        this.animation.changeDirection(!this.isHovered() ? Animation.Direction.BACKWARDS : Animation.Direction.FORWARDS);
        partialTicks *= this.animation.calcPercent();

        float fadeFactor = SmoothInterpolator.interpolate(this.animation.calcPercent(), 0.17f, 1.0f, 0.51f, 1.0f);

        if (this.animation.getDirection() == Animation.Direction.BACKWARDS) {
            fadeFactor = 1.0f;
        }

        this.drawBackground((int) (150.0f * (1.0f - fadeFactor)));
        this.method13225();
        RenderUtil.drawString(ResourceRegistry.JelloLightFont36, 100.0F, 100.0F, "Changelog", RenderUtil2.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks));
        TrueTypeFont jelloLightFont25 = ResourceRegistry.JelloLightFont25;
        String versionText = "You're currently using Sigma " + Client.FULL_VERSION;
        RenderUtil.drawString(
                jelloLightFont25,
                100.0f, 150.0f,
                versionText,
                RenderUtil2.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.6f * partialTicks)
        );
        super.draw(partialTicks);
    }

    public JsonArray getChangelog() {
        if (cachedChangelog != null) {
            return cachedChangelog;
        } else {
            String jsonString = getChanges();
            try {
                cachedChangelog = JsonParser.parseString(jsonString).getAsJsonArray();
            } catch (JsonParseException e) {
                throw new RuntimeException("Invalid JSON format for changelog", e);
            }
            return cachedChangelog;
        }
    }

    private String getChanges() {
        return """
                [
                    {
                        "title": "5.1.0 (1.16.4) Update",
                        "changes": [
                            "Added Verus glide flight - by @alarmingly_good",
                            "Added Verus speeds - by @alarmingly_good",
                            "Added Vulcan AntiKB and Speed",
                            "Added Verus NoFall",
                            "Added Switch button to Jello",
                            "Added Cloud configs",
                            "Added MiniBlox bypasses",
                            "Fixed JelloPortal",
                            "Fixed many rotation flags",
                            "Fixed music player issues",
                            "Fixed visual bugs + render modules"
                        ]
                    },
                    {
                        "title": "5.0.0 Beta 15 (1.16.4) Update",
                        "changes": [
                            "Added AI FightBot module",
                            "Added JelloAIBot module",
                            "Added StaffRepealer for hypixel",
                            "Added back VClip module",
                            "Added HitSounds module (unfinished)",
                            "Added NoServerInfo module that hides scoreboard (broken)",
                            "Added Murderer module to detect the murderer in mm",
                            "Added Streaming module to hide yourself when streaming",
                            "Added JelloEdit for schematics",
                            "Added AutoClicker module",
                            "Added back Dumper module to dump server commands & plugins",
                            "Added back Skeleton ESP to ESP (broken)",
                            "Added back Minemen AntiKnockBack",
                            "Added back Cubecraft & Cubecraft2 fly",
                            "Added back Mineplex HighJump (got patched on b2)",
                            "Added back Viper Disabler",
                            "Added back VeltPvP Disabler",
                            "Added back Minemen Criticals",
                            "Added back NickNameDetector that detects custom nicks (untested)",
                            "Added back Legit WTap",
                            "Added back AGC Fly",
                            "Added back TargetHUD module (unfinished)",
                            "Added back Info HUD module that shows stuff about the player",
                            "Added back PacketDumper module",
                            "Added back DebugSpeed module to debug the player speed",
                            "Added back AutoMiner module"
                        ]
                    },
                    {
                        "title": "5.0.0 Beta 14 (1.16.4) Update",
                        "changes": [
                            "Added New Cubecraft Fly",
                            "Added Bypass mode to Hypixel Fly",
                            "Added New Hypixel BlockFly",
                            "Added Hypixel Packet Criticals",
                            "Added New bypassing Hypixel Fast Fly",
                            "Added AutoTool Inv option",
                            "Improved Cubecraft2 Fly (Up & Down for 1.9+ users)",
                            "Improved NCP Step Added vanilla phase",
                            "Improved spiders & phases",
                            "Renamed Cubecraft Tower to 'Vanilla' (no longer bypasses)",
                            "Fixed Nameprotect rendering",
                            "Fixed an Optifine bug which prevents blocks from rendering",
                            "Fixed Music Player",
                            "Fixed AltManager skins",
                            "Removed Cubecraft speed mode from BlockFly (no longer bypasses)",
                            "Removed Old Hypixel fastfly"
                        ]
                    },
                    {
                        "title": "5.0.0 Beta 13 (1.16.1) Update",
                        "changes": [
                            "Added Animals & Monsters option to KillAura",
                            "Added Aura option to AAC4 Criticals",
                            "Added Redesky longjump",
                            "Fixed bugs",
                            "Updated Redesky config"
                        ]
                    },
                    {
                        "title": "5.0.0 Beta 12 (1.16.1) Update",
                        "changes": [
                            "Fixed bugs",
                            "Fixed XRay",
                            "Fixed baninfo for minemen",
                            "Added Optifine",
                            "Added Gomme Spider Step",
                            "Added Auto Disable option to ClickTP",
                            "Added silent option to KillAura",
                            "Improved pingspoof disabler",
                            "Improved gommeSpeed",
                            "Improved Jump Spider",
                            "Improved KillAura (raytrace & reach)",
                            "Removed Shaders"
                        ]
                    },
                    {
                        "title": "5.0.0 Beta 10/11 (1.16.1) Update",
                        "changes": [
                            "Fixed Music Player",
                            "Fixed FakeForge",
                            "Fixed a Jello Portal Issue"
                        ]
                    },
                    {
                        "title": "5.0.0 Beta 9 (1.16.1) Update",
                        "changes": [
                            "Added the best exit button ever",
                            "Added speed option to BoatFly",
                            "Added an option to hide date & server from hypixel scoreboard in GamePlay",
                            "Added Hypixel2 NoFall",
                            "Added InvBypass for null & pingspoof disabler",
                            "Added LockView KillAura rotation",
                            "Added NCP Step Timer",
                            "Added Friend Accept in Hypixel GamePlay",
                            "Added TargetStrafe void option",
                            "Improved TargetStrafe",
                            "Improved Vanilla Fly Speed",
                            "Improved Hypixel speed",
                            "Improved Omegacraft Fly",
                            "Fixed Spartan Fly",
                            "Fixed Realms & Alt Manager compatibility",
                            "Fixed a Jello Portal crash issue"
                        ]
                    },
                    {
                        "title": "5.0.0 Beta 8 (1.16.1) Update",
                        "changes": [
                            "Updated to 1.16.1",
                            "Fixed bugs"
                        ]
                    },
                    {
                        "title": "5.0.0 Beta 7 (1.16) Update",
                        "changes": [
                            "Updated to 1.16",
                            "Removed Optifine",
                            "Added Ancient Debris to Xray",
                            "Fixed Music Player Bugs",
                            "Fixed Music Player Spectrum getting darker",
                            "Fixed Invisible block glitches"
                        ]
                    },
                    {
                        "title": "5.0.0 Beta 6 Update",
                        "changes": [
                            "Improved Client inner workings",
                            "Improved Jello Maps terrain scanning",
                            "Added FPSBooster",
                            "Added shader support",
                            "Added Basic Realms support",
                            "Added Legit Nofall (Suggested by u/Cweepaw on Reddit)",
                            "Added Vanilla ESP back",
                            "Added Elytra Equipping to AutoArmor",
                            "Added Hypixel High Longjump mode",
                            "Added New Classic/Jello Switch GUI",
                            "Added Weird 'Ninja' AutoBlock Animation",
                            "Fixed Coords not hiding in F3/F1",
                            "Fixed Music Player Skipping",
                            "Fixed Music Player Windows Issues",
                            "Fixed Hypixel Flies (Special thanks to Pepa_Pig58 and Carlos)",
                            "Fixed Hypixel AutoL",
                            "Fixed a Main Menu GUI memory leak",
                            "Fixed a Jello Portal memory leak",
                            "Fixed a Texture leak",
                            "Removed AGC Fly (Patched)",
                            "Updated Optifine"
                        ]
                    },
                    {
                        "title": "5.0.0 Beta 5 Update",
                        "changes": [
                            "Added Ninja & Random TargetStrafe",
                            "Added Cubecraft2 fly (up & down)",
                            "Added Egg to Nuker",
                            "Added Jartex Gameplay Mods",
                            "Added Pingspoof option to TP Disabler",
                            "Added FakeLag",
                            "Added Ghostly Disabler",
                            "Added Advanced AutoSoup",
                            "Added Spartan Fly Ground Spoof Option",
                            "Added Delay to Speed Autopot",
                            "Made PingSpoof go up to 10k Ping",
                            "Fixed KillAura Raytrace",
                            "Fixed Nametags Issues",
                            "Fixed Minor MusicPlayer Issues",
                            "Fixed ChangeLog Spacing",
                            "Improved Hypixel Gameplay",
                            "Improved ChestStealer Aura"
                        ]
                    },
                    {
                        "title": "5.0.0 Beta 4 Update",
                        "changes": [
                            "Added Mineplex Fly",
                            "Added Vanilla Fly Kick Bypass",
                            "Added Music Player Repeat Options",
                            "Added Rollback NoteBlockPlayer Music",
                            "Improved Hypixel Speed",
                            "Fixed Cubecraft Gameplay",
                            "Fixed Funcraft Gameplay",
                            "Fixed Hypixel BlockFly",
                            "Fixed a Pasting issue",
                            "Fixed Ban Info for spanish servers"
                        ]
                    },
                    {
                        "title": "5.0.0 Beta 3 Update",
                        "changes": [
                            "Fixed minimap memory leaks",
                            "Fixed Hypixel BlockFly (New mode)",
                            "Fixed Hypixel Fly",
                            "Fixed Hypixel HighJump",
                            "Fixed Hypixel LongJump with Nofall",
                            "Fixed Hypixel AntiVoid",
                            "Fixed Hypixel Speed without Auto Jump",
                            "Fixed sword managing for 1.8",
                            "Fixed Explosion crashes",
                            "Fixed some issues with the music player",
                            "Updated Optifine (pre13)",
                            "Fixed 1.8 movement",
                            "Fixed 1.9 - 1.12 swim mechanics",
                            "Added old AAC Speed (HiiveMC)"
                        ]
                    },
                    {
                        "title": "5.0.0 Beta 2 Update",
                        "changes": [
                            "Fixed Hypixel Nofall",
                            "Added Angle BowAimbot sort mode",
                            "Added fight option with autopot",
                            "Added blockfly slow speed",
                            "Added Discord RPC",
                            "Removed Mineplex Highjump (patched)",
                            "Removed agc autoblock (patched?)",
                            "Fixed explosions with antikb",
                            "Improved AntiVoid for hypixel",
                            "Optimisations..."
                        ]
                    },
                    {
                        "title": "5.0.0 Update",
                        "changes": [
                            "[Error] Cannot render changelog: More than 1000 changes are trying to be displayed at the same time. Are the developers insane ?"
                        ]
                    }
                ]""";
    }

}
