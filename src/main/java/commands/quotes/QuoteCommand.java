package commands.quotes;

import commands.IBotCommand;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import util.EmbedPageBuilder;

import java.awt.*;
import java.util.*;
import java.util.List;

import static commands.quotes.QuoteIDs.*;
import static util.EmbedPageBuilder.*;

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

    private EmbedPageBuilder helpEmbed;


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

        /* ------ Page 1: Basic Overview ------ */
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
                "\n You will need to select the button for what you want to do. " +
                "Details on how to do each of these are in the next pages (click " +
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


        /* ------ Page 2: Adding a quote ------ */

        String quoteParts = "A quote consists of:\n" +
                "The quote itself, Who said the quote, and the year it was said. " +
                "The year optional, you don't need to specify it. " +
                "A single quote can be added to the bot at once, " +
                "or you can add multiple quotes at once that are all part " +
                "of the same conversational context! There are two ways " +
                "of adding quotes to the bot below.";

        String adding =
                "The bot will prompt you to answer some questions " +
                "relating to the quote you want to add such as what the " +
                "quote is, who said the quote, etc. This is the simpler but " +
                "more tedious way of adding a quote. There is a very recommended " +
                "shortcut to use described below.";

        String addingShortcut = "To quickly add a quote, use the following format:\n" +
                "```\"quote here\" WhoSaidTheQuote YearQuoteWasSaid(OPTIONAL)```\n" +
                "To add multiple quotes that are all part of the conversation context, " +
                "simply separate each quote by a new line, for example:\n" +
                "```\"Who drank all the cups?\" Bob 2023\n" +
                "\"Not me, I don't drink cups.\" Bill 2023```\n";

        String addConfirmation = "Once you're done adding a quote, the bot will " +
                "show you the quote you added and prompt you whether the quote " +
                "is correct or not. If correct, the quote will then be added to the " +
                "bot for later viewing!";

        fields.add(new MessageEmbed.Field(
                "Parts of a Quote/Context",
                quoteParts,
                false
        ));
        fields.add(new MessageEmbed.Field(
                "Simple but tedious method",
                adding,
                false
        ));
        fields.add(new MessageEmbed.Field(
                "Quick method (Recommended)",
                addingShortcut,
                false
        ));
        fields.add(new MessageEmbed.Field(
                "Add confirmation",
                addConfirmation,
                false
        ));


        /* ------ Page 3 : Viewing a quote ------ */

        String view = "When viewing quotes, you get three choices:\n" +
                "Viewing a random quote, viewing all quotes, or viewing " +
                "all quotes that match a certain search term " +
                "(searching for a quote)";

        String randomQuote = "When selecting the random quote option " +
                "the bot will send one randomly-selected quote out of all " +
                "server quotes stored in the bot.";

        String allQuotes = "When selecting the all quotes option," +
                "The bot will send an embed with all of the quotes for this server " +
                "stored in the bot for viewing. If the server all has a lot " +
                "of quotes, the embed will have more than one page, and you " +
                "can click on the buttons below to go to the next page, previous, " +
                "or close the embed once you're done. (Like the buttons below now!)";

        String search = "When selecting the search option, " +
                "you can see all quotes that match your search terms. " +
                "You can select from four options:\n" +
                "By quote: Get all quotes matching what was said\n" +
                "By Speaker: Get all quotes by a particular person who said quotes\n" +
                "By Author: Get all quotes added to the bot by a particular user,\n" +
                "By Year: Get all quotes said in a certain year (or all un-dated quotes)" +
                "The bot will return an embed with possibly more than one page of " +
                "all quotes matching the search terms (or no quotes if no matches found) ";
        fields.add(new MessageEmbed.Field(
                "Options",
                view,
                false
        ));
        fields.add(new MessageEmbed.Field(
                "Getting a Random Quote",
                randomQuote,
                false
        ));
        fields.add(new MessageEmbed.Field(
                "Get All quotes",
                allQuotes,
                false
        ));
        fields.add(new MessageEmbed.Field(
                "Search for a quote",
                search,
                false
        ));

        /* ------- Page 4: Removing a quote ------ */
        String choosing = "When removing a quote, you are prompted to either select " +
                "from a list of all the quotes, or you can search for a quote to remove. " +
                "both work in the same way as viewing a quote.";
        String removing = "The bot will show you a list of quotes. All quotes will be " +
                "numbered with a \"#\" symbol on top of it. Type in the number in front of " +
                "the # symbol to specify the quote you wish to remove from the bot.";
        String removeConfirmation = "Once you select a quote to remove, the bot will show you the " +
                "specific quote you selected and will prompt you whether this is the correct quote " +
                "you want to remove or not. If it is, the quote will be removed.";
        String caution = "Once a quote is removed, it is completely erased without a trace " +
                "from the bot. You cannot recover the quote anymore, so be sure you want to " +
                "remove it before doing so! The closest thing you can do to recovering it is " +
                "it re-add it to the bot using the add feature of this bot (page 2)";
        fields.add(new MessageEmbed.Field(
                "Methods",
                choosing,
                false
        ));
        fields.add(new MessageEmbed.Field(
                "Choosing a quote to remove",
                removing,
                false
        ));
        fields.add(new MessageEmbed.Field(
                "Removal confirmation",
                removeConfirmation,
                false
        ));
        fields.add(new MessageEmbed.Field(
                "A word of caution",
                caution,
                false
        ));


        helpEmbed = new EmbedPageBuilder(4, fields, false);
        helpEmbed.setTitle("**/" + getName() + "**");
        helpEmbed.setFooter("This command will make a person be remembered forever");
        helpEmbed.setDescription(getDesc());
        helpEmbed.setColor(Color.YELLOW);
        helpEmbed.setPageCounterPlacement(EmbedPageBuilder.EmbedComponent.AUTHOR);

        helpEmbed.setPageTitle(2, "Adding a Quote");
        helpEmbed.setPageDescription(2, "After using the command, click on the green button.");
        helpEmbed.setPageFooter(2, "+1 to your awesome list of quotes.");
        helpEmbed.setPageColor(2, Color.GREEN);

        helpEmbed.setPageTitle(3, "Viewing Quotes");
        helpEmbed.setPageDescription(3, "After using the command, click on the Blue button.");
        helpEmbed.setPageFooter(3, "Gotta look back and laugh at something funny they said");
        helpEmbed.setPageColor(3, Color.BLUE);

        helpEmbed.setPageTitle(4, "Removing a Quote");
        helpEmbed.setPageDescription(4, "After using the command, click on the Red button.");
        helpEmbed.setPageFooter(4, "This quote embarrasses me, lets remove it.");
        helpEmbed.setPageColor(4, Color.RED);

        event.editMessageEmbeds(helpEmbed.build()).setComponents()
                .setActionRow(PageBuilderActionRow).queue();
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


        /* For Embed page */
        if (event.getComponentId().equals(BUTTON_NEXT_PAGE) ||
            event.getComponentId().equals(BUTTON_PREVIOUS_PAGE) ||
            event.getComponentId().equals(DELETE_EMBED)) {

            helpEmbed.scroll(event);
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
