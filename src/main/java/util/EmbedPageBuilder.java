package util;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * This class represents a re-usable EmbedBuilder for an embed that
 * supports scrolling through multiple pages of fields.
 */
public class EmbedPageBuilder extends EmbedBuilder {

    public static final String BUTTON_NEXT_PAGE = "next_page";
    public static final String BUTTON_PREVIOUS_PAGE = "prev_page";
    public static final String DELETE_EMBED = "delete_embed";

    /* Used to differentiate between different events that use this class */
    private String id;

    private ItemComponent[] pageBuilderActionRow = new ItemComponent[3];

    /**
     * Possible choices on where to add a page counter or custom content
     * of a specific page to the embed if outside methods choose to do so.
     */
    public enum EmbedComponent {
        AUTHOR,
        TITLE,
        DESCRIPTION,
        FOOTER,
        THUMBNAIL,
        COLOR,
        IMAGE
    }

    /**
     * Indicates the positioning of a page counter within the embed.
     * If null, then no page counter will be included.
     */
    private EmbedComponent counterEmbedPlacement = null;

    /* Whether to set an id number above each field */
    private boolean fieldCounter;

    /* List structure to display as fields in embed. This is
    * only initialized once, so if the elements were to be updated
    * outside of this object, then the updates would not reflect here. */
    private List<MessageEmbed.Field> elements;

    /* current page number */
    private int pageNumber;

    /* maximum number of fields per embed page */
    private int maxFieldsPerPage;

    /* Whether the delete_embed has been triggered or not by a user */
    private boolean isErased = false;

    /* Custom components -> page number */
    private Map<EmbedComponent, Map<Integer, Object>> customPgs;


    /* Default components across all pages without set custom componenets */
    private String title = "";
    private String desc = "";
    private String author = "";
    private String footer = "";
    private String imgURL = "";
    private String thumbnail = "";
    private Color color = null;

    public EmbedPageBuilder(int maxFieldsPerPageIn, List<MessageEmbed.Field> elementsIn,
                            boolean doFieldCounter, String id) {

        this.id = id;
        pageBuilderActionRow[0] = Button.primary(BUTTON_PREVIOUS_PAGE + id, Emoji.fromUnicode("◀"));
        pageBuilderActionRow[1] = Button.danger(DELETE_EMBED + id, Emoji.fromUnicode("❌"));
        pageBuilderActionRow[2] = Button.primary(BUTTON_NEXT_PAGE + id, Emoji.fromUnicode("▶"));

        pageNumber = 1;
        maxFieldsPerPage = maxFieldsPerPageIn;
        elements = elementsIn;
        fieldCounter = doFieldCounter;
        customPgs = new HashMap<>();

        for (int i = 0; i < elements.size() && i < maxFieldsPerPage; i++) {
            if (fieldCounter) {
                this.addField(new MessageEmbed.Field(
                        "", "entry #: " + Integer.toString(i), true
                ));
            }
            this.addField(elements.get(i));
        }

    }

    /**
     * @return current page number of the embed
     */
    public int getPageNumber() {
        return pageNumber;
    }

    /**
     * @return Maximum page number
     */
    public int maxPageNumber() {
        return (elements.size() + maxFieldsPerPage - 1) / maxFieldsPerPage;
    }

    /**
     * @return True when this embed has been closed through the delete button
     */
    public boolean isErased() {
        return isErased;
    }

    /**
     * Set where in the embed a page counter will belong to. This will also add
     * the page counter to the specified position.
     * @param component where the page counter will go. null indicates no page counter
     */
    public void setPageCounterPlacement(EmbedComponent component) {
        counterEmbedPlacement = component;
        addPageCounter();
    }

    public void setFieldCounter(boolean doFieldCounter) {
        fieldCounter = doFieldCounter;
    }


    @Override
    public EmbedBuilder setTitle(String title) {
        this.title = title;
        super.setTitle(title);
        return this;
    }


    public EmbedBuilder setDescription(String description) {
        this.desc = description;
        super.setDescription(description);
        return this;
    }

    @Override
    public EmbedBuilder setAuthor(String author) {
        this.author = author;
        super.setAuthor(author);
        return this;
    }

    @Override
    public EmbedBuilder setFooter(String footer) {
        this.footer = footer;
        super.setFooter(footer);
        return this;
    }

    @Override
    public EmbedBuilder setImage(String img) {
        this.imgURL = img;
        super.setImage(img);
        return this;
    }

    @Override
    public EmbedBuilder setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
        super.setThumbnail(thumbnail);
        return this;
    }

    @Override
    public EmbedBuilder setColor(Color color) {
        this.color = color;
        super.setColor(color);
        return this;
    }

    /**
     * Sets a custom title to a specific page of this embed.
     *
     * @param pageNum specific page of embed to set
     * @param title String representing the title
     */
    public void setPageTitle(int pageNum, String title) {
        if (pageNum > maxPageNumber()) {
            throw new IllegalArgumentException("Given page number cannot" +
                    "exceed that maximum page number.");
        }

        if (!customPgs.containsKey(EmbedComponent.TITLE)) {
            customPgs.put(EmbedComponent.TITLE, new HashMap<>());
        }

        customPgs.get(EmbedComponent.TITLE).put(pageNum, title);
    }


    /**
     * Sets a custom description to a specific page of this embed.
     *
     * @param pageNum specific page of embed to set
     * @param desc String representing the description
     */
    public void setPageDescription(int pageNum, String desc) {
        if (pageNum > maxPageNumber()) {
            throw new IllegalArgumentException("Given page number cannot" +
                    "exceed that maximum page number.");
        }

        if (!customPgs.containsKey(EmbedComponent.DESCRIPTION)) {
            customPgs.put(EmbedComponent.DESCRIPTION, new HashMap<>());
        }

        customPgs.get(EmbedComponent.DESCRIPTION).put(pageNum, desc);
    }


    /**
     * Sets a custom author to a specific page of this embed.
     *
     * @param pageNum specific page of embed to set
     * @param author String representing the author
     */
    public void setPageAuthor(int pageNum, String author) {
        if (pageNum > maxPageNumber()) {
            throw new IllegalArgumentException("Given page number cannot" +
                    "exceed that maximum page number.");
        }

        if (!customPgs.containsKey(EmbedComponent.AUTHOR)) {
            customPgs.put(EmbedComponent.AUTHOR, new HashMap<>());
        }

        customPgs.get(EmbedComponent.AUTHOR).put(pageNum, author);
    }


    /**
     * Sets a custom footer to a specific page of this embed.
     *
     * @param pageNum specific page of embed to set
     * @param footer String representing the footer
     */
    public void setPageFooter(int pageNum, String footer) {
        if (pageNum > maxPageNumber()) {
            throw new IllegalArgumentException("Given page number cannot" +
                    "exceed that maximum page number.");
        }

        if (!customPgs.containsKey(EmbedComponent.FOOTER)) {
            customPgs.put(EmbedComponent.FOOTER, new HashMap<>());
        }

        customPgs.get(EmbedComponent.FOOTER).put(pageNum, footer);
    }


    /**
     * Sets a custom thumbnail to a specific page of this embed.
     *
     * @param pageNum specific page of embed to set
     * @param thumbnail String representing the URL of thumbnail
     */
    public void setPageThumbnail(int pageNum, String thumbnail) {
        if (pageNum > maxPageNumber()) {
            throw new IllegalArgumentException("Given page number cannot" +
                    "exceed that maximum page number.");
        }

        if (!customPgs.containsKey(EmbedComponent.THUMBNAIL)) {
            customPgs.put(EmbedComponent.THUMBNAIL, new HashMap<>());
        }

        customPgs.get(EmbedComponent.THUMBNAIL).put(pageNum, thumbnail);
    }


    /**
     * Sets a custom image to a specific page of this embed.
     *
     * @param pageNum specific page of embed to set
     * @param image String representing the URL of image
     */
    public void setPageImage(int pageNum, String image) {
        if (pageNum > maxPageNumber()) {
            throw new IllegalArgumentException("Given page number cannot" +
                    "exceed that maximum page number.");
        }

        if (!customPgs.containsKey(EmbedComponent.IMAGE)) {
            customPgs.put(EmbedComponent.IMAGE, new HashMap<>());
        }

        customPgs.get(EmbedComponent.IMAGE).put(pageNum, image);
    }


    /**
     * Sets a custom color to a specific page of this embed.
     *
     * @param pageNum specific page of embed to set
     * @param color Color to set to the page
     */
    public void setPageColor(int pageNum, Color color) {
        if (pageNum > maxPageNumber()) {
            throw new IllegalArgumentException("Given page number cannot" +
                    "exceed that maximum page number.");
        }

        if (!customPgs.containsKey(EmbedComponent.COLOR)) {
            customPgs.put(EmbedComponent.COLOR, new HashMap<>());
        }

        customPgs.get(EmbedComponent.COLOR).put(pageNum, color);
    }




    /**
     * Adds a page counter to the place specified by counterEmbedPlacement
     */
    private void addPageCounter() {
        if (counterEmbedPlacement != null) {
            String pageCounter = "Page " + pageNumber + "/" + maxPageNumber();
            switch (counterEmbedPlacement) {
                case AUTHOR:
                    this.setAuthor(pageCounter);
                    break;
                case TITLE:
                    this.setTitle(pageCounter);
                    break;
                case DESCRIPTION:
                    this.setDescription(pageCounter);
                    break;
                case FOOTER:
                    this.setFooter(pageCounter);
                    break;
            }
        }
    }


    /*
     * Helper method:
     * check if page has any custom component. If so, set it. If not,
     * use the defaults that have been set to prevent all pages afterward
     * from having the same custom components.
     */
    private void addCustomComponents() {
        if (customPgs.containsKey(EmbedComponent.TITLE)) {
            Map<Integer, Object> pageToTitle = customPgs.get(EmbedComponent.TITLE);
            if (pageToTitle.containsKey(pageNumber)) {
                super.setTitle((String)pageToTitle.get(pageNumber));
            }
            else if (!title.equals("")){
                super.setTitle(title);
            }
        }
        if (customPgs.containsKey(EmbedComponent.DESCRIPTION)) {
            Map<Integer, Object> pageToDesc = customPgs.get(EmbedComponent.DESCRIPTION);
            if (pageToDesc.containsKey(pageNumber)) {
                super.setDescription((String)pageToDesc.get(pageNumber));
            }
            else if (!desc.equals("")){
                super.setDescription(desc);
            }
        }
        if (customPgs.containsKey(EmbedComponent.FOOTER)) {
            Map<Integer, Object> pageToFooter = customPgs.get(EmbedComponent.FOOTER);
            if (pageToFooter.containsKey(pageNumber)) {
                super.setFooter((String)pageToFooter.get(pageNumber));
            }
            else if (!footer.equals("")){
                super.setFooter(footer);
            }
        }
        if (customPgs.containsKey(EmbedComponent.AUTHOR)) {
            Map<Integer, Object> pageToAuthor = customPgs.get(EmbedComponent.AUTHOR);
            if (pageToAuthor.containsKey(pageNumber)) {
                super.setAuthor((String)pageToAuthor.get(pageNumber));
            }
            else if (!author.equals("")){
                super.setAuthor(author);
            }
        }
        if (customPgs.containsKey(EmbedComponent.THUMBNAIL)) {
            Map<Integer, Object> pageToThumbnail = customPgs.get(EmbedComponent.THUMBNAIL);
            if (pageToThumbnail.containsKey(pageNumber)) {
                super.setThumbnail((String)pageToThumbnail.get(pageNumber));
            }
            else if (!thumbnail.equals("")){
                super.setThumbnail(thumbnail);
            }
        }
        if (customPgs.containsKey(EmbedComponent.IMAGE)) {
            Map<Integer, Object> pageToImg = customPgs.get(EmbedComponent.IMAGE);
            if (pageToImg.containsKey(pageNumber)) {
                super.setImage((String)pageToImg.get(pageNumber));
            }
            else if (!imgURL.equals("")){
                super.setImage(imgURL);
            }
        }
        if (customPgs.containsKey(EmbedComponent.COLOR)) {
            Map<Integer, Object> pageToColor = customPgs.get(EmbedComponent.COLOR);
            if (pageToColor.containsKey(pageNumber)) {
                super.setColor((Color)pageToColor.get(pageNumber));
            }
            else if (!color.equals("")){
                super.setColor(color);
            }
        }
    }

    /**
     * Get a new page of the embed. When this is triggered, all of the existing fields
     * are replaced with the next or previous set of fields gathered from the
     * elements list.
     *
     * @return this object with the next or previous set of fields
     */
    private EmbedPageBuilder getEmbedPage() {
        int startIndex = (pageNumber * maxFieldsPerPage) - maxFieldsPerPage; /* Since pageNumber starts at 1 */
        this.getFields().clear();

        for (int i = startIndex
             ; i < elements.size() && i < startIndex + maxFieldsPerPage; i++) {
            if (fieldCounter) {
                this.addField(new MessageEmbed.Field(
                        "", "entry #: " + Integer.toString(i), true
                ));
            }
            this.addField(elements.get(i));
        }

        addPageCounter();
        addCustomComponents();
        return this;
    }

    /**
     * Based on the buttons clicked on this embed, determine whether to scroll to the next
     * page, previous page, or close the embed entirely.
     * @param event this EmbedBuilder object with the updated page.
     */
    public void scroll(ButtonInteractionEvent event) {

        if (event.getComponentId().equals(BUTTON_NEXT_PAGE + id)) {
            if (pageNumber < maxPageNumber()) {
                pageNumber++;
                event.editMessageEmbeds(getEmbedPage().build()).queue();
                return;
            }
            event.editMessageEmbeds(this.build()).queue();

        }

        if (event.getComponentId().equals(DELETE_EMBED + id)) {
            event.getMessage().delete().queue();
        }

        if (event.getComponentId().equals(BUTTON_PREVIOUS_PAGE + id)) {
            if (pageNumber > 1) {
                pageNumber--;
                event.editMessageEmbeds(getEmbedPage().build()).queue();
            }
            else {
                event.editMessageEmbeds(this.build()).queue();
            }
        }
    }


    public ItemComponent[] getPageBuilderActionRow() {
        return pageBuilderActionRow;
    }
}
