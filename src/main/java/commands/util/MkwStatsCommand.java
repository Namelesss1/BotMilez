package commands.util;

import mariokart.MKW;
import commands.IBotCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static mariokart.MKW.*;
import mariokart.MKWVehicle;

public class MkwStatsCommand extends ListenerAdapter implements IBotCommand {

    private MKW mkw;
    public MkwStatsCommand() {
        mkw = MKW.initialize();
    }

    @Override
    public String getName() {
        return "mkwstats";
    }

    @Override
    public String getDesc() {
        return "Get stats about MKW vehicles or characters.";
    }

    @Override
    public List<OptionData> getOptions() {
        return null;
    }

    @Override
    public void doAction(SlashCommandInteractionEvent event) {

        if (!event.getUser().isBot()) {
            event.reply("What stats do you want to see?")
                    .addActionRow(StringSelectMenu.create(MENU_SELECT_STAT)
                            .addOption("Characters", SELECT_CHARACTERS,
                                    "View stats about characters.")
                            .addOption("Vehicles", SELECT_VEHICLES,
                                    "View stats about vehicles")
                            .build()
                    ).queue();

        }
    }


    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {

        /* After choosing vehicles vs characters */
        if (event.getComponentId().equals(MENU_SELECT_STAT)) {
            if (event.getValues().get(0).equals(SELECT_VEHICLES)) {
                event.editMessage("Select the weight class of vehicle").queue();
                event.editSelectMenu(StringSelectMenu.create(MENU_SELECT_CLASS)
                        .addOption("Light", SELECT_LIGHT_VEHICLE,
                                "Lightweight vehicles")
                        .addOption("Medium", SELECT_MEDIUM_VEHICLE,
                                "Medium vehicles")
                        .addOption("Heavy", SELECT_HEAVY_VEHICLE,
                                "Heavy vehicles")
                        .build()
                ).queue();

            }
        }


        /* After choosing weight class */
        if (event.getComponentId().equals(MENU_SELECT_CLASS)) {
            StringSelectMenu.Builder SSMBuilder = StringSelectMenu.create(MENU_SELECT_VEHICLE);
            event.editMessage("Select which vehicle you want.").queue();

            WeightClass weightClass = WeightClass.ALL;
            if (event.getValues().get(0).equals(SELECT_LIGHT_VEHICLE)) {
                weightClass = WeightClass.LIGHT;
            }

            else if (event.getValues().get(0).equals(SELECT_MEDIUM_VEHICLE)) {
                weightClass = WeightClass.MEDIUM;
            }

            else if (event.getValues().get(0).equals(SELECT_HEAVY_VEHICLE)) {
                weightClass = WeightClass.HEAVY;
            }

            Map<String, MKWVehicle> vehicles =
                    mkw.getVehicles(weightClass, VehicleType.ALL, DriftType.ALL);
            List<MKWVehicle> sortedVehicles = new ArrayList<>(vehicles.values());
            Collections.sort(sortedVehicles);

            for (MKWVehicle vehicle : sortedVehicles) {
                SSMBuilder.addOption(vehicle.getName(), vehicle.getName(),
                        vehicle.getType());
            }
            event.editSelectMenu(SSMBuilder.build()).queue();
        }



        /* After selecting specific vehicle/character */
        if (event.getComponentId().equals(MENU_SELECT_VEHICLE)) {
            Map<String, MKWVehicle> vehicles =
                    mkw.getVehicles(WeightClass.ALL, VehicleType.ALL, DriftType.ALL);

            EmbedBuilder emBuilder = new EmbedBuilder();
            emBuilder.setColor(Color.BLUE);
            emBuilder.setTitle("Vehicle Information");

            for (MKWVehicle vehicle : vehicles.values()) {
                if (event.getValues().get(0).equals(vehicle.getName())) {
                    emBuilder.addField(vehicle.getAsField());
                    emBuilder.setImage(vehicle.getImgURL());
                    emBuilder.setFooter("Image Credit: " + vehicle.getImgcredit());
                }
            }

            event.editMessage("Here are the stats.").queue();
            event.getHook().editOriginalEmbeds(emBuilder.build()).queue();
        }


    }


}
