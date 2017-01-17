package de.ids_mannheim.korap.query.object;

public enum ClassRefCheck {
    
    INTERSECTS, EQUALS, UNEQUALS, INCLUDES, DISJOINT;
    
    @Override
    public String toString() {
        return "classRefCheck:"+name().toLowerCase();
    }
}
