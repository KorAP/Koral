package de.ids_mannheim.korap.query.serialize;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.Tree;

import de.ids_mannheim.korap.query.cosmas2.c2psLexer;
import de.ids_mannheim.korap.query.cosmas2.c2psParser;

/**
 * Map representation of CosmasII syntax tree as returned by ANTLR
 * @author joachim
 *
 */
public class CosmasTree extends AbstractSyntaxTree {
	
	private static c2psParser cosmasParser;
	/*
	 * Following collections have the following functions:
	 * - the request is a map with two keys (meta/query):			{meta=[], query=[]}
	 * - the query is a list of token group maps: 					{meta=[], query=[tg1=[], tg2=[]]}
	 * - each token group is a list of tokens: 						{meta=[], query=[tg1=[t1_1, t1_2], tg2=[t2_1, t2_2, t2_3]]}
	 * - each token corresponds to a single 'fields' linked list	{meta=[], query=[tg1=[t1_1=[], t1_2=[]], ... ]}
	 * - each fields list contains a logical operator and 'field maps' defining attributes and values
	 * 																{meta=[], query=[tg1=[t1_1=[[disj, {base=foo}, {base=bar}]], t1_2=[]], ... ]}
	 */
	String query;
	LinkedHashMap<String,Object> requestMap = new LinkedHashMap<String,Object>();
	LinkedHashMap<String,Object> queryMap = new LinkedHashMap<String,Object>();
	LinkedHashMap<String,Object> tokenGroup = new LinkedHashMap<String,Object>();
	ArrayList<Object> fieldGroup = new ArrayList<Object>(); 
	LinkedHashMap<String,Object> fieldMap;
	ArrayList<List<Object>> distantTokens;
	/**
	 * Makes it possible to store several distantTokenGroups
	 */
	LinkedList<ArrayList<List<Object>>> distantTokensStack = new LinkedList<ArrayList<List<Object>>>();
	/**
	 * Field for repetition query (Kleene + or * operations, or min/max queries: {2,4}
	 */
	String repetition = "";
	int tokenCount=0;
	int tokenGroupCount=0;
	/**
	 * Keeps track of open node categories
	 */
	LinkedList<String> openNodeCats = new LinkedList<String>();
	/**
	 * Global control structure for fieldGroups, keeps track of open fieldGroups.
	 */
	LinkedList<ArrayList<Object>> openFieldGroups = new LinkedList<ArrayList<Object>>();
	/**
	 * Global control structure for tokenGroups, keeps track of open tokenGroups.
	 */
	LinkedList<LinkedHashMap<String,Object>> tokenGroupsStack = new LinkedList<LinkedHashMap<String,Object>>();
	/**
	 * Flag that indicates whether token fields or meta fields are currently being processed
	 */
	boolean inMeta = false;
	boolean negate = false;
	
	Tree cosmasTree;
	
	LinkedHashMap<String,Object> treeMap = new LinkedHashMap<String,Object>();
	/**
	 * Keeps track of all visited nodes in a tree
	 */
	List<Tree> visited = new ArrayList<Tree>();
	  
	
	/**
	 * 
	 * @param tree The syntax tree as returned by ANTLR
	 * @param parser The ANTLR parser instance that generated the parse tree
	 */
	public CosmasTree(String query) {
		this.query = query;
		process(query);
		System.out.println(requestMap);
	}
	
	@Override
	public Map<String, Object> getRequestMap() {
		return this.requestMap;
	}
	
	@Override
	public void process(String query) {
		Tree tree = parseCosmasQuery(query);
		System.out.println("Processing Cosmas");
		processNode(tree);
	}
	
	private void processNode(Tree node) {
		
		// Top-down processing
		if (visited.contains(node)) return;
		else visited.add(node);
		
		
		String nodeCat = getNodeCat(node);
		openNodeCats.push(nodeCat);
		
		
		
		System.out.println(openNodeCats);
		System.out.println(distantTokensStack);
		
		/* ***************************************
		 * Processing individual node categories *
		 *****************************************/
		// C2QP is tree root
		if (nodeCat.equals("C2PQ")) {
			queryMap = new LinkedHashMap<String,Object>();
			requestMap.put("query", queryMap);
		}
		
		// Nodes introducing tokens. Process all in the same manner, except for the fieldMap entry
		if (nodeCat.equals("OPWF") || nodeCat.equals("OPLEM") || nodeCat.equals("OPMORPH")) {
			
			if (tokenGroupsStack.isEmpty()) {
				tokenGroup = new LinkedHashMap<String, Object>();
				tokenCount=0;
				tokenGroupCount++;
				queryMap.put("tokenGroup"+tokenGroupCount, tokenGroup);
				tokenGroupsStack.push(tokenGroup);
			} else {
				tokenGroup = tokenGroupsStack.getFirst();
			}
			
			// check if this token comes after a distant operator (like "/+w3:4") and if yes,
			// insert the empty tokenGroups before the current token
			if (openNodeCats.get(1).equals("ARG2")) {
				if (openNodeCats.get(2).equals("OPPROX") && !distantTokensStack.isEmpty()) {
					for (List<Object> distantTokenGroup : distantTokensStack.pop()) {
//						if (tokenGroupsStack.isEmpty()) {
//							queryMap.put("token"+tokenGroupCount+"_1", distantTokenGroup);
//						} else {
						tokenCount++;
						tokenGroupsStack.getFirst().put("token"+tokenGroupCount+"_"+tokenCount, distantTokenGroup);
//						}
//						tokenGroupCount++;
					}
				}  
				// check negation of token by preceding OPNOT
//				else if (openNodeCats.get(2).equals("OPNOT")) {
//					negate = true;
//				}
			}
			
			fieldGroup = new ArrayList<Object>();
			tokenCount++;
			tokenGroup.put("token"+tokenGroupCount+"_"+tokenCount, fieldGroup);
			
			fieldMap = new LinkedHashMap<String, Object>();
			fieldGroup.add(fieldMap);
			
			// make category-specific fieldMap entry
			if (nodeCat.equals("OPWF")) {
				fieldMap.put("form", node.getChild(0).toStringTree());
			}
			if (nodeCat.equals("OPLEM")) {
				fieldMap.put("lemma", node.getChild(0).toStringTree());
			}
			if (nodeCat.equals("OPMORPH")) {
				fieldMap.put("morph", node.toStringTree());
				//TODO decompose morphology query
			}
			// negate field (see above)
			if (negate) {
				fieldMap.put("relation", "!=");
			}
			
//			tokenGroupsStack.push(tokenGroup);
		}
		
		// negate every token that's under OPNOT > ARG2
		if (nodeCat.equals("ARG2") && openNodeCats.get(1).equals("OPNOT")) {
			negate = true;
		}
		
		if (nodeCat.equals("OPOR")) {
			tokenGroup = new LinkedHashMap<String, Object>();
			tokenCount=0;
			tokenGroupCount++;
			if (tokenGroupsStack.isEmpty()) {
				queryMap.put("tokenGroup"+tokenGroupCount, tokenGroup);
			} else {
				tokenGroupsStack.getFirst().put("tokenGroup"+tokenGroupCount, tokenGroup);
			}
			tokenGroup.put("type", "disj");
			tokenGroupsStack.push(tokenGroup);
		}
		
		if (nodeCat.equals("OPAND")) {
			tokenGroup = new LinkedHashMap<String, Object>();
			tokenCount=0;
			tokenGroupCount++;
			if (tokenGroupsStack.isEmpty()) {
				queryMap.put("tokenGroup"+tokenGroupCount, tokenGroup);
			} else {
				tokenGroupsStack.getFirst().put("tokenGroup"+tokenGroupCount, tokenGroup);
			}
			tokenGroup.put("type", "conj");
			tokenGroupsStack.push(tokenGroup);
		}
		
		if (nodeCat.equals("OPPROX")) {
			distantTokens = new ArrayList<List<Object>>();
			Tree prox_opts = node.getChild(0);
			Tree typ = prox_opts.getChild(0);
			System.err.println(typ.getChild(0).toStringTree());
			Tree dist_list = prox_opts.getChild(1);
			// get relevant information
			String direction = dist_list.getChild(0).getChild(0).getChild(0).toStringTree();
			String min = dist_list.getChild(0).getChild(1).getChild(0).toStringTree();
			String max = dist_list.getChild(0).getChild(1).getChild(1).toStringTree();
			if (min.equals("VAL0")) {
				min=max;
			}
			// create empty tokens and put them on the stack to place them between arg1 and arg2
			for (int i=0; i<Integer.parseInt(max)-1; i++) {
				ArrayList<Object> emptyToken = new ArrayList<Object>();
				LinkedHashMap<String,Object> emptyFieldMap = new LinkedHashMap<String,Object>();
				emptyToken.add(emptyFieldMap);
				tokenGroup.put("token"+tokenGroupCount+"_1", emptyToken);
				// mark all tokens between min and max optional
				if (i>=Integer.parseInt(min)) {
					emptyFieldMap.put("optional", "true");
				}
				distantTokens.add(emptyToken);
			}
			distantTokensStack.push(distantTokens);
		}
		
		
//		System.err.println(tokenGroupsStack.size()+" "+tokenGroupsStack);
		// recursion until 'query' node (root of tree) is processed
		for (int i=0; i<node.getChildCount(); i++) {
			Tree child = node.getChild(i);
			processNode(child);
		}
		
		if (nodeCat.equals("ARG2") && openNodeCats.get(1).equals("OPNOT")) {
			negate = false;
		}

		if (nodeCat.equals("OPAND") || nodeCat.equals("OPOR")) {
			tokenGroupsStack.pop();
//			tokenGroupCount--;
//			tokenCount=0;
		}
		
		openNodeCats.pop();
		
	}

	/**
	 * Returns the category (or 'label') of the root of a ParseTree.
	 * @param node
	 * @return
	 */
	public String getNodeCat(Tree node) {
		String nodeCat = node.toStringTree();
		Pattern p = Pattern.compile("\\((.*?)\\s"); // from opening parenthesis to 1st whitespace
		Matcher m = p.matcher(node.toStringTree());
		if (m.find()) {
			nodeCat = m.group(1);
		} 
		return nodeCat;
	}
	
	private static Tree parseCosmasQuery(String p) {
		  Tree tree = null;
		  ANTLRStringStream
			ss = new ANTLRStringStream(p);
		  c2psLexer
			lex = new c2psLexer(ss);
		  org.antlr.runtime.CommonTokenStream tokens =   //v3
	  		new org.antlr.runtime.CommonTokenStream(lex);
		  cosmasParser = new c2psParser(tokens);
		  c2psParser.c2ps_query_return
			c2Return = null;
		  try 
			{
			c2Return = cosmasParser.c2ps_query();  // statt t().
			}
		  catch (RecognitionException e) 
			{
			e.printStackTrace();
			}
		  // AST Tree anzeigen:
		  tree = (Tree)c2Return.getTree();
		  return tree;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/*
		 * For testing
		 */
		String[] queries = new String[] {
				/* COSMAS 2 */
//				"&Mond",
//				"Mond Sterne",
//				"Mond*",
//				"Mond oder Sterne",
//				"(des oder eines) /+w2 (Bauern oder Bauers oder Bauerns)",
//				"(Sonne /+w2 Mond) /+w2:3 Sterne",
//				"Mond oder Sonne /w2 Sterne",
//				"MORPH(V PCP)",
//				"MORPH(V PCP) Baum" ,
//				"Sonne %w2 Mond",
//				"Sonne /w2 Mond",
//				"Sonne nicht (Mond Stern)",
//				"Sonne nicht (Mond oder Stern)",
//				"Sonne /+w1:4 Mond",
				"(sonne und mond) oder sterne",
				"(stern oder (sonne und mond)) und MORPH(V PCP)",
				"(sonne und (stern oder mond)) /+w2 luna???",
				"(Tag /+w2 $offenen) /+w1 Tür",
				"heißt /+w2 \"und\" ,"
				};
		for (String q : queries) {
			try {
				System.out.println(q);
				System.out.println(parseCosmasQuery(q).toStringTree());
				CosmasTree act = new CosmasTree(q);
				System.out.println();
				
			} catch (NullPointerException npe) {
				npe.printStackTrace();
				System.out.println("null\n");
			}
		}
	}
}