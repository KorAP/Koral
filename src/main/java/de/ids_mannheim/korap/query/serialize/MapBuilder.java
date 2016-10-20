package de.ids_mannheim.korap.query.serialize;

import java.util.Map;

import de.ids_mannheim.korap.query.object.KoralGroup;
import de.ids_mannheim.korap.query.object.KoralObject;
import de.ids_mannheim.korap.query.object.KoralSpan;
import de.ids_mannheim.korap.query.object.KoralTerm;
import de.ids_mannheim.korap.query.object.KoralTermGroup;
import de.ids_mannheim.korap.query.object.KoralToken;

/**
 * @author margaretha
 * 
 */
public class MapBuilder {

    /**
     * Builds a query map containing JSON-LD serialization parts of
     * the given KoralObject.
     * 
     * @param o
     * @return a map
     */
    public static Map<String, Object> buildQueryMap (KoralObject o) {
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
            else if (o instanceof KoralSpan) {
                KoralSpan span = (KoralSpan) o;
                return span.buildMap();
            }
        }
        return null;
    }
}
