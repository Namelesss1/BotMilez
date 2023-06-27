package commands.quotes;

import commands.helper.IO;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.List;
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

    private User user; /* User that initiated command */
    private Status status;
    private Quote quote;
    private QuoteContext context;

    /* Quote command instance that created this object */
    private QuoteCommand quoteCommand;

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

    public QuoteAdder(User userIn, QuoteCommand instance, ButtonInteractionEvent event) {
        user = userIn;
        status = Status.NEW;
        context = new QuoteContext();
        quoteCommand = instance;
        onNewStatus(event.getChannel());
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        User author = event.getAuthor();
        MessageChannel channel = event.getChannel();

        if (!author.isBot() && event.getAuthor().equals(user)) {
            String response = event.getMessage().getContentRaw();
            boolean shorthand = false;


            if (status == Status.INPUT_NAME) {
                shorthand = parseShorthand(response);
                if (shorthand) {
                    status = Status.INPUT_MORE;
                }
                else {
                    quote.setName(response);
                    status = Status.INPUT_QUOTE;
                    channel.sendMessage("Now enter the quote, WITHOUT \"quotations\"")
                            .queue();
                    return;
                }
            }


            if (status == Status.INPUT_QUOTE) {
                quote.setQuote(response);
                status = Status.INPUT_YEAR;
                channel.sendMessage(
                                "Now enter the year quote was mentioned \n" +
                                        "This is optional. If you don't want a year, type anything " +
                                        "that is not a number.")
                        .queue();
                return;
            }


            if (status == Status.INPUT_YEAR) {
                quote.setYear(response);
                status = Status.INPUT_MORE;
                channel.sendMessage(
                                "Does the quote include any other context? (more quotes?)\n" +
                                        "Input yes if so, anything else otherwise")
                        .queue();
                return;
            }


            if (status == Status.INPUT_MORE) {
                if (!shorthand) {
                    context.addQuoteToContext(quote);
                }

                if (response.equalsIgnoreCase("yes")) {
                    status = Status.NEW;
                }
                else {
                    channel.sendMessage("Does the following quote context look correct?" +
                        "Input yes if so, anything else otherwise").queue();
                    context.setAuthor(user.getName());
                    context.sendQuoteContext(event);
                    status = Status.INPUT_CORRECT;
                    return;
                }
            }


            if (status == Status.INPUT_CORRECT) {

                if (response.equalsIgnoreCase("yes")) {
                    //TODO: JSON STORE
                    boolean success = addQuoteToJSON(event);
                    if (success) {
                        channel.sendMessage("Quote Successfully added!").queue();
                    }
                    else {
                        channel.sendMessage("Failed to add quote, something went wrong").queue();
                    }

                    quoteCommand.removeQuoteAdder(event, user);
                    return;
                }

                else {
                    channel.sendMessage("Lets start over.").queue();
                    context = new QuoteContext();
                    status = Status.NEW;
                }
            }


            if (status == Status.NEW) {
                onNewStatus(channel);
            }
        }

    }


    /**
     * This method is called when a user is beginning the process of
     * entering a new quote to the context.
     *
     * @param channel channel for the bot to send a prompt to
     */
    private void onNewStatus(MessageChannel channel) {
        channel.sendMessage("Enter the name of who said the quote.\n" +
                "Note: there is a quicker way to set a quote using the following format:\n" +
                "```\"quote\" nameOfPersonWhoSaidQuote yearWhichIsOptional\n" +
                "\"quote #2\" nameOfPersonWhoSaidQuote yearWhichIsOptional\n" +
                "etc...```")
                .queue();
        status = Status.INPUT_NAME;
        quote = new Quote();
    }



    private boolean addQuoteToJSON(MessageReceivedEvent event) {
        JSONObject jsonObj = new JSONObject();

        JSONArray quoteArray = new JSONArray();
        quoteArray.addAll(context.getQuotes());

        jsonObj.put("author", user.getName());
        jsonObj.put("quotelist", quoteArray);


        String path = QUOTE_FILE_PREFIX + event.getGuild().getId() + QUOTE_FILE_SUFFIX;
        return IO.writeJson(jsonObj ,path);
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
    private boolean parseShorthand(String text) {
        String capture;

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
            quote = new Quote();

            /* Parsing: Quote */
            Matcher patternMatcher = quoteRegex.matcher(line);
            if (!patternMatcher.find()) {
                return false;
            }
            capture = patternMatcher.group(1); /* Get quote w/out quotation marks */
            quote.setQuote(capture);
            line = line.substring(patternMatcher.end()); /* Leaving only name & year */

            /* Parsing: Year */
            patternMatcher = yearRegex.matcher(line);
            if (patternMatcher.find()) {
                capture = patternMatcher.group(0).trim();
                quote.setYear(capture);
                line = line.substring(0, patternMatcher.start()); /* Leave only name */
            }

            /* Parsing: Name */
            patternMatcher = nameRegex.matcher(line);
            if (!patternMatcher.find()) {
                return false;
            }
            capture = patternMatcher.group(0).trim();
            quote.setName(capture);

            context.addQuoteToContext(quote);
        }


        return true;
    }

}
