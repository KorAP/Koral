package de.ids_mannheim.korap.query.object;

/**
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
