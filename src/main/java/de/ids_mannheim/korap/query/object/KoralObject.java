package de.ids_mannheim.korap.query.object;

import java.util.Map;

/**
 * @author margaretha
 * 
 */
public interface KoralObject {

    /** Serializes the KoralObject into JSON-LD and structures it as a map.  
     * 
     * @return a map containing parts of the JSON-LD serialization of the Koral object.
     */
    public Map<String, Object> buildMap();
}
