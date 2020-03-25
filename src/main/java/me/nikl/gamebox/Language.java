package me.nikl.gamebox;

import me.nikl.gamebox.module.GameBoxGame;
import me.nikl.gamebox.utility.ConfigManager;
import me.nikl.gamebox.utility.FileUtility;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nikl on 24.10.17.
 * <p>
 * Super class for all Language classes.
 * Holds the file configurations and some basic messages.
 * <p>
 * Provides methods to load messages from the files.
 */
public abstract class Language {
  public String PREFIX = "[" + ChatColor.DARK_AQUA + "GameBox" + ChatColor.RESET + "]";
  public String NAME = ChatColor.DARK_AQUA + "GameBox" + ChatColor.RESET;
  public String PLAIN_PREFIX = ChatColor.stripColor(PREFIX);
  public String PLAIN_NAME = ChatColor.stripColor(NAME);
  public String DEFAULT_NAME, DEFAULT_PLAIN_NAME;
  protected GameBox plugin;
  protected GameBoxGame module;
  protected File languageFile;
  protected FileConfiguration defaultLanguage;
  protected FileConfiguration language;

  public Language(GameBox plugin, GameBoxGame module) {
    this.plugin = plugin;
    this.module = module;
    getLangFile(ConfigManager.getConfig(module));

    PREFIX = getString("prefix");
    NAME = getString("name");
    PLAIN_PREFIX = ChatColor.stripColor(PREFIX);
    PLAIN_NAME = ChatColor.stripColor(NAME);
    // default is the value assigned to unknown games in bStats
    DEFAULT_NAME = ChatColor.translateAlternateColorCodes('&'
            , defaultLanguage.getString("name", "Other (custom game)"));
    DEFAULT_PLAIN_NAME = ChatColor.stripColor(DEFAULT_NAME);
    loadMessages();
  }

  public Language(GameBox plugin, String moduleID) {
    this(plugin, plugin.getGameRegistry().getModule(moduleID));
  }

  /**
   * Load all messages from the language file
   */
  protected abstract void loadMessages();

  /**
   * Try loading the language file specified in the
   * passed file configuration.
   * <p>
   * The required set option is 'langFile'. Possible options
   * are:
   * 'default'/'default.yml': loads the english language file from inside the jar
   * 'lang_xx.yml': will try to load the given file inside the namespaces language folder
   *
   * @param config configuration of the module
   */
  protected void getLangFile(FileConfiguration config) {
    String moduleID = module.getGameId();
    // load default language
    String defaultLangName = moduleID.equals(GameBox.MODULE_GAMEBOX) ? "language/lang_en.yml" : "language/" + module.getGameId() + "/lang_en.yml";
    defaultLanguage = YamlConfiguration.loadConfiguration(
            new InputStreamReader(module.getJarFile() == null
                    ? plugin.getResource(defaultLangName)
                    : FileUtility.getResource(module.getJarFile(), defaultLangName)
                    , StandardCharsets.UTF_8));
    String fileName = config.getString("langFile");
    if (fileName != null && (fileName.equalsIgnoreCase("default") || fileName.equalsIgnoreCase("default.yml"))) {
      language = defaultLanguage;
      return;
    }
    String configPath = moduleID.equals(GameBox.MODULE_GAMEBOX) ? "'config.yml'" : "'games" + "/" + moduleID + "/config.yml'";
    if (fileName == null || !fileName.endsWith(".yml")) {
      plugin.getLogger().warning("Language file for " + moduleID + " is not specified or not valid.");
      plugin.getLogger().warning("Did you forget to give the file ending '.yml'?");
      plugin.getLogger().warning("Should be set in " + configPath + " as value of 'langFile'");
      plugin.getLogger().warning("Falling back to the default file...");
      language = defaultLanguage;
      return;
    }
    languageFile = moduleID.equals(GameBox.MODULE_GAMEBOX) ?
            new File(plugin.getDataFolder().toString() + File.separatorChar + "language" + File.separatorChar
                    + fileName)
            :
            new File(plugin.getDataFolder().toString() + File.separatorChar + "language" + File.separatorChar
                    + moduleID + File.separatorChar + fileName);
    if (!languageFile.exists()) {
      plugin.getLogger().warning("The in " + configPath + " as 'langFile' configured file '" + fileName + "' does not exist!");
      plugin.getLogger().warning("Falling back to the default file...");
      language = defaultLanguage;
      return;
    }
    // File exists
    try {
      language = YamlConfiguration
              .loadConfiguration(new InputStreamReader(new FileInputStream(languageFile)
                      , StandardCharsets.UTF_8));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      language = defaultLanguage;
    }
  }


  /**
   * Find all string messages that are missing in the language file.
   * <p>
   * This method compares all message keys that hold a String in the default english
   * file with all set keys in the used language file. All missing keys are
   * collected and returned.
   *
   * @return list of all missing keys (can be empty list)
   */
  public List<String> findMissingStringMessages() {
    List<String> toReturn = new ArrayList<>();
    if (defaultLanguage.equals(language)) return toReturn;
    for (String key : defaultLanguage.getKeys(true)) {
      if (defaultLanguage.isString(key)) {
        if (!language.isString(key)) {
          // there is a message missing
          toReturn.add(key);
        }
      }
    }
    return toReturn;
  }

  /**
   * Find all string messages that are missing in the language file.
   * <p>
   * This method compares all message keys that hold a list in the default english
   * file with all set keys in the used language file. All missing keys are
   * collected and returned.
   *
   * @return list of all missing keys (can be empty list)
   */
  public List<String> findMissingListMessages() {
    List<String> toReturn = new ArrayList<>();
    if (defaultLanguage.equals(language)) return toReturn;
    for (String key : defaultLanguage.getKeys(true)) {
      if (defaultLanguage.isList(key)) {
        if (!language.isList(key)) {
          // there is a list missing
          toReturn.add(key);
        }
      }
    }
    return toReturn;
  }


  /**
   * Load list messages from the language file
   * <p>
   * If the requested path is not valid for the chosen
   * language file the corresponding list from the default
   * file is returned.
   * ChatColor can be translated here.
   *
   * @param path  path to the message
   * @param color if set, color the loaded message
   * @return message
   */
  protected List<String> getStringList(String path, boolean color) {
    List<String> toReturn;
    // load from default file if path is not valid
    if (!language.isList(path)) {
      toReturn = defaultLanguage.getStringList(path);
      if (color) {
        for (int i = 0; i < toReturn.size(); i++) {
          toReturn.set(i, ChatColor.translateAlternateColorCodes('&', toReturn.get(i)));
        }
      }
      return toReturn;
    }
    // load from language file
    toReturn = language.getStringList(path);
    if (color) {
      for (int i = 0; i < toReturn.size(); i++) {
        toReturn.set(i, ChatColor.translateAlternateColorCodes('&', toReturn.get(i)));
      }
    }
    return toReturn;
  }

  protected List<String> getStringList(String path) {
    return getStringList(path, true);
  }

  /**
   * Get a message from the language file
   * <p>
   * If the requested path is not valid for the
   * configured language file the corresponding
   * message from the default file is returned.
   * ChatColor is translated when reading the message.
   *
   * @param path  path to the message
   * @param color if set, color the loaded message
   * @return message
   */
  protected String getString(String path, boolean color) {
    String toReturn;
    if (!language.isString(path)) {
      toReturn = defaultLanguage.getString(path);
      if (toReturn == null)
        throw new IllegalArgumentException("The language key '" + path + "' is not a valid string!");
      if (color) {
        return ChatColor.translateAlternateColorCodes('&', defaultLanguage.getString(path));
      }
      return toReturn;
    }
    toReturn = language.getString(path);
    if (!color) return toReturn;
    return ChatColor.translateAlternateColorCodes('&', toReturn);
  }

  protected String getString(String path) {
    return getString(path, true);
  }
}
