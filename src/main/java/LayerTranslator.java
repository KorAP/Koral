import java.util.HashMap;
import java.util.Map;

/**
 * @author hanl
 * @date 24/01/2014
 */
public class LayerTranslator {

    //fixme: standardize
    private String[] bases = new String[]{"morph", "lemma", "mds", "dep"};
    private Map<String, String> mapper;


    public LayerTranslator() {
        mapper = new HashMap<>();
    }

    public void set(String m, String l, String mds, String dep) {
        mapper.clear();
        mapper.put(bases[0], m);
        mapper.put(bases[1], l);
        mapper.put(bases[2], mds);
        mapper.put(bases[3], dep);
    }

    public String translate(String base) {
        String r = mapper.get(base);
        if (r != null)
            return r;
        return "";
    }
}
