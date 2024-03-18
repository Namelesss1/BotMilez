package commands.quotes;

import util.EmbedPageBuilder;
import util.IO;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.awt.*;
import java.util.*;
import java.util.List;

import static botmilez.config.QUOTE_FILE_PREFIX;
import static botmilez.config.QUOTE_FILE_SUFFIX;
import static util.EmbedPageBuilder.*;
import static commands.quotes.QuoteIDs.*;


/**
 * QuoteViewer class: A class that is responsible for the reading of JSON files
 * that have quotes stored in them, and methods that return quote requests to
 * a user that asked for them. Supports searching for quotes as well.
 */
public class QuoteViewer extends ListenerAdapter {

    private String scrollId;

    /* serverId -> server's quotes */
    private Map<Long, List<QuoteContext>> quotesArrays;

    /* serverId -> if quotes have been modified since last update */
    private Map<Long, Boolean> isModified;
    /* messageId -> current embed page number */
    private Map<Long, EmbedPageBuilder> pageEmbeds;

    /* User -> search terms */
    private Map<User, String> usersSearching;

    /* Limit to how many quotes can be displayed in an embed */
    public static final int MAX_QUOTES_PER_EMBED = 15;

    private boolean isEventListener = false;

    private QuoteCommand command;

    public QuoteViewer(QuoteCommand instance) {
        quotesArrays = new HashMap<>();
        isModified = new HashMap<>();
        pageEmbeds = new HashMap<>();
        usersSearching = new HashMap<>();
        command = instance;
        scrollId = "quoteview";
    }

    /**
     * @return whether this object is currently an event listener.
     */
    public boolean isEventListener() {
        return isEventListener;
    }

    /**
     * Set this object as an event listener to the jda to listen for events
     * @param jda JDA to add this object as a listener to
     */
    public void setListening(JDA jda) {
        isEventListener = true;
        jda.addEventListener(this);
    }

    /**
     * Adds a new user to the set of users doing a search.
     * @param user user to add
     * @param searchBy search filters chosen by the user.
     */
    public void addToUsersSearching(User user, String searchBy) {
        usersSearching.put(user, searchBy);
    }

    /**
     * @param user User
     * @return true if given user is doing a search. False if not.
     */
    public boolean userIsSearching(User user) {
        return usersSearching.containsKey(user);
    }


    /**
     * Reads from a json file containing quotes and converts this file
     * into a list of QuoteContext objects that will be ready for viewing by
     * server members.
     *
     * @param path JSON file location to read from
     * @return a list of QuoteContext objects representing server quotes.
     */
    public static List<QuoteContext> jsonToQuoteContext(String path) {
        JSONArray contextArray = (JSONArray)IO.readJson(path);
        List<QuoteContext> quoteContexts = new ArrayList<>();

        for (Object contextElement : contextArray) {
            QuoteContext context = new QuoteContext();

            JSONObject contextObj = (JSONObject)contextElement;
            String author = (String)contextObj.get("author");
            context.setAuthor(author);
            JSONArray quoteArray = (JSONArray)contextObj.get("context");

            for (Object quoteElement : quoteArray) {
                Quote quote = new Quote();
                JSONObject quoteObj = (JSONObject)quoteElement;
                quote.setName((String)quoteObj.get("name"));
                quote.setQuote((String)quoteObj.get("quote"));
                Long year = (Long)quoteObj.get("year");
                if (year == null) {
                    quote.setYear(null);
                }
                else {
                    quote.setYear(Long.toString(year));
                }
                context.addQuoteToContext(quote);
            }
            quoteContexts.add(context);
        }


        return quoteContexts;

    }


    /**
     * Checks if the quotes of a server have either not been updated since a
     * previous addition or deletion, or have not been loaded at all. This
     * method will load the up-to-date quotes into the quote array for viewing
     * if needed.
     *
     * @param guildId id of the server to load quotes from
     * @return whether a needed update was successful or not
     */
    private boolean updateQuotesArrays(long guildId) {
        String path = QUOTE_FILE_PREFIX + guildId + QUOTE_FILE_SUFFIX;

        /* If quotes for this guild are not loaded, load them */
        if (!quotesArrays.containsKey(guildId)) {
            List<QuoteContext> quoteContexts = jsonToQuoteContext(path);
            if (quoteContexts == null) {
                return false;
            }
            quotesArrays.put(guildId, quoteContexts);
            isModified.put(guildId, false);
        }

        /* If quotes of this server were modified, update */
        if (isModified.get(guildId)) {
            List<QuoteContext> quoteContexts = jsonToQuoteContext(path);
            if (quoteContexts == null) {
                return false;
            }
            quotesArrays.replace(guildId, quoteContexts);
            isModified.replace(guildId, false);
        }

        return true;
    }


    /**
     * Attempts to load the up-to-date quotes for a server, and checks whether
     * the quotes are able to be loaded and non-empty. If not, a message is sent
     * to the user notifying them that the quote could not be loaded for their
     * request.
     *
     * @param guildId id for server to load quotes from
     * @param channel channel to send message to
     * @return true if successful non-empty quote loading, false if not
     */
    private boolean validateQuotesArrays(long guildId, MessageChannel channel) {
        if (!updateQuotesArrays(guildId)) {
            channel.sendMessage("An error has occurred. Cannot load quotes!")
                    .queue();
            return false;
        }

        if (quotesArrays.get(guildId).isEmpty()) {
            channel.sendMessage("No quotes are stored in the bot!" +
                    "You can add more quotes by using the add option.").queue();
            return false;
        }

        return true;
    }


    /**
     * Called by other methods to indicate the quotes of a server have been modified in some way,
     * to indicate to this class that the quotes array needs to be re-loaded with the
     * newly updated quotes.
     *
     * @param guildId id of the server that has had its quotes modified
     */
    public void setAsModified(long guildId) {
        isModified.replace(guildId, true);
    }


    /**
     * Sends a randomly selected quote belonging to the server
     * @param guildId server id to load quotes from
     * @param channel channel in server to send message to
     */
    public void getRandomQuote(long guildId, MessageChannel channel) {

        if (!validateQuotesArrays(guildId, channel)) {
            return;
        }

        List<QuoteContext> quoteContexts = quotesArrays.get(guildId);

        Random random = new Random();
        int index = random.nextInt(quoteContexts.size());
        QuoteContext quoteContext = quoteContexts.get(index);

        quoteContext.sendQuoteContext(channel);
    }


    /**
     * Initializes a page-scrollable embed that contains all current quotes
     * on a server.
     * @param event the event that triggered this method
     */
    public void initAllQuoteEmbed(StringSelectInteractionEvent event, boolean isDeleting) {

        long guildId = event.getGuild().getIdLong();
        MessageChannel channel = event.getChannel();

        if (!validateQuotesArrays(guildId, channel)) {
            return;
        }

        List<QuoteContext> contexts = quotesArrays.get(guildId);

        if (isDeleting) {
            command.getQuoteRemover().setDeletionCandidates(event.getUser(), contexts);
        }

        List<MessageEmbed.Field> quoteFields = new ArrayList<>();
        for (QuoteContext context : contexts) {
            quoteFields.add(context.getAsField());
        }

        EmbedPageBuilder emBuilder = new EmbedPageBuilder(MAX_QUOTES_PER_EMBED, quoteFields,
                isDeleting, scrollId);
        emBuilder.setTitle("All quotes");
        emBuilder.setColor(Color.YELLOW);
        emBuilder.setPageCounterPlacement(EmbedComponent.FOOTER);

        channel.sendMessageEmbeds(emBuilder.build()).addActionRow(emBuilder.getPageBuilderActionRow())
                .queue((message) ->
                {
                    long msgId = message.getIdLong();
                    pageEmbeds.put(msgId, emBuilder);
                });

    }

    /**
     * Initializes a page-scrollable embed for quotes that match a given search term
     *
     * @param event event that triggered this method
     * @param contexts list of quotes contexts that matched the search term to be added to embed
     * @param searchTerm the search term entered by the user.
     */
    public void initSearchEmbed(MessageReceivedEvent event, List<QuoteContext> contexts,
                                String searchTerm) {

        List<MessageEmbed.Field> quoteFields = new ArrayList<>();
        for (QuoteContext context : contexts) {
            quoteFields.add(context.getAsField());
        }
        if (contexts.isEmpty()) {
            MessageEmbed.Field field = new MessageEmbed.Field(
                    "No quotes found", /* name */
                    "", /* Value */
                    false /* inline */
            );
            quoteFields.add(field);
        }

        boolean isDeleting = command.getQuoteRemover().getUserState().containsKey(event.getAuthor());

        if (isDeleting) {
            command.getQuoteRemover().setDeletionCandidates(event.getAuthor(), contexts);
        }

        String searchBy = usersSearching.get(event.getAuthor());

        EmbedPageBuilder emBuilder = new EmbedPageBuilder(MAX_QUOTES_PER_EMBED, quoteFields,
                isDeleting, scrollId);

        if (searchBy.equals(SELECT_CHOICE_BY_SAID)) {
            emBuilder.setTitle("Search Results for term: \"" + searchTerm + "\"");
        }
        else if (searchBy.equals(SELECT_CHOICE_BY_SPEAKER)) {
            emBuilder.setTitle("Search Results for user: " + searchTerm);
        }
        else if (searchBy.equals(SELECT_CHOICE_BY_YEAR)) {
            emBuilder.setTitle("Search Results for year: " + searchTerm);
        }
        else if (searchBy.equals(SELECT_CHOICE_BY_AUTHOR)) {
            emBuilder.setTitle("Search Results for author: " + searchTerm);
        }
        emBuilder.setColor(Color.YELLOW);
        emBuilder.setPageCounterPlacement(EmbedComponent.FOOTER);

        event.getChannel().sendMessageEmbeds(emBuilder.build()).addActionRow(
                        emBuilder.getPageBuilderActionRow())
                .queue((message) ->
                {
                    long msgId = message.getIdLong();
                    pageEmbeds.put(msgId, emBuilder);
                });

        if (isDeleting) {
            event.getChannel().sendMessage("Enter the quote# for the quote you want to remove.").queue();
        }

    }


    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        long guildId = event.getGuild().getIdLong();
        MessageChannel channel = event.getChannel();

        /* --- On viewing a quote --- */
        if (event.getComponentId().equals(SELECT_MENU_VIEW)) {
            if (event.getValues().get(0).equals(SELECT_CHOICE_RANDOM)) {
                event.editMessage("Fetching a random quote..").setComponents().queue();
                getRandomQuote(guildId, channel);
            }
            if (event.getValues().get(0).equals(SELECT_CHOICE_ALL)) {
                event.editMessage("Fetching all quotes..").setComponents().queue();
                initAllQuoteEmbed(event, false);
            }
            if (event.getValues().get(0).equals(SELECT_CHOICE_SEARCH)) {
                event.editMessage("Select how you want to filter your search.").queue();
                event.editSelectMenu(StringSelectMenu.create(SELECT_MENU_SEARCH)
                        .addOption("By things said", SELECT_CHOICE_BY_SAID,
                                "Search for something that was said")
                        .addOption("By speaker", SELECT_CHOICE_BY_SPEAKER,
                                "Search for who said a quote")
                        .addOption("By author", SELECT_CHOICE_BY_AUTHOR,
                                "Search by who added the quote to the bot")
                        .addOption("By year", SELECT_CHOICE_BY_YEAR,
                                "Search a quote by year that it was said")
                        .build())
                        .queue();
            }
        }

        /* --- On searching --- */
        if (event.getComponentId().equals(SELECT_MENU_SEARCH)) {
            if (event.getValues().get(0).equals(SELECT_CHOICE_BY_SAID)) {
                event.editMessage("You selected: search by the quote").setComponents().queue();
                channel.sendMessage("Enter your search term:").queue();
                usersSearching.put(event.getUser(), SELECT_CHOICE_BY_SAID);
            }
            if (event.getValues().get(0).equals(SELECT_CHOICE_BY_SPEAKER)) {
                event.editMessage("You selected: search by who said the quote").setComponents().queue();
                channel.sendMessage("Enter who said the quote:").queue();
                usersSearching.put(event.getUser(), SELECT_CHOICE_BY_SPEAKER);
            }
            if (event.getValues().get(0).equals(SELECT_CHOICE_BY_AUTHOR)) {
                event.editMessage("You selected: search by who added the quote").setComponents().queue();
                channel.sendMessage("Enter who added the quote to bot:").queue();
                usersSearching.put(event.getUser(), SELECT_CHOICE_BY_AUTHOR);
            }
            if (event.getValues().get(0).equals(SELECT_CHOICE_BY_YEAR)) {
                event.editMessage("You selected: search by year").setComponents().queue();
                channel.sendMessage("Enter the year (or anything other than: " +
                        "a number if you want quotes without years)")
                        .queue();
                usersSearching.put(event.getUser(), SELECT_CHOICE_BY_YEAR);
            }
        }

    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String id = event.getComponentId();
        if (id.equals(BUTTON_NEXT_PAGE + id) || id.equals(BUTTON_PREVIOUS_PAGE + id) ||
                id.equals(DELETE_EMBED + id)) {

            long msgId = event.getMessage().getIdLong();
            EmbedPageBuilder emBuilder = pageEmbeds.get(msgId);
            emBuilder.scroll(event);
            if (emBuilder.isErased()) {
                pageEmbeds.remove(msgId);
            }
        }

    }


    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        User user = event.getAuthor();

        if (!user.isBot() && usersSearching.containsKey(user)) {
            command.getQuoteRemover().addToAcknowledgedMessage(event.getMessageIdLong());
            String searchBy = usersSearching.get(user);
            String searchTerm = event.getMessage().getContentRaw().toLowerCase();
            List<QuoteContext> matches = new ArrayList<>();

            if (!validateQuotesArrays(event.getGuild().getIdLong(), event.getChannel())) {
                return;
            }
            List<QuoteContext> contexts = quotesArrays.get(event.getGuild().getIdLong());


            for (QuoteContext context : contexts) {
                for (Quote quote : context.getQuotes()) {
                    if (searchBy.equals(SELECT_CHOICE_BY_SAID) && !matches.contains(context) &&
                            quote.getQuote().toLowerCase().contains(searchTerm)) {
                        matches.add(context);
                    }
                    else if (searchBy.equals(SELECT_CHOICE_BY_SPEAKER) && !matches.contains(context) &&
                            quote.getName().toLowerCase().contains(searchTerm)) {
                        matches.add(context);
                    }
                    else if (searchBy.equals(SELECT_CHOICE_BY_YEAR) && !matches.contains(context)) {
                        try {
                            int year = Integer.parseInt(searchTerm);
                            if (quote.getYear() != null && quote.getYear() == year) {
                                matches.add(context);
                            }
                        }
                        catch(NumberFormatException e) {
                            if (quote.getYear() == null) {
                                matches.add(context);
                            }
                        }
                    }
                }

                if (searchBy.equals(SELECT_CHOICE_BY_AUTHOR) &&
                        context.getAuthor().toLowerCase().contains(searchTerm)) {
                    matches.add(context);
                }
            }

            initSearchEmbed(event, matches, searchTerm);
            usersSearching.remove(user);
        }

    }





}
