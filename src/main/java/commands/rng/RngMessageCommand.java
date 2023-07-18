package commands.rng;

import commands.IBotCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.IPermissionHolder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import util.IO;
import util.sentencegenerators.MarkovChain;

import java.util.*;

/**
 * ===== RNG MESSAGE COMMAND =====
 * This command outputs a randomly-generated sentence or message
 */
public class RngMessageCommand extends ListenerAdapter implements IBotCommand {

    private final int ORDER = 3;
    private final int MAX_OUTPUT_SIZE = 30;
    /* Guild ID -> (TextChannel -> All Words in channel) */
    private Map<Long, Map<TextChannel, MarkovChain>> channelChains;
    private MarkovChain trainingChain;
    private final String TRAINING_PATH = "resources/sentencegenerators/training/";

    public RngMessageCommand() {
        String[] data = IO.readAllWordsIntoArray(TRAINING_PATH + "training.txt");
        trainingChain = new MarkovChain(ORDER, data, MAX_OUTPUT_SIZE);

        channelChains = new HashMap<>();
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
        long guildId = event.getGuild().getIdLong();

        if (event.getComponentId().equals(MENU_SELECT_SENTENCE_RNG)) {

            if (event.getValues().get(0).equals(SELECT_SERVER_HISTORY)) {

                /*
                 * If not done so already, extract all words from messages from each channel
                 * and store them in the map. Only channels with the VIEW_CHANNEL permission are
                 * considered, and messages that are sent by bots are ignored.
                 */
                if (!channelChains.containsKey(guildId)) {
                    channelChains.put(guildId, new HashMap<>());

                    for (TextChannel channel : event.getGuild().getTextChannels()) {
                        PermissionOverride permissions = channel.getPermissionOverride((IPermissionHolder)
                                event.getGuild().getRolesByName("@everyone", true).toArray() [0]);
                        boolean hasPermission = true;
                        if (permissions != null) {
                            if (permissions.getDenied().contains(Permission.VIEW_CHANNEL)) {
                                hasPermission = false;
                            }
                        }

                        if (hasPermission) {
                            List<String> channelWords = new ArrayList<>();
                            MessageHistory history = MessageHistory.getHistoryFromBeginning(channel).complete();
                            for (Message msg : history.getRetrievedHistory()) {
                                System.out.println(msg.getContentRaw());
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

    private final String MENU_SELECT_SENTENCE_RNG = "menusentencerng";
    private final String SELECT_TRAINING_FILE = "selectrrainingfile";
    private final String SELECT_SERVER_HISTORY = "selectserverhistory";
    private final String MENU_SELECT_CHANNEL = "selectchannel";
}
