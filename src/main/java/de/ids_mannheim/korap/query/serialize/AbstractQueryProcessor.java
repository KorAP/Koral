package de.ids_mannheim.korap.query.serialize;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.tree.ParseTree;
import org.slf4j.Logger;

/**
 * This is an abstract class which provides fields and methods for
 * concrete
 * query processor implementations. All of those must implement
 * {@link #process(String)}, which is expected to process the query
 * and store a {@link #java.util.Map} representation of the KoralQuery
 * JSON-LD tree for that query.
 * 
 * @author Joachim Bingel (bingel@ids-mannheim.de)
 * @version 0.3.0
 * @since 0.1.0
 */
public abstract class AbstractQueryProcessor {

    Logger log;
    /**
     * The query string.
     */
    String query;
    /**
     * Top-level map representing the whole request.
     */
    LinkedHashMap<String, Object> requestMap = new LinkedHashMap<String, Object>();
    /**
     * Keeps track of open node categories.
     */
    LinkedList<String> openNodeCats = new LinkedList<String>();
    /**
     * Keeps track of all visited nodes in a tree.
     */
    List<ParseTree> visited = new ArrayList<ParseTree>();
    /**
     * Keeps track of active object, used for inserting new KoralQuery
     * objects
     * into last created objects.
     */
    LinkedList<LinkedHashMap<String, Object>> objectStack = new LinkedList<LinkedHashMap<String, Object>>();
    /**
     * Keeps track of how many objects there are to pop after every
     * recursion
     * of {@link #processNode(ParseTree)}
     */
    LinkedList<Integer> objectsToPop = new LinkedList<Integer>();
    /**
     * If true, print debug statements
     */
    public static boolean verbose = false;
    protected Integer stackedObjects = 0;
    /**
     * Contains error arrays, consisting of an error code and a
     * message.
     */
    private ArrayList<List<Object>> errors = new ArrayList<List<Object>>();
    /**
     * Contains warning arrays, consisting of a warning code
     * (optional) and a
     * message.
     */
    private ArrayList<List<Object>> warnings = new ArrayList<List<Object>>();
    /**
     * Contains other messages.
     */
    private ArrayList<List<Object>> messages = new ArrayList<List<Object>>();
    /**
     * Virtual collection queries.
     */
    private LinkedHashMap<String, Object> collection = new LinkedHashMap<String, Object>();
    /**
     * Holds information on displaying directives.
     */
    private LinkedHashMap<String, Object> meta = new LinkedHashMap<String, Object>();
    /**
     * Indicates which classes are to be highlighted in KWIC view.
     */
    private ArrayList<Integer> highlightClasses = new ArrayList<Integer>();

    /**
     * Indicates positions of alignment rulers in KWIC view.
     */
    private ArrayList<List<Integer>> alignments = new ArrayList<List<Integer>>();


    AbstractQueryProcessor () {
        requestMap.put("@context",
                "http://korap.ids-mannheim.de/ns/koral/0.3/context.jsonld");
        requestMap.put("errors", errors);
        requestMap.put("warnings", warnings);
        requestMap.put("messages", messages);
        requestMap.put("collection", collection);
        requestMap.put("query", new LinkedHashMap<String, Object>());
        requestMap.put("meta", meta);
    }


    /**
     * Called to process the query, is expected to generate a
     * Map-based
     * KoralQuery representation in {@link #requestMap}.
     * 
     * @param query
     *            The query string.
     */
    public abstract void process (String query);


    /**
     * Adds a warning to {@link #warnings}.
     * 
     * @param code
     *            The warning code.
     * @param msg
     *            The warning message.
     */
    public void addWarning (int code, String msg) {
        List<Object> warning = Arrays.asList(new Object[] { code, msg });
        warnings.add(warning);
    }


    /**
     * Adds a warning to {@link #warnings}.
     * 
     * @param msg
     *            The warning message.
     */
    public void addWarning (String msg) {
        List<Object> warning = Arrays.asList(new Object[] { msg });
        warnings.add(warning);
    }


    /**
     * Adds a generic message to {@link #messages}.
     * 
     * @param code
     *            The message code.
     * @param msg
     *            The message string.
     */
    public void addMessage (int code, String msg) {
        List<Object> message = Arrays.asList(new Object[] { code, msg });
        messages.add(message);
    }


    /**
     * Adds a generic message to {@link #messages}.
     * 
     * @param msg
     *            The message string.
     */
    public void addMessage (String msg) {
        List<Object> message = Arrays.asList(new Object[] { msg });
        messages.add(message);
    }


    /**
     * Adds an error to {@link #errors}.
     * 
     * @param code
     *            The error code.
     * @param msg
     *            The error message.
     */
    public void addError (int code, String msg) {
        List<Object> error = Arrays.asList(new Object[] { code, msg });
        errors.add(error);
    }


    /**
     * Adds an error to {@link #errors}.
     * 
     * @param fullErrorMsg
     *            First object is expected to be an integer
     *            error code, second a message.
     */
    public void addError (List<Object> fullErrorMsg) {
        errors.add(fullErrorMsg);
    }


    /**
     * Add a class to the list of classes highlighted in KWIC view.
     * 
     * @param classId
     *            The class ID.
     */
    public void addHighlightClass (int classId) {
        highlightClasses.add(classId);
        meta.put("highlight", highlightClasses);
    }


    public void addAlignment (int leftClassId, int rightClassId) {
        List<Integer> alignment = Arrays
                .asList(new Integer[] { leftClassId, rightClassId });
        alignments.add(alignment);
        meta.put("alignment", alignments);
    }


    /**
     * Getter method for the {@link #requestMap}, which represents the
     * entire KoralQuery request (query, displaying directives,
     * virtual
     * collections, messages etc.) based on a Java Map.
     * 
     * @return
     */
    public Map<String, Object> getRequestMap () {
        return requestMap;
    }
}