package me.meowso.privatemines;

import org.bukkit.plugin.java.JavaPlugin;

public final class PrivateMines extends JavaPlugin {
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new Listeners(this), this);
        getCommand("privatemines").setExecutor(new CommandHandler(this));
        saveDefaultConfig();
    }
}
