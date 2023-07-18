package commands.quotes;

import commands.IBotCommand;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.json.simple.JSONObject;
import util.EmbedPageBuilder;

import java.awt.*;
import java.lang.reflect.Array;
import java.util.*;
import java.util.List;

import static commands.quotes.QuoteIDs.*;

/**
 * =============== Quotes ===============
 * This command allows a user to add notable quotes from
 * other people to the bot. These quotes can be viewed or
 * removed.
 *
 * TODO: Ensure everything occurs within the channel the slash command was initialized
 * TODO: Check the user has permissions to add/remove quote.
 */
public class QuoteCommand extends ListenerAdapter implements IBotCommand {

    private List<OptionData> options;
    private final QuoteAdder adder;
    private final QuoteViewer viewer;
    private final QuoteRemover remover;


    public QuoteCommand() {
        options = new ArrayList<>();
        adder = new QuoteAdder(this);
        viewer = new QuoteViewer(this);
        remover = new QuoteRemover(this);
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
                        Button.primary(BUTTON_ID_VIEW, "View quote"),
                        Button.danger(BUTTON_ID_DELETE, "Delete a quote"))
                .queue();

    }

    @Override
    public void getHelp(StringSelectInteractionEvent event) {
        String details = "Did you see or hear someone say something funny? " +
                "Something interesting? Something weird? You can use this command " +
                "to store notable quotes from server members! These quotes can be " +
                "viewed, searched for, or removed later. The bot can store " +
                "either single stand-alone quotes, or multiple quotes that are all part " +
                "of the same context.";

        String standalone = "\"Hey I just woke up at 5pm\" - Bill";

        String context = "\"Do you like butterflies?\" - James\n" +
                "\"I do not know what that is\" - John\n" +
                "\"How do you not know what butterflies are?\" - James\n";

        String howToUse = "When using the command, three buttons will appear:\n" +
                "Add a quote(green), View a quote(Blue), or Remove a quote (red)" +
                "\n You will need to select the button for what you want to do." +
                "Details on how to do each of these are in the next pages (click" +
                "the buttons below to scroll!)";

        ArrayList<MessageEmbed.Field> fields = new ArrayList<>();
        fields.add(new MessageEmbed.Field(
                "Details",
                details,
                false
        ));
        fields.add(new MessageEmbed.Field(
                "Standalone Quote",
                standalone,
                true
        ));
        fields.add(new MessageEmbed.Field(
                "Quote with context",
                context,
                true
        ));
        fields.add(new MessageEmbed.Field(
                "How To Use",
                howToUse,
                false
        ));



        EmbedPageBuilder pageBuilder = new EmbedPageBuilder(4, fields, false);
        pageBuilder.setTitle("**/" + getName() + "**");
        pageBuilder.setFooter("This command will make a person be remembered forever");
        pageBuilder.setDescription(getDesc());
        pageBuilder.setColor(Color.YELLOW);
        pageBuilder.setPageCounterPlacement(EmbedPageBuilder.CounterEmbedComponent.AUTHOR);

        event.editMessageEmbeds(pageBuilder.build()).setComponents().queue();
    }


    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        User user = event.getUser();
        MessageChannel channel = event.getChannel();

        if (event.getComponentId().equals(BUTTON_ID_ADD)) {
            if (!adder.isEventListener()) {
                adder.setListening(event.getJDA());
            }
            event.editMessage("You selected: Add a quote").setComponents().queue();
            adder.onNewStatus(user, event.getChannel());
        }

        if (event.getComponentId().equals(BUTTON_ID_VIEW)) {

            if (!viewer.isEventListener()) {
                viewer.setListening(event.getJDA());
            }
            event.editMessage("You selected: View a quote").setComponents().queue();
            event.getChannel().sendMessage("What do you want to do?")
                    .addActionRow(
                            StringSelectMenu.create(SELECT_MENU_VIEW)
                                    .addOption("Random Quote", SELECT_CHOICE_RANDOM,
                                            "Get a random quote from this server")
                                    .addOption("All Quotes", SELECT_CHOICE_ALL,
                                            "View all server quotes")
                                    .addOption("Search", SELECT_CHOICE_SEARCH,
                                            "Search for a quote by who added the quote," +
                                            "person who said quote, keyword, or year.")
                                    .build()
                    ).queue();

        }

        if (event.getComponentId().equals(BUTTON_ID_DELETE)) {
            if (!viewer.isEventListener()) {
                viewer.setListening(event.getJDA());
            }
            if (!remover.isEventListener()) {
                remover.setListening(event.getJDA());
            }

            event.editMessage("You selected: Remove a quote").setComponents().queue();
            event.getChannel().sendMessage("Choose one of the options below.")
                    .addActionRow(
                            StringSelectMenu.create(SELECT_MENU_DELETE)
                                    .addOption("Choose from all quotes", SELECT_CHOICE_ALL,
                                            "Get a list of all quotes, choose one to delete")
                                    .addOption("Search for a quote", SELECT_CHOICE_SEARCH,
                                            "Search for a quote to delete")
                                    .build()
                    ).queue();
        }

    }


    /**
     * @return The quote viewer object to a class that needs to view quotes.
     */
    public QuoteViewer getQuoteViewer() {
        return viewer;
    }

    /**
     * @return The quote viewer object to a class that needs to view quotes.
     */
    public QuoteRemover getQuoteRemover() {
        return remover;
    }
}
