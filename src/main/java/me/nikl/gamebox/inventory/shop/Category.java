package me.nikl.gamebox.inventory.shop;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.data.GBPlayer;
import me.nikl.gamebox.inventory.ClickAction;
import me.nikl.gamebox.inventory.GuiManager;
import me.nikl.gamebox.inventory.button.Button;
import me.nikl.gamebox.inventory.button.ButtonFactory;
import me.nikl.gamebox.inventory.gui.AGui;
import me.nikl.gamebox.utility.ItemStackUtility;
import me.nikl.gamebox.utility.StringUtility;
import me.nikl.nmsutilities.NmsFactory;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Niklas Eicker
 */
public class Category {
  private Map<Integer, Page> pages;
  private String key;
  private ShopManager shopManager;
  private GuiManager guiManager;
  private GameBox plugin;
  private FileConfiguration shop;
  private Map<String, ShopItem> shopItems = new HashMap<>();
  private int slots = 27;
  private int itemsPerPage = 18;
  private int backSlot = 21;
  private int forSlot = 23;

  public Category(GameBox plugin, ShopManager shopManager, GuiManager guiManager, String key) {
    this.key = key;
    this.shopManager = shopManager;
    this.guiManager = guiManager;
    this.plugin = plugin;
    this.shop = shopManager.getShop();
    pages = new HashMap<>();
    loadShopItems();
  }

  private void loadShopItems() {
    ConfigurationSection pageSection = shop.getConfigurationSection("shop.categories." + key + ".items");
    if (pageSection == null) return;
    Map<Integer, Button> allItems = new HashMap<>();
    ItemStack itemStack;
    ItemMeta meta;
    List<String> lore = new ArrayList<>();
    int counter = 0;
    int token;
    int money;
    int amount;
    for (String itemKey : pageSection.getKeys(false)) {
      // if null no item will be given
      itemStack = ItemStackUtility.getItemStack(pageSection.getString(itemKey + ".materialData"));
      // load the prices of the item
      if (pageSection.isSet(itemKey + ".tokens") && pageSection.isInt(itemKey + ".tokens")) {
        token = pageSection.getInt(itemKey + ".tokens");
        // old key was 'token' (@version 1.1.0) support it
      } else if (pageSection.isSet(itemKey + ".token") && pageSection.isInt(itemKey + ".token")) {
        token = pageSection.getInt(itemKey + ".token");
      } else {
        token = 0;
      }
      if (!pageSection.isSet(itemKey + ".money") || !pageSection.isInt(itemKey + ".money")) {
        money = 0;
      } else {
        money = pageSection.getInt(itemKey + ".money");
      }
      // get the amount of the item (default = 1). Check for MaxStackSize!
      if (itemStack != null) {
        if (pageSection.isSet(itemKey + ".count") && pageSection.isInt(itemKey + ".count")) {
          amount = pageSection.getInt(itemKey + ".count");
          itemStack.setAmount(Math.min(amount, itemStack.getMaxStackSize()));
        }
        if (pageSection.getBoolean(itemKey + ".glow", false)) {
          itemStack = NmsFactory.getNmsUtility().addGlow(itemStack);
        }
        meta = itemStack.getItemMeta();
        if (pageSection.isString(itemKey + ".displayName")) {
          meta.setDisplayName(StringUtility.color(pageSection.getString(itemKey + ".displayName")));
        }
        if (pageSection.isList(itemKey + ".lore")) {
          lore = new ArrayList<>(pageSection.getStringList(itemKey + ".lore"));
          for (int i = 0; i < lore.size(); i++) {
            lore.set(i, StringUtility.color(lore.get(i)));
          }
          meta.setLore(lore);
        }
        itemStack.setItemMeta(meta);
      }
      ItemStack buttonItem = getButtonItem(itemStack, pageSection, itemKey);
      if (buttonItem == null) {
        Bukkit.getConsoleSender().sendMessage(plugin.lang.PREFIX + ChatColor.RED + " problem in Shop: " + ChatColor.GREEN + "tokenShop.yml " + pageSection.getCurrentPath() + "." + itemKey);
        Bukkit.getConsoleSender().sendMessage(plugin.lang.PREFIX + ChatColor.RED + " Item AND PresentItem are not defined or not valid");
        Bukkit.getConsoleSender().sendMessage(plugin.lang.PREFIX + ChatColor.RED + "   Skipping...");
        continue;
      }
      // load shop item
      ShopItem shopItem = new ShopItem();
      if (itemStack != null) {
        shopItem.setItemStack(new ItemStack(itemStack));
      }
      if (pageSection.isList(itemKey + ".requirements" + ".permissions")) {
        shopItem.setPermissions(pageSection.getStringList(itemKey + ".requirements" + ".permissions"));
      }
      if (pageSection.isList(itemKey + ".requirements" + ".noPermissions")) {
        shopItem.setNoPermissions(pageSection.getStringList(itemKey + ".requirements" + ".noPermissions"));
      }
      if (pageSection.getBoolean(itemKey + ".manipulatesInventory", false)) {
        shopItem.setManipulatesInventory(true);
      }
      if (pageSection.isList(itemKey + ".commands")) {
        shopItem.setCommands(pageSection.getStringList(itemKey + ".commands"));
      }
      // load button
      Button button = new Button(buttonItem);
      button.setAction(ClickAction.BUY);
      meta = button.getItemMeta();
      lore.clear();
      lore.add("");
      if (money == 0 && token == 0) {
        lore.add(plugin.lang.SHOP_FREE);
      } else if (token != 0) {
        lore.add(plugin.lang.SHOP_TOKEN.replace("%token%", String.valueOf(token)));
      }
      if (money != 0) {
        lore.add(this.plugin.lang.SHOP_MONEY.replace("%money%", String.valueOf(money)));
      }
      if (meta.hasLore()) {
        lore.addAll(meta.getLore());
      }
      meta.setLore(lore);
      button.setItemMeta(meta);
      button.setArgs(key, String.valueOf(counter), String.valueOf(token), String.valueOf(money));
      allItems.put(counter, button);
      shopItems.put(String.valueOf(counter), shopItem);
      counter++;
    }
    counter++;
    int pageNum = counter / itemsPerPage + (counter % itemsPerPage == 0 ? 0 : 1);
    counter = 0;
    Page page = null;
    while (!allItems.isEmpty()) {
      if ((counter) % itemsPerPage == 0) {
        pages.put(counter / itemsPerPage, (page = new Page(plugin, guiManager, slots, counter / itemsPerPage, shopManager, new String[]{key, String.valueOf(counter / itemsPerPage)})));
        if (counter / itemsPerPage == 0) {
          page.setButton(ButtonFactory.createShopPageBackButton(plugin.lang, ShopManager.MAIN, "0"), backSlot);
        } else {
          page.setButton(ButtonFactory.createShopPageBackButton(plugin.lang, key, String.valueOf(counter / itemsPerPage - 1)), backSlot);
        }
        if (pageNum - 1 > counter / itemsPerPage) {
          page.setButton(ButtonFactory.createShopPageForwardButton(plugin.lang, key, String.valueOf(counter / itemsPerPage + 1)), forSlot);
        }
      }
      page.setButton(allItems.get(counter));
      allItems.remove(counter);
      counter++;
    }
  }

  private ItemStack getButtonItem(ItemStack itemStack, ConfigurationSection pageSection, String itemKey) {
    String path = itemKey + ".buttonItem";
    ItemStack presentItem = ItemStackUtility.getItemStack(pageSection.getString(path + ".materialData"));
    if (presentItem == null && itemStack == null) {
      return null;
    }
    if (presentItem == null) {
      presentItem = new ItemStack(itemStack);
    }
    if (pageSection.getBoolean(path + ".glow", false)) {
      presentItem = NmsFactory.getNmsUtility().addGlow(presentItem);
    }
    if (pageSection.isInt(path + ".count")) {
      presentItem.setAmount(pageSection.getInt(path + ".count"));
    }
    ItemMeta meta = presentItem.getItemMeta();
    if (pageSection.isString(path + ".displayName")) {
      meta.setDisplayName(StringUtility.color(pageSection.getString(path + ".displayName")));
    }
    if (pageSection.isList(path + ".additionalLore")) {
      List<String> lore = new ArrayList<>();
      List<String> addLore = new ArrayList<>(pageSection.getStringList(path + ".additionalLore"));
      for (int i = 0; i < addLore.size(); i++) {
        addLore.set(i, StringUtility.color(addLore.get(i)));
      }
      lore.addAll(addLore);
      meta.setLore(lore);
    }
    presentItem.setItemMeta(meta);
    return presentItem;
  }

  protected boolean inCategory(UUID uuid) {
    for (Page page : pages.values()) {
      if (page.isInGui(uuid)) return true;
    }
    return false;
  }

  public void onInvClick(InventoryClickEvent event) {
    for (Page page : pages.values()) {
      if (page.isInGui(event.getWhoClicked().getUniqueId())) {
        page.onInventoryClick(event);
        return;
      }
    }
  }

  public void onBottomInvClick(InventoryClickEvent event) {
    for (Page page : pages.values()) {
      if (page.isInGui(event.getWhoClicked().getUniqueId())) {
        page.onBottomInvClick(event);
        return;
      }
    }
  }

  public void onInvClose(InventoryCloseEvent event) {
    for (Page page : pages.values()) {
      if (page.isInGui(event.getPlayer().getUniqueId())) {
        page.onInventoryClose(event);
        return;
      }
    }
  }

  public boolean openPage(Player whoClicked, int page) {
    if (pages.containsKey(page)) {
      pages.get(page).open(whoClicked);
      return true;
    } else {
      return false;
    }
  }

  public void updateTokens(GBPlayer gbPlayer) {
    for (Page page : pages.values()) {
      page.updateToken(gbPlayer);
    }
  }

  public ItemStack getShopItemStack(String counter) {
    return shopItems.get(counter).getItemStack();
  }

  public ShopItem getShopItem(String counter) {
    return shopItems.get(counter);
  }

  public AGui getShopGui(UUID uuid) {
    for (Page page : pages.values()) {
      if (page.isInGui(uuid)) {
        return page;
      }
    }
    return null;
  }
}
