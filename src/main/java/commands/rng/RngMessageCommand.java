package commands.rng;

import commands.IBotCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import util.IO;
import util.sentencegenerators.MarkovChain;

import java.util.ArrayList;
import java.util.List;

/**
 * ===== RNG MESSAGE COMMAND =====
 * This command outputs a randomly-generated sentence or message
 */
public class RngMessageCommand implements IBotCommand {

    private MarkovChain wordChain;
    private final String TRAINING_PATH = "resources/sentencegenerators/training/";

    public RngMessageCommand() {
        String[] data = IO.readAllWordsIntoArray(TRAINING_PATH + "training.txt");
        wordChain = new MarkovChain(3, data, 25);
    }
    @Override
    public String getName() {
        return "rng-message";
    }

    @Override
    public String getDesc() {
        return "Generates a random sentence or message!";
    }

    @Override
    public List<OptionData> getOptions() {
        return null;
    }

    @Override
    public void doAction(SlashCommandInteractionEvent event) {
        event.reply(wordChain.generateSentence()).queue();
    }
}
