package de.ids_mannheim.korap.query.object;

/** Context definition for the search scope in WithinQuery,
 * for instance, a withinquery in poliqarp is
 * <code>"Bild" within s</code>.
 * 
 * @author margaretha
 * 
 */
public enum KoralContext {
	SENTENCE("s"), PARAGRAPH("p"), TEXT("t");
	
	private final String key;
	public static final String FOUNDRY ="base";
	public static final String LAYER="s"; // surface
	
	KoralContext(String key){
		this.key = key;
	}

	public String getKey() {
		return key;
	}

}
