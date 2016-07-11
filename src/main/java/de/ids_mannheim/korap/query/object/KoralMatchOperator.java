package de.ids_mannheim.korap.query.object;

/** Definition of the match operations used within koral:term.
 * @author margaretha
 * 
 */
public enum KoralMatchOperator {
    EQUALS("eq"), NOT_EQUALS("ne");

    String value;

    KoralMatchOperator (String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "match:" + value;
    };
}
