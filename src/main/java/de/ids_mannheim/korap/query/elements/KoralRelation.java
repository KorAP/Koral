package de.ids_mannheim.korap.query.elements;

public enum KoralRelation {

    AND("relation:and"), OR("relation:or");

    String value;

    KoralRelation (String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
