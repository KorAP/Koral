package de.ids_mannheim.korap.query.serialize;

import de.ids_mannheim.korap.utils.JsonUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * convenience builder class for collection query -- new one
 *
 * @author hanl
 * @date 16/09/2014
 */
public class CollectionQueryBuilder3 {

    private boolean verbose;
    private List<Map> rq;
    private StringBuilder builder;

    public CollectionQueryBuilder3() {
        this(false);
    }

    public CollectionQueryBuilder3(boolean verbose) {
        this.verbose = verbose;
        this.builder = new StringBuilder();
        this.rq = new LinkedList<>();
    }


    public CollectionQueryBuilder3 addSegment(String field, String value) {
        String f = field + "=" + value;
        this.builder.append(f);
        return this;
    }

    public CollectionQueryBuilder3 add(String query) {
        this.builder.append(query);
        return this;
    }

    public CollectionQueryBuilder3 and() {
        this.builder.append(" & ");
        return this;
    }

    public CollectionQueryBuilder3 or() {
        this.builder.append(" | ");
        return this;
    }

    public CollectionQueryBuilder3 addResource(String collection) {
        try {
            List v = JsonUtils.read(collection, LinkedList.class);
            this.rq.addAll(v);
        } catch (IOException e) {
            throw new IllegalArgumentException("Conversion went wrong!");
        }
        return this;
    }

    public List getRequest() {
        List list = new ArrayList();
        if (!this.rq.isEmpty())
            list.addAll(this.rq);
        System.out.println("RAW QUERY " + this.builder.toString());
        CollectionQueryProcessor tree = new CollectionQueryProcessor(this.verbose);
        tree.process(this.builder.toString());
        list.add(tree.getRequestMap());
        return list;
    }

    public String toJSON() {
        return JsonUtils.toJSON(getRequest());
    }


}
