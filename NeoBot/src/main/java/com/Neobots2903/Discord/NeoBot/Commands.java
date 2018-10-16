package com.Neobots2903.Discord.NeoBot;


import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Random;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import com.Neobots2903.Discord.NeoBot.interfaces.Command;
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;

import net.dv8tion.jda.client.exceptions.VerificationLevelException;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;

public class Commands {

	public static ArrayList<String> reactionMessages = new ArrayList<String>();

	public static void sendMessage(User user, TextChannel mChannel, String msg, boolean isPrivate) {
		try {
			if (isPrivate) {
				user.openPrivateChannel().queue((channel) -> channel
						.sendMessage(msg).queue());
			} else {
				mChannel.sendMessage(msg).queue();
			}
		} catch (InsufficientPermissionException | VerificationLevelException e) {
			user.openPrivateChannel().queue((channel) -> channel
					.sendMessage(String.format(
							"%s currently does not have permission to speak in %s, %s.\n"
									+ "If you feel this is a mistake, please contact the server administrator.",
									NeoBot.jda.getSelfUser().getName(), mChannel.getGuild().getName(), mChannel.getName()))
					.queue());
		} catch (IllegalArgumentException e) {
			sendMessage(user, mChannel, String.format("%s, I can't send an empty message!", user.getAsMention()),false);
		}
	}
	
	public static void sendMessage(MessageReceivedEvent e, String msg, boolean isPrivate) {
		sendMessage(e.getAuthor(),e.getTextChannel(),msg,isPrivate);
	}
	
	public static void sendMessage(Message m, String msg, boolean isPrivate) {
		sendMessage(m.getAuthor(),m.getTextChannel(),msg,isPrivate);
	}

	@Command(Name = "help",
			Summary = "Lists information about available commands",
			Syntax = "help [optional:command]")
	public static void IcanHELPyoU(MessageReceivedEvent e, ArrayList<String> args) {

		String helpMsg = "";

		Method[] commands = Commands.class.getMethods();
		for (Method m : commands) {
			if (m.isAnnotationPresent(Command.class)) {

				String Name = "";
				String formattedAliases = "";
				String Summary = "";
				String SpecialPerms = "";
				String Syntax = "";
				boolean matchingAlias = false;

				Name = m.getAnnotation(Command.class).Name();
				String[] Aliases = m.getAnnotation(Command.class).Aliases();
				for (String a : Aliases) {
					if (args.contains(a)) matchingAlias = true;
				}
				if (args.isEmpty() || args.contains(Name) || matchingAlias) {
					if (m.getAnnotation(Command.class).Aliases().length > 0)
						formattedAliases = " (also " + String.join(", ", Aliases) + ")";
					Summary = m.getAnnotation(Command.class).Summary();
					if (!m.getAnnotation(Command.class).SpecialPerms().isEmpty())
						SpecialPerms = " **<" + m.getAnnotation(Command.class).SpecialPerms() + ">**";
					if (!m.getAnnotation(Command.class).Syntax().isEmpty())
						Syntax = " ~ `" + m.getAnnotation(Command.class).Syntax() + "`";
					helpMsg += String.format("`%s%s%s` - %s%s%s\n",NeoBot.prefix,Name,formattedAliases,Summary,SpecialPerms,Syntax);
				}
			}
		}
		if (helpMsg == "") sendMessage(e,String.format("Sorry %s, the requested command(s) do not exist.", e.getAuthor().getAsMention()),false);
		else sendMessage(e, helpMsg, false);
	}

	@Command(Name = "echo",
			Aliases = {"repeat","copy"},
			Summary = "I, Yakkie, will copy what you say",
			Syntax = "echo [command]")
	public static void wowAnECHOCommand(MessageReceivedEvent e, ArrayList<String> args) {

		sendMessage(e,String.join(" ", args),false);
		if (e.getChannelType() == ChannelType.TEXT) {
			e.getMessage().delete().queue();
		}
	}

	@Command(Name = "test", 
			Summary = "My personal testing command while implementing features. TESTING TESTING 123",
			SpecialPerms = "Only I can run this :P")
	public static void thisIsATEST123(MessageReceivedEvent e, ArrayList<String> args) {

		if (!e.getAuthor().getId().equals("215507031375740928")) return;

		int EXCEPTION = 1/0;
		sendMessage(e,"Test code complete!",false);
		e.getMessage().delete().queue();
	}
	
	@Command(Name = "panic",
			Summary = "An urgent message that will audibly relay to the NeoBots. Only use when necessary!")
	public static void WARNINGWARNINGaaHHHHHHH(MessageReceivedEvent e, ArrayList<String> args) {
		try {
			
	        Clip clip = AudioSystem.getClip();
	        AudioInputStream inputStream = AudioSystem.getAudioInputStream(
	        	NeoBot.class.getClassLoader().getResourceAsStream("Warning.wav"));
	        clip.open(inputStream);
	        clip.start();
	        
	        while(clip.getMicrosecondLength() != clip.getMicrosecondPosition())
	        {
	        }
			
			  Voice voice;
			  System.setProperty("freetts.voices", 
		                "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");  
			  VoiceManager voiceManager = VoiceManager.getInstance();
			  voice = voiceManager.getVoices()[1];
			  voice.allocate();
			  voice.speak(String.join(" ", args));
			  voice.deallocate();
			  
	      } catch (Exception ex) {
	        System.err.println(ex.getMessage());
	      }
	}

	@Command(Name = "random", 
			Summary = "Provides a random number between 0 and 100")
	public static void superRanDOmLOL(MessageReceivedEvent e, ArrayList<String> args) {

		Random rnd = new Random();
		Integer randomInt = rnd.nextInt(100);
		sendMessage(e,randomInt.toString(),false);
	}

}