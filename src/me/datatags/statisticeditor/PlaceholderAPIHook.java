package me.datatags.statisticeditor;

import org.bukkit.Statistic;
import org.bukkit.entity.Player;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class PlaceholderAPIHook extends PlaceholderExpansion {
	private StatisticEditor se;
	public PlaceholderAPIHook(StatisticEditor se) {
		this.se = se;
	}
	@Override
	public String getAuthor() {
		return se.getDescription().getAuthors().get(0);
	}

	@Override
	public String getIdentifier() {
		return "stat";
	}

	@Override
	public String getVersion() {
		return se.getDescription().getVersion();
	}
	
	@Override
	public boolean canRegister() {
		return true;
	}
	
	@Override
	public boolean persist() {
	    return true;
	}
	
	@Override
	public String onPlaceholderRequest(Player p, String params) {
		String[] args = params.split("-");
		Statistic stat = StatisticManager.getStatistic(args[0]);
		return StatisticManager.getStatValue(p, stat, args.length > 1 ? args[1] : null).getValue() + "";
	}
}
