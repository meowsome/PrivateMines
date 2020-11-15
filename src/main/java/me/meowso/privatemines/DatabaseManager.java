package me.meowso.privatemines;

import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private final String pathName;

    public DatabaseManager(PrivateMines privateMines) {
        pathName = privateMines.getDataFolder() + "/database.yml";
    }

    public void addMine(String islandId, String mineName) throws IOException {
        File file = new File(pathName);
        YamlConfiguration database = YamlConfiguration.loadConfiguration(file);

        // Build array of all mine names on this island
        ArrayList<String> mineNames = new ArrayList<>();
        mineNames.add(mineName);
        if (database.contains("mines." + islandId)) mineNames.addAll(database.getStringList("mines." + islandId));

        // Save array to file
        database.set("mines." + islandId, mineNames);
        database.save(file);
    }

    public List<String> getMines(String islandId) {
        File file = new File(pathName);
        YamlConfiguration database = YamlConfiguration.loadConfiguration(file);
        return database.getStringList("mines." + islandId);
    }

    public void removeMines(String islandId) throws IOException {
        File file = new File(pathName);
        YamlConfiguration database = YamlConfiguration.loadConfiguration(file);

        database.set("mines." + islandId, null);
        database.save(file);
    }
}
