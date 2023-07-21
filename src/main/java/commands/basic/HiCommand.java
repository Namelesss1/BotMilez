package commands.basic;

import commands.IBotCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import util.IO;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.awt.*;
import java.util.List;
import java.util.Random;

/**
 * =============== Hi Command: ===============
 *
 * Basic command used to test out and play with the discord API,
 * and get some basic interaction with the bot.
 *
 * When the command is used, the bot will pick one greeting from
 * a random selection of pre-selected greetings, and reply with it.
 */
public class HiCommand implements IBotCommand {

    private String[] args;
    private String[] greetings;

    private final String GREETINGS_PATH = "resources/greetings/greetings.txt";

    public HiCommand() {
        greetings = IO.readAllFileLinesIntoArray(GREETINGS_PATH);
    }

    public String getName() {
        return "hi";
    }

    public String getDesc() {
        return "Say hi to BotMilez!";
    }

   @Override
   public List<OptionData> getOptions() {
        return null;
   }

   @Override
    public void doAction(SlashCommandInteractionEvent event) {
        Random random = new Random();
        int choice = random.nextInt(greetings.length);

        event.reply(greetings[choice]).queue();
    }

    @Override
    public void getHelp(StringSelectInteractionEvent event) {
        String details = "The bot will reply back with a randomly-selected " +
                "greeting or remark! This command is nothing special, just " +
                "meant as a way to get used to programming using Discord API.";

        EmbedBuilder emBuilder = new EmbedBuilder();
        emBuilder.setTitle("**/" + getName() + "**");
        emBuilder.setFooter("Be polite and say hello.");
        emBuilder.setDescription(getDesc());
        emBuilder.setColor(Color.ORANGE);
        emBuilder.addField("Details", details, false);

        event.editMessageEmbeds(emBuilder.build()).setComponents().queue();
    }
}
