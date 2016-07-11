package de.ids_mannheim.korap.query.object;

import java.util.LinkedHashMap;
import java.util.Map;

import de.ids_mannheim.korap.query.serialize.util.KoralException;
import de.ids_mannheim.korap.query.serialize.util.StatusCodes;
import de.ids_mannheim.korap.query.object.KoralMatchOperator;
import de.ids_mannheim.korap.query.object.KoralType;

/** Definition of koral:term in KoralQuery.
 * 
 * @author margaretha
 * 
 */
public class KoralTerm implements KoralObject {

    private static final KoralType koralType = KoralType.TERM;
  
    private final String key;
    private String value;
    private String layer;
    private String foundry;
    private KoralMatchOperator operator; // match
    
    private KoralTermType type;
    
    private boolean caseSensitive = true;
    private boolean diacriticSensitive = true; 

    public KoralTerm(String key) throws KoralException {
    	if (key == null){
    		throw new KoralException(StatusCodes.MALFORMED_QUERY, 
    				"KoralTerm key cannot be null.");
    	}
    	this.key = key;
    }
    
    public KoralTerm(KoralContext context) throws KoralException {
    	if (context.getKey() == null){
    		throw new KoralException(StatusCodes.MALFORMED_QUERY, 
    				"KoralTerm key cannot be null.");
    	}
    	this.key = context.getKey();
    	this.foundry = KoralContext.FOUNDRY;
    	this.layer = KoralContext.LAYER;
	}
    
    public String getValue() {
		return value;
	}
    
    public void setValue(String value) {
		this.value = value;
	}
    
	public String getLayer() {
        return layer;
    }

    public void setLayer(String layer) {
        this.layer = layer;
    }

    public String getFoundry() {
        return foundry;
    }

    public void setFoundry(String foundry) {
        this.foundry = foundry;
    }

    public KoralMatchOperator getOperator() {
		return operator;
	}
    
    public void setOperator(KoralMatchOperator operator) {
		this.operator = operator;
	}

    public String getKey() {
        return key;
    }

    public KoralTermType getType() {
        return type;
    }

    public void setType(KoralTermType regex) {
        this.type = regex;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean isCaseSensitive) {
        this.caseSensitive = isCaseSensitive;
    }

    public boolean isDiacriticSensitive() {
        return diacriticSensitive;
    }

    public void setDiacriticSensitive(boolean diacriticSensitive) {
        this.diacriticSensitive = diacriticSensitive;
    }

    @Override
    public Map<String, Object> buildMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("@type", koralType.toString());
        if (!isCaseSensitive()) {
            map.put("caseInsensitive", "true");
        }
        if (!isDiacriticSensitive()){
            map.put("diacriticInsensitive", "true");
        }
            
        map.put("key", getKey());
        if (value != null){
        	map.put("value", getValue());
        }
        if (foundry != null){
           map.put("foundry", getFoundry());
        }
        if (layer !=null){
           map.put("layer", getLayer());
        }
        if (type != null){
        	map.put("type", getType().toString());
        }
		if (operator !=null){
			map.put("match", getOperator().toString());
		}
        return map;
    }

    /** Definition of possible types of koral:term.
     * 
     * @author margaretha
     *
     */
    public enum KoralTermType {
        STRING("type:string"), REGEX("type:regex"), WILDCARD("type:wildcard"), PUNCT(
                "type:punct");

        String value;

        KoralTermType (String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

}
