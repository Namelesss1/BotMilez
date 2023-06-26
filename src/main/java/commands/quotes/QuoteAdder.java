package commands.quotes;

import commands.helper.IO;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.json.simple.JSONObject;

import static botmilez.config.QUOTE_FILE_SUFFIX;


/**
 * QuoteAdder: Helper class for the quote command to add
 * new quotes(s) for the bot to store so users can view the
 * quotes later on.
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


            if (status == Status.INPUT_NAME) {
                quote.setName(response);
                status = Status.INPUT_QUOTE;
                channel.sendMessage("Now enter the quote, WITHOUT \"quotations\"")
                        .queue();
                return;
            }


            else if (status == Status.INPUT_QUOTE) {
                quote.setQuote(response);
                status = Status.INPUT_YEAR;
                channel.sendMessage(
                                "Now enter the year quote was mentioned \n" +
                                        "This is optional. If you don't want a year, type anything " +
                                        "that is not a number.")
                        .queue();
                return;
            }


            else if (status == Status.INPUT_YEAR) {
                Integer year;
                try {
                    year = Integer.parseInt(response);
                }
                catch(NumberFormatException e) {
                    year = null;
                }
                quote.setYear(year);

                status = Status.INPUT_MORE;
                channel.sendMessage(
                                "Does the quote include any other context? (more quotes?)\n" +
                                        "Input yes if so, anything else otherwise")
                        .queue();
                return;
            }


            else if (status == Status.INPUT_MORE) {
                context.addQuoteToContext(quote);

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


            else if (status == Status.INPUT_CORRECT) {

                if (response.equalsIgnoreCase("yes")) {
                    //TODO: JSON STORE
                    boolean success = true;
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


    private void onNewStatus(MessageChannel channel) {
        channel.sendMessage("Enter the name of who said the quote.").queue();
        status = Status.INPUT_NAME;
        quote = new Quote();
    }


    private boolean addQuoteToJSON(QuoteContext context, MessageReceivedEvent event) {
        JSONObject jsonObj = new JSONObject();

        //TODO: context to jsonObj


        String path = event.getGuild().getId() + QUOTE_FILE_SUFFIX;
        return IO.writeJson(jsonObj ,path);
    }

}
