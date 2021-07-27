package me.Datatags.StatisticEditor;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;

public class Message {
	protected static StatisticEditor se = null;
	protected String message;
	protected Integer value = null;
	public Message(String type) {
		message = (type == null ? null : se.getMessage(type));
	}
	public Message setPlayer(OfflinePlayer player) {
		if (player != null) {
			setPlayer(player.getName());
		}
		return this;
	}
	public Message setPlayer(String player) {
		if (player != null) {
			message = message.replace("{player}", player);
		}
		return this;
	}
	public Message setStat(Statistic stat) {
		setStat(StatisticManager.getStatisticName(stat));
		return this;
	}
	public Message setStat(String stat) {
		if (stat != null) {
			message = message.replace("{stat}", stat);
		}
		return this;
	}
	public Message setArgument(EntityType argument) {
		if (argument != null) {
			setArgument(argument.toString());
		}
		return this;
	}
	public Message setArgument(Material argument) {
		if (argument != null) {
			setArgument(argument.toString());
		}
		return this;
	}
	public Message setArgument(String argument) {
		if (argument != null) {
			message = message.replace("{argument}", argument);
		}
		return this;
	}
	public Message setValue(int value) {
		this.value = value;
		if (value != Integer.MIN_VALUE) setValue(value + "");
		return this;
	}
	public Message setValue(String value) {
		if (value != null) {
			message = message.replace("{value}", value);
		}
		return this;
	}
	public String getMessage() {
		return message;
	}
	public Integer getValue() {
		return value;
	}
	public void send(CommandSender receiver) {
		receiver.sendMessage(getMessage());
	}
	public void reset(String type) {
		this.message = se.getMessage(type);
		this.value = null;
	}
}
