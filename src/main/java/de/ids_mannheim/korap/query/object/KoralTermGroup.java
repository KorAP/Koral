package de.ids_mannheim.korap.query.object;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.ids_mannheim.korap.query.serialize.MapBuilder;
import de.ids_mannheim.korap.query.serialize.util.KoralException;

/**
 * Definition of koral:termGroup in KoralQuery.
 * 
 * @author margaretha
 * 
 */
public class KoralTermGroup implements KoralObject {

    private static final KoralType type = KoralType.TERMGROUP;

    private String relation;
    private List<KoralObject> operands = new ArrayList<KoralObject>();


    public KoralTermGroup (KoralTermGroupRelation relation,
                           List<KoralObject> operands)
            throws KoralException {
        this.relation = relation.toString();
        this.operands = operands;
    }


    public String getRelation () {
        return relation;
    }


    public void setRelation (String relation) {
        this.relation = relation;
    }


    public List<KoralObject> getOperands () {
        return operands;
    }


    public void setOperands (List<KoralObject> operands) {
        this.operands = operands;
    }


    @Override
    public Map<String, Object> buildMap () {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("@type", type.toString());
        map.put("relation", getRelation());

        List<Map<String, Object>> operandList = new ArrayList<Map<String, Object>>();
        for (KoralObject o : getOperands()) {
            operandList.add(MapBuilder.buildQueryMap(o));
        }
        map.put("operands", operandList);
        return map;
    }
}
