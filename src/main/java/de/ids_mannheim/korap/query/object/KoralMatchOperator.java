package de.ids_mannheim.korap.query.elements;

public enum MatchOperator {
    EQUALS("eq"), NOT_EQUALS("ne");

    String value;

    MatchOperator (String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "match:" + value;
    };
}
