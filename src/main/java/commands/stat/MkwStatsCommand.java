package commands.stat;

import mariokart.MKW;
import commands.IBotCommand;
import mariokart.MKWCharacter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

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
    public void getHelp(StringSelectInteractionEvent event) {

        String overview = "Use this command to view information and stats on Mario Kart Wii\n" +
                "characters and vehicles.\n";
        String vehicles = "When looking up information on a vehicle, the following stats are shown:\n" +
                "**id**: internal id of vehicle\n" +
                "**name**: vehicle's name\n" +
                "**alias**: Other names given to the vehicle\n" +
                "**type**: Kart or Bike\n" +
                "**Drift Type**: Outward-drifting or Inward-Drifting\n" +
                "**codename**: internal string name of vehicle\n" +
                "**weightclass**: Light, Medium, or Heavy weight\n" +
                "**speed**: How fast vehicle goes\n" +
                "**weight**: Vehicle's weight\n" +
                "**acceleration**: Vehicle's acceleration from standing still\n" +
                "**handling**: How smooth a vehicle's movement handling is\n" +
                "**drift**: Vehicle's drift\n" +
                "**offroad**: How well the vehicle handles offroad\n" +
                "**miniturbo**: boost obtained from miniturbo\n" +
                "**total**: total amount of above values summed up\n" +
                "And an image representing the vehicle.";
        String characters = "When looking up information on a character, the following stats are shown:\n" +
                "**id**: internal id of character\n" +
                "**name**: character's name\n" +
                "**codename**: internal string name of character\n" +
                "**weightclass**: Light, Medium, or Heavy weight\n" +
                "**speed**: Speed boost amount of character\n" +
                "**weight**: Weight boost of character\n" +
                "**acceleration**: acceleration boost of character\n" +
                "**handling**: handling boost of character\n" +
                "**drift**: drifting boost of character\n" +
                "**offroad**: offroad boost of character\n" +
                "**miniturbo**: miniturbo boost of character\n" +
                "**total**: total amount of above boost values summed up\n" +
                "And an image representing the character.";

        EmbedBuilder emBuilder = new EmbedBuilder();
        emBuilder.setTitle("/" + getName());
        emBuilder.setDescription(getDesc());
        emBuilder.setColor(Color.ORANGE);
        emBuilder.setFooter("Stats brought to ya faster than Funky Kong on a Flame Runner");
        emBuilder.addField(new MessageEmbed.Field(
                "Description",
                overview,
                false
        ));
        emBuilder.addField(new MessageEmbed.Field(
                "Vehicle Stats",
                vehicles,
                false
        ));
        emBuilder.addField(new MessageEmbed.Field(
                "Character Stats",
                characters,
                false
        ));

        event.editMessageEmbeds(emBuilder.build()).setComponents().queue();
    }


    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {

        /* After choosing vehicles vs characters */
        if (event.getComponentId().equals(MENU_SELECT_STAT)) {
            if (event.getValues().get(0).equals(SELECT_VEHICLES)) {
                event.editMessage("Select the weight class of vehicle").queue();
                event.editSelectMenu(StringSelectMenu.create(MENU_SELECT_VEHICLE_CLASS)
                        .addOption("Light", SELECT_LIGHT_VEHICLE,
                                "Lightweight vehicles")
                        .addOption("Medium", SELECT_MEDIUM_VEHICLE,
                                "Medium vehicles")
                        .addOption("Heavy", SELECT_HEAVY_VEHICLE,
                                "Heavy vehicles")
                        .build()
                ).queue();
            }

            if (event.getValues().get(0).equals(SELECT_CHARACTERS)) {
                event.editMessage("Select the weight class of character").queue();
                event.editSelectMenu(StringSelectMenu.create(MENU_SELECT_CHARACTER_CLASS)
                        .addOption("Light", SELECT_LIGHT_CHARACTER,
                                "Lightweight characters")
                        .addOption("Medium", SELECT_MEDIUM_CHARACTER,
                                "Medium characters")
                        .addOption("Heavy", SELECT_HEAVY_CHARACTER,
                                "Heavy characters")
                        .build()
                ).queue();
            }
        }


        /* After choosing weight class */
        if (event.getComponentId().equals(MENU_SELECT_VEHICLE_CLASS)) {
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


        if (event.getComponentId().equals(MENU_SELECT_CHARACTER_CLASS)) {
            StringSelectMenu.Builder SSMBuilder = StringSelectMenu.create(MENU_SELECT_CHARACTER);
            event.editMessage("Select which character you want.").queue();

            WeightClass weightClass = WeightClass.ALL;
            if (event.getValues().get(0).equals(SELECT_LIGHT_CHARACTER)) {
                weightClass = WeightClass.LIGHT;
            } else if (event.getValues().get(0).equals(SELECT_MEDIUM_CHARACTER)) {
                weightClass = WeightClass.MEDIUM;
            } else if (event.getValues().get(0).equals(SELECT_HEAVY_CHARACTER)) {
                weightClass = WeightClass.HEAVY;
            }

            Map<String, MKWCharacter> characters =
                    mkw.getCharacters(weightClass);
            List<MKWCharacter> sortedCharacters = new ArrayList<>(characters.values());
            Collections.sort(sortedCharacters);

            for (MKWCharacter character : sortedCharacters) {
                SSMBuilder.addOption(character.getName(), character.getName());
            }
            event.editSelectMenu(SSMBuilder.build()).queue();
        }



        /* After selecting specific vehicle*/
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

            event.editMessage("Here are the stats.").setComponents().queue();
            event.getHook().editOriginalEmbeds(emBuilder.build()).queue();
        }


        /* After selecting specific character */
        if (event.getComponentId().equals(MENU_SELECT_CHARACTER)) {
            Map<String, MKWCharacter> characters = mkw.getCharacters(WeightClass.ALL);

            EmbedBuilder emBuilder = new EmbedBuilder();
            emBuilder.setColor(Color.BLUE);
            emBuilder.setTitle("Character Information");

            for (MKWCharacter character : characters.values()) {
                if (event.getValues().get(0).equals(character.getName())) {
                    emBuilder.addField(character.getAsField());
                    emBuilder.setImage(character.getImgURL());
                    emBuilder.setFooter("Image Credit: " + character.getImgcredit());
                }
            }

            event.editMessage("Here are the stats.").setComponents().queue();
            event.getHook().editOriginalEmbeds(emBuilder.build()).queue();
        }


    }


}
