package commands.quotes;

import commands.helper.IO;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;

import static botmilez.config.QUOTE_FILE_PREFIX;
import static botmilez.config.QUOTE_FILE_SUFFIX;
import static commands.quotes.QuoteIDs.*;

public class QuoteRemover extends ListenerAdapter {

    private Map<User, DeletionState> usersState;
    private Map<User, List<QuoteContext>> usersCandidates;
    private Map<User, QuoteContext> usersDeleting;
    private QuoteCommand quoteCommand;
    boolean isEventListener;

    private Set<Long> acknowledgedMessages;

    /**
     * The current state of deletion that a user has accomplished
     */
    private enum DeletionState {
        CHOOSING_NUM, /* User has chosen a quote number to delete */
        CONFIRMING /* User has confirmed whether they wanted to delete the chosen quote */
    }

    public QuoteRemover(QuoteCommand instance) {
        usersState = new HashMap<>();
        quoteCommand = instance;
        isEventListener = false;
        usersCandidates = new HashMap<>();
        acknowledgedMessages = new HashSet<>();
        usersDeleting = new HashMap<>();
    }


    /**
     * Removes a quote context from a file
     *
     * @param event The event that triggered this method
     * @return true if successful removal, false if not.
     */
    private boolean removeQuoteFromJSON(MessageReceivedEvent event) {
        String path = QUOTE_FILE_PREFIX + event.getGuild().getId() + QUOTE_FILE_SUFFIX;
        List<QuoteContext> contexts = QuoteViewer.jsonToQuoteContext(path);
        if (contexts == null) {
            return false;
        }

        contexts.remove(usersDeleting.get(event.getAuthor()));

        /* Re-pack JSON object to write back to file with the quote removed */
        JSONArray contextArray= new JSONArray();
        for (QuoteContext context : contexts) {
            JSONObject jsonContext = new JSONObject();
            JSONArray quoteArray = new JSONArray();
            JSONObject jsonQuote;

            for (Quote quote : context.getQuotes()) {
                jsonQuote = new JSONObject();
                jsonQuote.put("name", quote.getName());
                jsonQuote.put("quote", quote.getQuote());
                jsonQuote.put("year", quote.getYear());
                quoteArray.add(jsonQuote);
            }

            jsonContext.put("author", context.getAuthor());
            jsonContext.put("context", quoteArray);
            contextArray.add(jsonContext);
        }

        return IO.writeJson(contextArray ,path);

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
     * Returns the current removal state the user is going through
     * @return state
     */
    public Map<User, DeletionState> getUserState() {
        return usersState;
    }

    /**
     * Adds a message ID to the list of messages that have been acknowledged, so listeners
     * do not repond twice to a message.
     * @param id message ID
     */
    public void addToAcknowledgedMessage(long id) {
        acknowledgedMessages.add(id);
    }

    /**
     * Sets a list of quotes contexts, one of which the user can potentially delete.
     * @param user User performing a deletion
     * @param contexts list of candidates to be deleted.
     */
    public void setDeletionCandidates(User user, List<QuoteContext> contexts) {
        usersCandidates.put(user, contexts);
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        long guildId = event.getGuild().getIdLong();
        MessageChannel channel = event.getChannel();

        if (event.getComponentId().equals(SELECT_MENU_DELETE)) {
            usersState.put(event.getUser(), DeletionState.CHOOSING_NUM);

            if (event.getValues().get(0).equals(SELECT_CHOICE_ALL)) {
                event.editMessage("Fetching all quotes..").queue();
                quoteCommand.getQuoteViewer().initAllQuoteEmbed(event, true);
                event.getChannel().sendMessage("Enter the entry # of the quote you want to delete").queue();
            }
            if (event.getValues().get(0).equals(SELECT_CHOICE_SEARCH)) {
                event.editMessage("Select how you want to filter your search.").queue();
                event.editSelectMenu(StringSelectMenu.create(SELECT_MENU_SEARCH_DELETE)
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
        if (event.getComponentId().equals(SELECT_MENU_SEARCH_DELETE)) {
            if (event.getValues().get(0).equals(SELECT_CHOICE_BY_SAID)) {
                event.reply("Enter your search term:").queue();
                quoteCommand.getQuoteViewer().addToUsersSearching(event.getUser(), SELECT_CHOICE_BY_SAID);
            }
            if (event.getValues().get(0).equals(SELECT_CHOICE_BY_SPEAKER)) {
                event.reply("Enter who said the quote:").queue();
                quoteCommand.getQuoteViewer().addToUsersSearching(event.getUser(), SELECT_CHOICE_BY_SPEAKER);
            }
            if (event.getValues().get(0).equals(SELECT_CHOICE_BY_AUTHOR)) {
                event.reply("Enter who added the quote to bot:").queue();
                quoteCommand.getQuoteViewer().addToUsersSearching(event.getUser(), SELECT_CHOICE_BY_AUTHOR);
            }
            if (event.getValues().get(0).equals(SELECT_CHOICE_BY_YEAR)) {
                event.reply("Enter the year (or anything other than: " +
                                "a number if you want quotes without years)")
                        .queue();
                quoteCommand.getQuoteViewer().addToUsersSearching(event.getUser(), SELECT_CHOICE_BY_YEAR);
            }
        }
    }


    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        User user = event.getAuthor();
        String response = event.getMessage().getContentRaw();
        MessageChannel channel = event.getChannel();

        if (!user.isBot() && usersState.containsKey(user) && !acknowledgedMessages.contains(event.getMessageIdLong())) {

            acknowledgedMessages.clear();
            if (usersState.get(user) == DeletionState.CHOOSING_NUM) {
                int contextNum;
                try {
                    contextNum = Integer.parseInt(response);
                }
                catch (NumberFormatException e) {
                    channel.sendMessage(" is not a valid number.").queue();
                    usersState.remove(user);
                    usersCandidates.remove(user);
                    return;
                }
                List<QuoteContext> candidates = usersCandidates.get(user);
                if (contextNum >= candidates.size()) {
                    channel.sendMessage("Number out of range: This number is greater " +
                            "than number of quotes shown. Deletion aborted.").queue();
                    usersState.remove(user);
                    usersCandidates.remove(user);
                    return;
                }
                if (contextNum < 0) {
                    channel.sendMessage("Number out of range: The number should be positive" +
                            ". Deletion aborted.").queue();
                    usersState.remove(user);
                    usersCandidates.remove(user);
                    return;
                }

                QuoteContext context = candidates.get(contextNum);
                channel.sendMessage("Is this the quote you wish to delete? Type yes if so, " +
                        "anything else if not.").queue();
                context.sendQuoteContext(channel);
                usersState.replace(user, DeletionState.CONFIRMING);
                usersDeleting.put(user, context);
                return;
            }

            if (usersState.get(user) == DeletionState.CONFIRMING) {
                if (response.equalsIgnoreCase("yes")) {
                    boolean success = removeQuoteFromJSON(event);
                    if (success) {
                        quoteCommand.getQuoteViewer().setAsModified(event.getGuild().getIdLong());
                        channel.sendMessage("Quote has been removed.").queue();
                    }
                    else {
                        channel.sendMessage("Error: Removal failed.").queue();
                    }
                }
                else {
                    channel.sendMessage("Deletion aborted.").queue();
                }

                usersDeleting.remove(user);
                usersState.remove(user);
                usersCandidates.remove(user);
            }
        }
    }

}
