package commands.quotes;

import commands.IBotCommand;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * =============== Quotes ===============
 * This command allows a user to add notable quotes from
 * other people to the bot. These quotes can be viewed or
 * removed.
 *
 * TODO: LOOK INTO ADDING CHOICES DROP DOWN BOX INSTEAD OF RAW INPUTS
 * TODO: Add View Quote And Delete Quote
 * TODO: JSON storage
 */
public class QuoteCommand extends ListenerAdapter implements IBotCommand {

    private List<OptionData> options;
    private static final String BUTTON_ID_ADD = "addquote";
    private Map<User, Status> userStatus;
    private Map<User, QuoteContext> userContext;

    /**
     * Status: Used to indicate the current state of a user adding a new quote
     * to the bot.
     */
    private enum Status {
        INPUT_USER, /* User inputting alias of person adding quote */
        INPUT_QUOTE, /* User inputting quote that was said */
        INPUT_YEAR, /* User inputting year the quote was said */
        INPUT_NEW, /* User choosing whether to add more quotes to context */
        INPUT_CORRECT /* User choosing whether the quote was correctly added */
    }


    public QuoteCommand() {
        options = new ArrayList<>();
        userStatus = new HashMap<>();
        userContext = new HashMap<>();
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
                .addActionRow(Button.success(BUTTON_ID_ADD, "Add a quote"))
                .queue();

    }


    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {

        if (event.getComponentId().equals(BUTTON_ID_ADD)) {
            userStatus.put(event.getUser(), Status.INPUT_USER);
            event.reply("Enter the name of who said the quote: ").queue();
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        User author = event.getAuthor();

        if (!author.isBot() && userStatus.containsKey(author)) {
            Status status = userStatus.get(author);
            String response = event.getMessage().getContentRaw();

            if (status == Status.INPUT_USER) {
                if (!userContext.containsKey(author)) {
                    userContext.put(author, new QuoteContext());
                }
                Quote quote = new Quote();
                quote.setName(response);
                List<Quote> context = userContext.get(author).getQuotes();
                context.add(quote);
                userContext.get(author).setQuotes(context);
                userStatus.replace(author, Status.INPUT_QUOTE);
                event.getChannel().sendMessage(
                        "Now enter the quote, WITHOUT \"quotations\"")
                        .queue();
                return;
            }
            else if (status == Status.INPUT_QUOTE) {
                List<Quote> context = userContext.get(author).getQuotes();
                Quote quote = context.get(context.size() - 1);
                quote.setQuote(response);
                userStatus.replace(author, Status.INPUT_YEAR);
                event.getChannel().sendMessage(
                        "Now enter the year quote was mentioned \n" +
                                "This is optional. If you don't want a year, type anything" +
                                "that is not a number.")
                        .queue();
                return;
            }
            else if (status == Status.INPUT_YEAR) {
                List<Quote> context = userContext.get(author).getQuotes();
                Quote quote = context.get(context.size() - 1);

                Integer year;
                try {
                    year = Integer.parseInt(response);
                    quote.setYear(year);
                }
                catch(NumberFormatException e) {
                    quote.setYear(null);
                }


                userStatus.replace(author, Status.INPUT_NEW);
                event.getChannel().sendMessage(
                        "Does the quote include any other context? (more quotes?)\n" +
                                "Input yes if so, anything else otherwise")
                        .queue();
                return;
            }

            else if (status == Status.INPUT_NEW) {
                if (response.equalsIgnoreCase("yes")) {
                    event.getChannel().sendMessage("Enter the name of who said the next quote.").queue();
                    userStatus.replace(author, Status.INPUT_USER);
                }
                else {
                    event.getChannel().sendMessage("Does the following quote context look correct?").queue();
                    event.getChannel().sendMessage("Input yes if so, anything else otherwise").queue();
                    userContext.get(author).setAuthor(author.getName());
                    printQuoteContext(event, userContext.get(author));
                    userStatus.replace(author, Status.INPUT_CORRECT);
                }
                return;
            }

            else if (status == Status.INPUT_CORRECT) {
                clearUser(author);

                if (response.equalsIgnoreCase("yes")) {
                    //TODO: JSON STORE
                    boolean success = true;
                    if (success) {
                        event.getChannel().sendMessage("Quote Successfully added!").queue();
                    }
                    else {
                        event.getChannel().sendMessage("Failed to add quote, something went wrong").queue();
                    }
                }

                else {
                    event.getChannel().sendMessage("Lets start over. " +
                            "Enter the name of person who said the quote.").queue();
                    userStatus.put(author, Status.INPUT_USER);
                }
            }
        }
    }

    /**
     * Clears the user from objects used in this class once they are done
     * inputting quotes.
     *
     * @param user User to clear from maps
     */
    private void clearUser(User user) {
        userContext.remove(user);
        userStatus.remove(user);
    }




    /**
     * Sends a message containing an embed that prints out a quote context added
     * by a user into the bot.
     *
     * @param event MessageReceivedEvent object giving the channel for bot to print quote in
     * @param context QuoteContext representing the quotes to print
     */
    private void printQuoteContext(MessageReceivedEvent event, QuoteContext context) {

        MessageEmbed.Field field = new MessageEmbed.Field(
                "Quote added by " + context.getAuthor(), /* name */
                context.toString(), /* Value */
                true /* inline */
        );

        EmbedBuilder embBuilder = new EmbedBuilder()
                //.setAuthor("Quote added by: " + context.getAuthor())
                //.setTitle("-------------")
                .setColor(Color.YELLOW)
                //.setFooter("test")
                .addField(field);

        event.getChannel().sendMessageEmbeds(embBuilder.build()).queue();
    }




    /**
     * A class that represents a list of quotes, to represent contexts
     * that involve more than one quote.
     * Sometimes, quotes are funny when they are said as a reply to what
     * someone else has said. Having a context of quotes like this allows
     * for a user to input such a case.
     */
    private class QuoteContext {
        /* Quotes that make up the context */
        private List<Quote> quotes;
        /* Name of user that added the quote */
        private String author; /* Name of user that added this context to the bot */


        public QuoteContext() {
            quotes = new ArrayList<>();
        }

        public QuoteContext(List<Quote> quotesIn, String authorIn) {
            quotes = quotesIn;
            author = authorIn;
        }

        public void setAuthor(String authorIn) {
            author = authorIn;
        }
        public String getAuthor() {
            return author;
        }

        public void setQuotes(List<Quote> quotesIn) {
            quotes = quotesIn;
        }
        public List<Quote> getQuotes() {
            return quotes;
        }


        public void toJSON() {

        }


        @Override
        public String toString() {
            StringBuilder result = new StringBuilder();
            for (Quote quote : quotes) {
                result.append(quote.toString());
                result.append("\n");
            }
            return result.toString();
        }
    }


    /**
     * Class representing one individual quote which includes
     * alias of the person who said the quote, the quote itself,
     * and an (optional) year that a quote was said.
     * Includes getter/setters, and a toString to format the outputted
     * quote based on its contents.
     */
    private class Quote {
        private String name;
        private String quote;
        private Integer year;

        public void setName(String nameIn) {
            name = nameIn;
        }

        public void setQuote(String quoteIn) {
            quote = quoteIn;
        }

        public void setYear(Integer yearIn) {
            year = yearIn;
        }

        public String getName() {
            return name;
        }

        public String getQuote() {
            return quote;
        }

        public int getYear() {
            return year;
        }

        @Override
        public String toString() {
            StringBuilder result =
                    new StringBuilder("\"" + quote + "\" - **" + name + "**");

            if (year != null) {
                result.append(", **" + year + "**");
            }

            return result.toString();
        }
    }


}
