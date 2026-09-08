package de.ids_mannheim.korap.query.serialize.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * MORPH_Mapper:
 * - resource for translating C2-style CONNEXOR tags to marmot annotations 
 *   and C2-style STTS tags to original STTS annotations as used in KorAP.
 * 15.06.26/FB
 */

/**
 * @author Franck Bodmer
 * @date 15.06.26
 */
public class MORPH_Mapper {

	/* Conversion from C2-style STTS to original STTS tagset.
	 * C2 style: map represents an annotation hierarchy:
	 * e.g. VRB > fin > m or a or v.
	 * input: 1st value = Word class in upper case.
	 *        following values are in lower case.
	 * output: all STTS values are in upper case.
	 */
	
    private static final Map<List<String>, String> stts = new HashMap<>();
    
	private static final boolean DEBUG = false;

    static {
        stts.put(List.of("N", "nn"), 				"NN");
        stts.put(List.of("N", "ne"), 				"NE");
        stts.put(List.of("N"), 						"N.*");
        
        stts.put(List.of("ADJ", "at"),				"ADJA");
        stts.put(List.of("ADJ", "d"), 				"ADJD");
        stts.put(List.of("ADJ"), 					"ADJ.");
        
        stts.put(List.of("CARD"), 					"CARD");
        
        stts.put(List.of("VRB", "fin", "m"),		"VMFIN");
        stts.put(List.of("VRB", "fin", "a"),		"VAFIN");
        stts.put(List.of("VRB", "fin", "v"),		"VVFIN");
        stts.put(List.of("VRB", "fin"),				"V.FIN");
        stts.put(List.of("VRB", "imp", "a"),		"VAIMP");
        stts.put(List.of("VRB", "imp", "v"),		"VVIMP");
        stts.put(List.of("VRB", "imp"),				"V.IMP");
        
        stts.put(List.of("VRB", "inf", "a"),		"VAINF");
        stts.put(List.of("VRB", "inf", "v"),		"VVINF");
        stts.put(List.of("VRB", "inf", "m"),		"VMINF");
        stts.put(List.of("VRB", "inf", "z"),		"VVIZU");
        stts.put(List.of("VRB", "inf"),				"\"(V.INF|VVIZU)\"");   // must be returned between "..." because of the () inside, 
        																	// or wordOrRegex in TermToFieldMap will fail.
        stts.put(List.of("VRB", "pp", "a"),			"VAPP");
        stts.put(List.of("VRB", "pp", "m"),			"VMPP");
        stts.put(List.of("VRB", "pp", "v"),			"VVPP");
        stts.put(List.of("VRB", "pp"),				"V.PP");

        stts.put(List.of("VRB"),					"V.*");
        
        stts.put(List.of("ART"),					"ART");

        stts.put(List.of("PRON","per", "ref"),		"PRF");
        stts.put(List.of("PRON","per", "irr"),		"PPER");
        stts.put(List.of("PRON","per"),				"\"(PRF|PPER)\"");	// reg. expression must be returned with "...".

        stts.put(List.of("PRON","pos", "at"),		"PPOSAT");
        stts.put(List.of("PRON","pos", "sub"),		"PPOSS");
        stts.put(List.of("PRON","pos"),				"PPOS.*");

        stts.put(List.of("PRON","dem", "at"),		"PDAT");
        stts.put(List.of("PRON","dem", "sub"),		"PDS");
        stts.put(List.of("PRON","dem"),				"PP.*");

        stts.put(List.of("PRON","ind", "at"),		"PIAT");
        stts.put(List.of("PRON","ind", "atd"),		"PIDAT");
        stts.put(List.of("PRON","ind", "sub"),		"PIS");
        stts.put(List.of("PRON","ind"),				"PI.*");
    
        stts.put(List.of("PRON","rel", "at"),		"PRELAT");
        stts.put(List.of("PRON","rel", "sub"),		"PRELS");
        stts.put(List.of("PRON","rel"),				"PREL.*");

        stts.put(List.of("PRON","w", "at"),			"PWAT");
        stts.put(List.of("PRON","w", "sub"),		"PWS");
        stts.put(List.of("PRON","w", "av"),			"PWAV");
        stts.put(List.of("PRON","w"),				"PW.*");

        stts.put(List.of("PRON","ad"),				"PROAV");

        stts.put(List.of("PRON"),					"P.*");

        stts.put(List.of("ADV"),					"ADV");
        
        stts.put(List.of("KON", "unt", "i"),		"KOUI");
        stts.put(List.of("KON", "unt", "s"),		"KOUS");
        stts.put(List.of("KON", "neb"),				"KON");
        stts.put(List.of("KON", "kom"),				"KOKOM");
        stts.put(List.of("KON"),					"KO.*");
    
        stts.put(List.of("AP", "pr"),				"APPR");
        stts.put(List.of("AP", "prart"),			"APPRART");
        stts.put(List.of("AP", "po"),				"APPO");
        stts.put(List.of("AP", "zr"),				"APZR");
        stts.put(List.of("AP"),						"AP.*");

        stts.put(List.of("PTK", "zu"),				"PTKZU");
        stts.put(List.of("PTK", "neg"),				"PTKNEG");
        stts.put(List.of("PTK", "vz"),				"PTKVZ");
        stts.put(List.of("PTK", "ka"),				"PTKA");
        stts.put(List.of("PTK", "ant"),				"PTKANT");
        stts.put(List.of("PTK"),					"PTK.*");

        stts.put(List.of("ITJ"),					"ITJ");
        stts.put(List.of("TRUNC"),					"TRUNC");
        stts.put(List.of("XY"),						"XY");
        stts.put(List.of("FM"),						"FM");
            
    }

    //private static final int maxEntries = 3; // max. no. of entries in stts(List<String>...).
    
    /* trasnslate_STTS
     * - translates a list of annotations containing no negative values.
     * - input is C2-style STTS.
     * - output is original STTS tagset.
     * - e.g. [VRB, fin, a] -> "VAFIN".
     *  
     * 15.06.26/FB
     */
    
    public static String translate_STTS (List<String> annots) 
    {
    	if (stts.containsKey(annots))
            return stts.get(annots);
        else
            return null;
    }

    /* translate_STTS_withNeg()
     * 
     * - translates annotations containing negative values.
     * - e.g. [VRB, fin, -a] -> VRB/fin - VRB/fin/a -> [V.FIN, -VAFIN].
     * - e.g. [VRB, -fin, a] -> [V.*, -V.FIN].
     * - e.g. [-VRB, fin, a] -> [-VAFIN]. Or should it be [-V.*] ?
     * 
     * Args:
     * annots : list of annotations. Hierarchie from left to right conform to STTS.
     *          assuming the negation char has been removed yet.
     * iNeg   : zero-based index of the first negative element in annots.
     * Returns: a list of STTS annotations corresponding to the input list,
     *          or null if no translation is found.
     * @author Franck Bodmer
     * @date 16.06.26
     */
    
    public static List<String> translate_STTS_withNeg(List<String> annots, int iNeg)
    
    {
    if( iNeg < 0 || iNeg >= annots.size() )
    	return null;

    List<String> 
    	newAnnots = new ArrayList<String>();
    String
    	searchAnnot = translate_STTS(annots.subList(0, iNeg)); // includes 0..iNeg-1.
    
    if( searchAnnot == null )
    	return null;
    
    newAnnots.add(searchAnnot);
    
    searchAnnot = translate_STTS(annots.subList(0, iNeg+1)); // include 0..iNeg.
    
    if( searchAnnot == null )
    	return null;
    
    // a negation char must be included inside the "..." of searchAnnot if searchAnnot == \"...\":
    // e.g. "-" + "\"(A|B)\"" --> "-\"(A|B)\".
    if( searchAnnot.startsWith("\"") )
    	newAnnots.add("\"-" + searchAnnot.substring(1));
    else
    	newAnnots.add("-" + searchAnnot);
    
    if( DEBUG )
    	System.out.printf("Debug: translate_STTS_withNeg: annots='%s' -> '%s'.\n", annots.toString(), newAnnots.toString());
    
    return newAnnots;	
    }

    /*
     *  CONNEXOR tagset. Conversion from the C2 style CONNEXOR to marmot tagset.
     *  
     *  - first value in CONNEXOR tags is mandatory and expected to be a POS with capital letters.
     *    It is converted to marmot/p layer (STTS). Again STTS values may be expressed as a regular expresion,
     *    e.g. "N" -> "N.", "V" -> "V.*".
     *  - all other values expected to be lower cased and translate to a morphological annot. 
     *    on the marmot/m layer.
     *  - most values translate as single value : single val -> single val. E.g. "N" -> "p=N.".
     *  - some annotation contain a value pair ("N prop"), or a value tripple ("V pcp prog").
     *  - in such cases, the first value is expected to be the POS.
     *  - mandatory and optional values are not checked.
     */
    
    private static final Map<String, String> connexor = new HashMap<>();

    static {
    	connexor.put("N", 			"p=N."); // N = NN+NE.
    	connexor.put("N prop",		"p=NE"); // N+prop = NE = named entity.
    	connexor.put("N -prop",		"p=NN"); // N-prop = NN = normal entity.
    	connexor.put("N abbr",		"p=NE"); // N+abbr = NE = named entity. There is no abbreviation key word in marmot.
    	connexor.put("N -abbr",		"p=NN"); // N+abbr = NN = normal entity. There is no abbreviation key word in marmot.
    	connexor.put("pl",			"m=number:pl");
    	connexor.put("-pl",			"m=number:sg");	// CONNEXOR: sing encoded as -PL
        connexor.put("imp", 		"m=mood:imper");
        connexor.put("ind", 		"m=mood:indic");
        connexor.put("sub", 		"m=mood:subj");
        connexor.put("A", 			"p=ADJ.");
        connexor.put("cmp", 		"m=degree:comp");
        connexor.put("sup", 		"m=degree:sup");
        connexor.put("pos", 		"m=degree:pos");
        connexor.put("ADV", 		"p=ADV");
        connexor.put("DET",			"p=ART");
        connexor.put("INTERJ",		"p=ITJ");
        connexor.put("C",			"p=KO.*"); // extention of MORPH().
        connexor.put("CC",			"p=KON");
        connexor.put("CS",			"p=KOUI");
        connexor.put("NUM -ord",	"p=CARD");	// STTS: has no tag for ORD.        
        connexor.put("PREP",		"p=APPR");
        connexor.put("PRON",		"p=P.*");        
        connexor.put("V",			"p=V.*");     
        connexor.put("V -inf -pcp",	"p=V.FIN");	// CONNEXOR: finite verbs.     
        connexor.put("V inf",		"p=V.INF");	// CONNEXOR: infinitive verbs; STTS: includes modal and auxiliary infinitives.     
        connexor.put("V pcp prog",	"p=ADJ.");	// CONNEXOR: progressive participle, Partizip I; STTS=kein Tag direkt, sondern POS=ADJA oder ADJD.     
        connexor.put("V pcp perf",	"p=V.PP");	// CONNEXOR: perfect participle, Partizip II; STTS: includes modal and auxiliary participle.     
        connexor.put("pres",		"m=tense:pres");     
        connexor.put("past",		"m=tense:past");     
        
        // extensions: supplying (missing) tags for MORPH corresponding to a morph-Tag in marmoT:
        
        connexor.put("acc",			"m=case:acc");     
        connexor.put("dat",			"m=case:dat");     
        connexor.put("gen",			"m=case:gen");     
        connexor.put("nom",			"m=case:nom");
        connexor.put("fem",			"m=gender:fem");     
        connexor.put("masc",		"m=gender:masc");     
        connexor.put("neut",		"m=gender:neut");     
        connexor.put("1",			"m=person:1");     
        connexor.put("2",			"m=person:2");     
        connexor.put("3",			"m=person:3");     
        connexor.put("sg",			"m=number:sg");	// CONNEXOR: has no explicit tag for sing. Added here.
        
        }
    
    /* trasnslate_CONNEXOR
     * - translates a single c2-style CONNEXOR annotation to a marmot annotation.
     * - input is C2-style CONNEXOR.
     * - output is a marmot pos or morphological layer annotation.
     * - e.g. "N"   -> "p=N".
     * - e.g. "ind" -> "m=mood:indic"
     *  
     * 16.06.26/FB
     */
    
    public static String translate_CONNEXOR (String annot) 
    {
    	if (connexor.containsKey(annot))
            return connexor.get(annot);
        else
            return null;
    }

    /* isMorphAnnot
     * - e.g. annot = "imp" translate to: "m=mood:imper" "m" = morph. annotation -> return true.
     * - skip leading negation char in annot: e.g. "-imp" -> check "imp".
     * - morph. annotations are expected to be in lower case.
     * 22.06.26/FB
     */
    
    public static boolean isMorphAnnot(String annot)
    
    {
    if( annot == null )
    	return false;
    
    String 
    	annot2 = (annot.startsWith("-") ? annot.substring(1) : annot).toLowerCase(),
    	translatedAnnot = connexor.get( annot2);
    
    if( translatedAnnot == null )
    	return false;
    
    return translatedAnnot.startsWith("m=") ? true : false;
    }
}
