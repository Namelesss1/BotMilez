package commands.rng;

import commands.IBotCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import util.IO;
import util.sentencegenerators.MarkovChain;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * ===== RNG MESSAGE COMMAND =====
 * This command outputs a randomly-generated sentence or message.
 * The method the sentence is generated is determined by the user.
 * The user can set the training data to be from a specialized file belonging to
 * this bot, or from all the messages in a channel belonging to the
 * server where the command is called.
 *
 * //TODO: Ensure an empty channel is not used for training data
 * //TODO: Add another method of sentence-generation using syntactic rules
 * //TODO: "Proper" starts and ends to sentences being output.
 */
public class RngMessageCommand extends ListenerAdapter implements IBotCommand {

    //private final List<OptionData> options;
    private final String DEFAULT_NAME = "default";

    private final int ORDER = 2;
    private final int MAX_OUTPUT_SIZE = 30;
    /* Guild ID -> (TextChannel -> All Words in channel) */
    private Map<Long, Map<TextChannel, MarkovChain>> channelChains;
    private MarkovChain trainingChain;
    private final String TRAINING_PATH = "resources/sentencegenerators/training/";

    public RngMessageCommand() {
        String[] data = IO.readAllWordsIntoArray(TRAINING_PATH + "blogRaw.txt");
        trainingChain = new MarkovChain(ORDER, data, MAX_OUTPUT_SIZE);

        channelChains = new HashMap<>();

        //options = new ArrayList<>();
        //options.add(
          //      new OptionData(
            //            OptionType.STRING,
              //          DEFAULT_NAME,
                //        "type anything to use default settings",
                  //      false, true
     //           ));
    }
    @Override
    public String getName() {
        return "rng-message";
    }

    @Override
    public String getDesc() {
        return "Generates a random sentence or message!";
    }

    @Override
    public List<OptionData> getOptions() {
        return null;
    }

    @Override
    public void doAction(SlashCommandInteractionEvent event) {

        if (event.getOption(DEFAULT_NAME) != null) {

        }

        event.reply("What method should the bot use to generate the sentence? ")
                .addActionRow(StringSelectMenu.create(MENU_SELECT_SENTENCE_RNG)
                        .addOption("Using server history", SELECT_SERVER_HISTORY,
                                "Randomly generates a sentence from server history)")
                        .addOption("Using training data", SELECT_TRAINING_FILE,
                                "Generates a sentence from training data")
                        .build())
                .queue();
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        Guild guild = event.getGuild();
        long guildId = guild.getIdLong();


        /* Selecting between sentence-generation methods */
        if (event.getComponentId().equals(MENU_SELECT_SENTENCE_RNG)) {
            if (event.getValues().get(0).equals(SELECT_SERVER_HISTORY)) {

                IPermissionHolder holder =
                        (IPermissionHolder) guild.getRolesByName("@everyone", true).toArray() [0];

                storeTextChannelHistory(guildId, guild.getTextChannels(), holder);

                Set<TextChannel> channelSet = channelChains.get(guildId).keySet();
                StringSelectMenu.Builder SSMBuilder = StringSelectMenu.create(MENU_SELECT_CHANNEL);
                for (TextChannel channel : channelSet) {
                    SSMBuilder.addOption(channel.getName(), Long.toString(channel.getIdLong()),
                            channel.getName());
                }

                event.editMessage("From what channel?").setActionRow(SSMBuilder.build()).queue();
            }

            if (event.getValues().get(0).equals(SELECT_TRAINING_FILE)) {
                event.editMessage(trainingChain.generateSentence()).setComponents().queue();
            }
        }


        /* On selecting a channel for training data */
        if (event.getComponentId().equals(MENU_SELECT_CHANNEL)) {
            Map<TextChannel, MarkovChain> channelToChain = channelChains.get(guildId);
            /* Will not remain null since a channel must have been selected */
            TextChannel selectedChannel = null;

            for (TextChannel channel : channelToChain.keySet()) {
                if (event.getValues().get(0).equals(Long.toString(channel.getIdLong()))) {
                    selectedChannel = channel;
                    break;
                }
            }

            MarkovChain chain = channelToChain.get(selectedChannel);
            event.editMessage(chain.generateSentence()).setComponents().queue();
        }
    }

    @Override
    public void getHelp(StringSelectInteractionEvent event) {
        String overview = "This command generates a random sentence or message. There are two ways " +
                "in which a sentence is generated:";

        String training = "The first is by training data. The bot reads a lot of " +
                "sentences and messages sent by actual people, finds patterns in what they say, and learns from" +
                " it. From there, the bot uses these patterns to try to form its own sentence.";

        String syntax = "The second way is through syntactic rules. Humans have general rules within their " +
                "heads relating to language in general, and also language-specific about what sounds correct " +
                "and what does not. The bot uses these rules to form a basic sentence. Note that this " +
                "method of generating a sentence in the bot is currently limited to English-only. " +
                "\n**This option has not yet been implemented**";

        String options = "When using the command, the bot will ask you to select from three options." +
                "The options and what they are, are listed below:\n" +
                "**By training data**: The bot will use training data from a prepared file containing " +
                "many sentences and phrases.\n" +
                "**By server history**: Upon choosing this option, the bot asks you what channel to read from. " +
                "After selecting a channel, the bot will use server history from that channel as training data " +
                "to try to generate its own sentence.\n" +
                "**By syntactic rules**: The bot attempts to create a sentence through pre-determined rules." +
                " (*This option is not yet available!*)";

        EmbedBuilder emBuilder = new EmbedBuilder();
        emBuilder.setTitle("/" + getName());
        emBuilder.setDescription(getDesc());
        emBuilder.setColor(Color.RED);
        emBuilder.setFooter("The bots are now learning how to form their own sentences..");
        emBuilder.addField(new MessageEmbed.Field(
                "Command Info",
                overview,
                false
        ));
        emBuilder.addField(new MessageEmbed.Field(
                "Method 1: Training Data",
                training,
                false
        ));
        emBuilder.addField(new MessageEmbed.Field(
                "Method 2: Syntax Rules",
                syntax,
                false
        ));
        emBuilder.addField(new MessageEmbed.Field(
                "Command options",
                options,
                false
        ));

        event.editMessageEmbeds(emBuilder.build()).setComponents().queue();
    }


    /**
     * If not done so already, extract all words from messages from each channel
     * and store them in the map. Only channels with the VIEW_CHANNEL permission are
     * considered, and messages that are sent by bots are ignored.
     *
     * @param guildId id of the server in question
     * @param textChannels list of all text channels in the server
     * @param permissionHolder information about what channels have the VIEW_CHANNEL permission
     */
    private void storeTextChannelHistory(long guildId, List<TextChannel> textChannels,
                                         IPermissionHolder permissionHolder) {

        if (!channelChains.containsKey(guildId)) {
            channelChains.put(guildId, new HashMap<>());

            for (TextChannel channel : textChannels) {
                boolean hasPermission = true;

                PermissionOverride permissions = channel.getPermissionOverride(permissionHolder);
                if (permissions != null) {
                    if (permissions.getDenied().contains(Permission.VIEW_CHANNEL)) {
                        hasPermission = false;
                    }
                }

                if (hasPermission) {
                    List<String> channelWords = new ArrayList<>();
                    MessageHistory history = MessageHistory.getHistoryFromBeginning(channel).complete();
                    for (Message msg : history.getRetrievedHistory()) {
                        if (!msg.getAuthor().isBot()) {
                            String[] msgWords = msg.getContentRaw().split("\\s");
                            channelWords.addAll(Arrays.asList(msgWords));
                        }
                    }
                    String[] wordsArray = channelWords.toArray(new String[channelWords.size()]);
                    channelChains.get(guildId).put(
                            channel, new MarkovChain(ORDER,wordsArray,MAX_OUTPUT_SIZE));
                }
            }
        }
    }


    private final String MENU_SELECT_SENTENCE_RNG = "menusentencerng";
    private final String SELECT_TRAINING_FILE = "selectrrainingfile";
    private final String SELECT_SERVER_HISTORY = "selectserverhistory";
    private final String MENU_SELECT_CHANNEL = "selectchannel";
}
