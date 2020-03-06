package me.AlanZ.StatisticEditor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.util.StringUtil;

public class StatisticCommand implements TabExecutor {
	public static final Permission USE_PERMISSION = new Permission("statisticeditor.statistic");
	public static final Permission EDIT_PERMISSION = new Permission("statisticeditor.editstatistic");
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!sender.hasPermission(USE_PERMISSION)) {
			new Message("no-permission");
			return true;
		}
		if (args.length < 2) {
			return false;
		}
		Player target = Bukkit.getPlayer(args[0]);
		if (target == null) {
			new Message("player-not-found").setPlayer(args[0]).send(sender);;
			return true;
		}
		if (args[1].equalsIgnoreCase("all")) {
			List<Statistic> sortedStats = new ArrayList<Statistic>();
			for (Statistic stat : Statistic.values()) {
				sortedStats.add(stat);
			}
			sortedStats.sort(Comparator.comparing(Statistic::toString));
			for (Statistic stat : sortedStats) {
				if (stat.getType() != Statistic.Type.UNTYPED) {
					new Message("all-stat-requires-argument").setStat(stat.toString()).setArgument(stat.getType().toString()).send(sender);;
				} else {
					new Message("all-stat").setStat(stat.toString()).setValue(target.getStatistic(stat) + "").send(sender);;
				}
			}
			return true;
		}
		boolean write = false;
		if (args.length > 2) {
			try {
				Integer.parseInt(args[args.length - 1]);
				write = true;
			} catch (NumberFormatException e) {
				
			}
		}
		if (!write) {
			StatisticManager.getStatValue(target, args[1], args.length > 2 ? args[2] : null).send(sender);
		} else {
			if (!sender.hasPermission(EDIT_PERMISSION)) {
				new Message("no-edit-permission").send(sender);
				return true;
			}
			StatisticManager.setStatValue(target, args[1], args.length > 2 ? args[2] : null, Integer.parseInt(args[3])).send(sender);
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		String token = null;
		List<String> options = new ArrayList<>();
		List<String> results = new ArrayList<>();
		if (!sender.hasPermission(USE_PERMISSION)) return results;
		if (args.length == 1) {
			token = args[0];
			for (Player player : Bukkit.getOnlinePlayers()) {
				options.add(player.getName());
			}
		} else if (args.length == 2) {
			token = args[1];
			options.add("ALL");
			for (Statistic stat : Statistic.values()) {
				options.add(stat.toString());
			}
		} else if (args.length == 3) {
			token = args[2];
			Statistic stat;
			try {
				stat = Statistic.valueOf(args[1].toUpperCase());
			} catch (IllegalArgumentException e) {
				return results;
			}
			if (stat.getType() == Statistic.Type.UNTYPED) {
				return results;
			}
			if (stat.getType() == Statistic.Type.ENTITY) {
				options.add("ALL");
				for (EntityType type : EntityType.values()) {
					options.add(type.toString());
				}
			} else if (args[2].length() > 0) { // the material enum has so many things in it, make sure we have at least one filter character
				boolean item = stat.getType() == Statistic.Type.ITEM;
				options.add("ALL");
				for (Material mat : Material.values()) {
					if (item ? mat.isItem() : mat.isBlock()) options.add(mat.toString());
				}
			}
		}
		if (token != null) StringUtil.copyPartialMatches(token, options, results);
		return results;
	}
}
