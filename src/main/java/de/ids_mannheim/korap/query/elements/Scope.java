package de.ids_mannheim.korap.query.elements;

public enum Scope {
    SENTENCE("s"), PARAGRAPH("p"), TEXT("t");
    String value;

    Scope (String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
