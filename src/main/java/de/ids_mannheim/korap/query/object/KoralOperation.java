package de.ids_mannheim.korap.query.object;

/**
 * Definition of possible operations within koral:group.
 * 
 * @author margaretha
 * 
 */
public enum KoralOperation {
    SEQUENCE, POSITION, DISJUNCTION, REPETITION, CLASS, MERGE, RELATION, FOCUS, EXCLUSION, HIERARCHY;

    @Override
    public String toString () {
        return "operation:" + super.toString().toLowerCase();
    }

}
