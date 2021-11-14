package me.datatags.statisticeditor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.util.StringUtil;

public class RunIfStatCommand implements TabExecutor {
	public static final Permission RUNIFSTAT = new Permission("statisticeditor.runifstat");
	private Set<String> lessthan = new HashSet<>();
	private Set<String> greaterthan = new HashSet<>();
	private Set<String> equal = new HashSet<>();
	private Set<String> notequal = new HashSet<>();
	private List<String> acceptedOperators = new ArrayList<String>();
	public RunIfStatCommand() {
		lessthan.add("lt");
		lessthan.add("<");
		
		greaterthan.add("gt");
		greaterthan.add(">");
		
		equal.add("==");
		equal.add("eq");
		equal.add("is");
		
		notequal.add("!=");
		notequal.add("ne");
		notequal.add("isnot");
		
		acceptedOperators.addAll(lessthan);
		acceptedOperators.addAll(greaterthan);
		acceptedOperators.addAll(equal);
		acceptedOperators.addAll(notequal);
		Collections.sort(acceptedOperators);
	}
	// /runifstat target STAT [arg] (lt OR gt OR is OR isnot) number command 
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!sender.hasPermission(RUNIFSTAT)) {
			new Message("no-permission").send(sender);
			return true;
		}
		if (args.length < 5) {
			return false;
		}
		Player target = Bukkit.getPlayer(args[0]);
		Statistic stat = StatisticManager.getStatistic(args[1]);
		if (target == null) {
			new Message("player-not-found").setPlayer(args[0]).send(sender);
			return true;
		}
		if (stat == null) {
			new Message("invalid-stat").setStat(args[1]).send(sender);
			return true;
		}
		int offset = 0;
		int number;
		try {
			number = Integer.parseInt(args[3]);
		} catch (NumberFormatException e) {
			if (args.length < 6) {
				return false;
			}
			try {
				number = Integer.parseInt(args[4]);
				offset = 1;
			} catch (NumberFormatException e2) {
				return false;
			}
		}
		
		String op = args[2 + offset];
		if (containsIgnoreCase(lessthan, op)) {
			op = "lt";
		} else if (containsIgnoreCase(greaterthan, op)) {
			op = "gt";
		} else if (containsIgnoreCase(equal, op)) {
			op = "eq";
		} else if (containsIgnoreCase(notequal, op)) {
			op = "ne";
		} else {
			new Message("invalid-operator").setArgument(op).send(sender);
			return true;
		}
		Message mesg = StatisticManager.getStatValue(target, stat, offset == 1 ? args[2] : null);
		if (mesg.getValue() == null) {
			mesg.send(sender);
			return true;
		}
		if (!calculateOperator(op, mesg.getValue(), number)) {
			return true;
		}
		StringBuilder command = new StringBuilder(args[4 + offset]);
		for (int i = 5 + offset; i < args.length; i++) {
			command.append(" " + args[i]);
		}
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.toString().replace("%player%", target.getName()));
		return true;
	}
	private boolean containsIgnoreCase(Set<String> set, String search) {
		for (String test : set) {
			if (test.equalsIgnoreCase(search)) {
				return true;
			}
		}
		return false;
	}
	private boolean calculateOperator(String op, int a, int b) {
		if (op.equals("lt") && a < b) return true;
		if (op.equals("gt") && a > b) return true;
		if (op.equals("eq") && a == b) return true;
		if (op.equals("ne") && a != b) return true;
		return false;
	}
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		String token = null;
		List<String> options = new ArrayList<>();
		List<String> results = new ArrayList<>();
		if (!sender.hasPermission(RUNIFSTAT)) return results;
		if (args.length == 1) {
			token = args[0];
			TabUtils.addPlayers(options);
		} else if (args.length == 2) {
			token = args[1];
			TabUtils.addStats(options, false);
		} else if (args.length == 3) {
			token = args[2];
			TabUtils.addArgsOrAlt(options, args[1], args[2], false, acceptedOperators);
		} else if (args.length == 4) {
			token = args[3];
			Statistic stat = StatisticManager.getStatistic(args[1]);
			if (stat == null) return results;
			if (stat.getType() != Statistic.Type.UNTYPED) {
				options.addAll(acceptedOperators);
			}
		}
		// don't need to suggest any numbers or commands so we're done
		if (token != null) StringUtil.copyPartialMatches(token, options, results);
		return results;
	}
}
