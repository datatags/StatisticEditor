package me.Datatags.StatisticEditor;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class TabUtils {
	public static void addPlayers(List<String> options) {
		for (Player player : Bukkit.getOnlinePlayers()) {
			options.add(player.getName());
		}
	}
	public static void addStats(List<String> options, boolean includeAll) {
		if (includeAll) {
			options.add("ALL");
			options.add("NONZERO");
		}
		for (Statistic stat : Statistic.values()) {
			options.add(stat.toString());
		}
	}
	public static void addArgsOrAlt(List<String> options, String statString, String arg, boolean includeAll, List<String> alternatives) {
		Statistic stat = StatisticManager.getStatistic(statString);
		if (stat == null) return;
		if (stat.getType() == Statistic.Type.UNTYPED) {
			options.addAll(alternatives);
			return;
		}
		if (includeAll) {
			options.add("ALL");
			options.add("NONZERO");
		}
		if (stat.getType() == Statistic.Type.ENTITY) {
			for (EntityType type : EntityType.values()) {
				if (type == EntityType.UNKNOWN) continue;
				options.add(type.toString());
			}
		} else if (arg.length() > 0) { // the material enum has so many things in it, make sure we have at least one filter character
			boolean item = stat.getType() == Statistic.Type.ITEM;
			for (Material mat : Material.values()) {
				if (item ? mat.isItem() : mat.isBlock()) options.add(mat.toString());
			}
		}
	}
}
