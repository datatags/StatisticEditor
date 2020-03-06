package me.AlanZ.StatisticEditor;

import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class StatisticManager {
	
	public static Message getStatValue(Player player, String statString, String arg) {
		return setStatValue(player, statString, arg, null);
	}
	public static Message setStatValue(Player player, String statString, String arg, Integer value) {
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
			if (value == null) {
				return new Message("player-stat").setPlayer(player).setStat(stat).setValue(player.getStatistic(stat));
			}
			player.setStatistic(stat, value);
			return new Message("stat-set").setPlayer(player).setStat(stat).setValue(value);
		} else if (stat.getType() == Statistic.Type.ENTITY) {
			if (arg.equalsIgnoreCase("all")) {
				CompoundMessage cmsg = new CompoundMessage("all-stat-with-argument");
				for (EntityType entity : EntityType.values()) {
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
			if (value == null) {
				return new Message("player-stat-with-argument").setPlayer(player).setStat(stat).setValue(player.getStatistic(stat, entity)).setArgument(entity);
			}
			player.setStatistic(stat, entity, value);
			return new Message("set-stat-with-argument").setPlayer(player).setStat(stat).setValue(value).setArgument(entity);
		} else {
			boolean item = stat.getType() == Statistic.Type.ITEM;
			if (arg.equalsIgnoreCase("all")) {
				CompoundMessage cmsg = new CompoundMessage("all-stat-with-argument");
				for (Material mat : Material.values()) {
					if ((item && mat.isItem()) || (!item && mat.isBlock())) {
						if (player.getStatistic(stat, mat) == 0) continue; // skip the ones with no value
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
			if (value == null) {
				return new Message("player-stat-with-argument").setPlayer(player).setStat(stat).setValue(player.getStatistic(stat, mat)).setArgument(mat);
			}
			player.setStatistic(stat, mat, value);
			return new Message("stat-set-with-argument").setPlayer(player).setStat(stat).setValue(value).setArgument(mat);
		}
	}
}
