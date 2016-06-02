package de.ids_mannheim.korap.query.elements;

/**
 * @author margaretha
 * 
 */
public enum KoralRelation {

    AND, OR;

    @Override
    public String toString() {
        return "relation:" + super.toString().toLowerCase();
    }
}
