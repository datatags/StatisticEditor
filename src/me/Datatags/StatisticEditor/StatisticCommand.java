package me.Datatags.StatisticEditor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.util.StringUtil;

public class StatisticCommand implements TabExecutor {
	public static final Permission USE_PERMISSION = new Permission("statisticeditor.statistic");
	public static final Permission EDIT_PERMISSION = new Permission("statisticeditor.editstatistic");
	
	// /stats <target> <statistic> [argument] [value]
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
			sortedStats.sort(Comparator.comparing(s -> StatisticManager.getStatisticName(s)));
			for (Statistic stat : sortedStats) {
				if (stat.getType() != Statistic.Type.UNTYPED) {
					new Message("all-stat-requires-argument").setStat(stat).setArgument(stat.getType().toString()).send(sender);
				} else {
					int value = target.getStatistic(stat);
					if (nonzero && value == 0) continue;
					new Message("all-stat").setStat(stat).setValue(target.getStatistic(stat)).send(sender);
				}
			}
			return true;
		}
		Statistic stat = StatisticManager.getStatistic(args[1]);
		if (stat == null) {
			new Message("invalid-stat").setStat(args[1]).send(sender);
			return true;
		}
		// command formats:
		// 1: @p stat
		// 2: @p stat arg
		// 3: @p stat val
		// 4: @p stat arg val
		Integer value = null;
		if (args.length > 2) { // true if not 1
			value = getNumber(args[args.length - 1]);
		}
		if (value == null) { // true if 1 or 2
			StatisticManager.getStatValue(target, stat, args.length > 2 ? args[2] : null).send(sender);
			return true;
		}
		if (!sender.hasPermission(EDIT_PERMISSION)) {
			new Message("no-edit-permission").send(sender);
			return true;
		}
		boolean relative;
		String valString = args[args.length - 1];
		relative = valString.startsWith("--") || valString.startsWith("++");
		StatisticManager.setStatValue(target, stat, args.length > 3 ? args[2] : null, value, relative).send(sender);
		return true;
	}
	private Integer getNumber(String number) {
		try {
			return Integer.parseInt(number.replaceFirst("^[-\\+]", "")); // replace + and - at start of string
		} catch (NumberFormatException e) {
			return null;
		}
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
			TabUtils.addPlayers(options);
		} else if (args.length == 2) {
			token = args[1];
			TabUtils.addStats(options, true);
		} else if (args.length == 3) {
			token = args[2];
			TabUtils.addArgsOrAlt(options, args[1], args[2], true, results);
		}
		if (token != null) StringUtil.copyPartialMatches(token, options, results);
		return results;
	}
}
