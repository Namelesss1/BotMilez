package commands.basic;

import commands.IBotCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;
import java.util.Random;

/**
 * Hi Command:
 *
 * Basic command used to test out and play with the discord API,
 * and get some basic interaction with the bot.
 *
 * When the command is used, the bot will pick one greeting from
 * a random selection of pre-selected greetings, and reply with it.
 */
public class HiCommand implements IBotCommand {

    private String[] args;

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

    public void doAction(SlashCommandInteractionEvent event) {
        Random random = new Random();
        int choice = random.nextInt(6);

        switch (choice) {
            case 0:
                event.reply("Hi!").queue();
                break;
            case 1:
                event.reply("Howdy!").queue();
                break;
            case 2:
                event.reply("Greetings, "
                        + event.getUser().getName() + ".").queue();
                break;
            case 3:
                event.reply("YOOO WHATS UP, " +
                        event.getUser().getName() + "?").queue();
                break;
            case 4:
                event.reply("Airmilez says hello back!").queue();
                break;
            case 5:
                event.reply("Hi.. wait a sec, you're not Airmilez!");
                break;
        }
    }

}
