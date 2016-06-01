package de.ids_mannheim.korap.query.elements;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.ids_mannheim.korap.query.parse.fcsql.ExpressionParser;
import de.ids_mannheim.korap.query.serialize.MapBuilder;
import eu.clarin.sru.server.fcs.parser.QueryNode;

public class KoralTermGroup implements Element {

    private static final KoralType type = KoralType.TERMGROUP;

    private String relation;
    private List<Object> operands = new ArrayList<Object>();

    public KoralTermGroup () {

    }

    public KoralTermGroup (ExpressionParser parser, KoralRelation relation,
            List<QueryNode> nodes) {
        this.relation = relation.toString();
        for (QueryNode node : nodes) {
            operands.add(parser.parseExpression(node, false, false));
        }
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public List<Object> getOperands() {
        return operands;
    }

    public void setOperands(List<Object> operands) {
        this.operands = operands;
    }

    @Override
    public Map<String, Object> buildMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("@type", type.toString());
        map.put("relation", getRelation());

        List<Map<String, Object>> operandList = new ArrayList<Map<String, Object>>();
        for (Object o : getOperands()) {
            operandList.add(MapBuilder.buildQueryMap(o));
        }
        map.put("operands", operandList);
        return map;
    }
}
