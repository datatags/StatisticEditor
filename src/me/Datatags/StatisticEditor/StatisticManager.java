package me.Datatags.StatisticEditor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class StatisticManager {
	private static boolean isVanillaNames = false;
	private static BiMap<Statistic,String> vanillaNames = null;
	public static void setVanillaNames(boolean vanillaNames) {
		isVanillaNames = vanillaNames;
	}
	public static Statistic getStatistic(String statString) {
		if (!isVanillaNames) {
			try {
				return Statistic.valueOf(statString.toUpperCase());
			} catch (IllegalArgumentException e) {
				return null;
			}
		}
		if (vanillaNames == null) setupNames();
		return vanillaNames.inverse().get(statString);
	}
	@SuppressWarnings("unchecked")
	private static void setupNames() {
		try {
			String bukkitVersion = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
			Class<?> craftStatClass = Class.forName("org.bukkit.craftbukkit." + bukkitVersion + ".CraftStatistic");
			Field mapField = craftStatClass.getDeclaredField("statistics");
			mapField.setAccessible(true);
			BiMap<Statistic, Object> statMap = ((BiMap<Object, Statistic>) mapField.get(null)).inverse();
			Class<?> keyNameClass = statMap.values().iterator().next().getClass();
			vanillaNames = HashBiMap.create(statMap.size());
			if (keyNameClass.getName().equals("java.lang.String")) { // 1.12.2 and under
				statMap.forEach((s,v) -> vanillaNames.put(s, ((String)v).replace("stat.", "")));
				return;
			}
			Field[] fields = keyNameClass.getDeclaredFields();
			Field keyNameField = fields[fields.length - 1];
			keyNameField.setAccessible(true);
			for (Statistic stat : Statistic.values()) {
				vanillaNames.put(stat, (String) keyNameField.get(statMap.get(stat)));
			}
		} catch (SecurityException | IllegalAccessException | IllegalArgumentException | ClassNotFoundException | NoSuchFieldException e) {
			e.printStackTrace();
		}
	}
	public static String getStatisticName(Statistic stat) {
		if (!isVanillaNames) return stat.name();
		if (vanillaNames == null) setupNames();
		return vanillaNames.get(stat);
	}
	public static Message getStatValue(Player player, Statistic stat, String arg) {
		return setStatValue(player, stat, arg, null, false);
	}
	public static Message setStatValue(Player player, Statistic stat, String arg, Integer rawValue, boolean relative) {
		if (stat == null) {
			return new Message("invalid-stat").setStat("null");
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
		sortedStats.sort(Comparator.comparing(s -> getStatisticName(s)));
		CompoundMessage msg = new CompoundMessage();
		if (stat == null) {
			for (Statistic loopStat : sortedStats) {
				if (loopStat.getType() != Statistic.Type.UNTYPED) {
					msg.add("all-stat-requires-argument").setStat(loopStat).setArgument(loopStat.getType().toString());
				} else {
					int value = target.getStatistic(loopStat);
					if (nonZero && value == 0) continue;
					msg.add("all-stat").setStat(loopStat).setValue(target.getStatistic(loopStat));
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
