package de.ids_mannheim.korap.query.serialize.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import de.ids_mannheim.korap.query.object.ClassRefCheck;
import de.ids_mannheim.korap.query.object.ClassRefOp;
import de.ids_mannheim.korap.query.object.KoralFrame;
import de.ids_mannheim.korap.query.object.KoralOperation;
import de.ids_mannheim.korap.query.object.KoralTermGroupRelation;
import de.ids_mannheim.korap.query.object.KoralType;
import de.ids_mannheim.korap.query.serialize.AbstractQueryProcessor;

public class KoralObjectGenerator {

    private static final Integer MAXIMUM_DISTANCE = 100;
    private static AbstractQueryProcessor qp;

    public static void setQueryProcessor (AbstractQueryProcessor qp) {
        KoralObjectGenerator.qp = qp;
    }


    public static Map<String, Object> makeSpan () {
        Map<String, Object> span = new HashMap<String, Object>();
        span.put("@type", KoralType.SPAN.toString());
        return span;
    }


    public static Map<String, Object> makeSpan (String key) {
        Map<String, Object> span = new HashMap<String, Object>();
        span.put("@type", KoralType.SPAN.toString());
        Map<String, Object> term = makeTerm();
        if (key.equals("s")){
            term.put("layer", "s");
        }
        term.put("key", key);
        span.put("wrap", term);
        return span;
    }


    public static Map<String, Object> makeTerm () {
        Map<String, Object> term = new HashMap<String, Object>();
        term.put("@type", KoralType.TERM.toString());
        return term;
    }


    public static Map<String, Object> makeTermGroup (KoralTermGroupRelation relation) {
        Map<String, Object> term = new HashMap<String, Object>();
        term.put("@type", KoralType.TERMGROUP.toString());
        term.put("relation", relation.toString());
        term.put("operands", new ArrayList<Object>());
        return term;
    }


    public static Map<String, Object> makeDoc () {
        Map<String, Object> term = new HashMap<String, Object>();
        term.put("@type", KoralType.DOCUMENT.toString());
        return term;
    }
    
    public static Map<String, Object> makeDocGroupRef (String ref) {
        Map<String, Object> term = new HashMap<String, Object>();
        term.put("@type", KoralType.DOCUMENT_GROUP_REF.toString());
        term.put("ref", ref);
        return term;
    }

    public static Map<String, Object> makeDocGroup (String relation) {
        Map<String, Object> term = new HashMap<String, Object>();
        term.put("@type", KoralType.DOCUMENT_GROUP.toString());
        term.put("operation", "operation:" + relation);
        term.put("operands", new ArrayList<Object>());
        return term;
    }


    public static Map<String, Object> makeToken () {
        Map<String, Object> token = new HashMap<String, Object>();
        token.put("@type", KoralType.TOKEN.toString());
        return token;
    }


    public static Map<String, Object> makeGroup (KoralOperation operation) {
        Map<String, Object> group = new HashMap<String, Object>();
        group.put("@type", KoralType.GROUP.toString());
        group.put("operation", operation.toString());
        group.put("operands", new ArrayList<Object>());
        return group;
    }


    public static Map<String, Object> makeRepetition (Integer min,
            Integer max) {
        Map<String, Object> group = makeGroup(KoralOperation.REPETITION);
        group.put("boundary", makeBoundary(min, max));
        return group;
    }


    @Deprecated
    public static Map<String, Object> makePosition (KoralFrame frame) {
        Map<String, Object> group = new HashMap<String, Object>();
        group.put("@type", KoralType.GROUP.toString());
        group.put("operation", KoralOperation.POSITION.toString());
        group.put("frame", frame.toString());
        group.put("operands", new ArrayList<Object>());
        return group;
    }


    public static Map<String, Object> makePosition (
            List<KoralFrame> allowedFrames) {
        Map<String, Object> group = new HashMap<String, Object>();
        group.put("@type", KoralType.GROUP.toString());
        group.put("operation", KoralOperation.POSITION.toString());
        group.put("frames", Converter.enumListToStringList(allowedFrames));
        group.put("operands", new ArrayList<Object>());
        return group;
    }

    public static Map<String, Object> makeSpanClass (int classId) {
        return makeSpanClass(classId, false);
    }


    @Deprecated
    public static Map<String, Object> makeSpanClass (int classId,
            boolean setBySystem) {
        Map<String, Object> group = new HashMap<String, Object>();
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

    public static Map<String, Object> makeQueryRef (String ref) {
        Map<String, Object> term = new HashMap<String, Object>();
        term.put("@type", KoralType.QUERY_REF.toString());
        term.put("ref", ref);
        return term;
    }

    public static Map<String, Object> makeClassRefCheck (
            ArrayList<ClassRefCheck> checks, ArrayList<Integer> classIn) {
        Map<String, Object> group = new HashMap<String, Object>();
        group.put("@type", KoralType.GROUP.toString());
        group.put("operation", KoralOperation.CLASS.toString());
        group.put("classRefCheck", Converter.enumListToStringList(checks));
        group.put("classIn", classIn);
        group.put("operands", new ArrayList<Object>());
        return group;
    }


    public static Map<String, Object> makeClassRefOp (
            ClassRefOp operation, Integer[] classIn, int classOut) {
        Map<String, Object> group = new HashMap<String, Object>();
        group.put("@type", KoralType.GROUP.toString());
        group.put("operation", KoralOperation.CLASS.toString());
        group.put("classRefOp", operation.toString());
        group.put("classIn", Arrays.asList(classIn));
        group.put("classOut", classOut);
        group.put("operands", new ArrayList<Object>());
        return group;
    }


//    @Deprecated
//    public static Map<String, Object> makeTreeRelation (String reltype) {
//        Map<String, Object> group = new HashMap<String, Object>();
//        group.put("@type", "koral:treeRelation");
//        if (reltype != null)
//            group.put("reltype", reltype);
//        return group;
//    }


    public static Map<String, Object> makeRelation () {
        Map<String, Object> group = new HashMap<String, Object>();
        group.put("@type", KoralType.RELATION.toString());
        return group;
    }
    
    public static Map<String, Object> makeBoundary (Integer min,
            Integer max) {
        Map<String, Object> group = new HashMap<String, Object>();
        group.put("@type", KoralType.BOUNDARY.toString());
        group.put("min", min);
        if (max != null) {
            group.put("max", max);
        }
        return group;
    }


    public static Map<String, Object> makeDistance (String key,
            Integer min, Integer max) {
        Map<String, Object> group = new HashMap<String, Object>();
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


    public static Map<String, Object> makeReference (
            ArrayList<Integer> classRefs, KoralOperation operation) {
        Map<String, Object> group = new HashMap<String, Object>();
        group.put("@type", KoralType.REFERENCE.toString());
        group.put("operation", operation.toString());
        if (classRefs != null && !classRefs.isEmpty()) {
            group.put("classRef", classRefs);
        }
        return group;
    }


    public static Map<String, Object> makeReference (
            ArrayList<Integer> classRefs) {
        return makeReference(classRefs, KoralOperation.FOCUS);
    }


    @Deprecated
    public static Map<String, Object> makeReference (int classRef,
            KoralOperation operation, boolean setBySystem) {
        ArrayList<Integer> classRefs = new ArrayList<Integer>();
        if (setBySystem)
            classRef = classRef + 128;
        classRefs.add(classRef);
        return makeReference(classRefs, operation);
    }


    @Deprecated
    public static Map<String, Object> makeReference (int classRef,
            boolean setBySystem) {
        ArrayList<Integer> classRefs = new ArrayList<Integer>();
        if (setBySystem)
            classRef = classRef + 128;
        classRefs.add(classRef);
        return makeReference(classRefs, KoralOperation.FOCUS);
    }


    public static Map<String, Object> makeReference (int classRef) {
        return makeReference(classRef, false);
    }


    @Deprecated
    public static Map<String, Object> makeResetReference () {
        Map<String, Object> group = new HashMap<String, Object>();
        group.put("@type", KoralType.REFERENCE.toString());
        group.put("operation", KoralOperation.FOCUS.toString());
        group.put("reset", true);
        group.put("operands", new ArrayList<Object>());
        return group;
    }


    public static Map<String, Object> makeSpanReference (
            Integer[] spanRef, KoralOperation operation) {
        Map<String, Object> group = new HashMap<String, Object>();
        group.put("@type", KoralType.REFERENCE.toString());
        group.put("operation", operation.toString());
        group.put("spanRef", Arrays.asList(spanRef));
        group.put("operands", new ArrayList<Object>());
        return group;
    }


    public static void addOperandsToGroup (Map<String, Object> group) {
        ArrayList<Object> operands = new ArrayList<Object>();
        group.put("operands", operands);
    }


    public static Map<String, Object> wrapInReference (
            Map<String, Object> group, Integer classId) {
        Map<String, Object> refGroup = makeReference(classId);
        ArrayList<Object> operands = new ArrayList<Object>();
        operands.add(group);
        refGroup.put("operands", operands);
        return refGroup;
    }


    @Deprecated
    public static Map<String, Object> wrapInReference (
            Map<String, Object> group, Integer classId,
            boolean setBySystem) {
        Map<String, Object> refGroup = makeReference(classId);
        ArrayList<Object> operands = new ArrayList<Object>();
        operands.add(group);
        refGroup.put("operands", operands);
        return refGroup;
    }


    @SuppressWarnings("unchecked")
    public static Map<String, Object> wrapInClass (
            Map<String, Object> group, Integer classId) {
        Map<String, Object> classGroup = makeSpanClass(classId);
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
