package commands.quotes;

import commands.helper.IO;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;

import static botmilez.config.QUOTE_FILE_PREFIX;
import static botmilez.config.QUOTE_FILE_SUFFIX;


/**
 * QuoteViewer class: A class that is responsible for the reading of JSON files
 * that have quotes stored in them, and methods that return quote requests to
 * a user that asked for them.
 */
public class QuoteViewer extends ListenerAdapter {

    /* serverId -> server's quotes */
    Map<Long, List<QuoteContext>> quotesArrays;

    /* serverId -> if quotes have been modified since last update */
    Map<Long, Boolean> isModified;
    public QuoteViewer() {
        quotesArrays = new HashMap<>();
        isModified = new HashMap<>();
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

        if (!updateQuotesArrays(guildId)) {
            channel.sendMessage("An error has occurred. Cannot load quotes!")
                    .queue();
        }

        List<QuoteContext> quoteContexts = quotesArrays.get(guildId);
        if (quoteContexts.isEmpty()) {
            channel.sendMessage("No quotes are stored in the bot!" +
                    "You can add more quotes by using the add option.").queue();
            return;
        }

        Random random = new Random();
        int index = random.nextInt(quoteContexts.size());
        QuoteContext quoteContext = quoteContexts.get(index);

        quoteContext.sendQuoteContext(channel);
    }





}
