package me.nikl.gamebox.utility;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.Module;
import org.bukkit.command.CommandSender;

import javax.annotation.Nullable;
import java.util.ArrayList;

/**
 * Created by niklas on 10/27/16.
 *
 * easier permission storage
 * just change the permission nodes here
 */
public enum Permission {
    USE("use"),
    PLAY_GAME("play", true),
    OPEN_GAME_GUI("gamegui", true),
    CMD_INFO("info"),
    CMD_HELP("help"),
    CMD_TOKEN("token"),
    BYPASS_ALL("bypass"),
    BYPASS_GAME("bypass", true),
    OPEN_SHOP("shop"),
    ADMIN_SETTINGS("admin.settings"),
    ADMIN_LANGUAGE("admin.language"),
    ADMIN_RELOAD("admin.reload"),
    ADMIN_TOKEN("admin.token"),
    ADMIN_DATABASE("admin.database");

    private static ArrayList<String> moduleIDs = new ArrayList<>();
    private boolean perGame;
    private String perm;
    private String preNode = GameBox.MODULE_GAMEBOX;

    Permission(String perm, boolean perGame) {
        this.perm = preNode + "." + perm + (perGame ? ".%moduleID%" : "");
        this.perGame = perGame;
    }

    Permission(String perm) {
        this(perm, false);
    }

    public static void registerModuleID(String moduleID) {
        Permission.moduleIDs.add(moduleID);
        GameBox.debug("registered permissions for: " + moduleID);
    }

    public static void unregisterModuleID(String moduleID) {
        Permission.moduleIDs.remove(moduleID);
        GameBox.debug("unregistered permissions for: " + moduleID);
    }

    /**
     * Check sender for the permission.
     *
     * If the passed moduleID is null, only the wildcard permission is checked.
     *
     * @param sender to check permission for
     * @param moduleID for module specific permissions
     * @return permission is set
     */
    public boolean hasPermission(CommandSender sender, @Nullable String moduleID) {
        if (moduleID == null) {
            return sender.hasPermission(perm.replace("%moduleID%", "*"));
        } else {
            if (!moduleIDs.contains(moduleID)) {
                throw new IllegalArgumentException("Unknown moduleID: " + moduleID);
            }
            return (sender.hasPermission(perm.replace("%moduleID%", moduleID))
                    || sender.hasPermission(perm.replace("%moduleID%", "*")));
        }
    }

    public boolean hasPermission(CommandSender sender, Module module) {
        return hasPermission(sender, module.getModuleID());
    }

    public boolean hasPermission(CommandSender sender) {
        if (perGame) throw new IllegalArgumentException("Accessing a per-game permission without a gameID");
        return sender.hasPermission(perm);
    }

    public String getPermission() {
        return this.perm;
    }

}