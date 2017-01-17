package de.ids_mannheim.korap.query.object;

public enum ClassRefOp {

    INVERSION, INTERSECTION, UNION, DELETE;

    @Override
    public String toString () {
        return "classRefOp:" + name().toLowerCase();
    }
}
