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
			new Message("no-permission").send(sender);
			return true;
		}
		if (args.length < 2 || args.length > 4) {
			return false;
		}
		Player target = Bukkit.getPlayer(args[0]);
		if (target == null) {
			new Message("player-not-found").setPlayer(args[0]).send(sender);
			return true;
		}
		boolean nonzero = args[1].equalsIgnoreCase("nonzero");
		if (nonzero || args[1].equalsIgnoreCase("all")) {
			List<Statistic> sortedStats = new ArrayList<Statistic>();
			for (Statistic stat : Statistic.values()) {
				sortedStats.add(stat);
			}
			sortedStats.sort(Comparator.comparing(Statistic::toString));
			for (Statistic stat : sortedStats) {
				if (stat.getType() != Statistic.Type.UNTYPED) {
					new Message("all-stat-requires-argument").setStat(stat.toString()).setArgument(stat.getType().toString()).send(sender);;
				} else {
					int value = target.getStatistic(stat);
					if (nonzero && value == 0) continue;
					new Message("all-stat").setStat(stat.toString()).setValue(target.getStatistic(stat) + "").send(sender);
				}
			}
			return true;
		}
		boolean write = false;
		if (args.length > 2) {
			try {
				Integer.parseInt(args[args.length - 1].replaceFirst("^[-\\+]", "")); // replace + and - at start of string
				write = true;
			} catch (NumberFormatException e) {
				// args[2] is a string, meaning it should be a statistic argument
			}
		}
		if (!write) {
			StatisticManager.getStatValue(target, args[1], args.length > 2 ? args[2] : null).send(sender);
			return true;
		}
		if (!sender.hasPermission(EDIT_PERMISSION)) {
			new Message("no-edit-permission").send(sender);
			return true;
		}
		boolean relative;
		int value;
		String valString = args[args.length - 1];
		if (valString.startsWith("--") || valString.startsWith("++")) {
			relative = true;
			value = Integer.parseInt(valString.substring(1)); // chop off - or +, because +12 and -12 should both be valid ints
		} else {
			relative = false;
			value = Integer.parseInt(valString);
		}
		StatisticManager.setStatValue(target, args[1], args.length > 3 ? args[2] : null, value, relative).send(sender);
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		// "return results;" is basically "give up" except for on the last line since results doesn't get written to until late.
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
			options.add("NONZERO");
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
			options.add("ALL");
			options.add("NONZERO");
			if (stat.getType() == Statistic.Type.ENTITY) {
				for (EntityType type : EntityType.values()) {
					options.add(type.toString());
				}
			} else if (args[2].length() > 0) { // the material enum has so many things in it, make sure we have at least one filter character
				boolean item = stat.getType() == Statistic.Type.ITEM;
				for (Material mat : Material.values()) {
					if (item ? mat.isItem() : mat.isBlock()) options.add(mat.toString());
				}
			} else {
				return results;
			}
		}
		if (token != null) StringUtil.copyPartialMatches(token, options, results);
		return results;
	}
}
