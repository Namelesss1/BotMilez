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

    public void setYear(String yearIn) {
        try {
            year = Integer.parseInt(yearIn);
        }
        catch(NumberFormatException e) {
            year = null;
        }
    }

    public String getName() {
        return name;
    }

    public String getQuote() {
        return quote;
    }

    public Integer getYear() {
        return year;
    }


    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (getClass() != other.getClass()) {
            return false;
        }

        Quote otherQuote = (Quote)other;
        if (!name.equals(otherQuote.getName()) || !quote.equals(otherQuote.getQuote())) {
            return false;
        }

        if (year == null && otherQuote.getYear() != null) {
            return false;
        }
        if (year != null) {
            if (otherQuote.getYear() == null) {
                return false;
            }
            if (!year.equals(otherQuote.getYear())) {
                return false;
            }
        }
        return true;
    }


    @Override
    public String toString() {
        StringBuilder result =
                new StringBuilder("\"" + quote + "\" - **" + name + "**");

        if (year != null) {
            result.append(", *" + year + "*");
        }

        return result.toString();
    }
}
