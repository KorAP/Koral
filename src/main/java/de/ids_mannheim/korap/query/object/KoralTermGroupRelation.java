package de.ids_mannheim.korap.query.object;

/**
 * Definition of the relations used within koral:termGroup.
 * 
 * @author margaretha
 * 
 */
public enum KoralTermGroupRelation {

    AND, OR;

    @Override
    public String toString () {
        return "relation:" + super.toString().toLowerCase();
    }
}
