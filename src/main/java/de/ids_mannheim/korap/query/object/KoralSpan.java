package de.ids_mannheim.korap.query.object;

import java.util.LinkedHashMap;
import java.util.Map;

import de.ids_mannheim.korap.query.serialize.util.KoralException;
import de.ids_mannheim.korap.query.serialize.util.StatusCodes;

/**
 * @author margaretha
 *
 */
public class KoralSpan implements KoralObject{
	
	private static final KoralType type = KoralType.SPAN;
	private final KoralObject wrappedObject;
	private KoralObject attribute;
	
	public KoralSpan(KoralTerm term) throws KoralException {
		if (term == null){
			throw new KoralException(StatusCodes.MALFORMED_QUERY, "KoralSpan must not wrap null.");
		}
		this.wrappedObject = term;
	}
	
	public KoralSpan(KoralTermGroup termGroup) throws KoralException {
		if (termGroup == null){
			throw new KoralException(StatusCodes.MALFORMED_QUERY,"KoralSpan must not wrap null.");
		}
		this.wrappedObject = termGroup;
	}

	public KoralObject getWrappedObject() {
		return wrappedObject;
	}
	
	public KoralObject getAttribute() {
		return attribute;
	}
	
	public void setAttribute(KoralTerm attribute) {
		this.attribute = attribute;
	}
	
	public void setAttribute(KoralTermGroup attributes) {
		this.attribute = attributes;
	}
	
	@Override
	public Map<String, Object> buildMap() {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("@type", type.toString());
        if (wrappedObject == null){
        	throw new NullPointerException("KoralSpan does not have a wrapped object.");
        }
        map.put("wrap", wrappedObject.buildMap());
        if(attribute != null){
        	map.put("wrap", attribute.buildMap());
        }
		return map;
	}
}
