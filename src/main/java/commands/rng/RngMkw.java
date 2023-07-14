package commands.rng;

import commands.IBotCommand;
import mariokart.MKW;
import mariokart.MKWCharacter;
import mariokart.MKWVehicle;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectInteraction;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.util.*;

import static mariokart.MKW.*;

public class RngMkw extends ListenerAdapter implements IBotCommand {

    private MKW mkw;
    private Map<User, UserRandomSelection> userSelections;

    public RngMkw() {
        userSelections = new HashMap<>();
        mkw = MKW.initialize();
    }

    @Override
    public String getName() {
        return "rng-mkw";
    }

    @Override
    public String getDesc() {
        return "Select a random MKW vehicle / character";
    }

    @Override
    public List<OptionData> getOptions() {
        return null;
    }

    @Override
    public void doAction(SlashCommandInteractionEvent event) {
        userSelections.put(event.getUser(), new UserRandomSelection());

        event.reply("What kind of random combination do you want?")
                .addActionRow(StringSelectMenu.create(SELECT_RANDOM)
                        .addOption("Any Weight", RANDOM_ALL_WEIGHT,
                                "All weight classes are possible")
                        .addOption("Lightweight", RANDOM_LIGHT_WEIGHT,
                                "Only lightweight characters/vehicles possible")
                        .addOption("Mediumweight", RANDOM_MEDIUM_WEIGHT,
                                "Only Medium weight characters/vehicles are possible")
                        .addOption("Heavyweight", RANDOM_HEAVY_WEIGHT,
                                "Only Heavy weight characters/vehicles are possible")
                        .build())
                .queue();
    }


    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {

        String chosen = event.getValues().get(0);
        User user = event.getUser();
        UserRandomSelection selection = userSelections.get(user);

        /* After selecting weight class */
        if (event.getComponentId().equals(SELECT_RANDOM)) {
            if (chosen.equals(RANDOM_ALL_WEIGHT)) {
                selection.setWeightClass(MKW.WeightClass.ALL);
            }
            if (chosen.equals(RANDOM_LIGHT_WEIGHT)) {
                selection.setWeightClass(MKW.WeightClass.LIGHT);
            }
            if (chosen.equals(RANDOM_MEDIUM_WEIGHT)) {
                selection.setWeightClass(MKW.WeightClass.MEDIUM);
            }
            if (chosen.equals(RANDOM_HEAVY_WEIGHT)) {
                selection.setWeightClass(MKW.WeightClass.HEAVY);
            }

            event.editMessage("Now Select which of these you want.")
                    .setActionRow(StringSelectMenu.create(MENU_SELECT_RANDOM_ENTITY)
                            .addOption("Character/Vehicle", RANDOM_CHARACTER_VEHICLE,
                                    "Get both a random character/vehicle combo")
                            .addOption("Vehicle only", RANDOM_VEHICLE,
                                    "Get a random vehicle only")
                            .addOption("Character only", RANDOM_CHARACTER,
                                    "Get a random character only")
                            .build())
                    .queue();
        }

        /* After Selecting character vs vehicle */
        if (event.getComponentId().equals(MENU_SELECT_RANDOM_ENTITY)) {
            if (chosen.equals(RANDOM_CHARACTER)) {
                selection.setEntityType(MKW.EntityType.CHARACTER);
                event.editMessage(getRandomSelection(user)).setComponents().queue();
                return;
            }
            if (chosen.equals(RANDOM_VEHICLE)) {
                selection.setEntityType(MKW.EntityType.VEHICLE);
            }
            if (chosen.equals(RANDOM_CHARACTER_VEHICLE)) {
                selection.setEntityType(MKW.EntityType.ALL);
            }

            event.editMessage("Now Select vehicle type")
                    .setActionRow(StringSelectMenu.create(MENU_SELECT_RANDOM_VEHICLE_TYPE)
                            .addOption("Any", RANDOM_ALL_VEHICLE_TYPES,
                                    "Both Karts/Bikes can be randomly selected")
                            .addOption("Karts", RANDOM_KART_VEHICLE_TYPE,
                                    "Only a kart will be randomly selected")
                            .addOption("Bikes", RANDOM_BIKE_VEHICLE_TYPE,
                                    "Only a bike will be randomly selected")
                            .build())
                    .queue();
        }

        /* After selecting vehicle type */
        if (event.getComponentId().equals(MENU_SELECT_RANDOM_VEHICLE_TYPE)) {
            if (chosen.equals(RANDOM_ALL_VEHICLE_TYPES)) {
                selection.setVehicleType(MKW.VehicleType.ALL);
            }
            if (chosen.equals(RANDOM_KART_VEHICLE_TYPE)) {
                selection.setVehicleType(MKW.VehicleType.KART);
                selection.setDriftType(MKW.DriftType.OUTWARD);
                event.editMessage(getRandomSelection(user)).setComponents().queue();
                return;
            }
            if (chosen.equals(RANDOM_BIKE_VEHICLE_TYPE)) {
                selection.setVehicleType(MKW.VehicleType.BIKE);
            }

            event.editMessage("Now Select vehicle drift type")
                    .setActionRow(StringSelectMenu.create(MENU_SELECT_RANDOM_VEHICLE_DRIFT_TYPE)
                            .addOption("Any", RANDOM_ALL_VEHICLE_DRIFT_TYPES,
                                    "Both outer & inner drifting allowed")
                            .addOption("Outer drift only", RANDOM_OUTER_VEHICLE_DRIFT_TYPE,
                                    "Outer drifting bikes will be chosen")
                            .addOption("Inner drift only", RANDOM_INNER_VEHICLE_DRIFT_TYPE,
                                    "Inner drifting bikes will be chosen")
                            .build())
                    .queue();
        }


        /* After selecting drift type */
        if (event.getComponentId().equals(MENU_SELECT_RANDOM_VEHICLE_DRIFT_TYPE)) {
            if (chosen.equals(RANDOM_ALL_VEHICLE_DRIFT_TYPES)) {
                selection.setDriftType(MKW.DriftType.ALL);
            }
            if (chosen.equals(RANDOM_OUTER_VEHICLE_DRIFT_TYPE)) {
                selection.setDriftType(MKW.DriftType.OUTWARD);
            }
            if (chosen.equals(RANDOM_INNER_VEHICLE_DRIFT_TYPE)) {
                selection.setDriftType(MKW.DriftType.INWARD);
            }

            event.editMessage(getRandomSelection(user)).setComponents().queue();
        }
    }


    /**
     * Randomly selects a vehicle and/or character from a list of them
     * based on the options selected by a user.
     * @param user user that initiated command
     * @return a string containing the names of the character/vehicle
     */
    private String getRandomSelection(User user) {
        UserRandomSelection selection = userSelections.get(user);
        List<MKWVehicle> possibleVehicles = null;
        List<MKWCharacter> possibleCharacters = null;
        StringBuilder msg = new StringBuilder("");

        if ((selection.getEntityType() == MKW.EntityType.ALL) ||
                (selection.getEntityType() == MKW.EntityType.CHARACTER)) {
            possibleCharacters =
                    (List<MKWCharacter>) mkw.getCharacters(selection.getWeightClass()).values();
        }
        if ((selection.getEntityType() == MKW.EntityType.ALL) ||
                (selection.getEntityType() == MKW.EntityType.VEHICLE)) {
            possibleVehicles =
                    (List<MKWVehicle>) mkw.getVehicles(
                            selection.getWeightClass(), selection.getVehicleType(), selection.getDriftType())
                            .values();
        }

        Random random = new Random();

        if (possibleCharacters != null) {
            msg.append(possibleCharacters.get(random.nextInt()).getName());
        }
        if (possibleVehicles != null) {
            msg.append(possibleVehicles.get(random.nextInt()).getName());
        }

        userSelections.remove(user);
        return msg.toString();
    }


    /**
     * A helper class used to store all of the selection options
     * that a user wants to use to generate a random character/vehicle.
     * This will determine the possible options to choose from when
     * selecting a possible option.
     */
    private class UserRandomSelection {
        private MKW.WeightClass weightClass;
        private MKW.VehicleType vehicleType;
        private MKW.DriftType driftType;
        private MKW.EntityType entityType;


        public void setWeightClass(MKW.WeightClass weightIn) {
            weightClass = weightIn;
        }
        public void setVehicleType(MKW.VehicleType typeIn) {
            vehicleType = typeIn;
        }
        public void setDriftType(MKW.DriftType typeIn) {
            driftType = typeIn;
        }
        public void setEntityType(MKW.EntityType typeIn) {
            entityType = typeIn;
        }


        public MKW.WeightClass getWeightClass() {
            return weightClass;
        }
        public MKW.VehicleType getVehicleType() {
            return vehicleType;
        }
        public MKW.DriftType getDriftType() {
            return driftType;
        }
        public MKW.EntityType getEntityType() {
            return entityType;
        }

    }
}
