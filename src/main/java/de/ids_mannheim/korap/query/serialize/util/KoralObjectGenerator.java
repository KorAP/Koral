package de.ids_mannheim.korap.query.serialize.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;

import de.ids_mannheim.korap.query.serialize.AbstractQueryProcessor;

public class KoralObjectGenerator {

    protected static final Integer MAXIMUM_DISTANCE = 100;
    private static AbstractQueryProcessor qp;


    public static void setQueryProcessor (AbstractQueryProcessor qp) {
        KoralObjectGenerator.qp = qp;
    }


    public static LinkedHashMap<String, Object> makeSpan () {
        LinkedHashMap<String, Object> span = new LinkedHashMap<String, Object>();
        span.put("@type", "koral:span");
        return span;
    }


    public static LinkedHashMap<String, Object> makeSpan (String key) {
        LinkedHashMap<String, Object> span = new LinkedHashMap<String, Object>();
        span.put("@type", "koral:span");
        LinkedHashMap<String, Object> term = makeTerm();
        term.put("key", key);
        span.put("wrap", term);
        return span;
    }


    public static LinkedHashMap<String, Object> makeTerm () {
        LinkedHashMap<String, Object> term = new LinkedHashMap<String, Object>();
        term.put("@type", "koral:term");
        return term;
    }


    public static LinkedHashMap<String, Object> makeTermGroup (String relation) {
        LinkedHashMap<String, Object> term = new LinkedHashMap<String, Object>();
        term.put("@type", "koral:termGroup");
        term.put("relation", "relation:" + relation);
        term.put("operands", new ArrayList<Object>());
        return term;
    }


    public static LinkedHashMap<String, Object> makeDoc () {
        LinkedHashMap<String, Object> term = new LinkedHashMap<String, Object>();
        term.put("@type", "koral:doc");
        return term;
    }


    public static LinkedHashMap<String, Object> makeDocGroup (String relation) {
        LinkedHashMap<String, Object> term = new LinkedHashMap<String, Object>();
        term.put("@type", "koral:docGroup");
        term.put("operation", "operation:" + relation);
        term.put("operands", new ArrayList<Object>());
        return term;
    }


    public static LinkedHashMap<String, Object> makeToken () {
        LinkedHashMap<String, Object> token = new LinkedHashMap<String, Object>();
        token.put("@type", "koral:token");
        return token;
    }


    public static LinkedHashMap<String, Object> makeGroup (String operation) {
        LinkedHashMap<String, Object> group = new LinkedHashMap<String, Object>();
        group.put("@type", "koral:group");
        group.put("operation", "operation:" + operation);
        group.put("operands", new ArrayList<Object>());
        return group;
    }


    public static LinkedHashMap<String, Object> makeRepetition (Integer min,
            Integer max) {
        LinkedHashMap<String, Object> group = makeGroup("repetition");
        group.put("boundary", makeBoundary(min, max));
        return group;
    }


    @Deprecated
    public static LinkedHashMap<String, Object> makePosition (String frame) {
        LinkedHashMap<String, Object> group = new LinkedHashMap<String, Object>();
        group.put("@type", "koral:group");
        group.put("operation", "operation:position");
        group.put("frame", "frame:" + frame);
        group.put("operands", new ArrayList<Object>());
        return group;
    }


    public static LinkedHashMap<String, Object> makePosition (
            String[] allowedFrames) {
        LinkedHashMap<String, Object> group = new LinkedHashMap<String, Object>();
        group.put("@type", "koral:group");
        group.put("operation", "operation:position");
        group.put("frames", Arrays.asList(allowedFrames));
        group.put("operands", new ArrayList<Object>());
        return group;
    }


    public static LinkedHashMap<String, Object> makeSpanClass (int classId) {
        return makeSpanClass(classId, false);
    }


    @Deprecated
    public static LinkedHashMap<String, Object> makeSpanClass (int classId,
            boolean setBySystem) {
        LinkedHashMap<String, Object> group = new LinkedHashMap<String, Object>();
        group.put("@type", "koral:group");
        group.put("operation", "operation:class");
        if (setBySystem) {
            classId += 128;
            qp.addMessage("A class has been introduced into the backend representation of "
                    + "your query for later reference to a part of the query. The class id is "
                    + classId);
        }
        group.put("classOut", classId);
        group.put("operands", new ArrayList<Object>());
        return group;
    }


    public static LinkedHashMap<String, Object> makeClassRefCheck (
            ArrayList<String> check, Integer[] classIn, int classOut) {
        LinkedHashMap<String, Object> group = new LinkedHashMap<String, Object>();
        group.put("@type", "koral:group");
        group.put("operation", "operation:class");
        group.put("classRefCheck", check);
        group.put("classIn", Arrays.asList(classIn));
        group.put("classOut", classOut);
        group.put("operands", new ArrayList<Object>());
        return group;
    }


    public static LinkedHashMap<String, Object> makeClassRefOp (
            String operation, Integer[] classIn, int classOut) {
        LinkedHashMap<String, Object> group = new LinkedHashMap<String, Object>();
        group.put("@type", "koral:group");
        group.put("operation", "operation:class");
        group.put("classRefOp", operation);
        group.put("classIn", Arrays.asList(classIn));
        group.put("classOut", classOut);
        group.put("operands", new ArrayList<Object>());
        return group;
    }


    @Deprecated
    public static LinkedHashMap<String, Object> makeTreeRelation (String reltype) {
        LinkedHashMap<String, Object> group = new LinkedHashMap<String, Object>();
        group.put("@type", "koral:treeRelation");
        if (reltype != null)
            group.put("reltype", reltype);
        return group;
    }


    public static LinkedHashMap<String, Object> makeRelation () {
        LinkedHashMap<String, Object> group = new LinkedHashMap<String, Object>();
        group.put("@type", "koral:relation");
        return group;
    }


    public static LinkedHashMap<String, Object> makeBoundary (Integer min,
            Integer max) {
        LinkedHashMap<String, Object> group = new LinkedHashMap<String, Object>();
        group.put("@type", "koral:boundary");
        group.put("min", min);
        if (max != null) {
            group.put("max", max);
        }
        return group;
    }


    public static LinkedHashMap<String, Object> makeDistance (String key,
            Integer min, Integer max) {
        LinkedHashMap<String, Object> group = new LinkedHashMap<String, Object>();
        if (key.equals("w")) {
            group.put("@type", "koral:distance");
        }
        else {
            group.put("@type", "cosmas:distance");
        }
        group.put("key", key);
        group.put("boundary", makeBoundary(min, max));
        group.put("min", min);
        if (max != null) {
            group.put("max", max);
        }
        qp.addMessage(
                StatusCodes.DEPRECATED_QUERY_ELEMENT,
                "Deprecated 2014-07-24: 'min' and 'max' to be supported until 3 months from deprecation date.");
        return group;
    }


    public static LinkedHashMap<String, Object> makeReference (
            ArrayList<Integer> classRefs, String operation) {
        LinkedHashMap<String, Object> group = new LinkedHashMap<String, Object>();
        group.put("@type", "koral:reference");
        group.put("operation", "operation:" + operation);
        if (classRefs != null && !classRefs.isEmpty()) {
            group.put("classRef", classRefs);
        }
        return group;
    }


    public static LinkedHashMap<String, Object> makeReference (
            ArrayList<Integer> classRefs) {
        return makeReference(classRefs, "focus");
    }


    @Deprecated
    public static LinkedHashMap<String, Object> makeReference (int classRef,
            String operation, boolean setBySystem) {
        ArrayList<Integer> classRefs = new ArrayList<Integer>();
        if (setBySystem)
            classRef = classRef + 128;
        classRefs.add(classRef);
        return makeReference(classRefs, operation);
    }


    @Deprecated
    public static LinkedHashMap<String, Object> makeReference (int classRef,
            boolean setBySystem) {
        ArrayList<Integer> classRefs = new ArrayList<Integer>();
        if (setBySystem)
            classRef = classRef + 128;
        classRefs.add(classRef);
        return makeReference(classRefs, "focus");
    }


    public static LinkedHashMap<String, Object> makeReference (int classRef) {
        return makeReference(classRef, false);
    }


    @Deprecated
    public static LinkedHashMap<String, Object> makeResetReference () {
        LinkedHashMap<String, Object> group = new LinkedHashMap<String, Object>();
        group.put("@type", "koral:reference");
        group.put("operation", "operation:focus");
        group.put("reset", true);
        group.put("operands", new ArrayList<Object>());
        return group;
    }


    public static LinkedHashMap<String, Object> makeSpanReference (
            Integer[] spanRef, String operation) {
        LinkedHashMap<String, Object> group = new LinkedHashMap<String, Object>();
        group.put("@type", "koral:reference");
        group.put("operation", "operation:" + operation);
        group.put("spanRef", Arrays.asList(spanRef));
        group.put("operands", new ArrayList<Object>());
        return group;
    }


    public static void addOperandsToGroup (LinkedHashMap<String, Object> group) {
        ArrayList<Object> operands = new ArrayList<Object>();
        group.put("operands", operands);
    }


    public static LinkedHashMap<String, Object> wrapInReference (
            LinkedHashMap<String, Object> group, Integer classId) {
        LinkedHashMap<String, Object> refGroup = makeReference(classId);
        ArrayList<Object> operands = new ArrayList<Object>();
        operands.add(group);
        refGroup.put("operands", operands);
        return refGroup;
    }


    @Deprecated
    public static LinkedHashMap<String, Object> wrapInReference (
            LinkedHashMap<String, Object> group, Integer classId,
            boolean setBySystem) {
        LinkedHashMap<String, Object> refGroup = makeReference(classId);
        ArrayList<Object> operands = new ArrayList<Object>();
        operands.add(group);
        refGroup.put("operands", operands);
        return refGroup;
    }


    @SuppressWarnings("unchecked")
    public static LinkedHashMap<String, Object> wrapInClass (
            LinkedHashMap<String, Object> group, Integer classId) {
        LinkedHashMap<String, Object> classGroup = makeSpanClass(classId);
        ((ArrayList<Object>) classGroup.get("operands")).add(group);
        return classGroup;
    }


    /**
     * Ensures that a distance or quantification value does not exceed
     * the allowed maximum value.
     * 
     * @param number
     * @return The input number if it is below the allowed maximum
     *         value, else the maximum value.
     */
    public static int cropToMaxValue (int number) {
        if (number > MAXIMUM_DISTANCE) {
            number = MAXIMUM_DISTANCE;
            String warning = String
                    .format("You specified a distance between two segments that is greater than "
                            + "the allowed max value of %d. Your query will be re-interpreted using a distance of %d.",
                            MAXIMUM_DISTANCE, MAXIMUM_DISTANCE);
            qp.addWarning(warning);
        }
        return number;
    }
}
