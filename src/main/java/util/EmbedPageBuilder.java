package util;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import java.util.List;

/**
 * This class represents a re-usable EmbedBuilder for an embed that
 * supports scrolling through multiple pages of fields.
 */
public class EmbedPageBuilder extends EmbedBuilder {

    public static final String BUTTON_NEXT_PAGE = "next_page";
    public static final String BUTTON_PREVIOUS_PAGE = "prev_page";
    public static final String DELETE_QUOTE_EMBED = "delete_embed";

    /**
     * Possible choices on where to add a page counter to the embed,
     * if outside methods choose to do so.
     */
    public enum CounterEmbedComponent {
        AUTHOR,
        TITLE,
        DESCRIPTION,
        FOOTER
    }

    /**
     * Indicates the positioning of a page counter within the embed.
     * If null, then no page counter will be included.
     */
    private CounterEmbedComponent counterEmbedPlacement = null;

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

    public EmbedPageBuilder(int maxFieldsPerPageIn, List<MessageEmbed.Field> elementsIn,
                            boolean doFieldCounter) {
        pageNumber = 1;
        maxFieldsPerPage = maxFieldsPerPageIn;
        elements = elementsIn;
        fieldCounter = doFieldCounter;

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
    public void setPageCounterPlacement(CounterEmbedComponent component) {
        counterEmbedPlacement = component;
        addPageCounter();
    }

    public void setFieldCounter(boolean doFieldCounter) {
        fieldCounter = doFieldCounter;
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

        return this;
    }

    /**
     * Based on the buttons clicked on this embed, determine whether to scroll to the next
     * page, previous page, or close the embed entirely.
     * @param event this EmbedBuilder object with the updated page.
     */
    public void scroll(ButtonInteractionEvent event) {

        if (event.getComponentId().equals(BUTTON_NEXT_PAGE)) {
            if (pageNumber < maxPageNumber()) {
                pageNumber++;
                event.editMessageEmbeds(getEmbedPage().build()).queue();
                return;
            }
            event.editMessageEmbeds(this.build()).queue();

        }

        if (event.getComponentId().equals(DELETE_QUOTE_EMBED)) {
            event.getMessage().delete().queue();
        }

        if (event.getComponentId().equals(BUTTON_PREVIOUS_PAGE)) {
            if (pageNumber > 1) {
                pageNumber--;
                event.editMessageEmbeds(getEmbedPage().build()).queue();
            }
            else {
                event.editMessageEmbeds(this.build()).queue();
            }
        }
    }

}
