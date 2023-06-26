package commands.quotes;

/**
 * Class representing one individual quote which includes
 * alias of the person who said the quote, the quote itself,
 * and an (optional) year that a quote was said.
 * Includes getter/setters, and a toString to format the outputted
 * quote based on its contents.
 */
public class Quote {

    private String name;
    private String quote;
    private Integer year; /* optional, possibly null */

    public void setName(String nameIn) {
        name = nameIn;
    }

    public void setQuote(String quoteIn) {
        quote = quoteIn;
    }

    public void setYear(Integer yearIn) {
        year = yearIn;
    }

    public String getName() {
        return name;
    }

    public String getQuote() {
        return quote;
    }

    public int getYear() {
        return year;
    }

    @Override
    public String toString() {
        StringBuilder result =
                new StringBuilder("\"" + quote + "\" - **" + name + "**");

        if (year != null) {
            result.append(", **" + year + "**");
        }

        return result.toString();
    }
}
