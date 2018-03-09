package me.nikl.gamebox.data.database;

import com.zaxxer.hikari.HikariDataSource;
import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.GameBoxSettings;
import me.nikl.gamebox.data.GBPlayer;
import me.nikl.gamebox.data.toplist.PlayerScore;
import me.nikl.gamebox.data.toplist.SaveType;
import me.nikl.gamebox.data.toplist.TopList;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Niklas
 */
public class MysqlDB extends DataBase {
    private static final String INSERT = "INSERT INTO " + PLAYER_TABLE + " VALUES(?,?,?,?,?) ON DUPLICATE KEY UPDATE " + PLAYER_NAME + "=?";
    private static final String SELECT = "SELECT * FROM `" + PLAYER_TABLE + "` WHERE " + PLAYER_UUID + "=?";
    private static final String SELECT_TOKEN = "SELECT " + PLAYER_TOKEN_PATH + " FROM " + PLAYER_TABLE + " WHERE " + PLAYER_UUID + "=?";
    private static final String SAVE = "UPDATE " + PLAYER_TABLE + " SET " + PLAYER_TOKEN_PATH + "=?, " + PLAYER_PLAY_SOUNDS + "=?, " + PLAYER_ALLOW_INVITATIONS + "=? WHERE " + PLAYER_UUID + "=?";
    private static final String SET_TOKEN = "UPDATE " + PLAYER_TABLE + " SET " + PLAYER_TOKEN_PATH + "=? WHERE " + PLAYER_UUID + "=?";
    private static final String UPDATE_HIGH_SCORE_GREATEST = "INSERT INTO `" + HIGH_SCORES_TABLE + "` (`" + PLAYER_UUID + "`,`%column%`) VALUES(?,?) ON DUPLICATE KEY UPDATE `%column%`=GREATEST(`%column%`, VALUES(`%column%`))";
    private static final String UPDATE_HIGH_SCORE_LEAST = "INSERT INTO `" + HIGH_SCORES_TABLE + "` (`" + PLAYER_UUID + "`,`%column%`) VALUES(?,?) ON DUPLICATE KEY UPDATE `%column%`=LEAST(`%column%`, VALUES(`%column%`))";
    private static final String UPDATE_HIGH_SCORE_ADD_WIN = "INSERT INTO `" + HIGH_SCORES_TABLE + "` (`" + PLAYER_UUID + "`,`%column%`) VALUES(?,?) ON DUPLICATE KEY UPDATE `%column%`=VALUES(`%column%`)";
    private static final String COLLECT_TOP_SCORES = "SELECT e1.* FROM (SELECT DISTINCT `%column%` FROM `" + HIGH_SCORES_TABLE + "` ORDER BY `%column%` %order% LIMIT %n%) s1 JOIN `" + HIGH_SCORES_TABLE + "` e1 ON e1.`%column%` = s1.`%column%` ORDER BY e1.`%column%` %order%";
    private static final String COLLECT_COLUMNS_STARTING_WITH = "SELECT column_name FROM INFORMATION_SCHEMA.columns WHERE table_schema = ? AND table_name = `" + HIGH_SCORES_TABLE + "` AND LEFT(column_name, %length%) =?";
    private static final String SELECT_HIGH_SCORE = "SELECT `%column%` FROM `" + HIGH_SCORES_TABLE + "` WHERE " + PLAYER_UUID + "=?";

    private String host;
    private String database;
    private String username;
    private String password;
    private int port;
    private HikariDataSource hikari;
    private Set<String> knownHighScoreColumns = new HashSet<>();

    public MysqlDB(GameBox plugin) {
        super(plugin);
        FileConfiguration config = plugin.getConfig();
        host = config.getString("mysql.host");
        port = config.getInt("mysql.port");
        database = config.getString("mysql.database");
        username = config.getString("mysql.username");
        password = config.getString("mysql.password");
    }

    @Override
    public boolean load(boolean async) {
        hikari = new HikariDataSource();
        hikari.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
        hikari.addDataSourceProperty("serverName", host);
        hikari.addDataSourceProperty("port", port);
        hikari.addDataSourceProperty("databaseName", database);
        hikari.addDataSourceProperty("user", username);
        hikari.addDataSourceProperty("password", password);

        try (Connection connection = hikari.getConnection()) {
            Statement statement = connection.createStatement();
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS `" + PLAYER_TABLE + "`(`" +
                    PLAYER_UUID + "` varchar(36), `" +
                    PLAYER_NAME + "` VARCHAR(16), `" +
                    PLAYER_TOKEN_PATH + "` int, `" +
                    PLAYER_PLAY_SOUNDS + "` BOOL, `" +
                    PLAYER_ALLOW_INVITATIONS + "` BOOL, " +
                    "PRIMARY KEY (`" + PLAYER_UUID + "`))");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS `" + HIGH_SCORES_TABLE + "`(`" +
                    PLAYER_UUID + "` varchar(36), " +
                    "PRIMARY KEY (`" + PLAYER_UUID + "`))");
        } catch (SQLException e) {
            e.printStackTrace();
            GameBoxSettings.useMysql = false;
            return false;
        }
        return true;
    }

    @Override
    public void save(boolean async) {
        // nothing to do here since all players are saved before and the database is synchronised already
    }

    @Override
    public void addStatistics(UUID uuid, String gameID, String gameTypeID, double value, SaveType saveType, boolean async) {
        GameBox.debug("Add stats...");
        String columnName = buildColumnName(gameID, gameTypeID, saveType);
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                createColumnIfNecessary(columnName);
                double newValue;
                if (saveType == SaveType.WINS) {
                    newValue = value + getHighscore(columnName, uuid);
                    updateHighscoreSetValue(columnName, uuid, newValue);
                }
                else if (saveType.isHigherScore()) {
                    newValue = value;
                    updateHighscoreGreatest(columnName, uuid, value);
                } else {
                    newValue = value;
                    updateHighscoreLeast(columnName, uuid, value);
                }
                BukkitRunnable update = new BukkitRunnable() {
                    @Override
                    public void run() {
                        GameBox.debug("update top list score: " + uuid.toString() + "    " + newValue + "      " + saveType.toString());
                        getTopList(gameID, gameTypeID, saveType).update(new PlayerScore(uuid, newValue, saveType));
                    }
                };
                if (async) update.runTask(plugin);
                else update.run();
            }
        };
        if (async) runnable.runTaskAsynchronously(plugin);
        else runnable.run();
    }

    private void updateHighscoreLeast(String columnName, UUID uuid, double value) {
        try (Connection connection = hikari.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_HIGH_SCORE_LEAST.replace("%column%", columnName))) {
            statement.setString(1, uuid.toString());
            statement.setDouble(2, value);
            statement.execute();
            GameBox.debug("High score added!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateHighscoreSetValue(String columnName, UUID uuid, double value) {
        try (Connection connection = hikari.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_HIGH_SCORE_ADD_WIN.replace("%column%", columnName))) {
            statement.setString(1, uuid.toString());
            statement.setDouble(2, value);
            statement.execute();
            GameBox.debug("High score added!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateHighscoreGreatest(String columnName, UUID uuid, double value) {
        try (Connection connection = hikari.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_HIGH_SCORE_GREATEST.replace("%column%", columnName))) {
            statement.setString(1, uuid.toString());
            statement.setDouble(2, value);
            statement.execute();
            GameBox.debug("High score added!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private double getHighscore(String columnName, UUID uuid) {
        try (Connection connection = hikari.getConnection();
             PreparedStatement select = connection.prepareStatement(SELECT_HIGH_SCORE.replace("%column%", columnName))) {
            select.setString(1, uuid.toString());
            ResultSet result = select.executeQuery();
            if (result.next()) {
                return result.getDouble(columnName);
            }
            return 0.;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0.;
        }
    }

    private void createColumnIfNecessary(String columnName) {
        if (!doesHighScoreColumnExist(columnName)) {
            try (Connection connection = hikari.getConnection();
                 Statement statement = connection.createStatement()) {
                GameBox.debug("  Adding the column " + columnName);
                statement.executeUpdate("ALTER TABLE `" + HIGH_SCORES_TABLE + "` ADD `" + columnName + "` DOUBLE NULL");
                knownHighScoreColumns.add(columnName);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean doesHighScoreColumnExist(String columnName) {
        if (knownHighScoreColumns.contains(columnName)) {
            GameBox.debug("  Found known high score column: " + columnName);
            return true;
        }
        // first time this column is used in this server session... better check it exists
        GameBox.debug("  Column name (length = " + columnName.length() + "): " + columnName);
        try (Connection connection = hikari.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement
                    .executeQuery("SELECT * FROM information_schema.COLUMNS " +
                            "WHERE TABLE_SCHEMA = '" + database + "'" +
                            "AND TABLE_NAME = '" + HIGH_SCORES_TABLE + "'" +
                            "AND COLUMN_NAME = '" + columnName + "'");
            if (!resultSet.next()) {
                GameBox.debug("  Column does not exist");
                return false;
            } else {
                GameBox.debug("  Column already exists");
                knownHighScoreColumns.add(columnName);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String buildColumnName(String gameID, String gameTypeID, SaveType saveType) {
        return gameID + gameTypeID + saveType.toString().replace("_", "");
    }

    @Override
    public TopList getTopList(String gameID, String gameTypeID, SaveType saveType) {
        String topListIdentifier = buildColumnName(gameID, gameTypeID, saveType);
        if (cachedTopLists.containsKey(topListIdentifier)) return cachedTopLists.get(topListIdentifier);
        TopList newTopList = new TopList(topListIdentifier, new ArrayList<>());
        cachedTopLists.put(topListIdentifier, newTopList);
        initialiseNewTopList(newTopList, saveType);
        return newTopList;
    }

    @Override
    public void getTopNPlayerScores(int n, String gameID, String gameTypeID, SaveType saveType, Callback<List<PlayerScore>> callback) {
        String identifier = buildColumnName(gameID, gameTypeID, saveType);
        getTopNPlayerScores(n, identifier, saveType, callback);
    }

    private void getTopNPlayerScores(int n, String identifier, SaveType saveType, Callback<List<PlayerScore>> callback) {
        new BukkitRunnable() {
            @Override
            public void run() {
                createColumnIfNecessary(identifier);
                try (Connection connection = hikari.getConnection();
                     PreparedStatement select = connection.prepareStatement(COLLECT_TOP_SCORES
                             .replace("%column%", identifier)
                             .replace("%order%", saveType.isHigherScore() ? "DESC" : "ASC")
                             .replace("%n%", String.valueOf(n)))) {
                    ResultSet result = select.executeQuery();
                    List<PlayerScore> scores = new ArrayList<>();
                    while (result.next()) {
                        final UUID uuid = UUID.fromString(result.getString(PLAYER_UUID));
                        final double value = result.getDouble(identifier);
                        scores.add(new PlayerScore(uuid, value, saveType));
                    }
                    // back to main thread and update score
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(scores);
                        }
                    }.runTask(plugin);
                    try {
                        result.close();
                    } catch (SQLException e) {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                callback.onFailure(e, scores);
                            }
                        }.runTask(plugin);
                    }
                } catch (SQLException e) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            callback.onFailure(e, null);
                        }
                    }.runTask(plugin);
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    private void initialiseNewTopList(TopList newTopList, SaveType saveType) {
        getTopNPlayerScores(TopList.TOP_LIST_LENGTH, newTopList.getIdentifier(), saveType, new Callback<List<PlayerScore>>() {
            @Override
            public void onSuccess(List<PlayerScore> done) {
                newTopList.updatePlayerScores(done);
            }

            @Override
            public void onFailure(@Nullable Throwable throwable, @Nullable List<PlayerScore> value) {
                plugin.getLogger().warning("Error while grabbing top list entries!");
                if (throwable != null) throwable.printStackTrace();
            }
        });
    }

    @Override
    public void loadPlayer(GBPlayer player, boolean async) {
        // i am going to ignore the async bool here, since I don't want sync database calls for player loading...
        if (!async) plugin.warning(" plugin tried to load player from MySQL sync...");

        // load player from database and set the results in the player class
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection connection = hikari.getConnection();
                     PreparedStatement insert = connection.prepareStatement(INSERT);
                     PreparedStatement select = connection.prepareStatement(SELECT)) {
                    Player p = player.getPlayer();
                    insert.setString(1, p.getUniqueId().toString());
                    insert.setString(2, p.getName());
                    insert.setInt(3, 0);
                    insert.setBoolean(4, true);
                    insert.setBoolean(5, true);
                    insert.setString(6, p.getName());
                    insert.execute();

                    select.setString(1, p.getUniqueId().toString());
                    ResultSet result = select.executeQuery();
                    if (result.next()) {
                        final boolean sound = result.getBoolean(PLAYER_PLAY_SOUNDS);
                        final boolean invites = result.getBoolean(PLAYER_ALLOW_INVITATIONS);
                        final int token = result.getInt(PLAYER_TOKEN_PATH);

                        // back to main thread and set player
                        Bukkit.getScheduler().runTask(plugin, () ->
                                player.setPlayerData(token, sound, invites));
                        try {
                            result.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    } else {
                        plugin.warning(" empty result set when loading player " + p.getName());
                        try {
                            result.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        };
        task.runTaskAsynchronously(plugin);
    }

    @Override
    public void savePlayer(final GBPlayer player, boolean async) {
        // must work async and sync since sync is needed on server shutdown
        if (async) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    savePlayer(player);
                }
            }.runTaskAsynchronously(plugin);
        } else {
            savePlayer(player);
        }
    }

    private void savePlayer(final GBPlayer player) {
        try (Connection connection = hikari.getConnection();
             PreparedStatement statement = connection.prepareStatement(SAVE)) {
            statement.setInt(1, player.getTokens());
            statement.setBoolean(2, player.isPlaySounds());
            statement.setBoolean(3, player.allowsInvites());
            statement.setString(4, player.getUuid().toString());
            statement.execute();
        } catch (SQLException e) {
            plugin.warning("Error while saving a player to the database!");
            e.printStackTrace();
        }
    }

    @Override
    public void getToken(UUID uuid, Callback<Integer> callback) {

        // load token for a player
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection connection = hikari.getConnection();
                     PreparedStatement select = connection.prepareStatement(SELECT_TOKEN)) {
                    select.setString(1, uuid.toString());
                    ResultSet result = select.executeQuery();
                    if (result.next()) {
                        new BukkitRunnable() {

                            @Override
                            public void run() {
                                // call callable back on main thread
                                Bukkit.getScheduler().runTask(plugin, () -> {
                                    try {
                                        callback.onSuccess(result.getInt(PLAYER_TOKEN_PATH));
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    } finally {
                                        try {
                                            result.close();
                                        } catch (SQLException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }
                        }.runTask(plugin);
                    } else {
                        plugin.warning(" empty result set trying to get token for " + uuid.toString());
                        try {
                            result.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        };
        task.runTaskAsynchronously(plugin);
    }

    @Override
    public void setToken(UUID uuid, int token) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = hikari.getConnection();
                 PreparedStatement statement = connection.prepareStatement(SET_TOKEN)) {
                statement.setInt(1, token);
                statement.setString(2, uuid.toString());
                statement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onShutDown() {
        super.onShutDown();
        hikari.close();
    }

    @Override
    public void resetHighScores() {
        try (Connection connection = hikari.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("TRUNCATE TABLE `" + HIGH_SCORES_TABLE + "`");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void resetHighScores(String gameID, String gameTypeID, SaveType saveType) {
        try (Connection connection = hikari.getConnection();
             Statement statement = connection.createStatement()) {
            String columnName = buildColumnName(gameID, gameTypeID,saveType);
            if(!doesHighScoreColumnExist(columnName)) return;
            statement.executeUpdate("ALTER TABLE `" + HIGH_SCORES_TABLE + "` DROP COLUMN `" + columnName + "`");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void convertFromFile(CommandSender sender) {
        FileDB fromDB = new FileDB(plugin);
        new BukkitRunnable() {

            @Override
            public void run() {
                sender.sendMessage(plugin.getLanguage(GameBox.MODULE_GAMEBOX).PREFIX + " Starting async conversion.");
                sender.sendMessage(plugin.getLanguage(GameBox.MODULE_GAMEBOX).PREFIX + " Additional output in the console!");
                fromDB.load(false);
                fromDB.convertToMySQL();
                fromDB.onShutDown();
                sender.sendMessage(plugin.getLanguage(GameBox.MODULE_GAMEBOX).PREFIX + " Conversion is completed.");
            }
        }.runTaskAsynchronously(plugin);
    }

    public List<String> getHighScoreColumnsBeginningWith(String beginningOfColumnName) {
        ArrayList<String> toReturn = new ArrayList<>();
        try (Connection connection = hikari.getConnection();
             PreparedStatement select = connection.prepareStatement(COLLECT_COLUMNS_STARTING_WITH.replace("%length%", String.valueOf(beginningOfColumnName.length())))) {
            select.setString(1, database);
            select.setString(2, "`" + beginningOfColumnName + "`");
            ResultSet result = select.executeQuery();
            while (result.next()) {
                toReturn.add(result.getString("column_name"));
            }
            try {
                result.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return toReturn;
    }
}
