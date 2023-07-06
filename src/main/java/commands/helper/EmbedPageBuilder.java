package commands.helper;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import java.awt.*;
import java.util.List;

/**
 * This class represents a re-usable EmbedBuilder for an embed that
 * supports scrolling through multiple pages of fields.
 */
public class EmbedPageBuilder extends EmbedBuilder {

    public static final String BUTTON_NEXT_PAGE = "next_page";
    public static final String BUTTON_PREVIOUS_PAGE = "prev_page";
    public static final String DELETE_QUOTE_EMBED = "delete_embed";


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

    public EmbedPageBuilder(int maxFieldsPerPageIn, List<MessageEmbed.Field> elementsIn) {
        pageNumber = 0;
        maxFieldsPerPage = maxFieldsPerPageIn;
        elements = elementsIn;

        for (int i = 0; i < elements.size() && i < maxFieldsPerPage; i++) {
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
        return elements.size() / maxFieldsPerPage;
    }

    /**
     * @return True when this embed has been closed through the delete button
     */
    public boolean isErased() {
        return isErased;
    }

    /**
     * Get a new page of the embed. When this is triggered, all of the existing fields
     * are replaced with the next or previous set of fields gathered from the
     * elements list.
     *
     * @return this object with the next or previous set of fields
     */
    private EmbedPageBuilder getEmbedPage() {
        int startIndex = pageNumber * maxFieldsPerPage;
        this.getFields().clear();

        for (int i = startIndex
             ; i < elements.size() && i < startIndex + maxFieldsPerPage; i++) {
            this.addField(elements.get(i));
        }

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
            }
            else {
                event.editMessageEmbeds(this.build()).queue();
            }
        }

        if (event.getComponentId().equals(DELETE_QUOTE_EMBED)) {
            event.editMessage("Deleting...").queue();
            event.getMessage().delete().queue();
        }

        if (event.getComponentId().equals(BUTTON_PREVIOUS_PAGE)) {
            if (pageNumber > 0) {
                pageNumber--;
                event.editMessageEmbeds(getEmbedPage().build()).queue();
            }
            else {
                event.editMessageEmbeds(this.build()).queue();
            }
        }
    }

}
