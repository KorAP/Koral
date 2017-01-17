package de.ids_mannheim.korap.query.serialize.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import de.ids_mannheim.korap.query.object.ClassRefCheck;
import de.ids_mannheim.korap.query.object.ClassRefOp;
import de.ids_mannheim.korap.query.object.KoralFrame;
import de.ids_mannheim.korap.query.object.KoralOperation;
import de.ids_mannheim.korap.query.object.KoralTermGroupRelation;
import de.ids_mannheim.korap.query.object.KoralType;
import de.ids_mannheim.korap.query.serialize.AbstractQueryProcessor;

public class KoralObjectGenerator {

    protected static final Integer MAXIMUM_DISTANCE = 100;
    private static AbstractQueryProcessor qp;


    public static void setQueryProcessor (AbstractQueryProcessor qp) {
        KoralObjectGenerator.qp = qp;
    }


    public static LinkedHashMap<String, Object> makeSpan () {
        LinkedHashMap<String, Object> span = new LinkedHashMap<String, Object>();
        span.put("@type", KoralType.SPAN.toString());
        return span;
    }


    public static LinkedHashMap<String, Object> makeSpan (String key) {
        LinkedHashMap<String, Object> span = new LinkedHashMap<String, Object>();
        span.put("@type", KoralType.SPAN.toString());
        LinkedHashMap<String, Object> term = makeTerm();
        term.put("key", key);
        span.put("wrap", term);
        return span;
    }


    public static LinkedHashMap<String, Object> makeTerm () {
        LinkedHashMap<String, Object> term = new LinkedHashMap<String, Object>();
        term.put("@type", KoralType.TERM.toString());
        return term;
    }


    public static LinkedHashMap<String, Object> makeTermGroup (KoralTermGroupRelation relation) {
        LinkedHashMap<String, Object> term = new LinkedHashMap<String, Object>();
        term.put("@type", KoralType.TERMGROUP.toString());
        term.put("relation", relation.toString());
        term.put("operands", new ArrayList<Object>());
        return term;
    }


    public static LinkedHashMap<String, Object> makeDoc () {
        LinkedHashMap<String, Object> term = new LinkedHashMap<String, Object>();
        term.put("@type", KoralType.DOCUMENT.toString());
        return term;
    }


    public static LinkedHashMap<String, Object> makeDocGroup (String relation) {
        LinkedHashMap<String, Object> term = new LinkedHashMap<String, Object>();
        term.put("@type", KoralType.DOCUMENTGROUP.toString());
        term.put("operation", "operation:" + relation);
        term.put("operands", new ArrayList<Object>());
        return term;
    }


    public static LinkedHashMap<String, Object> makeToken () {
        LinkedHashMap<String, Object> token = new LinkedHashMap<String, Object>();
        token.put("@type", KoralType.TOKEN.toString());
        return token;
    }


    public static LinkedHashMap<String, Object> makeGroup (KoralOperation operation) {
        LinkedHashMap<String, Object> group = new LinkedHashMap<String, Object>();
        group.put("@type", KoralType.GROUP.toString());
        group.put("operation", operation.toString());
        group.put("operands", new ArrayList<Object>());
        return group;
    }


    public static LinkedHashMap<String, Object> makeRepetition (Integer min,
            Integer max) {
        LinkedHashMap<String, Object> group = makeGroup(KoralOperation.REPETITION);
        group.put("boundary", makeBoundary(min, max));
        return group;
    }


    @Deprecated
    public static LinkedHashMap<String, Object> makePosition (KoralFrame frame) {
        LinkedHashMap<String, Object> group = new LinkedHashMap<String, Object>();
        group.put("@type", KoralType.GROUP.toString());
        group.put("operation", KoralOperation.POSITION.toString());
        group.put("frame", frame.toString());
        group.put("operands", new ArrayList<Object>());
        return group;
    }


    public static LinkedHashMap<String, Object> makePosition (
            List<KoralFrame> allowedFrames) {
        LinkedHashMap<String, Object> group = new LinkedHashMap<String, Object>();
        group.put("@type", KoralType.GROUP.toString());
        group.put("operation", KoralOperation.POSITION.toString());
        group.put("frames", Converter.enumListToStringList(allowedFrames));
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
        group.put("@type", KoralType.GROUP.toString());
        group.put("operation", KoralOperation.CLASS.toString());
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
            ArrayList<ClassRefCheck> checks, Integer[] classIn, int classOut) {
        LinkedHashMap<String, Object> group = new LinkedHashMap<String, Object>();
        group.put("@type", KoralType.GROUP.toString());
        group.put("operation", KoralOperation.CLASS.toString());
        group.put("classRefCheck", Converter.enumListToStringList(checks));
        group.put("classIn", Arrays.asList(classIn));
        group.put("classOut", classOut);
        group.put("operands", new ArrayList<Object>());
        return group;
    }


    public static LinkedHashMap<String, Object> makeClassRefOp (
            ClassRefOp operation, Integer[] classIn, int classOut) {
        LinkedHashMap<String, Object> group = new LinkedHashMap<String, Object>();
        group.put("@type", KoralType.GROUP.toString());
        group.put("operation", KoralOperation.CLASS.toString());
        group.put("classRefOp", operation.toString());
        group.put("classIn", Arrays.asList(classIn));
        group.put("classOut", classOut);
        group.put("operands", new ArrayList<Object>());
        return group;
    }


//    @Deprecated
//    public static LinkedHashMap<String, Object> makeTreeRelation (String reltype) {
//        LinkedHashMap<String, Object> group = new LinkedHashMap<String, Object>();
//        group.put("@type", "koral:treeRelation");
//        if (reltype != null)
//            group.put("reltype", reltype);
//        return group;
//    }


    public static LinkedHashMap<String, Object> makeRelation () {
        LinkedHashMap<String, Object> group = new LinkedHashMap<String, Object>();
        group.put("@type", KoralType.RELATION.toString());
        return group;
    }


    public static LinkedHashMap<String, Object> makeBoundary (Integer min,
            Integer max) {
        LinkedHashMap<String, Object> group = new LinkedHashMap<String, Object>();
        group.put("@type", KoralType.BOUNDARY.toString());
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
            group.put("@type", KoralType.DISTANCE.toString());
        }
        else {
            group.put("@type", KoralType.COSMAS_DISTANCE.toString());
        }
        group.put("key", key);
        group.put("boundary", makeBoundary(min, max));
//        group.put("min", min);
//        if (max != null) {
//            group.put("max", max);
//        }
//        qp.addMessage(
//                StatusCodes.DEPRECATED_QUERY_ELEMENT,
//                "Deprecated 2014-07-24: 'min' and 'max' to be supported until 3 months from deprecation date.");
        return group;
    }


    public static LinkedHashMap<String, Object> makeReference (
            ArrayList<Integer> classRefs, KoralOperation operation) {
        LinkedHashMap<String, Object> group = new LinkedHashMap<String, Object>();
        group.put("@type", KoralType.REFERENCE.toString());
        group.put("operation", operation.toString());
        if (classRefs != null && !classRefs.isEmpty()) {
            group.put("classRef", classRefs);
        }
        return group;
    }


    public static LinkedHashMap<String, Object> makeReference (
            ArrayList<Integer> classRefs) {
        return makeReference(classRefs, KoralOperation.FOCUS);
    }


    @Deprecated
    public static LinkedHashMap<String, Object> makeReference (int classRef,
            KoralOperation operation, boolean setBySystem) {
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
        return makeReference(classRefs, KoralOperation.FOCUS);
    }


    public static LinkedHashMap<String, Object> makeReference (int classRef) {
        return makeReference(classRef, false);
    }


    @Deprecated
    public static LinkedHashMap<String, Object> makeResetReference () {
        LinkedHashMap<String, Object> group = new LinkedHashMap<String, Object>();
        group.put("@type", KoralType.REFERENCE.toString());
        group.put("operation", KoralOperation.FOCUS.toString());
        group.put("reset", true);
        group.put("operands", new ArrayList<Object>());
        return group;
    }


    public static LinkedHashMap<String, Object> makeSpanReference (
            Integer[] spanRef, KoralOperation operation) {
        LinkedHashMap<String, Object> group = new LinkedHashMap<String, Object>();
        group.put("@type", KoralType.REFERENCE.toString());
        group.put("operation", operation.toString());
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
