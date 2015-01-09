package de.ids_mannheim.korap.query.serialize;

import com.fasterxml.jackson.databind.JsonNode;
import de.ids_mannheim.korap.query.serialize.util.QueryException;
import de.ids_mannheim.korap.utils.JsonUtils;

import java.io.IOException;
import java.util.*;

/**
 * @author hanl
 * @date 13/05/2014
 */
public class CollectionQueryBuilder2 {

    private List<Map> rq;
    private Map groups;
    private CollectionTypes types;
    private boolean verbose;

    public CollectionQueryBuilder2() {
        this.verbose = false;
        this.rq = new ArrayList<>();
        this.groups = new HashMap();
        this.types = new CollectionTypes();
    }

    public CollectionQueryBuilder2(boolean verbose) {
        this();
        this.verbose = verbose;
    }


    public CollectionQueryBuilder2 addResource(String collections) {
        try {
            List v = JsonUtils.read(collections, LinkedList.class);
            this.rq.addAll(v);
        } catch (IOException e) {
            throw new IllegalArgumentException("Conversion went wrong!");
        }
        return this;
    }

    public CollectionQueryBuilder2 addResources(List<String> queries) {
        for (String query : queries)
            addResource(query);
        return this;
    }

    public CollectionQueryBuilder2 setQuery(String query) throws QueryException {
        CollectionQueryProcessor tree = new CollectionQueryProcessor();
        tree.process(query);
        this.groups = tree.getRequestMap();
        return this;
    }

    public List raw() {
        List list = new ArrayList(this.rq);
        list.add(types.createMetaFilter(this.groups));
        return list;
    }

    private Object raw2() {
        return this.groups;
    }

    public String toCollections() {
        Map value = new HashMap();
        value.put("collections", raw2());
        return JsonUtils.toJSON(value);
    }

    public JsonNode toNode() {
        return JsonUtils.valueToTree(raw2());
    }

    public String toJSON() {
        return JsonUtils.toJSON(raw2());
    }


    // add public filter to original query
    private void addToGroup() {
        Map first = this.rq.get(0);

    }
}
