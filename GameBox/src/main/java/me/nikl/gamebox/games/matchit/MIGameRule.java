package me.nikl.gamebox.games.matchit;

import me.nikl.gamebox.data.SaveType;
import me.nikl.gamebox.games.GameRule;

import java.util.HashSet;

/**
 * Created by nikl on 02.12.17.
 */
public class MIGameRule extends GameRule {
    private double cost;

    public MIGameRule(boolean saveStats, double cost, String key) {
        super(saveStats, new HashSet<>(), key);
        this.saveTypes.add(SaveType.TIME_LOW);

        this.cost = cost;
    }

    public double getCost() {
        return cost;
    }
}
