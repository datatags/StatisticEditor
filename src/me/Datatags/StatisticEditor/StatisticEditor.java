package me.Datatags.StatisticEditor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class StatisticEditor extends JavaPlugin {
	private File messagesFile = new File(getDataFolder(), "messages.yml");
	private YamlConfiguration messages = new YamlConfiguration();
	public void onEnable() {
		Message.se = this;
		loadMessages();
		getCommand("statistic").setExecutor(new StatisticCommand());
		if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
			getLogger().info("PlaceholderAPI found, registering hook");
			new PlaceholderAPIHook(this).register();
		}
	}
	private void loadMessages() {
		if (!messagesFile.exists()) saveResource("messages.yml", false);
		try {
			messages.load(messagesFile);
		} catch (FileNotFoundException e) {
			// shouldn't ever happen, we save the resource immediately before
			getLogger().warning("Couldn't find messages.yml");
			e.printStackTrace();
		} catch (IOException e) {
			getLogger().warning("Failed to load messages file:");
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			getLogger().warning("messages.yml is invalid! Please regenerate it or fix it!");
			e.printStackTrace();
		}
	}
	public String getMessage(String id) {
		return ChatColor.translateAlternateColorCodes('&', messages.getString(id, "&cMissing message: " + id));
	}
}
