package de.ids_mannheim.korap.query.object;

public enum KoralFrame {

    SUCCEDS("succeeds"), SUCCEDS_DIRECTLY("succeedsDirectly"), OVERLAPS_RIGHT("overlapsRight"), 
    ALIGNS_RIGHT("alignsRight"), IS_WITHIN("isWithin"), STARTS_WITH("startsWith"), 
    MATCHES("matches"), ALIGNS_LEFT("alignsLeft"), IS_AROUND("isAround"), ENDS_WITH("endsWith"),
    OVERLAPS_LEFT("overlapsLeft"), PRECEEDS_DIRECTLY("precedesDirectly"), PRECEDES("precedes");
    
    private String value;
    KoralFrame(String value) {
        this.value = value;
    }
    
    @Override
    public String toString() {
        return "frames:"+value;
    }
}
