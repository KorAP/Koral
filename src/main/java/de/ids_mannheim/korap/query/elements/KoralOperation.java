package de.ids_mannheim.korap.query.elements;

public enum KoralOperation {
    SEQUENCE("operation:sequence"), POSITION("operation:position"), DISJUNCTION(
            "operation:disjunction"), REPETITION("operation:repetition"), CLASS(
            "operation:class"), MERGE("operation:merge"), RELATION(
            "operation:relation");

    String value;

    KoralOperation (String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

}
