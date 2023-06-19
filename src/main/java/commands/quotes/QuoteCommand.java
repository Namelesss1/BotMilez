package commands.quotes;

import commands.IBotCommand;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.ArrayList;
import java.util.List;

/**
 * =============== Quotes ===============
 * This command allows a user to add notable quotes from
 * other people to the bot. These quotes can be viewed or
 * removed.
 */
public class QuoteCommand extends ListenerAdapter implements IBotCommand {

    private List<OptionData> options;
    private static final String BUTTON_ID_ADD = "addquote";

    public QuoteCommand() {
        options = new ArrayList<>();


    }

    @Override
    public String getName() {
        return "quotes";
    }

    @Override
    public String getDesc() {
        return "Add, remove, or see notable quotes from server members!";
    }

    @Override
    public List<OptionData> getOptions() {
        return options;
    }

    @Override
    public void doAction(SlashCommandInteractionEvent event) {
        event.reply("Choose which option you'd like to use")
                .addActionRow(Button.success(BUTTON_ID_ADD, "Add a quote"))
                .queue();

    }


    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getComponentId().equals(BUTTON_ID_ADD)) {
            QuoteCommand.addQuote(event);
        }
    }




    private static void addQuote(ButtonInteractionEvent event) {
        event.reply("Alright, now what command are we adding?").queue();
    }
}
