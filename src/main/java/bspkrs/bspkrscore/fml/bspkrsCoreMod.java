package bspkrs.bspkrscore.fml;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.config.Configuration;
import bspkrs.util.CommonUtils;
import bspkrs.util.Const;
import bspkrs.util.ModVersionChecker;
import bspkrs.util.UniqueNameListGenerator;
import cpw.mods.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.Metadata;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Mod(modid = Reference.MODID, name = Reference.NAME, version = "@MOD_VERSION@", useMetadata = true, guiFactory = Reference.GUI_FACTORY)
public class bspkrsCoreMod
{
    // config stuff
    private final boolean       allowUpdateCheckDefault          = true;
    public boolean              allowUpdateCheck                 = allowUpdateCheckDefault;
    private final boolean       allowDebugOutputDefault          = false;
    public boolean              allowDebugOutput                 = allowDebugOutputDefault;
    private final int           updateTimeoutMillisecondsDefault = 3000;
    public int                  updateTimeoutMilliseconds        = updateTimeoutMillisecondsDefault;
    private final boolean       generateUniqueNamesFileDefault   = true;
    public boolean              generateUniqueNamesFile          = generateUniqueNamesFileDefault;
    private final boolean       showMainMenuMobsDefault          = true;
    public boolean              showMainMenuMobs                 = showMainMenuMobsDefault;
    
    // Example config stuff
    private final boolean[]     fixedBooleanListDefault          = new boolean[] { true, true, true, false, false, false };
    public boolean[]            fixedBooleanList                 = Arrays.copyOf(fixedBooleanListDefault, fixedBooleanListDefault.length);
    private final String[]      variablePatternStringListDefault = new String[] { "minecraft:stone_pickaxe,1,0", "minecraft:stone_shovel,1,0",
                                                                 "minecraft:stone_sword,1,0,{RepairCost:2,display:{Name:\"Rapier\",},}",
                                                                 "minecraft:stone_axe,1,0", "minecraft:apple,16,0", "minecraft:torch,16,0",
                                                                 "minecraft:enchanted_book,1,0,{StoredEnchantments:[0:{id:3s,lvl:4s,},],}",
                                                                 "minecraft:enchanted_book,1,0,{StoredEnchantments:[0:{id:51s,lvl:1s,},],}",
                                                                 "minecraft:anvil,1,0" };
    private final Pattern       variablePatternStringListPattern = Pattern.compile("[A-Za-z]+:[A-Za-z_]+,[0-9]+,[0-9]+($|,\\{.+?\\}$)");
    public String[]             variablePatternStringList        = Arrays.copyOf(variablePatternStringListDefault, variablePatternStringListDefault.length);
    private final String        regularStringDefault             = "This is a regular string, anything goes.";
    public String               regularString                    = regularStringDefault;
    private final String        patternStringDefault             = "Only, comma, separated, words, can, be, entered, in, this, box";
    public String               patternString                    = patternStringDefault;
    private final Pattern       patternStringPattern             = Pattern.compile("([A-Za-z]+((,){1}( )*|$))+?");
    private final String        selectStringDefault              = "This";
    public String               selectString                     = selectStringDefault;
    private final String[]      selectStringValues               = new String[] { "This", "property", "cycles", "through", "a", "list", "of", "valid", "choices." };
    private final int           unboundedIntegerDefault          = 25;
    public int                  unboundedInteger                 = unboundedIntegerDefault;
    private final int           boundedIntegerDefault            = 100;
    public int                  boundedInteger                   = boundedIntegerDefault;
    private final float         unboundedFloatDefault            = 25.0F;
    public float                unboundedFloat                   = unboundedFloatDefault;
    private final float         boundedFloatDefault              = 100.0F;
    public float                boundedFloat                     = boundedFloatDefault;
    private final String        chatColorPickerDefault           = "c";
    public String               chatColorPicker                  = chatColorPickerDefault;
    private final String[]      chatColorPickerValues            = new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f" };
    
    @Metadata(value = Reference.MODID)
    public static ModMetadata   metadata;
    
    @Instance(value = Reference.MODID)
    public static bspkrsCoreMod instance;
    
    @SidedProxy(clientSide = Reference.PROXY_CLIENT, serverSide = Reference.PROXY_COMMON)
    public static CommonProxy   proxy;
    
    protected ModVersionChecker versionChecker;
    protected final String      versionURL                       = Const.VERSION_URL + "/Minecraft/" + Const.MCVERSION + "/bspkrsCore.version";
    protected final String      mcfTopic                         = "http://www.minecraftforum.net/topic/1114612-";
    
    @SideOnly(Side.CLIENT)
    protected BSCClientTicker   ticker;
    private boolean             isCommandRegistered;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        metadata = event.getModMetadata();
        
        File file = event.getSuggestedConfigurationFile();
        
        if (!CommonUtils.isObfuscatedEnv())
        { // debug settings for deobfuscated execution
          //            if (file.exists())
          //                file.delete();
        }
        
        Reference.config = new Configuration(file);
        
        syncConfig();
    }
    
    public void syncConfig()
    {
        String ctgyGen = Configuration.CATEGORY_GENERAL;
        Reference.config.load();
        
        Reference.config.setCategoryComment(ctgyGen, "ATTENTION: Editing this file manually is no longer necessary. \n" +
                "On the Mods list screen select the entry for bspkrsCore, then click the Config button to modify these settings.");
        
        List<String> orderedKeys = new ArrayList<String>(ConfigElement.values().length);
        
        allowUpdateCheck = Reference.config.getBoolean(ConfigElement.ALLOW_UPDATE_CHECK.key(), ctgyGen, allowUpdateCheckDefault,
                ConfigElement.ALLOW_UPDATE_CHECK.desc(), ConfigElement.ALLOW_UPDATE_CHECK.languageKey());
        orderedKeys.add(ConfigElement.ALLOW_UPDATE_CHECK.key());
        updateTimeoutMilliseconds = Reference.config.getInt(ConfigElement.UPDATE_TIMEOUT_MILLISECONDS.key(), ctgyGen, updateTimeoutMillisecondsDefault,
                100, 30000, ConfigElement.UPDATE_TIMEOUT_MILLISECONDS.desc(), ConfigElement.UPDATE_TIMEOUT_MILLISECONDS.languageKey());
        orderedKeys.add(ConfigElement.UPDATE_TIMEOUT_MILLISECONDS.key());
        allowDebugOutput = Reference.config.getBoolean(ConfigElement.ALLOW_DEBUG_OUTPUT.key(), ctgyGen, allowDebugOutput,
                ConfigElement.ALLOW_DEBUG_OUTPUT.desc(), ConfigElement.ALLOW_DEBUG_OUTPUT.languageKey());
        orderedKeys.add(ConfigElement.ALLOW_DEBUG_OUTPUT.key());
        generateUniqueNamesFile = Reference.config.getBoolean(ConfigElement.GENERATE_UNIQUE_NAMES_FILE.key(), ctgyGen, generateUniqueNamesFileDefault,
                ConfigElement.GENERATE_UNIQUE_NAMES_FILE.desc(), ConfigElement.GENERATE_UNIQUE_NAMES_FILE.languageKey());
        orderedKeys.add(ConfigElement.GENERATE_UNIQUE_NAMES_FILE.key());
        showMainMenuMobs = Reference.config.getBoolean(ConfigElement.SHOW_MAIN_MENU_MOBS.key(), ctgyGen, showMainMenuMobsDefault,
                ConfigElement.SHOW_MAIN_MENU_MOBS.desc(), ConfigElement.SHOW_MAIN_MENU_MOBS.languageKey());
        orderedKeys.add(ConfigElement.SHOW_MAIN_MENU_MOBS.key());
        
        Reference.config.setCategoryPropertyOrder(ctgyGen, orderedKeys);
        
        if (Reference.config.hasCategory(ctgyGen + ".example_properties"))
            Reference.config.removeCategory(Reference.config.getCategory(ctgyGen + ".example_properties"));
        
        Reference.config.save();
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        if (allowUpdateCheck)
        {
            versionChecker = new ModVersionChecker(Reference.MODID, metadata.version, versionURL, mcfTopic);
            versionChecker.checkVersionWithLogging();
        }
        
        if (event.getSide().isClient())
        {
            FMLCommonHandler.instance().bus().register(new NetworkHandler());
            try
            {
                ClientCommandHandler.instance.registerCommand(new CommandBS());
                isCommandRegistered = true;
            }
            catch (Throwable e)
            {
                isCommandRegistered = false;
            }
        }
        
        FMLCommonHandler.instance().bus().register(instance);
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        if (generateUniqueNamesFile)
            UniqueNameListGenerator.instance().run();
        
        proxy.registerMainMenuTickHandler();
    }
    
    @EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
        if (!isCommandRegistered)
            event.registerServerCommand(new CommandBS());
    }
    
    @SubscribeEvent
    public void onConfigChanged(OnConfigChangedEvent event)
    {
        if (event.modID.equals(Reference.MODID))
        {
            Reference.config.save();
            syncConfig();
        }
    }
}
