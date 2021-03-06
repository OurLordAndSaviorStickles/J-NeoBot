package com.Neobots2903.Discord.NeoBot;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;

import com.Neobots2903.Discord.NeoBot.interfaces.Command;
import com.Neobots2903.Discord.NeoBot.objects.DiscordUser;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class CommandHandler extends ListenerAdapter {

	@Override
	public void onMessageReceived(MessageReceivedEvent e) { // runs when message is received

		String id = e.getAuthor().getId(); // gets user id
		String name = e.getAuthor().getName(); // gets user name

		if (NeoBot.GetDiscordUser(id) == null)
			NeoBot.SaveDiscordUser(new DiscordUser(id, name)); // adds user to database if not already there

		if (NeoBot.GetDiscordUser(id).getBlocked())
			return; // if the user is blocked, ignore the command
		if (e.getAuthor().isBot())
			return; // quits if message came from bot
		// if(e.getChannelType() != ChannelType.TEXT) return; //quits if the message
		// didn't come from a normal guild text channel
		if (!e.getMessage().getContentRaw().startsWith(NeoBot.prefix)
				&& !e.getMessage().getContentRaw().startsWith(NeoBot.mention))
			return; // quits if message doesn't start with command prefix

		NeoBot.SaveDiscordUser(NeoBot.GetDiscordUser(id).setUseTime(LocalTime.now().toSecondOfDay())); 
		// save the time the command is used

		ArrayList<String> args;
		if (e.getMessage().getContentRaw().startsWith(NeoBot.prefix)) // message is chopped up into an ArrayList
																		// containing the command and arguments
			args = new ArrayList<String>(
					Arrays.asList(e.getMessage().getContentRaw().substring(NeoBot.prefix.length()).trim().split(" ")));
		else if (e.getMessage().getContentRaw().startsWith(NeoBot.mention + " " + NeoBot.prefix))
			args = new ArrayList<String>(Arrays.asList(e.getMessage().getContentRaw()
					.substring(NeoBot.mention.length() + 1 + NeoBot.prefix.length()).trim().split(" ")));
		else
			args = new ArrayList<String>(
					Arrays.asList(e.getMessage().getContentRaw().substring(NeoBot.mention.length()).trim().split(" ")));

		String cmd = args.remove(0).toLowerCase(); // command is removed from the arguments and put into its own variable

		Method[] commands = Commands.class.getMethods(); // gets all of the methods in the Commands class
		for (Method m : commands) { // for each method in Commands
			if (m.isAnnotationPresent(Command.class)) { // if method is a command...
				String annotationName = m.getAnnotation(Command.class).Name(); // gets the command name of the method
				ArrayList<String> Aliases = new ArrayList<String>(
						Arrays.asList(m.getAnnotation(Command.class).Aliases())); // gets the other aliases of the method
				if (annotationName.equals(cmd) || Aliases.contains(cmd)) { // if the method has the command we are looking for...
					
					if (!NeoBot.RoleExists(NeoBot.database.getModRoleId()))	//if mod role doesn't exist
						NeoBot.database.setModRoleId("");	//make sure role is set to nothing
						
					if (NeoBot.database.getModRoleId().equals("")) {	//if there is no mod role id saved, warn user
						Commands.sendMessage(e,"Warning: I don't what who the mods are! Please type `setMod [mod role name]` ASAP!", false);
					} else {	//otherwise, check if user has role
						if (m.getAnnotation(Command.class).SpecialPerms() &&	// if the command requires special permissions...
								!NeoBot.DiscordUserHasRole(e.getAuthor().getId(), NeoBot.database.getModRoleId())) {	//and the user isn't mod
							Commands.sendMessage(e, String.format("%s, you do not have permission to run this command!",e.getAuthor().getAsMention()), false);
							return;	//quit - they don't have permission
						}
					}
							
					Thread commandThread = new Thread() {
						public void run() {
							try {
								m.invoke(Commands.class, e, args);
							} catch (InvocationTargetException e1) {
								Throwable fe = e1.getCause();
								if (fe == null) fe = e1;
								Commands.sendMessage(e, 
										"`Error: Command crashed! This isn't supposed to happen :/" + 
										((e.getAuthor().getId().equals("215507031375740928")) ? 
										(System.lineSeparator() + "Since you're the Bot Tech, here's the error:" + System.lineSeparator() + fe.toString()) : "") + 
										"`", 
										false);
							} catch (Exception e2) {
								Commands.sendMessage(e, "`Critical: CommandHandler Failed!", false);
							}
						}
					};
					commandThread.start();

					return;
				}
			}
		}

		Commands.sendMessage(e,
				String.format("Sorry %s, I do not recognize this command.", e.getAuthor().getAsMention()), false);
	}

}