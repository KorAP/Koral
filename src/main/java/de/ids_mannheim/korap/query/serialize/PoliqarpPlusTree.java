package de.ids_mannheim.korap.query.serialize;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

//import de.ids_mannheim.korap.query.poliqarpplus.PoliqarpPlusLexer;
//import de.ids_mannheim.korap.query.poliqarpplus.PoliqarpPlusParser;
import de.ids_mannheim.korap.query.PoliqarpPlusLexer;
import de.ids_mannheim.korap.query.PoliqarpPlusParser;
import de.ids_mannheim.korap.query.serialize.AbstractSyntaxTree;

/**
 * Map representation of Poliqarp syntax tree as returned by ANTLR
 * @author joachim
 *
 */
public class PoliqarpPlusTree extends AbstractSyntaxTree {
	
	/**
	 * Top-level map representing the whole request.
	 */
	LinkedHashMap<String,Object> requestMap = new LinkedHashMap<String,Object>();
	/**
	 * Keeps track of open node categories
	 */
	LinkedList<String> openNodeCats = new LinkedList<String>();
	/**
	 * Flag that indicates whether token fields or meta fields are currently being processed
	 */
	boolean inMeta = false;
	/**
	 * Flag that indicates whether a cq_segment is to be ignored (e.g. when it is empty, is followed directly by only a spanclass and has no other children etc...). 
	 */
	boolean ignoreCq_segment = false;
	/**
	 * Parser object deriving the ANTLR parse tree.
	 */
	static Parser poliqarpParser;
	/**
	 * Keeps track of all visited nodes in a tree
	 */
	List<ParseTree> visited = new ArrayList<ParseTree>();

	/**
	 * Keeps track of active fields (like 'base=foo').
	 */
	LinkedList<ArrayList<Object>> fieldStack = new LinkedList<ArrayList<Object>>();
	/**
	 * Keeps track of active sequences.
	 */
	LinkedList<LinkedHashMap<String,Object>> sequenceStack = new LinkedList<LinkedHashMap<String,Object>>();
	/**
	 * Keeps track of active tokens.
	 */
	LinkedList<LinkedHashMap<String,Object>> tokenStack = new LinkedList<LinkedHashMap<String,Object>>();
	/**
	 * Keeps track of sequence/token/field groups.
	 */
	LinkedList<ArrayList<Object>> groupStack = new LinkedList<ArrayList<Object>>();
	/**
	 * Marks the currently active object (sequence/token/group...) in order to know where to add stuff like occurrence info etc.
	 */
//	LinkedHashMap<String,Object> curObject = new LinkedHashMap<String,Object>();
	/**
	 * Marks the currently active token in order to know where to add flags (might already have been taken away from token stack).
	 */
	LinkedHashMap<String,Object> curToken = new LinkedHashMap<String,Object>();
	/**
	 * Keeps track of active object.
	 */
	LinkedList<Object> objectStack = new LinkedList<Object>();
	
	/**
	 * 
	 * @param tree The syntax tree as returned by ANTLR
	 * @param parser The ANTLR parser instance that generated the parse tree
	 */
	public PoliqarpPlusTree(String query) {
		prepareContext();
		process(query);
		System.out.println(">>> "+requestMap.get("query")+" <<<");
	}

	private void prepareContext() {
		LinkedHashMap<String,Object> context = new LinkedHashMap<String,Object>();
		LinkedHashMap<String,Object> operands = new LinkedHashMap<String,Object>();
		LinkedHashMap<String,Object> relation = new LinkedHashMap<String,Object>();
		LinkedHashMap<String,Object> classMap = new LinkedHashMap<String,Object>();
		
		operands.put("@id", "korap:operands");
		operands.put("@container", "@list");
		
		relation.put("@id", "korap:relation");
		relation.put("@type", "korap:relation#types");
		
		classMap.put("@id", "korap:class");
		classMap.put("@type", "xsd:integer");
		
		context.put("korap", "http://korap.ids-mannheim.de/ns/query");
		context.put("@language", "de");
		context.put("operands", operands);
		context.put("relation", relation);
		context.put("class", classMap);
		context.put("query", "korap:query");
		context.put("filter", "korap:filter");
		context.put("meta", "korap:meta");
		
		requestMap.put("@context", context);		
	}

	@Override
	public Map<String, Object> getRequestMap() {
		return this.requestMap;
	}
	
	@Override
	public void process(String query) {
		ParseTree tree = parsePoliqarpQuery(query);
		System.out.println("Processing Poliqarp");
		processNode(tree);
	}
	
	@SuppressWarnings("unchecked")
	private void processNode(ParseTree node) {
		// Top-down processing
		if (visited.contains(node)) return;
		else visited.add(node);
		
		String nodeCat = getNodeCat(node);
		openNodeCats.push(nodeCat);
		
//		System.out.println(openNodeCats);
		

		/*
		 ****************************************************************
		 **************************************************************** 
		 * 			Processing individual node categories  				*
		 ****************************************************************
		 ****************************************************************
		 */
		if (nodeCat.equals("query")) {
		}

		// cq_segments/sq_segments: token group
		if (nodeCat.equals("cq_segments") || nodeCat.equals("sq_segments")) {
			// disregard empty segments in simple queries (parsed by ANTLR as empty cq_segments) 
			ignoreCq_segment = (node.getChildCount() == 1 && (node.getChild(0).toStringTree(poliqarpParser).equals(" ") || getNodeCat(node.getChild(0)).equals("spanclass") || getNodeCat(node.getChild(0)).equals("position")));
//			ignoreCq_segment = (node.getChildCount() == 1 && node.getChild(0).toStringTree(poliqarpParser).equals(" ") && getNodeCat(node.getChild(0)).equals("spanclass") );
			if (!ignoreCq_segment) {
				LinkedHashMap<String,Object> sequence = new LinkedHashMap<String,Object>();
//				curObject = sequence;
				objectStack.push(sequence);
				// Step I: decide type of element (one or more elements? -> token or sequence)
				if (node.getChildCount()>1) {
					sequence.put("@type", "korap:sequence");
					ArrayList<Object> sequenceOperands = new ArrayList<Object>();
					sequence.put("operands", sequenceOperands);
				} else {
					if (openNodeCats.get(1).equals("position") || openNodeCats.get(1).equals("spanclass")) {
						sequence.put("@type", "korap:group");
					} else {
						// if only child, make the sequence a mere korap:token
						sequence.put("@type", "korap:token");
					}
					tokenStack.push(sequence);
				}
				// Step II: decide where to put this element (top query node or embedded in super sequence?)
//				System.out.println(openNodeCats);
				if (openNodeCats.get(1).equals("query")) {
					// top query node
					requestMap.put("query", sequence);
				} else if (!groupStack.isEmpty()) {
					// embed in super group
					ArrayList<Object> topGroupOperands = (ArrayList<Object>) groupStack.getFirst();
					topGroupOperands.add(sequence);
				} else if (!sequenceStack.isEmpty()){
					// embed in super sequence
					ArrayList<Object> topSequenceOperands = (ArrayList<Object>) sequenceStack.getFirst().get("operands");
					topSequenceOperands.add(sequence);
				}
				sequenceStack.push(sequence);
			}
		}
		
		// cq_segment
		if (nodeCat.equals("cq_segment")) {
			// Step I: determine whether to create new token or get token from the stack (if added by cq_segments)
			LinkedHashMap<String, Object> token;
			if (tokenStack.isEmpty()) {
				token = new LinkedHashMap<String, Object>();
				tokenStack.push(token);
			} else {
				// in case cq_segments has already added the token
				token = tokenStack.getFirst();
			}
//			curObject = token;
			curToken = token;
			objectStack.push(token);
			
			// Step II: start filling object and add to containing sequence
			token.put("@type", "korap:token");
			// add token to sequence only if it is not an only child (in that case, cq_segments has already added the info and is just waiting for the values from "field")
			if (node.getParent().getChildCount()>1) {
				ArrayList<Object> topSequenceOperands = (ArrayList<Object>) sequenceStack.getFirst().get("operands");
				topSequenceOperands.add(token);
			}
		}

		// disjoint cq_segments, like ([base=foo][base=bar])|[base=foobar]
		if (nodeCat.equals("cq_disj_segments")) {
			LinkedHashMap<String,Object> disjunction = new LinkedHashMap<String,Object>();
//			curObject = disjunction;
			objectStack.push(disjunction);
			ArrayList<Object> disjOperands = new ArrayList<Object>();
			disjunction.put("@type", "korap:group");
			disjunction.put("relation", "or");
			disjunction.put("operands", disjOperands);
			groupStack.push(disjOperands);
			
			// decide where to put the disjunction
			if (openNodeCats.get(1).equals("query")) {
				requestMap.put("query", disjunction);	
			} else if (openNodeCats.get(1).equals("cq_segments")) {
				ArrayList<Object> topSequenceOperands = (ArrayList<Object>) sequenceStack.getFirst().get("operands");
				topSequenceOperands.add(disjunction);
			}
		}
		
		// field element (outside meta)
		if (nodeCat.equals("field")) {
			LinkedHashMap<String,Object> fieldMap = new LinkedHashMap<String,Object>();

			// Step I: extract info
			String featureName = node.getChild(0).getChild(0).toStringTree(poliqarpParser);   //e.g. (field_name base) (field_op !=) (re_query "bar*")
			String relation = node.getChild(1).getChild(0).toStringTree(poliqarpParser);
			String value = "";
			ParseTree valNode = node.getChild(2);
			String valType = getNodeCat(valNode);
			fieldMap.put("@type", "korap:term");
			if (valType.equals("simple_query")) {
				value = valNode.getChild(0).getChild(0).toStringTree(poliqarpParser);   //e.g. (simple_query (sq_segment foo))
			} else if (valType.equals("re_query")) {
				value = valNode.getChild(0).toStringTree(poliqarpParser); 				//e.g. (re_query "bar*")
				fieldMap.put("@subtype", "korap:value#regex");
			}
			fieldMap.put("@value", featureName+":"+value);
			fieldMap.put("relation", relation);

			// Step II: decide where to put the field map (as the only value of a token or the meta filter or as a part of a group in case of coordinated fields)
			if (fieldStack.isEmpty()) {
				if (!inMeta) {
					tokenStack.getFirst().put("@value", fieldMap);
				} else {
					((HashMap<String, Object>) requestMap.get("meta")).put("@value", fieldMap);
				}
			} else {
				fieldStack.getFirst().add(fieldMap);
			}
			visited.add(node.getChild(0));
			visited.add(node.getChild(1));
			visited.add(node.getChild(2));
		}
		
		// conj_field serves for both conjunctions and disjunctions
		if (nodeCat.equals("conj_field")) {
			LinkedHashMap<String,Object> group = new LinkedHashMap<String,Object>(); 
			ArrayList<Object> groupOperands = new ArrayList<Object>();
			
			group.put("@type", "korap:group");
			group.put("operands", groupOperands);
			fieldStack.push(groupOperands);
			
			// Step I: get operator (& or |)
			ParseTree operatorNode = node.getChild(1).getChild(0);
			String operator = getNodeCat(operatorNode);
			if (operator.equals("|")) {
				group.put("relation", "or");
			} else if (operator.equals("&")) {
				group.put("relation", "and");
			}
			
			// Step II: decide where to put the group (directly under token or in top meta filter section or embed in super group)
			if (openNodeCats.get(1).equals("cq_segment")) {
				tokenStack.getFirst().put("@value", group);
			} else if (openNodeCats.get(1).equals("meta_field_group")) {
				((HashMap<String, Object>) requestMap.get("meta")).put("@value", group);
			} else {
				fieldStack.get(1).add(group);
			}
			// skip the operator
			visited.add(node.getChild(1));
		}
		
		
		if (nodeCat.equals("sq_segment")) {
			// Step I: determine whether to create new token or get token from the stack (if added by cq_segments)
			LinkedHashMap<String, Object> token;
			if (tokenStack.isEmpty()) {
				token = new LinkedHashMap<String, Object>();
				tokenStack.push(token);
			} else {
				// in case sq_segments has already added the token
				token = tokenStack.getFirst();
			}
//			curObject = token;
			curToken = token;
			objectStack.push(token);
			// Step II: fill object (token values) and put into containing sequence
			token.put("@type", "korap:token");
			String word = node.getChild(0).toStringTree(poliqarpParser);
			LinkedHashMap<String,Object> tokenValues = new LinkedHashMap<String,Object>();
			token.put("@value", tokenValues);
			tokenValues.put("orth", word);
			tokenValues.put("relation", "=");
			
			// add token to sequence only if it is not an only child (in that case, sq_segments has already added the info and is just waiting for the values from "field")
			if (node.getParent().getChildCount()>1) {
				ArrayList<Object> topSequenceOperands = (ArrayList<Object>) sequenceStack.getFirst().get("operands");
				topSequenceOperands.add(token);
			}
		}
		
		if (nodeCat.equals("element")) {
			// Step I: determine whether to create new token or get token from the stack (if added by cq_segments)
			LinkedHashMap<String, Object> elem;
			if (tokenStack.isEmpty()) {
				elem = new LinkedHashMap<String, Object>();
			} else {
				// in case sq_segments has already added the token
				elem = tokenStack.getFirst();
			}
//			curObject = elem;
			curToken = elem;
			objectStack.push(elem);
			// Step II: fill object (token values) and put into containing sequence
			elem.put("@type", "korap:element");
			String value = node.getChild(1).toStringTree(poliqarpParser);
			elem.put("@value", value);
			// add token to sequence only if it is not an only child (in that case, cq_segments has already added the info and is just waiting for the values from "field")
			if (node.getParent().getChildCount()>1) {
				ArrayList<Object> topSequenceOperands = (ArrayList<Object>) sequenceStack.getFirst().get("operands");
				topSequenceOperands.add(elem);
			}
		}
		
		if (nodeCat.equals("spanclass")) {
			LinkedHashMap<String,Object> span = new LinkedHashMap<String,Object>();
//			curObject = span;
			objectStack.push(span);
			ArrayList<Object> spanOperands = new ArrayList<Object>();
			String id = "0";
			// Step I: get info
			if (getNodeCat(node.getChild(1)).equals("spanclass_id")) {
				id = node.getChild(1).getChild(0).toStringTree(poliqarpParser);
				id = id.substring(0, id.length()-1); // remove trailing colon ':'
				// only allow class ids up to 255
				if (Integer.parseInt(id)>255) {
					id = "0";
				}
			}
			span.put("@type", "korap:group");
			span.put("class", id);
			span.put("operands", spanOperands);
			groupStack.push(spanOperands);
			
			// Step II: decide where to put the span
			// add span to sequence only if it is not an only child (in that case, sq_segments has already added the info and is just waiting for the relevant info)
			if (node.getParent().getChildCount()>1) {
				ArrayList<Object> topSequenceOperands = (ArrayList<Object>) sequenceStack.getFirst().get("operands");
				topSequenceOperands.add(span);
			} else if (openNodeCats.get(2).equals("query")) {
				requestMap.put("query", span);	
			} else if (groupStack.size()>1) {
				groupStack.get(1).add(span);
			} 
			
		}
		
		if (nodeCat.equals("position")) {
			LinkedHashMap<String,Object> positionGroup = new LinkedHashMap<String,Object>();
//			curObject = positionGroup;
			objectStack.push(positionGroup);
			ArrayList<Object> posOperands = new ArrayList<Object>();
			// Step I: get info
			String relation = getNodeCat(node.getChild(0));
			positionGroup.put("@type", "korap:group");
			positionGroup.put("relation", relation.toLowerCase());
			positionGroup.put("operands", posOperands);
			groupStack.push(posOperands);
			
			// Step II: decide where to put the group
			// add group to sequence only if it is not an only child (in that case, sq_segments has already added the info and is just waiting for the relevant info)
			if (node.getParent().getChildCount()>1) {
				ArrayList<Object> topSequenceOperands = (ArrayList<Object>) sequenceStack.getFirst().get("operands");
				topSequenceOperands.add(positionGroup);
			} else if (openNodeCats.get(2).equals("query")) {
				requestMap.put("query", positionGroup);	
			} else if (groupStack.size()>1) {
				groupStack.get(1).add(positionGroup);
			} 
		}
		
		if (nodeCat.equals("shrink") || nodeCat.equals("split")) {
			//TODO
		}
		
		// repetition of token group
		if (nodeCat.equals("occ")) {
			ParseTree occChild = node.getChild(0);
			String repetition = occChild.toStringTree(poliqarpParser);
//			curObject.put("repetition", repetition);
			LinkedHashMap<String,Object> curObject = (LinkedHashMap<String, Object>) objectStack.getFirst();
			curObject.put("repetition", repetition);
			visited.add(occChild);
		}
				
		// flags for case sensitivity and whole-word-matching
		if (nodeCat.equals("flag")) {
			String flag = getNodeCat(node.getChild(0)).substring(1); //substring removes leading slash '/'
			// add to current token's value
			((HashMap<String, Object>) curToken.get("@value")).put("flag", flag);
		}
		
		if (nodeCat.equals("meta")) {
			inMeta=true;
			LinkedHashMap<String,Object> metaFilter = new LinkedHashMap<String,Object>();
			requestMap.put("meta", metaFilter);
			metaFilter.put("@type", "korap:meta");
		}
		
		
		
		if (nodeCat.equals("within")) {
			ParseTree domainNode = node.getChild(2);
			String domain = getNodeCat(domainNode);
//			queryOperands.add("within:"+domain);
			LinkedHashMap<String,Object> curObject = (LinkedHashMap<String, Object>) objectStack.getFirst();
			curObject.put("within", domain);
			visited.add(node.getChild(0));
			visited.add(node.getChild(1));
			visited.add(domainNode);
		}
		
//		System.out.println(">>> "+requestMap.get("query")+" <<<");
		/*
		 ****************************************************************
		 **************************************************************** 
		 *  recursion until 'request' node (root of tree) is processed  *
		 * **************************************************************
		 ****************************************************************
		 */
		for (int i=0; i<node.getChildCount(); i++) {
			ParseTree child = node.getChild(i);
			processNode(child);
		}
				
		// Stuff that happens when leaving a node (taking it off the stack)
		if (nodeCat.equals("cq_segments") || nodeCat.equals("sq_segments")) {
			// exclude whitespaces analysed as empty cq_segments
			if (!ignoreCq_segment && !sequenceStack.isEmpty()) {
//			if (!ignoreCq_segment) {	
				sequenceStack.pop();
				objectStack.pop();
			}
		}
		
		if (nodeCat.equals("cq_disj_segments")) {
			groupStack.pop();
			objectStack.pop();
		}
		
		if (nodeCat.equals("cq_segment") || nodeCat.equals("sq_segment")){
			tokenStack.pop();
			objectStack.pop();
		}
		
		if (nodeCat.equals("conj_field")) {
			fieldStack.pop();
		}
		
		if (nodeCat.equals("position") || nodeCat.equals("spanclass")) {
			groupStack.pop();
			objectStack.pop();
		}
		
		openNodeCats.pop();
		
	}

	/**
	 * Returns the category (or 'label') of the root of a ParseTree.
	 * @param node
	 * @return
	 */
	public String getNodeCat(ParseTree node) {
		String nodeCat = node.toStringTree(poliqarpParser);
		Pattern p = Pattern.compile("\\((.*?)\\s"); // from opening parenthesis to 1st whitespace
		Matcher m = p.matcher(node.toStringTree(poliqarpParser));
		if (m.find()) {
			nodeCat = m.group(1);
		} 
		return nodeCat;
	}
	
	private static ParserRuleContext parsePoliqarpQuery (String p) {
		Lexer poliqarpLexer = new PoliqarpPlusLexer((CharStream)null);
	    ParserRuleContext tree = null;
	    // Like p. 111
	    try {

	      // Tokenize input data
	      ANTLRInputStream input = new ANTLRInputStream(p);
	      poliqarpLexer.setInputStream(input);
	      CommonTokenStream tokens = new CommonTokenStream(poliqarpLexer);
	      poliqarpParser = new PoliqarpPlusParser(tokens);

	      // Don't throw out erroneous stuff
	      poliqarpParser.setErrorHandler(new BailErrorStrategy());
	      poliqarpParser.removeErrorListeners();

	      // Get starting rule from parser
	      Method startRule = PoliqarpPlusParser.class.getMethod("request");
	      tree = (ParserRuleContext) startRule.invoke(poliqarpParser, (Object[])null);
	    }

	    // Some things went wrong ...
	    catch (Exception e) {
	      System.err.println( e.getMessage() );
	    }

	    // Return the generated tree
	    return tree;
	  }
	
	public static void main(String[] args) {
		/*
		 * For testing
		 */
		String[] queries = new String[] {
				"[base=foo]|([base=foo][base=bar])*",
				"([base=foo]|[base=bar])[base=foobar]",
				"[base=foo]([base=bar]|[base=foobar/i])",
				"[base=bar|base=foo]",
				"[base=bar]",
				"[base=foo][base=bar]",
				"[(base=bar|base=foo)&orth=wee]",
				"[base=foo/i][base=bar]{2,4}",
				"foo bar/i",
				"{[base=foo]}[orth=bar]",
				"{[base=foo]}{[orth=bar]}",
				"{1:[base=foo]<np>}",
				"shrink({[base=foo]}[orth=bar])",
				"<np>",
				"startsWith({<sentence>},<np>)",
				"startsWith({<sentence>},[base=foo])",
				"[base=foo]|([base=foo][base=bar])* meta author=Goethe&year=1815",
				"([base=foo][base=bar])*",
				};
		for (String q : queries) {
			try {
				System.out.println(q);
				System.out.println(PoliqarpPlusTree.parsePoliqarpQuery(q).toStringTree(PoliqarpPlusTree.poliqarpParser));
				@SuppressWarnings("unused")
				PoliqarpPlusTree pt = new PoliqarpPlusTree(q);
//				System.out.println(PoliqarpPlusTree.parsePoliqarpQuery(q).toStringTree(PoliqarpPlusTree.poliqarpParser));
				System.out.println();
				
			} catch (NullPointerException npe) {
				npe.printStackTrace();
				System.out.println("null\n");
			}
		}
	}

}