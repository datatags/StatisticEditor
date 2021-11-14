package me.datatags.statisticeditor;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;

public class CompoundMessage extends Message {
	protected List<String> messages = new ArrayList<>();
	protected String type = null;
	public CompoundMessage() {
		super(null);
	}
	public CompoundMessage(String type) {
		this();
		this.type = type;
	}
	public Message add() {
		return add(this.type);
	}
	public Message add(String newType) {
		if (message != null) messages.add(message);
		this.reset(newType);
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
