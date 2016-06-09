package de.ids_mannheim.korap.query.elements;

/**
 * @author margaretha
 * 
 */
public enum KoralOperation {
    SEQUENCE, POSITION, DISJUNCTION, REPETITION, CLASS, MERGE, RELATION;

    @Override
    public String toString() {
        return "operation:" + super.toString().toLowerCase();
    }

}
