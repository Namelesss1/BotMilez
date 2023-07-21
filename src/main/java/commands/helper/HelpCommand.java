package commands.helper;

import commands.CommandManager;
import commands.IBotCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import okhttp3.internal.http2.Http2Connection;
import util.EmbedPageBuilder;

import java.awt.*;
import java.util.List;

public class HelpCommand extends ListenerAdapter implements IBotCommand {

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDesc() {
        return "Get help on how to use certain commands";
    }

    @Override
    public List<OptionData> getOptions() {
        return null;
    }

    @Override
    public void doAction(SlashCommandInteractionEvent event) {
        StringSelectMenu.Builder SSMBuilder = StringSelectMenu.create(HELP_MENU);
        for (IBotCommand command : CommandManager.getCommands()) {
            SSMBuilder.addOption(command.getName(), command.getName(),
                    command.getDesc());
        }

        event.reply("What command would you like help with?")
                .addActionRow(SSMBuilder.build()).queue();
    }

    @Override
    public void getHelp(StringSelectInteractionEvent event) {

        String howToUse = "After typing the command, the bot sends a list " +
                "of options. Each of these options represents a command belonging " +
                "to this bot. Select the command that you want help with.";

        EmbedBuilder emBuilder = new EmbedBuilder();
        emBuilder.setTitle("**/" + getName() + "**");
        emBuilder.setFooter("This is the know-it-all command.");
        emBuilder.setDescription(getDesc());
        emBuilder.setColor(Color.DARK_GRAY);
        emBuilder.setThumbnail("https://cdn-icons-png.flaticon.com/512/682/682055.png");
        emBuilder.addField("How To Use", howToUse, false);

        event.editMessageEmbeds(emBuilder.build()).setComponents().queue();
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        if (event.getComponentId().equals(HELP_MENU)) {
            IBotCommand selectedCmd = null;

            for (IBotCommand command : CommandManager.getCommands()) {
                if (event.getValues().get(0).equals(command.getName())) {
                    selectedCmd = command;
                    break;
                }
            }

            selectedCmd.getHelp(event);
        }
    }

    private static final String HELP_MENU = "help_menu";

}
