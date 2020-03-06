package me.AlanZ.StatisticEditor;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;

public class CompoundMessage extends Message {
	private List<String> messages = new ArrayList<>();
	private String type;
	public CompoundMessage(String type) {
		super(type);
		this.type = type;
		this.message = null;
	}
	public Message add() {
		if (message != null) messages.add(message);
		this.reset(type);
		return this;
	}
	@Override
	public void send(CommandSender receiver) {
		if (message != null) messages.add(message);
		if (messages.size() == 0) {
			this.reset("all-stat-no-results");
			receiver.sendMessage(getMessage());
			return;
		}
		for (String msg : messages) {
			receiver.sendMessage(msg);
		}
	}
}
