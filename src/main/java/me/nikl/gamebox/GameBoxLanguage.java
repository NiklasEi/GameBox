package me.nikl.gamebox;

import me.nikl.gamebox.utility.NumberUtility;
import org.bukkit.ChatColor;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * @author Niklas Eicker
 * <p>
 * This class loades and saves all messages for GameBox
 * Other modules like games and addons have their own Langauge classes
 */
public class GameBoxLanguage extends Language {
  public SimpleDateFormat dateFormat;

  public String CMD_NO_PERM, CMD_ONLY_PLAYER, CMD_RELOADED, CMD_DISABLED_WORLD, CMD_TOKEN_INFO, CMD_TOOK_TOKEN,
          CMD_NOT_ENOUGH_TOKEN, CMD_GAVE_TOKEN, CMD_SET_TOKEN, RELOAD_SUCCESS, RELOAD_FAIL, CMD_OWN_TOKEN_INFO,
          CMD_MODULES_INVALID_SEM_VERSION, CMD_MODULES_VERSION_NOT_FOUND, CMD_CANNOT_CONNECT_TO_MODULES_CLOUD,
          CMD_CLOUD_MODULE_NOT_FOUND, CMD_MODULES_ALREADY_INSTALLED, CMD_MODULES_NO_UPDATE_AVAILABLE,
          CMD_MODULES_NOT_INSTALLED, CMD_MODULES_INSTALLING_LATEST_VERSION, CMD_MODULES_LIST_HEADER,
          CMD_MODULES_LIST_HEADER_SECOND, CMD_MODULES_LIST_ENTRY, CMD_MODULES_LIST_FOOTER, CMD_MODULES_REMOVE_SUCCESS,
          CMD_MODULES_INSTALL_SUCCESS, CMD_SETTINGS_GAME_ENABLE_SUCCESS, CMD_SETTINGS_GAME_ENABLE_FAIL,
          CMD_SETTINGS_GAME_DISABLE_SUCCESS, CMD_SETTINGS_GAME_DISABLE_FAIL, CMD_SETTINGS_GAME_INVALID_SETTING,
          CMD_MODULES_DEPENDENCY_NOT_FULFILLED;
  public List<String> CMD_HELP, CMD_WRONG_USAGE, CMD_INFO_HEADER, CMD_INFO_PER_GAME, CMD_INFO_FOOTER;

  public String BUTTON_EXIT, BUTTON_TO_MAIN_MENU, BUTTON_TO_GAME_MENU, BUTTON_TOKENS, BUTTON_FORWARD, BUTTON_BACK,
          BUTTON_SOUND_ON_NAME, BUTTON_SOUND_OFF_NAME, BUTTON_INVITE_BUTTON_NAME, BUTTON_INVITE_SKULL_NAME,
          BUTTON_MODULES_GUI_NAME;
  public List<String> BUTTON_MAIN_MENU_INFO, BUTTON_SOUND_ON_LORE, BUTTON_SOUND_OFF_LORE, BUTTON_INVITE_BUTTON_LORE,
          BUTTON_INVITE_SKULL_LORE, BUTTON_MODULES_GUI_LORE;

  public String TITLE_MAIN_GUI, TITLE_GAME_GUI, TITLE_NO_PERM, TITLE_NOT_ENOUGH_MONEY,
          TITLE_OTHER_PLAYER_NOT_ENOUGH_MONEY, TITLE_ALREADY_IN_ANOTHER_GAME,
          TITLE_ERROR = ChatColor.RED + "              Error", TITLE_NOT_ENOUGH_TOKEN,
          TITLE_OTHER_PLAYER_IN_BLOCKED_WORLD, TITLE_MODULES_PAGE, TITLE_MODULE_DETAILS_PAGE,
          TITLE_MODULE_DETAILS_PAGE_LOADING;

  public String SHOP_TITLE_BOUGHT_SUCCESSFULLY, SHOP_TITLE_INVENTORY_FULL, SHOP_TITLE_MAIN_SHOP, SHOP_TITLE_PAGE_SHOP,
          SHOP_TITLE_NOT_ENOUGH_TOKEN, SHOP_TITLE_NOT_ENOUGH_MONEY, SHOP_FREE, SHOP_MONEY, SHOP_TOKEN, SHOP_IS_CLOSED,
          SHOP_TITLE_REQUIREMENT_NOT_FULFILLED;

  public String TITLE_MODULE_NOT_INSTALLED, TITLE_MODULE_REMOVED, TITLE_MODULE_INSTALLED, TITLE_MODULE_UPDATED;

  public String INPUT_START_MESSAGE, INPUT_TIME_RAN_OUT, INVITATION_SUCCESSFUL, INVITATION_ALREADY_THERE,
          INVITATION_NOT_VALID_PLAYER_NAME, INVITATION_NOT_ONLINE, INPUT_CLOSED, INVITATION_NOT_YOURSELF,
          INVITATION_PRE_TEXT, INVITATION_PRE_COLOR, INVITATION_CLICK_TEXT, INVITATION_CLICK_COLOR,
          INVITATION_HOVER_TEXT, INVITATION_HOVER_COLOR, INVITATION_AFTER_TEXT, INVITATION_AFTER_COLOR,
          INVITATION_OTHER_IN_DISABLED_WORLD;

  public String JSON_PREFIX_PRE_TEXT, JSON_PREFIX_PRE_COLOR, JSON_PREFIX_TEXT, JSON_PREFIX_COLOR,
          JSON_PREFIX_AFTER_TEXT, JSON_PREFIX_AFTER_COLOR;
  public List<String> INPUT_HELP_MESSAGE;
  public String WON_TOKEN;
  public List<String> INVITE_MESSAGE;
  public String INVITE_TITLE_MESSAGE;
  public String INVITE_SUBTITLE_MESSAGE;
  public String INVITE_ACTIONBAR_MESSAGE;
  public String UNKNOWN_SKULL_NAME, PLAYER_SKULL_NAME;
  public List<String> UNKNOWN_SKULL_LORE;
  public List<String> MODULE_INSTALLED_BUTTON_LORE, MODULE_BUTTON_LORE, MODULE_VERSION_BUTTON_LORE,
          MODULE_VERSION_INSTALLED_BUTTON_LORE, MODULE_VERSION_UPDATE_BUTTON_LORE, MODULE_VERSION_OLDER_BUTTON_LORE,
          MODULE_PRIVATE_BUTTON_LORE, MODULE_UPDATE_IS_MAJOR;
  public String MODULE_INSTALLED_BUTTON_NAME, MODULE_BUTTON_NAME, MODULE_VERSION_BUTTON_NAME,
          MODULE_VERSION_INSTALLED_BUTTON_NAME, MODULE_VERSION_UPDATE_BUTTON_NAME, MODULE_VERSION_OLDER_BUTTON_NAME,
          MODULE_VERSION_UNKNOWN, MODULE_PRIVATE_BUTTON_NAME, MODULES_AUTO_UPDATING_INFO, MODULE_AUTO_UPDATE,
          MODULE_AUTO_UPDATE_NOT_IN_CLOUD, MODULE_AUTO_UPDATE_FOOTER;

  GameBoxLanguage(GameBox plugin) {
    super(plugin, GameBox.MODULE_GAMEBOX);
  }

  @Override
  protected void loadMessages() {
    PREFIX = getString("prefix");
    PLAIN_PREFIX = ChatColor.stripColor(PREFIX);
    NAME = getString("name");

    dateFormat = new SimpleDateFormat(getString("dateFormat", false));

    loadJsonRelatedMessages();
    loadClickableInvitationMessages();
    getCommandMessages();
    getInvTitles();
    getButtons();
    getOthers();
    getShop();
    getTopList();
    getModuleButtons();
    loadHighNumberNames();
  }

  private void getModuleButtons() {
    this.MODULE_VERSION_UNKNOWN = getString("moduleGui.unknownModuleVersion");
    this.MODULE_INSTALLED_BUTTON_LORE = getStringList("moduleGui.moduleButtons.installed.lore");
    this.MODULE_INSTALLED_BUTTON_NAME = getString("moduleGui.moduleButtons.installed.displayName");
    this.MODULE_PRIVATE_BUTTON_LORE = getStringList("moduleGui.moduleButtons.private.lore");
    this.MODULE_PRIVATE_BUTTON_NAME = getString("moduleGui.moduleButtons.private.displayName");
    this.MODULE_BUTTON_LORE = getStringList("moduleGui.moduleButtons.notInstalled.lore");
    this.MODULE_BUTTON_NAME = getString("moduleGui.moduleButtons.notInstalled.displayName");
    this.MODULE_VERSION_INSTALLED_BUTTON_LORE = getStringList("moduleGui.moduleVersionButtons.installedVersion.lore");
    this.MODULE_VERSION_INSTALLED_BUTTON_NAME = getString("moduleGui.moduleVersionButtons.installedVersion.displayName");
    this.MODULE_VERSION_BUTTON_LORE = getStringList("moduleGui.moduleVersionButtons.moduleNotInstalled.lore");
    this.MODULE_VERSION_BUTTON_NAME = getString("moduleGui.moduleVersionButtons.moduleNotInstalled.displayName");
    this.MODULE_VERSION_UPDATE_BUTTON_LORE = getStringList("moduleGui.moduleVersionButtons.updateOfInstalledVersion.lore");
    this.MODULE_VERSION_UPDATE_BUTTON_NAME = getString("moduleGui.moduleVersionButtons.updateOfInstalledVersion.displayName");
    this.MODULE_VERSION_OLDER_BUTTON_LORE = getStringList("moduleGui.moduleVersionButtons.olderVersionThanInstalled.lore");
    this.MODULE_VERSION_OLDER_BUTTON_NAME = getString("moduleGui.moduleVersionButtons.olderVersionThanInstalled.displayName");

    this.MODULES_AUTO_UPDATING_INFO = getString("modules.autoUpdating.info");
    this.MODULE_AUTO_UPDATE = getString("modules.autoUpdating.updatingModule");
    this.MODULE_AUTO_UPDATE_NOT_IN_CLOUD = getString("modules.autoUpdating.notInCloud");
    this.MODULE_AUTO_UPDATE_FOOTER = getString("modules.autoUpdating.footer");
    this.MODULE_UPDATE_IS_MAJOR = getStringList("modules.autoUpdating.updateIsMajor");
  }

  private void loadHighNumberNames() {
    if (!language.isConfigurationSection("highNumberNames"))
      NumberUtility.overwriteNames(defaultLanguage.getConfigurationSection("highNumberNames"));
    else NumberUtility.overwriteNames(language.getConfigurationSection("highNumberNames"));
    if (!language.isConfigurationSection("highNumberShortNames"))
      NumberUtility.overwriteShortnames(defaultLanguage.getConfigurationSection("highNumberShortNames"));
    else
      NumberUtility.overwriteShortnames(language.getConfigurationSection("highNumberShortNames"));
  }

  private void getTopList() {
    this.UNKNOWN_SKULL_NAME = getString("topList.unknownSkullName");
    this.UNKNOWN_SKULL_LORE = getStringList("topList.unknownSkullLore");
    this.PLAYER_SKULL_NAME = getString("topList.playerSkullName");
  }

  private void loadClickableInvitationMessages() {
    this.INVITATION_PRE_TEXT = getString("others.invitationClickMessage.preText");
    this.INVITATION_PRE_COLOR = getString("others.invitationClickMessage.preColor");
    this.INVITATION_CLICK_TEXT = getString("others.invitationClickMessage.clickText");
    this.INVITATION_CLICK_COLOR = getString("others.invitationClickMessage.clickColor");
    this.INVITATION_HOVER_TEXT = getString("others.invitationClickMessage.hoverText");
    this.INVITATION_HOVER_COLOR = getString("others.invitationClickMessage.hoverColor");
    this.INVITATION_AFTER_TEXT = getString("others.invitationClickMessage.afterText");
    this.INVITATION_AFTER_COLOR = getString("others.invitationClickMessage.afterColor");
  }

  private void loadJsonRelatedMessages() {
    this.JSON_PREFIX_PRE_TEXT = getString("jsonPrefix.preText");
    this.JSON_PREFIX_PRE_COLOR = getString("jsonPrefix.preColor");
    this.JSON_PREFIX_TEXT = getString("jsonPrefix.text");
    this.JSON_PREFIX_COLOR = getString("jsonPrefix.color");
    this.JSON_PREFIX_AFTER_TEXT = getString("jsonPrefix.afterText");
    this.JSON_PREFIX_AFTER_COLOR = getString("jsonPrefix.afterColor");
  }

  private void getOthers() {
    this.INPUT_START_MESSAGE = getString("others.playerInput.openingMessage");
    this.INPUT_TIME_RAN_OUT = getString("others.playerInput.timeRanOut");
    this.INPUT_HELP_MESSAGE = getStringList("others.playerInput.helpMessage");
    this.INVITATION_SUCCESSFUL = getString("others.playerInput.inputSuccessful");
    this.INVITATION_ALREADY_THERE = getString("others.playerInput.sameInvitation");
    this.INVITATION_NOT_VALID_PLAYER_NAME = getString("others.playerInput.notValidPlayerName");
    this.INVITATION_NOT_ONLINE = getString("others.playerInput.notOnline");
    this.INVITATION_OTHER_IN_DISABLED_WORLD = getString("others.playerInput.otherInDisabledWorld");
    this.INPUT_CLOSED = getString("others.playerInput.inputClosed");
    this.INVITATION_NOT_YOURSELF = getString("others.playerInput.notInviteYourself");
    this.WON_TOKEN = getString("others.wonToken");
    this.INVITE_MESSAGE = getStringList("others.invitation");
    this.INVITE_TITLE_MESSAGE = getString("others.invitationTitleMessage.title");
    this.INVITE_SUBTITLE_MESSAGE = getString("others.invitationTitleMessage.subTitle");
    this.INVITE_ACTIONBAR_MESSAGE = getString("others.invitationActionbarMessage");
  }

  private void getButtons() {
    this.BUTTON_EXIT = getString("mainButtons.exitButton");
    this.BUTTON_TO_MAIN_MENU = getString("mainButtons.toMainGUIButton");
    this.BUTTON_TO_GAME_MENU = getString("mainButtons.toGameGUIButton");
    this.BUTTON_TOKENS = getString("mainButtons.tokensButton");
    this.BUTTON_FORWARD = getString("mainButtons.forwardButton");
    this.BUTTON_BACK = getString("mainButtons.backwardButton");
    this.BUTTON_MAIN_MENU_INFO = getStringList("mainButtons.infoMainMenu");
    this.BUTTON_SOUND_ON_NAME = getString("mainButtons.soundToggle.onDisplayName");
    this.BUTTON_SOUND_OFF_NAME = getString("mainButtons.soundToggle.offDisplayName");
    this.BUTTON_SOUND_ON_LORE = getStringList("mainButtons.soundToggle.onLore");
    this.BUTTON_SOUND_OFF_LORE = getStringList("mainButtons.soundToggle.offLore");
    this.BUTTON_INVITE_BUTTON_NAME = getString("mainButtons.inviteButton.displayName");
    this.BUTTON_INVITE_BUTTON_LORE = getStringList("mainButtons.inviteButton.lore");
    this.BUTTON_INVITE_SKULL_NAME = getString("mainButtons.invitationSkull.displayName");
    this.BUTTON_INVITE_SKULL_LORE = getStringList("mainButtons.invitationSkull.lore");
    this.BUTTON_MODULES_GUI_NAME = getString("mainButtons.modulesGui.displayName");
    this.BUTTON_MODULES_GUI_LORE = getStringList("mainButtons.modulesGui.lore");
  }

  private void getInvTitles() {
    this.TITLE_MAIN_GUI = getString("inventoryTitles.mainGUI");
    this.TITLE_GAME_GUI = getString("inventoryTitles.gameGUIs");
    this.TITLE_NO_PERM = getString("inventoryTitles.noPermMessage");
    this.TITLE_NOT_ENOUGH_MONEY = getString("inventoryTitles.notEnoughMoney");
    this.TITLE_NOT_ENOUGH_TOKEN = getString("inventoryTitles.notEnoughTokens");
    this.TITLE_ALREADY_IN_ANOTHER_GAME = getString("inventoryTitles.alreadyInAnotherGame");
    this.TITLE_OTHER_PLAYER_NOT_ENOUGH_MONEY = getString("inventoryTitles.otherPlayerNotEnoughMoney");
    this.TITLE_OTHER_PLAYER_IN_BLOCKED_WORLD = getString("inventoryTitles.otherPlayerInBlockedWorld");
    this.TITLE_MODULES_PAGE = getString("inventoryTitles.modulesPage");
    this.TITLE_MODULE_DETAILS_PAGE = getString("inventoryTitles.moduleDetailsPage");
    this.TITLE_MODULE_DETAILS_PAGE_LOADING = getString("inventoryTitles.moduleDetailsPageLoading");

    // module gui title messages
    this.TITLE_MODULE_NOT_INSTALLED = getString("inventoryTitles.moduleGuiTitleMessages.moduleNotInstalled");
    this.TITLE_MODULE_REMOVED = getString("inventoryTitles.moduleGuiTitleMessages.moduleRemoved");
    this.TITLE_MODULE_INSTALLED = getString("inventoryTitles.moduleGuiTitleMessages.moduleInstalled");
    this.TITLE_MODULE_UPDATED = getString("inventoryTitles.moduleGuiTitleMessages.moduleUpdated");
  }

  private void getCommandMessages() {
    this.CMD_NO_PERM = getString("commandMessages.noPermission");
    this.CMD_DISABLED_WORLD = getString("commandMessages.inDisabledWorld");
    this.CMD_ONLY_PLAYER = getString("commandMessages.onlyAsPlayer");
    this.CMD_RELOADED = getString("commandMessages.pluginReloaded");
    this.RELOAD_FAIL = getString("commandMessages.reload.fail");
    this.RELOAD_SUCCESS = getString("commandMessages.reload.success");
    this.CMD_TOKEN_INFO = getString("commandMessages.tokenInfo");
    this.CMD_OWN_TOKEN_INFO = getString("commandMessages.ownTokenInfo");
    this.CMD_TOOK_TOKEN = getString("commandMessages.tookToken");
    this.CMD_GAVE_TOKEN = getString("commandMessages.gaveToken");
    this.CMD_SET_TOKEN = getString("commandMessages.setToken");
    this.CMD_NOT_ENOUGH_TOKEN = getString("commandMessages.notEnoughToken");
    this.CMD_HELP = getStringList("commandMessages.help");
    this.CMD_WRONG_USAGE = getStringList("commandMessages.wrongUsage");
    this.CMD_INFO_HEADER = getStringList("commandMessages.info.header");
    this.CMD_INFO_PER_GAME = getStringList("commandMessages.info.perGame");
    this.CMD_INFO_FOOTER = getStringList("commandMessages.info.footer");
    this.CMD_MODULES_INVALID_SEM_VERSION = getString("commandMessages.modules.invalidSemVersion");
    this.CMD_MODULES_VERSION_NOT_FOUND = getString("commandMessages.modules.versionNotFound");
    this.CMD_MODULES_ALREADY_INSTALLED = getString("commandMessages.modules.moduleAlreadyInstalled");
    this.CMD_MODULES_NO_UPDATE_AVAILABLE = getString("commandMessages.modules.noUpdateAvailable");
    this.CMD_MODULES_NOT_INSTALLED = getString("commandMessages.modules.moduleNotInstalled");
    this.CMD_MODULES_INSTALLING_LATEST_VERSION = getString("commandMessages.modules.installingLatestVersion");
    this.CMD_MODULES_LIST_HEADER = getString("commandMessages.modules.list.header");
    this.CMD_MODULES_LIST_HEADER_SECOND = getString("commandMessages.modules.list.header2");
    this.CMD_MODULES_LIST_ENTRY = getString("commandMessages.modules.list.entry");
    this.CMD_MODULES_LIST_FOOTER = getString("commandMessages.modules.list.footer");
    this.CMD_MODULES_INSTALL_SUCCESS = getString("commandMessages.modules.moduleInstalled");
    this.CMD_MODULES_DEPENDENCY_NOT_FULFILLED =  getString("commandMessages.modules.dependencyNotFulfilled");
    this.CMD_MODULES_REMOVE_SUCCESS = getString("commandMessages.modules.moduleRemoved");
    this.CMD_CANNOT_CONNECT_TO_MODULES_CLOUD = getString("commandMessages.cannotConnectToModulesCloud");
    this.CMD_CLOUD_MODULE_NOT_FOUND = getString("commandMessages.modules.notFoundInCloud");
    getSettingsCommandMessages();
  }

  private void getSettingsCommandMessages() {
    this.CMD_SETTINGS_GAME_ENABLE_SUCCESS = getString("commandMessages.settingsCommand.gameEnableSuccess");
    this.CMD_SETTINGS_GAME_ENABLE_FAIL = getString("commandMessages.settingsCommand.gameEnableFail");
    this.CMD_SETTINGS_GAME_DISABLE_SUCCESS = getString("commandMessages.settingsCommand.gameDisableSuccess");
    this.CMD_SETTINGS_GAME_DISABLE_FAIL = getString("commandMessages.settingsCommand.gameDisableFail");
    this.CMD_SETTINGS_GAME_INVALID_SETTING = getString("commandMessages.settingsCommand.gameInvalidSetting");
  }

  private void getShop() {
    this.SHOP_TITLE_MAIN_SHOP = getString("shop.mainShop");
    this.SHOP_TITLE_PAGE_SHOP = getString("shop.pageShop");
    this.SHOP_TITLE_INVENTORY_FULL = getString("shop.inventoryIsFull");
    this.SHOP_TITLE_REQUIREMENT_NOT_FULFILLED = getString("shop.requirementNotFulfilled");
    this.SHOP_TITLE_BOUGHT_SUCCESSFULLY = getString("shop.boughtSuccessful");
    this.SHOP_TITLE_NOT_ENOUGH_TOKEN = getString("shop.notEnoughTokens");
    this.SHOP_TITLE_NOT_ENOUGH_MONEY = getString("shop.notEnoughMoney");
    this.SHOP_IS_CLOSED = getString("shop.shopIsClosed");
    this.SHOP_FREE = getString("shop.freeItem");
    this.SHOP_MONEY = getString("shop.moneyItem");
    this.SHOP_TOKEN = getString("shop.tokenItem");
  }

  @Override
  public List<String> findMissingStringMessages() {
    List<String> toReturn = super.findMissingStringMessages();
    toReturn.removeIf(next -> next.contains("highNumberNames.") || next.contains("highNumberShortNames."));
    return toReturn;
  }
}
