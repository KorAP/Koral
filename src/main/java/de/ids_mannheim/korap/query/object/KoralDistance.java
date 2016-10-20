package de.ids_mannheim.korap.query.object;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Definition of koral:distance in KoralQuery.
 * 
 * @author margaretha
 *
 */
public class KoralDistance implements KoralObject {

    private final KoralType type = KoralType.DISTANCE;
    private String key = "w";
    private String foundry;
    private String layer;
    private KoralBoundary boundary;


    public KoralDistance (KoralBoundary boundary) {
        this.boundary = boundary;
    }


    public KoralDistance (String key, KoralBoundary boundary) {
        this(boundary);
        this.key = key;
    }


    public String getKey () {
        return key;
    }


    public void setKey (String key) {
        this.key = key;
    }


    public String getFoundry () {
        return foundry;
    }


    public void setFoundry (String foundry) {
        this.foundry = foundry;
    }


    public String getLayer () {
        return layer;
    }


    public void setLayer (String layer) {
        this.layer = layer;
    }


    public KoralBoundary getBoundary () {
        return boundary;
    }


    public void setBoundary (KoralBoundary boundary) {
        this.boundary = boundary;
    }


    @Override
    public Map<String, Object> buildMap () {
        Map<String, Object> distanceMap = new LinkedHashMap<String, Object>();
        distanceMap.put("@type", type.toString());
        distanceMap.put("key", key);
        if (foundry != null) {
            distanceMap.put("foundry", foundry);
        }
        if (layer != null) {
            distanceMap.put("layer", layer);
        }
        distanceMap.put("boundary", boundary.buildMap());
        return distanceMap;
    }
}
