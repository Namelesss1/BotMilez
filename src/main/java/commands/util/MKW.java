package commands.util;

import commands.helper.IO;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.HashMap;
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
    private final Map<String, Vehicle> vehicles = new HashMap<>();

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
        JSONArray jsonArray = (JSONArray)IO.readJson(path + "vehicles.json");

        for (Object element : jsonArray) {
            JSONObject entry = (JSONObject)element;
            Vehicle vehicle = new Vehicle(
                    (Integer)entry.get("id"),
                    (String)entry.get("name"),
                    (String)entry.get("alias"),
                    (String)entry.get("codename"),
                    (String)entry.get("type"),
                    (String)entry.get("weightclass"),
                    (String)entry.get("drift type"),
                    (Integer)entry.get("speed"),
                    (Integer)entry.get("weight"),
                    (Integer)entry.get("acceleration"),
                    (Integer)entry.get("handling"),
                    (Integer)entry.get("drift"),
                    (Integer)entry.get("offroad"),
                    (Integer)entry.get("miniturbo"),
                    (Integer)entry.get("total")
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
    public Map<String, Vehicle> getVehicles(WeightClass weightClass,
                                            VehicleType vehicleType, DriftType driftType) {

        Map<String,Vehicle> result = new HashMap<>(vehicles);

        for (Vehicle vehicle : result.values()) {

            if ((weightClass == WeightClass.HEAVY) && !vehicle.getWeightclass().equals("Heavy")) {
                result.remove(vehicle.getName());
            }
            else if ((weightClass == WeightClass.MEDIUM) && !vehicle.getWeightclass().equals("Medium")) {
                result.remove(vehicle.getName());
            }
            else if ((weightClass == WeightClass.LIGHT) && !vehicle.getWeightclass().equals("Light")) {
                result.remove(vehicle.getName());
            }


            if ((vehicleType == VehicleType.BIKE) && !vehicle.getType().equals("Bike")) {
                result.remove(vehicle.getName());
            }
            else if ((vehicleType == VehicleType.KART) && !vehicle.getType().equals("Kart")) {
                result.remove(vehicle.getName());
            }


            if ((driftType == DriftType.INWARD) && !vehicle.getDrifttype().equals("Inward")) {
                result.remove(vehicle.getName());
            }
            else if ((driftType == DriftType.INWARD) && !vehicle.getDrifttype().equals("Outward")) {
                result.remove(vehicle.getName());
            }

        }

        return result;
    }



    /**
     * A class that represents a Mario Kart Wii vehicle & Its stats.
     * All stats of a vehicle are final, so there are no setter methods. Values
     * of fields are set once in the constructor when the object is created.
     */
    private final class Vehicle {
        private final int id; /* vehicle's internal id */
        private final String name; /* name of vehicle */
        private final String alias; /* Alternative name, given in EU version */
        private final String codename; /* internal name string */
        private final String type; /* Bike or Kart */
        private final String weightclass; /* Light, Medium, Heavy */
        private final String drifttype; /* Inward or Outward */


        /* The higher the integer, the higher the performance of below stats */
        private final int speed;
        private final int weight;
        private final int acceleration;
        private final int handling;
        private final int drift;
        private final int offroad;
        private final int miniturbo;
        private final int total;


        public Vehicle(int id, String name, String alias, String codename,
                       String type, String weightclass, String driftype,
                       int speed, int weight, int acceleration, int handling,
                       int drift, int offroad, int miniturbo,  int total) {

            this.id = id;
            this.name = name;
            this.alias = alias;
            this.codename = codename;
            this.type = type;
            this.weightclass = weightclass;
            this.drifttype = driftype;
            this.speed = speed;
            this.weight = weight;
            this.acceleration = acceleration;
            this.handling = handling;
            this.drift = drift;
            this.offroad = offroad;
            this.miniturbo = miniturbo;
            this.total = total;
        }


        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getAlias() {
            return alias;
        }

        public String getCodename() {
            return codename;
        }

        public String getType() {
            return type;
        }

        public String getWeightclass() {
            return weightclass;
        }

        public String getDrifttype() {
            return drifttype;
        }

        public int getSpeed() {
            return speed;
        }

        public int getAcceleration() {
            return acceleration;
        }

        public int getWeight() {
            return weight;
        }

        public int getHandling() {
            return handling;
        }

        public int getDrift() {
            return drift;
        }

        public int getOffroad() {
            return offroad;
        }

        public int getMiniturbo() {
            return miniturbo;
        }

        public int getTotal() {
            return total;
        }
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

    
}
