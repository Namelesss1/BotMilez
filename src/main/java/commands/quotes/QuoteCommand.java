package commands.quotes;

import commands.IBotCommand;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
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
 * TODO: Ensure everything occurs within the channel the slash command was initialized
 * TODO: Ensure the user that initiated slash command is the one taking control
 * TODO: Have the bot edit its original message instead of sending a new one
 * TODO: Check the user has permissions to add/remove quote.
 */
public class QuoteCommand extends ListenerAdapter implements IBotCommand {

    private List<OptionData> options;

    private static final String BUTTON_ID_ADD = "addquote";
    private static final String BUTTON_ID_VIEW = "viewquote";
    private static final String SELECT_MENU_VIEW = "choiceview";

    private Map<User, QuoteAdder> activeAdders;
    private Map<User, QuoteViewer> activeViewers;

    private final QuoteViewer viewer;


    public QuoteCommand() {
        options = new ArrayList<>();
        activeAdders = new HashMap<>();
        activeViewers = new HashMap<>();
        viewer = new QuoteViewer();
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
                .addActionRow(
                        Button.success(BUTTON_ID_ADD, "Add a quote"),
                        Button.primary(BUTTON_ID_VIEW, "View quote"))
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
        if (event.getComponentId().equals(BUTTON_ID_VIEW)) {
            event.reply("What do you want to do?")
                    .addActionRow(
                            StringSelectMenu.create(SELECT_MENU_VIEW)
                                    .addOption("Random Quote", "random",
                                            "Get a random quote from this server")
                                    .addOption("All Quotes", "all",
                                            "View all server quotes")
                                    .addOption("Search", "search",
                                            "Search for a quote by who added the quote," +
                                            "person who said quote, keyword, or year.")
                                    .build()
                    ).queue();

        }
    }

    public void removeQuoteAdder(MessageReceivedEvent event, User user) {
        event.getJDA().removeEventListener(activeAdders.get(user));
        activeAdders.remove(user);
    }


    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        if (event.getComponentId().equals(SELECT_MENU_VIEW)) {
            if (event.getValues().get(0).equals("random")) {
                event.reply("Here is a random quote").queue();
                viewer.getRandomQuote(event.getGuild().getIdLong(), event.getChannel());
            }
        }
    }

    public QuoteViewer getQuoteViewer() {
        return viewer;
    }
}
