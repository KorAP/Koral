package de.ids_mannheim.korap.query.serialize;

import java.util.Map;

import de.ids_mannheim.korap.query.elements.KoralGroup;
import de.ids_mannheim.korap.query.elements.KoralTerm;
import de.ids_mannheim.korap.query.elements.KoralTermGroup;
import de.ids_mannheim.korap.query.elements.KoralToken;

public class MapBuilder {

    public static Map<String, Object> buildQueryMap(Object o) {
        if (o != null) {
            if (o instanceof KoralToken) {
                KoralToken token = (KoralToken) o;
                return token.buildMap();
            }
            else if (o instanceof KoralGroup) {
                KoralGroup group = (KoralGroup) o;
                return group.buildMap();
            }
            if (o instanceof KoralTerm) {
                KoralTerm term = (KoralTerm) o;
                return term.buildMap();
            }
            else if (o instanceof KoralTermGroup) {
                KoralTermGroup termGroup = (KoralTermGroup) o;
                return termGroup.buildMap();
            }
        }
        return null;
    }
}
