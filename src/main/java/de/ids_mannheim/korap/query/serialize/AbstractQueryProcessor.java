package de.ids_mannheim.korap.query.serialize;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.tree.ParseTree;
import org.slf4j.Logger;

public abstract class AbstractQueryProcessor {

    Logger log;
    /**
     *  The query
     */
    String query;
    /**
     * Top-level map representing the whole request.
     */
    LinkedHashMap<String, Object> requestMap = new LinkedHashMap<String, Object>();
    /**
     * Keeps track of open node categories
     */
    LinkedList<String> openNodeCats = new LinkedList<String>();
    /**
     * Keeps track of all visited nodes in a tree
     */
    List<ParseTree> visited = new ArrayList<ParseTree>();
    /**
     * Keeps track of active object.
     */
    LinkedList<LinkedHashMap<String, Object>> objectStack = new LinkedList<LinkedHashMap<String, Object>>();
    /**
     * Keeps track of how many objects there are to pop after every recursion of {@link #processNode(ParseTree)}
     */
    LinkedList<Integer> objectsToPop = new LinkedList<Integer>();
    /**
     * If true, print debug statements
     */
    public static boolean verbose = false;
    protected Integer stackedObjects = 0;
    private ArrayList<List<Object>> errors = new ArrayList<List<Object>>();
    private ArrayList<List<Object>> warnings = new ArrayList<List<Object>>();
    private ArrayList<List<Object>> messages = new ArrayList<List<Object>>();
    private LinkedHashMap<String, Object> collection = new LinkedHashMap<String,Object>();
    private LinkedHashMap<String, Object> meta = new LinkedHashMap<String,Object>();
    private ArrayList<Integer> highlightClasses = new ArrayList<Integer>();

    AbstractQueryProcessor() {
        requestMap.put("@context", "http://ids-mannheim.de/ns/KorAP/json-ld/v0.2/context.jsonld");
        requestMap.put("errors", errors);
        requestMap.put("warnings", warnings);
        requestMap.put("messages", messages);
        requestMap.put("collection", collection);
        requestMap.put("query", new LinkedHashMap<String, Object>());
        requestMap.put("meta", meta);
    }

    public abstract void process(String query);

    public void addWarning(int code, String msg) {
        List<Object> warning = Arrays.asList(new Object[]{code, msg}); 
        warnings.add(warning);
    }

    public void addWarning(String msg) {
        List<Object> warning = Arrays.asList(new Object[]{msg}); 
        warnings.add(warning);
    }

    public void addMessage(int code, String msg) {
        List<Object> message = Arrays.asList(new Object[]{code, msg}); 
        messages.add(message);
    }

    public void addMessage(String msg) {
        List<Object> message = Arrays.asList(new Object[]{msg}); 
        messages.add(message);
    }

    public void addError(int code, String msg) {
        List<Object> error = Arrays.asList(new Object[]{code, msg}); 
        errors.add(error);
    }

    public void addError(List<Object> fullErrorMsg) {
        errors.add(fullErrorMsg);
    }

    public void addHighlightClass(int classId) {
        highlightClasses.add(classId);
        meta.put("highlight", highlightClasses);
    }

    public Map<String, Object> getRequestMap() {
        return requestMap;
    }
}