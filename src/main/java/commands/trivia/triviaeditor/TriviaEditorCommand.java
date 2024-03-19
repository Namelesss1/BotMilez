package commands.trivia.triviaeditor;

import commands.IBotCommand;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import util.EmbedPageBuilder;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static util.EmbedPageBuilder.*;

public class TriviaEditorCommand extends ListenerAdapter implements IBotCommand {

    private static List<String> activeEditIDs;

    private static final String BUTTON_ID = "triviaeditorcommand";
    private EmbedPageBuilder helpEmbed;

    public TriviaEditorCommand() {
        activeEditIDs = new ArrayList<>();
    }

    @Override
    public String getName() {
        return "trivia_edit";
    }

    @Override
    public String getDesc() {
        return "Create your own custom trivia, or edit one you made!";
    }

    @Override
    public List<OptionData> getOptions() {
        return null;
    }

    @Override
    public void doAction(SlashCommandInteractionEvent event) {

        if (activeEditIDs.contains(event.getUser().getId())) {
            event.getChannel().sendMessage("Cannot start session, " +
                    "you already have an active trivia editing session! ")
                    .queue();
            return;

        }

        event.reply("Check your dms. Instructions to editing your trivia are there to " +
                        "prevent other users from seeing information of it such as" +
                        " answers to your questions.")
                .setEphemeral(true)
                .queue();

        TriviaEditSession session = new TriviaEditSession(event.getUser());
    }

    @Override
    public void getHelp(StringSelectInteractionEvent event) {
        List<MessageEmbed.Field> fields = new ArrayList<>();

        /* Page 1 */
        fields.add(new MessageEmbed.Field(
                "How to use the command",
                "type */" + getName() + "* to get started. The bot will send you a private message " +
                        "asking you if you want to create, modify, or delete a trivia. Then, " +
                        "answer all of the bots prompts asking you what you want to do. You may " +
                        "type \"stop\", \"cancel\", or \"end\" to stop the process at any time. " +
                        "But if you do, no changes will be saved. ",
                false
        ));
        fields.add(new MessageEmbed.Field(
                "Creating a trivia",
                "When the bot prompts you in a private message, type 'create'." +
                        " the bot will then prompt you for all the details of your new " +
                        "trivia: name, tags, allowed servers, allowed editors, and all the " +
                        "questions you want to add to it.",
                false
        ));
        fields.add(new MessageEmbed.Field(
                "Modifying an existing trivia",
                "When the bot prompts you in a private message, type 'modify'." +
                        " the bot will send you all of the details of your trivia. " +
                        " then type what you want to edit and follow additional prompts." +
                        " Only editors or the creator of the trivia are allowed to edit it",
                false
        ));
        fields.add(new MessageEmbed.Field(
                "Delete an existing trivia",
                "When the bot prompts you in a private message, type 'delete'. " +
                        "The bot will ask if you're sure. Type 'yes' if you are. " +
                        "This will erase the trivia completely, so be cautious. Only" +
                        " the original creator of the trivia can do delete it",
                false
        ));

        /* Page 2 */
        fields.add(new MessageEmbed.Field(
           "Trivia Name",
                "This is the name of the trivia, which is used to identify it." +
                        " Within the same server there " +
                        " there can't be two trivias with the same name.",
                false
        ));
        fields.add(new MessageEmbed.Field(
                "Trivia Author",
                "This is the Discord username of the person who created the trivia. ",
                false
        ));
        fields.add(new MessageEmbed.Field(
                "is_default",
                "This value is true if it is a default trivia that was created along with" +
                        " the bot, and is available across all servers the bot is in. ",
                false
        ));
        fields.add(new MessageEmbed.Field(
                "Trivia Tags",
                "Keywords that are used to help identify what your trivia is about. Tags " +
                        "are mainly used to group multiple different trivias together with " +
                        "the same themes when playing a round. ",
                false
        ));

        /* Page 3 */
        fields.add(new MessageEmbed.Field(
                "Universal/all_servers",
                "If true, then this trivia can be seen and played across all mutual " +
                        "servers that the bot and the trivia creator are in without " +
                        "having to give the server permission",
                false
        ));
        fields.add(new MessageEmbed.Field(
                "Servers",
                "If universal/all_servers is off, These are the allowed servers that " +
                        "have permission to access your trivia. Only mutual servers between " +
                        "the user and the bot can be allowed",
                false
        ));
        fields.add(new MessageEmbed.Field(
                "Editors",
                "These are the usernames of all people aside from the creator that have " +
                        "permission to edit, and view a trivia to collaborate together.",
                false
        ));
        fields.add(new MessageEmbed.Field(
                "Questions",
                "This is the main content of a trivia. These are all of the possible questions " +
                        "that can be asked in a trivia. A question consists of the question itself, " +
                        "a list of possible answers to that question, how many points the question is " +
                        "worth if a user gets it correct, and an id. The Id is used as a way to " +
                        "uniquely identify each question in a trivia ",
                false
        ));




        helpEmbed = new EmbedPageBuilder(4, fields,
                false, BUTTON_ID);
        helpEmbed.setColor(Color.MAGENTA);
        helpEmbed.setFooter("Use the below arrows to go to next page, or the red X to " +
                "delete this embed.");
        helpEmbed.setTitle("Trivia Editor");
        helpEmbed.setDescription("Allows you to create and edit your own" +
                " trivias, which you can play using the bot!");
        helpEmbed.setPageTitle(2, "Trivia components");
        helpEmbed.setPageDescription(2, "Explaining all the parts of a trivia.");
        helpEmbed.setPageTitle(3, "Trivia components (cont.)");
        helpEmbed.setPageDescription(3, "Explaining all the parts of a trivia.");

        event.editMessageEmbeds(helpEmbed.build()).setComponents()
                .setActionRow(helpEmbed.getPageBuilderActionRow()).queue();
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getComponentId().equals(BUTTON_NEXT_PAGE + BUTTON_ID) ||
                event.getComponentId().equals(BUTTON_PREVIOUS_PAGE + BUTTON_ID) ||
                event.getComponentId().equals(DELETE_EMBED + BUTTON_ID)) {

            helpEmbed.scroll(event);
        }
    }

    /**
     * Adds an active user to the list of active ones
     * @param id id of channel to add
     */
    public static void addToActiveUser(String id) {
        activeEditIDs.add(id);
    }

    public static void removeActiveUsers(String id) {
        activeEditIDs.remove(id);
    }
}
