package commands.quotes;

import commands.helper.IO;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;

import static botmilez.config.QUOTE_FILE_PREFIX;
import static botmilez.config.QUOTE_FILE_SUFFIX;

//TODO: Track changes to the json files and include into arrays maybe by calling a method to check and does so
// before any of the reading ones.
public class QuoteViewer extends ListenerAdapter {

    Map<Long, List<QuoteContext>> quotesArrays;
    Map<Long, Boolean> isModified;
    public QuoteViewer() {
        quotesArrays = new HashMap<>();
        isModified = new HashMap<>();
    }

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

    public void setAsModified(long guildId) {
        isModified.replace(guildId, true);
    }

    public void getRandomQuote(long guildId, MessageChannel channel) {

        updateQuotesArrays(guildId);
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

    private void updateQuotesArrays(long guildId) {
        String path = QUOTE_FILE_PREFIX + guildId + QUOTE_FILE_SUFFIX;

        /* If quotes for this guild are not loaded, load them */
        if (!quotesArrays.containsKey(guildId)) {
            List<QuoteContext> quoteContexts = jsonToQuoteContext(path);
            quotesArrays.put(guildId, quoteContexts);
            isModified.put(guildId, false);
        }

        /* If quotes of this server were modified, update */
        if (isModified.get(guildId)) {
            List<QuoteContext> quoteContexts = jsonToQuoteContext(path);
            quotesArrays.replace(guildId, quoteContexts);
            isModified.replace(guildId, false);
        }
    }


}
