package me.AlanZ.StatisticEditor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class StatisticManager {
	
	public static Message getStatValue(Player player, String statString, String arg) {
		return setStatValue(player, statString, arg, null, false);
	}
	public static Message setStatValue(Player player, String statString, String arg, Integer rawValue, boolean relative) {
		Statistic stat;
		try {
			stat = Statistic.valueOf(statString.toUpperCase());
		} catch (IllegalArgumentException e) {
			return new Message("invalid-stat").setStat(statString);
		}
		// two birds, one stone by comparing the booleans
		if ((arg == null) != (stat.getType() == Statistic.Type.UNTYPED)) {
			if (arg == null) {
				return new Message("stat-missing-argument").setArgument(stat.getType().toString()).setStat(stat);
			} else {
				return new Message("stat-extra-argument").setArgument(arg).setStat(stat);
			}
		} else if (arg == null && stat.getType() == Statistic.Type.UNTYPED) {
			if (rawValue == null) {
				return new Message("player-stat").setPlayer(player).setStat(stat).setValue(player.getStatistic(stat));
			}
			int value = rawValue;
			if (relative) {
				value += player.getStatistic(stat);
			}
			if (value < 0) {
				return new Message("negative-value").setValue(value);
			}
			player.setStatistic(stat, value);
			return new Message("stat-set").setPlayer(player).setStat(stat).setValue(value);
		} else if (stat.getType() == Statistic.Type.ENTITY) {
			boolean nonzero = arg.equalsIgnoreCase("nonzero");
			if (arg.equalsIgnoreCase("all") || nonzero) {
				CompoundMessage cmsg = new CompoundMessage("all-stat-with-argument");
				for (EntityType entity : EntityType.values()) {
					if (entity == EntityType.UNKNOWN) continue;
					if (nonzero && player.getStatistic(stat, entity) == 0) continue;
					cmsg.add().setPlayer(player).setStat(stat).setArgument(entity).setValue(player.getStatistic(stat, entity));
				}
				return cmsg;
			}
			EntityType entity;
			try {
				entity = EntityType.valueOf(arg.toUpperCase());
			} catch (IllegalArgumentException e) {
				return new Message("invalid-entity").setArgument(arg);
			}
			if (rawValue == null) {
				return new Message("player-stat-with-argument").setPlayer(player).setStat(stat).setValue(player.getStatistic(stat, entity)).setArgument(entity);
			}
			int value = rawValue;
			if (relative) {
				value += player.getStatistic(stat, entity);
			}
			if (value < 0) {
				return new Message("negative-value").setValue(value);
			}
			player.setStatistic(stat, entity, value);
			return new Message("stat-set-with-argument").setPlayer(player).setStat(stat).setValue(value).setArgument(entity);
		} else {
			boolean item = stat.getType() == Statistic.Type.ITEM;
			boolean nonzero = arg.equalsIgnoreCase("nonzero");
			if (arg.equalsIgnoreCase("all") || nonzero) {
				CompoundMessage cmsg = new CompoundMessage("all-stat-with-argument");
				for (Material mat : Material.values()) {
					if ((item && mat.isItem()) || (!item && mat.isBlock())) {
						if (nonzero && player.getStatistic(stat, mat) == 0) continue; // skip the ones with no value
						cmsg.add().setPlayer(player).setStat(stat).setValue(player.getStatistic(stat, mat)).setArgument(mat);
					}
				}
				return cmsg;
			}
			Material mat = Material.matchMaterial(arg);
			if (mat == null) {
				return new Message("invalid-material").setArgument(arg);
			}
			if (item && !mat.isItem()) {
				return new Message("not-an-item").setArgument(mat);
			} else if (!item && !mat.isBlock()) {
				return new Message("not-a-block").setArgument(mat);
			}
			if (rawValue == null) {
				return new Message("player-stat-with-argument").setPlayer(player).setStat(stat).setValue(player.getStatistic(stat, mat)).setArgument(mat);
			}
			int value = rawValue;
			if (relative) {
				value += player.getStatistic(stat, mat);
			}
			if (value < 0) {
				return new Message("negative-value").setValue(value);
			}
			player.setStatistic(stat, mat, value);
			return new Message("stat-set-with-argument").setPlayer(player).setStat(stat).setValue(value).setArgument(mat);
		}
	}
	public static Message getAllStats(Player target, Statistic stat, boolean nonZero) {
		List<Statistic> sortedStats = new ArrayList<Statistic>();
		for (Statistic loopStat : Statistic.values()) {
			sortedStats.add(loopStat);
		}
		sortedStats.sort(Comparator.comparing(Statistic::toString));
		CompoundMessage msg = new CompoundMessage();
		if (stat == null) {
			for (Statistic loopStat : sortedStats) {
				if (loopStat.getType() != Statistic.Type.UNTYPED) {
					msg.add("all-stat-requires-argument").setStat(loopStat.toString()).setArgument(loopStat.getType().toString());
				} else {
					int value = target.getStatistic(loopStat);
					if (nonZero && value == 0) continue;
					msg.add("all-stat").setStat(loopStat.toString()).setValue(target.getStatistic(loopStat) + "");
				}
			}
		} else {
			if (stat.getType() == Statistic.Type.UNTYPED) {
				return new Message("player-stat").setPlayer(target).setStat(stat).setValue(target.getStatistic(stat));
			} else if (stat.getType() == Statistic.Type.ENTITY) {
				for (EntityType type : EntityType.values()) {
					if (type == EntityType.UNKNOWN) continue; // it freaks out if you don't skip this one
					int value = target.getStatistic(stat, type);
					if (nonZero && value == 0) continue;
					msg.add("all-stat-with-argument").setPlayer(target).setStat(stat).setArgument(type).setValue(value);
					return msg;
				}
			}
			boolean item = stat.getType() == Statistic.Type.ITEM;
			for (Material mat : Material.values()) {
				if (item && !mat.isItem())   continue;
				if (!item && !mat.isBlock()) continue;
				int value = target.getStatistic(stat, mat);
				if (nonZero && value == 0) continue;
				msg.add("all-stat-with-argument").setPlayer(target).setStat(stat).setArgument(mat).setValue(value);
			}
		}
		return msg;
	}
}
