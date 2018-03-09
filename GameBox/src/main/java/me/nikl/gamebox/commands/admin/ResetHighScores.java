package me.nikl.gamebox.commands.admin;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.commands.GameBoxBaseCommand;
import me.nikl.gamebox.data.database.DataBase;
import me.nikl.gamebox.data.toplist.SaveType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author Niklas Eicker
 */
@CommandAlias("%adminCommand")
public class ResetHighScores extends GameBoxBaseCommand {
    private boolean hasAttemptedToResetHighScoresBefore = false;

    public ResetHighScores(GameBox gameBox) {
        super(gameBox);
    }

    @Subcommand("resetstats")
    public void resetHighScores(CommandSender sender, String gameID, String gameTypeID, @Single String saveTypeStr) {
        DataBase dataBase = gameBox.getDataBase();
        try {
            SaveType saveType = SaveType.valueOf(saveTypeStr);
            dataBase.resetHighScores(gameID, gameTypeID, saveType);
            sender.sendMessage( " High score reset successful");
            gameBox.reload();
        } catch (IllegalArgumentException exception) {
            sender.sendMessage("Valid saveTyps: " + SaveType.values().toString());
        }
    }

    @Subcommand("resetstats")
    public void resetHighScores(CommandSender sender) {
        if (!hasAttemptedToResetHighScoresBefore) {
            hasAttemptedToResetHighScoresBefore = true;
            new BukkitRunnable(){
                @Override
                public void run() {
                    hasAttemptedToResetHighScoresBefore = false;
                }
            }.runTaskLater(gameBox, 20*10);
            sender.sendMessage(gameBox.lang.PREFIX + " This will remove ALL high scores!");
            sender.sendMessage(gameBox.lang.PREFIX + " There is no going back, are you sure?");
            sender.sendMessage(gameBox.lang.PREFIX + " Run this command again in the next 10 seconds");
            sender.sendMessage(gameBox.lang.PREFIX + "     to reset all high scores.");
            return;
        }
        gameBox.getDataBase().resetHighScores();
        gameBox.reload();
    }
}
