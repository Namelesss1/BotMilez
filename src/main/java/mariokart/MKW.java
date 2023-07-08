package mariokart;

import commands.helper.IO;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A singleton class that holds information related to the game Mario Kart Wii such
 * as its characters and vehicles. Because MKW is a finished game that is
 * not updated by the developers themselves, all information held in this
 * class is not modified once initialized.
 */
public class MKW {

    private final String PATH = "resources/mkw-stats/json/";

    /* Vehicle name -> Vehicle */
    private final Map<String, MKWVehicle> vehicles = new HashMap<>();

    /* Single instance of this class to be created */
    private static MKW mkwInstance = null;

    private MKW() {
        initVehicleMap(PATH + "vehicles.json");
    }


    public static MKW initialize() {
        if (mkwInstance == null) {
            mkwInstance = new MKW();
        }
        return mkwInstance;
    }


    /**
     * Initializes the statistics and information of each vehicle in the game into
     * a map.
     * @param path path to a JSON file containing the data to read from into map.
     */
    private void initVehicleMap(String path) {
        JSONArray jsonArray = (JSONArray)IO.readJson(path);

        for (Object element : jsonArray) {
            JSONObject entry = (JSONObject)element;
            MKWVehicle vehicle = new MKWVehicle(
                    (Long)entry.get("id"),
                    (String)entry.get("name"),
                    (String)entry.get("alias"),
                    (String)entry.get("codename"),
                    (String)entry.get("type"),
                    (String)entry.get("weightclass"),
                    (String)entry.get("drifttype"),
                    (Long)entry.get("speed"),
                    (Long)entry.get("weight"),
                    (Long)entry.get("acceleration"),
                    (Long)entry.get("handling"),
                    (Long)entry.get("drift"),
                    (Long)entry.get("offroad"),
                    (Long)entry.get("miniturbo"),
                    (Long)entry.get("total")
            );
            vehicles.put(vehicle.getName(), vehicle);
        }
    }



    /**
     * Get a sub-map of the game's vehicles that match all of the given parameters.
     *
     * @param weightClass desired weightclass of vehicles
     * @param vehicleType desired vehicle type
     * @param driftType desired drifting type
     * @return a map that contains only vehicles that match the criteria.
     */
    public Map<String, MKWVehicle> getVehicles(WeightClass weightClass,
                                               VehicleType vehicleType, DriftType driftType) {

        Map<String, MKWVehicle> result = new HashMap<>(vehicles);
        List<String> vehiclesToRemove = new ArrayList<>();

        for (MKWVehicle vehicle : result.values()) {

            if ((weightClass == WeightClass.HEAVY) && !vehicle.getWeightclass().equals("Heavy")) {
                vehiclesToRemove.add(vehicle.getName());
            }
            else if ((weightClass == WeightClass.MEDIUM) && !vehicle.getWeightclass().equals("Medium")) {
                vehiclesToRemove.add(vehicle.getName());
            }
            else if ((weightClass == WeightClass.LIGHT) && !vehicle.getWeightclass().equals("Light")) {
                vehiclesToRemove.add(vehicle.getName());
            }


            if ((vehicleType == VehicleType.BIKE) && !vehicle.getType().equals("Bike")) {
                vehiclesToRemove.add(vehicle.getName());
            }
            else if ((vehicleType == VehicleType.KART) && !vehicle.getType().equals("Kart")) {
                vehiclesToRemove.add(vehicle.getName());
            }


            if ((driftType == DriftType.INWARD) && !vehicle.getDrifttype().equals("Inward")) {
                vehiclesToRemove.add(vehicle.getName());
            }
            else if ((driftType == DriftType.INWARD) && !vehicle.getDrifttype().equals("Outward")) {
                vehiclesToRemove.add(vehicle.getName());
            }

        }

        for (String name : vehiclesToRemove) {
            result.remove(name);
        }

        return result;
    }


    /*
     * The below enums are used to filter vehicles and characters
     * by certain properties
     */

    /* weightclass of characters / vehicles */
    public enum WeightClass {
        ALL,
        LIGHT,
        MEDIUM,
        HEAVY
    }

    /* Vehicle-only: Kart or Bike */
    public enum VehicleType {
        ALL,
        KART,
        BIKE,
    }

    /* Vehicle-only: Which drift does a vehicle use */
    public enum DriftType {
        ALL,
        INWARD,
        OUTWARD
    }


    /*
     * IDs for MKW-related JDA StringSelectMenus
     */

    public static final String MENU_SELECT_STAT = "selectstat";
    public static final  String SELECT_CHARACTERS = "selectcharacters";
    public static final String SELECT_VEHICLES = "selectvehicles";
    public static final String MENU_SELECT_CLASS = "selectclass";
    public static final String SELECT_LIGHT_VEHICLE = "selectlightvehicle";
    public static final String SELECT_MEDIUM_VEHICLE = "selectmediumvehicle";
    public static final String SELECT_HEAVY_VEHICLE = "selectheavyvehicle";
    public static final String MENU_SELECT_VEHICLE = "selectvehiclemenu";
}
