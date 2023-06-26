package commands.quotes;

import commands.IBotCommand;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.json.simple.JSONObject;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * =============== Quotes ===============
 * This command allows a user to add notable quotes from
 * other people to the bot. These quotes can be viewed or
 * removed.
 *
 * TODO: LOOK INTO ADDING CHOICES DROP DOWN BOX INSTEAD OF RAW INPUTS
 * TODO: Add View Quote And Delete Quote
 * TODO: JSON storage
 * TODO: Ensure everything occurs within the channel the slash command was initialized
 * TODO: Ensure the user that initiated slash command is the one taking control
 */
public class QuoteCommand extends ListenerAdapter implements IBotCommand {

    private List<OptionData> options;

    private static final String BUTTON_ID_ADD = "addquote";

    private Map<User, QuoteAdder> activeAdders;


    public QuoteCommand() {
        options = new ArrayList<>();
        activeAdders = new HashMap<>();
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
        User user = event.getUser();

        if (event.getComponentId().equals(BUTTON_ID_ADD)) {
            if (activeAdders.containsKey(user)) {
                event.reply("Error: You are already in the process of adding " +
                        "a quote.").queue();
            }
            else {
                event.reply("Alright, ").queue();
                activeAdders.put(user, new QuoteAdder(user, this, event));
                event.getJDA().addEventListener(activeAdders.get(user));
            }
        }
    }

    public void removeQuoteAdder(MessageReceivedEvent event, User user) {
        event.getJDA().removeEventListener(activeAdders.get(user));
        activeAdders.remove(user);
    }




    private QuoteContext readQuoteFromJSON() {
        QuoteContext context = new QuoteContext();



        return context;
    }


}
