package de.ids_mannheim.korap.query.object;

public enum ClassRefCheck {
    
    INTERSECTS, EQUALS, DIFFERS, INCLUDES, DISJOINT;
    
    @Override
    public String toString() {
        return "classRefCheck:"+name().toLowerCase();
    }
}
