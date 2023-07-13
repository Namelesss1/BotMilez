package mariokart;

import net.dv8tion.jda.api.entities.MessageEmbed;

/**
 * A class that represents a Mario Kart Wii character & their stats.
 * All stats are final and only loaded from a JSON, so there are no
 * setter methods. Values of fields are set once in the constructor
 * when the object is created.
 */
public class MKWCharacter implements Comparable<MKWCharacter>{

    private final long id; /* character's internal id */
    private final String name; /* name of character */
    private final String codename; /* internal name string */
    private final String weightclass; /* Light, Medium, Heavy */


    /* Added onto a vehicle that a character rides */
    private final long speed;
    private final long weight;
    private final long acceleration;
    private final long handling;
    private final long drift;
    private final long offroad;
    private final long miniturbo;
    private final long total;
    private final String imgURL;
    private final String imgcredit;


    public MKWCharacter(long id, String name, String codename, String weightclass,
                      long speed, long weight, long acceleration, long handling,
                      long drift, long offroad, long miniturbo, long total,
                      String imgURL, String imgcredit) {

        this.id = id;
        this.name = name;
        this.codename = codename;
        this.weightclass = weightclass;
        this.speed = speed;
        this.weight = weight;
        this.acceleration = acceleration;
        this.handling = handling;
        this.drift = drift;
        this.offroad = offroad;
        this.miniturbo = miniturbo;
        this.total = total;
        this.imgURL = imgURL;
        this.imgcredit = imgcredit;
    }


    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCodename() {
        return codename;
    }

    public String getWeightclass() {
        return weightclass;
    }

    public long getSpeed() {
        return speed;
    }

    public long getAcceleration() {
        return acceleration;
    }

    public long getWeight() {
        return weight;
    }

    public long getHandling() {
        return handling;
    }

    public long getDrift() {
        return drift;
    }

    public long getOffroad() {
        return offroad;
    }

    public long getMiniturbo() {
        return miniturbo;
    }

    public long getTotal() {
        return total;
    }

    public String getImgURL() {
        return imgURL;
    }

    public String getImgcredit() {
        return imgcredit;
    }


    public MessageEmbed.Field getAsField() {
        String desc =
                "*ID*:　　　　　　　" + id + "\n" +
                        "*Internal name*:　　" + codename + "\n" +
                        "*Weight Class*:　　　" + weightclass + "\n" +
                        "*Speed*:　　　　　" + "+" + speed + "\n" +
                        "*Acceleration*:　　　" + "+" + acceleration + "\n" +
                        "*Weight*:　　　　　" + "+" + weight + "\n" +
                        "*Handling*:　　　　" + "+" + handling + "\n" +
                        "*Drift*:　　　　　　" + "+" + drift + "\n" +
                        "*Offroad*:　　　　　" + "+" + offroad + "\n" +
                        "*Miniturbo*:　　　　" + "+" + miniturbo + "\n" +
                        "*Total*:　　　　　　" + "+" + total;

        MessageEmbed.Field field = new MessageEmbed.Field(
                name,
                desc,
                false
        );
        return field;
    }

    @Override
    public int compareTo(MKWCharacter other) {
        /* Ordering done by name instead of id */
        return name.compareTo(other.getName());
    }
}
