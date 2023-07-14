package commands.quotes;

import commands.helper.IO;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static botmilez.config.QUOTE_FILE_PREFIX;
import static botmilez.config.QUOTE_FILE_SUFFIX;


/**
 * QuoteAdder: Helper class for the quote command to add
 * new quotes(s) for the bot to store so users can view the
 * quotes later on.
 *
 * Quotes can be added in two ways. The first way is for the user
 * to enter each part of a quote and each quote individually one by one
 * based on the prompts given by the bot. The second way is the shorthand
 * and much faster way, where the user can send one or more quotes in a single
 * message in the form:
 *
 * "quote" speakerWhoSaidQuote year(year is optional)
 */
public class QuoteAdder extends ListenerAdapter {

    private Map<User, Status> statuses;
    private Map<User,Quote> quotes;
    private Map<User,QuoteContext> contexts;

    /* Quote command instance that created this object */
    private QuoteCommand quoteCommand;

    private boolean isEventListener = false;

    /**
     * Status: Used to indicate the current state of a user adding a new quote
     * to the bot.
     */
    private enum Status {
        NEW, /* User is adding a new quote to the context */
        INPUT_NAME, /* User inputting alias of person adding quote */
        INPUT_QUOTE, /* User inputting quote that was said */
        INPUT_YEAR, /* User inputting year the quote was said */
        INPUT_MORE, /* User choosing whether to add more quotes to context */
        INPUT_CORRECT /* User choosing whether the quote was correctly added */
    }

    public QuoteAdder(QuoteCommand instance) {
        quoteCommand = instance;
        statuses = new HashMap<>();
        quotes = new HashMap<>();
        contexts = new HashMap<>();
    }

    public void setListening(JDA jda) {
        isEventListener = true;
        jda.addEventListener(this);
    }

    public boolean isEventListener() {
        return isEventListener;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        User author = event.getAuthor();
        MessageChannel channel = event.getChannel();

        if (!author.isBot() && statuses.containsKey(author)) {
            String response = event.getMessage().getContentRaw();
            boolean shorthand = false;


            if (statuses.get(author) == Status.INPUT_NAME) {
                shorthand = parseShorthand(author, response);
                if (shorthand) {
                    statuses.replace(author, Status.INPUT_MORE);
                }
                else {
                    quotes.get(author).setName(response);
                    statuses.replace(author, Status.INPUT_QUOTE);
                    channel.sendMessage("Now enter the quote, WITHOUT \"quotations\"")
                            .queue();
                    return;
                }
            }


            if (statuses.get(author) == Status.INPUT_QUOTE) {
                quotes.get(author).setQuote(response);
                statuses.replace(author, Status.INPUT_YEAR);
                channel.sendMessage(
                                "Now enter the year quote was mentioned \n" +
                                        "This is optional. If you don't want a year, type anything " +
                                        "that is not a number.")
                        .queue();
                return;
            }


            if (statuses.get(author) == Status.INPUT_YEAR) {
                quotes.get(author).setYear(response);
                statuses.replace(author, Status.INPUT_MORE);
                channel.sendMessage(
                                "Does the quote include any other context? (more quotes?)\n" +
                                        "Input yes if so, anything else otherwise")
                        .queue();
                return;
            }


            if (statuses.get(author) == Status.INPUT_MORE) {
                if (!shorthand) {
                    contexts.get(author).addQuoteToContext(quotes.get(author));
                }

                if (response.equalsIgnoreCase("yes")) {
                    statuses.replace(author, Status.NEW);
                }
                else {
                    channel.sendMessage("Does the following quote context look correct?" +
                        "Input yes if so, anything else otherwise").queue();
                    contexts.get(author).setAuthor(author.getName());
                    contexts.get(author).sendQuoteContext(channel);
                    statuses.replace(author, Status.INPUT_CORRECT);
                    return;
                }
            }


            if (statuses.get(author) == Status.INPUT_CORRECT) {

                if (response.equalsIgnoreCase("yes")) {
                    boolean success = addQuoteToJSON(event);
                    if (success) {
                        quoteCommand.getQuoteViewer().setAsModified(event.getGuild().getIdLong());
                        channel.sendMessage("Quote Successfully added!").queue();
                    }
                    else {
                        channel.sendMessage("Failed to add quote, something went wrong").queue();
                    }

                    stopCommand(author);
                    return;
                }

                else {
                    channel.sendMessage("Lets start over.").queue();
                    contexts.replace(author, new QuoteContext());
                    statuses.replace(author, Status.NEW);
                }
            }


            if (statuses.get(author) == Status.NEW) {
                onNewStatus(author, channel);
            }
        }

    }


    /**
     * This method is called when a user is beginning the process of
     * entering a new quote to the context.
     *
     * @param user user that is adding a quote
     * @param channel channel for the bot to send a prompt to
     */
    public void onNewStatus(User user, MessageChannel channel) {
        channel.sendMessage("Enter the name of who said the quote.\n" +
                "Note: there is a quicker way to set a quote using the following format:\n" +
                "```\"quote\" nameOfPersonWhoSaidQuote yearWhichIsOptional\n" +
                "\"quote #2\" nameOfPersonWhoSaidQuote yearWhichIsOptional\n" +
                "etc...```")
                .queue();
        if (!statuses.containsKey(user)) {
            statuses.put(user, Status.INPUT_NAME);
            quotes.put(user, new Quote());
            contexts.put(user, new QuoteContext());
        }
        else {
            statuses.replace(user, Status.INPUT_NAME);
            quotes.replace(user, new Quote());
        }


    }


    /**
     * Prepares a quote context into a JSON format, then sends the json object
     * to a method that stores the object as a JSON file to save the quote.
     * @param event gives the guild to which the quote belongs to
     * @return true if addition of quote was successful, false if not.
     */
    private boolean addQuoteToJSON(MessageReceivedEvent event) {
        String path = QUOTE_FILE_PREFIX + event.getGuild().getId() + QUOTE_FILE_SUFFIX;
        JSONObject jsonObj = new JSONObject();

        JSONArray quoteArray = new JSONArray();
        JSONObject jsonQuote;

        for (Quote quote : contexts.get(event.getAuthor()).getQuotes()) {
            jsonQuote = new JSONObject();
            jsonQuote.put("name", quote.getName());
            jsonQuote.put("quote", quote.getQuote());
            jsonQuote.put("year", quote.getYear());
            quoteArray.add(jsonQuote);
        }

        jsonObj.put("author", event.getAuthor().getName());
        jsonObj.put("context", quoteArray);

        JSONArray contextArray;
        if (IO.fileExists(path)) {
            contextArray = (JSONArray)IO.readJson(path);
        }
        else {
            contextArray = new JSONArray();
        }
        contextArray.add(jsonObj);



        return IO.writeJson(contextArray ,path);
    }


    /**
     * Takes a string and checks whether the formatting is sufficient to
     * create a quote object from it and add it. If string is good, then
     * it is taken and parsed to obtain each part of the quote(s) which is added
     * to the QuoteContext. The format of the string must be that of the shorthand
     * way of adding a quote. The year is flexible and need not be a "valid" year
     * (e.g. 2819938) and spaces between someone's name is allowed.
     *
     * @param text String to parse
     * @return true if string is valid, false if it cannot be taken apart
     */
    private boolean parseShorthand(User user, String text) {
        String capture;

        /* Some IOS devices use curly quotation mark automatically */
        text = text.replace("”", "\"");
        text = text.replace("“", "\"");
        text = text.replaceAll("\'", "\"");

        /* Capture at least one of any character between quotation marks
         * parenthesis to capture quote itself in group 1, and capturing
         * whitespace to remove any whitespace before the username */
        Pattern quoteRegex = Pattern.compile("\"(.+)\"[\\s]*");

        /* Capture a year (optional) which should be at least one number at end of line,
         *  separated from the username by whitespace or a comma symbol */
        Pattern yearRegex = Pattern.compile("([\\s]+|[,])[\\d]+$");

        /* Capture a name consisting of at least one of any characters */
        Pattern nameRegex = Pattern.compile(".+");

        String[] lines = text.split("\n");

        /* Each quote separated by a new line, done on a line-by-line basis */
        for (String line: lines) {
            quotes.put(user, new Quote());

            /* Parsing: Quote */
            Matcher patternMatcher = quoteRegex.matcher(line);
            if (!patternMatcher.find()) {
                return false;
            }
            capture = patternMatcher.group(1); /* Get quote w/out quotation marks */
            quotes.get(user).setQuote(capture);
            line = line.substring(patternMatcher.end()); /* Leaving only name & year */

            /* Parsing: Year */
            patternMatcher = yearRegex.matcher(line);
            if (patternMatcher.find()) {
                capture = patternMatcher.group(0).trim();
                quotes.get(user).setYear(capture);
                line = line.substring(0, patternMatcher.start()); /* Leave only name */
            }

            /* Parsing: Name */
            patternMatcher = nameRegex.matcher(line);
            if (!patternMatcher.find()) {
                return false;
            }
            capture = patternMatcher.group(0).trim();
            quotes.get(user).setName(capture);

            contexts.get(user).addQuoteToContext(quotes.get(user));
        }


        return true;
    }


    public void stopCommand(User user) {
        if (statuses.containsKey(user)) {
            statuses.remove(user);
        }
        if (quotes.containsKey(user)) {
            quotes.remove(user);
        }
        if (contexts.containsKey(user)) {
            contexts.remove(user);
        }
    }

}
