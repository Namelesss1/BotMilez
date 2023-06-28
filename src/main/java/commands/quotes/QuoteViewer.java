package commands.quotes;

import commands.helper.IO;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class QuoteViewer extends ListenerAdapter {

    Map<Integer, List<QuoteContext>> quotesArrays;

    public QuoteViewer() {
        quotesArrays = new HashMap<>();
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



}
