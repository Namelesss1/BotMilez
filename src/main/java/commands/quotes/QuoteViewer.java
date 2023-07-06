package commands.quotes;

import commands.helper.EmbedPageBuilder;
import commands.helper.IO;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.awt.*;
import java.util.*;
import java.util.List;

import static botmilez.config.QUOTE_FILE_PREFIX;
import static botmilez.config.QUOTE_FILE_SUFFIX;
import static commands.helper.EmbedPageBuilder.*;
import static commands.quotes.QuoteIDs.*;


/**
 * QuoteViewer class: A class that is responsible for the reading of JSON files
 * that have quotes stored in them, and methods that return quote requests to
 * a user that asked for them.
 */
public class QuoteViewer extends ListenerAdapter {

    /* serverId -> server's quotes */
    private Map<Long, List<QuoteContext>> quotesArrays;

    /* serverId -> if quotes have been modified since last update */
    private Map<Long, Boolean> isModified;
    /* messageId -> current embed page number */
    private Map<Long, EmbedPageBuilder> pageEmbeds;

    /* Limit to how many quotes can be displayed in an embed */
    public static final int MAX_QUOTES_PER_EMBED = 2;

    private boolean isEventListener = false;

    public QuoteViewer() {
        quotesArrays = new HashMap<>();
        isModified = new HashMap<>();
        pageEmbeds = new HashMap<>();
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


    public void initializeQuoteEmbed(long guildId, MessageChannel channel) {

        if (!validateQuotesArrays(guildId, channel)) {
            return;
        }

        List<QuoteContext> contexts = quotesArrays.get(guildId);
        List<MessageEmbed.Field> quoteFields = new ArrayList<>();
        for (QuoteContext context : contexts) {
            quoteFields.add(context.getAsField());
        }

        EmbedPageBuilder emBuilder = new EmbedPageBuilder(MAX_QUOTES_PER_EMBED, quoteFields);
        emBuilder.setTitle("All quotes");
        emBuilder.setColor(Color.YELLOW);

        channel.sendMessageEmbeds(emBuilder.build()).addActionRow(
                Button.primary(BUTTON_PREVIOUS_PAGE, Emoji.fromUnicode("◀")),
                Button.danger(DELETE_QUOTE_EMBED, Emoji.fromUnicode("❌")),
                Button.primary(BUTTON_NEXT_PAGE, Emoji.fromUnicode("▶")))
                .queue((message) ->
                {
                    long msgId = message.getIdLong();
                    pageEmbeds.put(msgId, emBuilder);
                });

    }


    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        long guildId = event.getGuild().getIdLong();
        MessageChannel channel = event.getChannel();

        if (event.getComponentId().equals(SELECT_MENU_VIEW)) {
            if (event.getValues().get(0).equals(SELECT_CHOICE_RANDOM)) {
                event.editMessage("Fetching a random quote..").queue();
                getRandomQuote(guildId, channel);
            }
            if (event.getValues().get(0).equals(SELECT_CHOICE_ALL)) {
                event.editMessage("Fetching all quotes..").queue();
                initializeQuoteEmbed(guildId, channel);
            }
        }

    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        long msgId = event.getMessage().getIdLong();
        EmbedPageBuilder emBuilder = pageEmbeds.get(msgId);
        emBuilder.scroll(event);
        if (emBuilder.isErased()) {
            pageEmbeds.remove(msgId);
        }
    }







}
