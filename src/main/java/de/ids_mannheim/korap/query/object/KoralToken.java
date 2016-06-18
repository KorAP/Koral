package de.ids_mannheim.korap.query.object;

import java.util.LinkedHashMap;
import java.util.Map;

import de.ids_mannheim.korap.query.object.KoralObject;
import de.ids_mannheim.korap.query.object.KoralType;

/**
 * @author margaretha
 * 
 */
public class KoralToken implements KoralObject {

    private final static KoralType type = KoralType.TOKEN;
    private KoralObject wrappedObject;

    public KoralToken () {}
    
    public KoralToken (KoralObject wrappedObject) {
        this.wrappedObject = wrappedObject;
    }

    public KoralObject getWrappedObject() {
		return wrappedObject;
	}
    public void setWrappedObject(KoralObject wrappedObject) {
		this.wrappedObject = wrappedObject;
	}

    @Override
    public Map<String, Object> buildMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("@type", type.toString());
        if (wrappedObject != null){
            map.put("wrap", wrappedObject.buildMap());
        }
        return map;
    }
}
