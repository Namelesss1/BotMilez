package commands.quotes;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that contains many quotes within one context. Quotes added to the bot
 * may contain extra context involved which is why a user may find a quote notable,
 * so it is necessary to allow the user to input more than one quote as a single instance.
 *
 * e.g.
 * "Do you like butterflies?" - Bob
 * "I don't know what that is" - Isa
 */
public class QuoteContext {
    /* Quotes that make up the context */
    private List<Quote> quotes;
    /* Name of user that added this context */
    private String author;


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

    public void addQuoteToContext(Quote quote) {
        quotes.add(quote);
    }

    public List<Quote> getQuotes() {
        return quotes;
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

    @Override
    public boolean equals(Object other) {

        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (getClass() != other.getClass()) {
            return false;
        }

        QuoteContext otherContext = (QuoteContext)other;
        if (author.equals(otherContext.getAuthor()) && quotes.equals(otherContext.getQuotes())) {
            return true;
        }
        return false;
    }

    /**
     * Sends a message containing an embed that prints out a quote context added
     * by a user into the bot.
     *
     * @param channel the channel for bot to print quote in
     */
    public void sendQuoteContext(MessageChannel channel) {

        MessageEmbed.Field field = new MessageEmbed.Field(
                "Quote added by " + author, /* name */
                toString(), /* Value */
                true /* inline */
        );

        EmbedBuilder embBuilder = new EmbedBuilder()
                .setColor(Color.YELLOW)
                .addField(field);

        channel.sendMessageEmbeds(embBuilder.build()).queue();
    }

    /**
     *
     * @return this QuoteContext object as a MessageEmbed field
     */
    public MessageEmbed.Field getAsField() {

        MessageEmbed.Field field = new MessageEmbed.Field(
                "Quote added by " + author, /* name */
                toString(), /* Value */
                false /* inline */
        );

        return field;
    }

}
