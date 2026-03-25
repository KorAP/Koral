// $ANTLR 3.5.3 c2ps_opMORPH.g 2026-07-28 12:36:06
package de.ids_mannheim.korap.query.parse.cosmas;

import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

import org.antlr.runtime.tree.*;


@SuppressWarnings("all")
public class c2ps_opMORPHParser extends Parser {
	public static final String[] tokenNames = new String[] {
		"<invalid>", "<EOR>", "<DOWN>", "<UP>", "AVEXPR1", "AVEXPR3", "AVEXPR5", 
		"AVEXPR6", "B_EXPR", "EMPTY", "EXPR", "OP", "OPMORPH", "REG_EXPR", "WS", 
		"'&'", "')'", "'MORPH('"
	};
	public static final int EOF=-1;
	public static final int T__15=15;
	public static final int T__16=16;
	public static final int T__17=17;
	public static final int AVEXPR1=4;
	public static final int AVEXPR3=5;
	public static final int AVEXPR5=6;
	public static final int AVEXPR6=7;
	public static final int B_EXPR=8;
	public static final int EMPTY=9;
	public static final int EXPR=10;
	public static final int OP=11;
	public static final int OPMORPH=12;
	public static final int REG_EXPR=13;
	public static final int WS=14;

	// delegates
	public Parser[] getDelegates() {
		return new Parser[] {};
	}

	// delegators


	public c2ps_opMORPHParser(TokenStream input) {
		this(input, new RecognizerSharedState());
	}
	public c2ps_opMORPHParser(TokenStream input, RecognizerSharedState state) {
		super(input, state);
	}

	protected TreeAdaptor adaptor = new CommonTreeAdaptor();

	public void setTreeAdaptor(TreeAdaptor adaptor) {
		this.adaptor = adaptor;
	}
	public TreeAdaptor getTreeAdaptor() {
		return adaptor;
	}
	@Override public String[] getTokenNames() { return c2ps_opMORPHParser.tokenNames; }
	@Override public String getGrammarFileName() { return "c2ps_opMORPH.g"; }


	public static class opMORPH_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "opMORPH"
	// c2ps_opMORPH.g:58:1: opMORPH : ( 'MORPH(' ')' EOF -> ^( OPMORPH EMPTY ) | 'MORPH(' avExpr ( ( '&' )? avExpr )* ')' EOF -> ^( OPMORPH ( avExpr )+ ) );
	public final c2ps_opMORPHParser.opMORPH_return opMORPH() throws RecognitionException {
		c2ps_opMORPHParser.opMORPH_return retval = new c2ps_opMORPHParser.opMORPH_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token string_literal1=null;
		Token char_literal2=null;
		Token EOF3=null;
		Token string_literal4=null;
		Token char_literal6=null;
		Token char_literal8=null;
		Token EOF9=null;
		ParserRuleReturnScope avExpr5 =null;
		ParserRuleReturnScope avExpr7 =null;

		Object string_literal1_tree=null;
		Object char_literal2_tree=null;
		Object EOF3_tree=null;
		Object string_literal4_tree=null;
		Object char_literal6_tree=null;
		Object char_literal8_tree=null;
		Object EOF9_tree=null;
		RewriteRuleTokenStream stream_15=new RewriteRuleTokenStream(adaptor,"token 15");
		RewriteRuleTokenStream stream_16=new RewriteRuleTokenStream(adaptor,"token 16");
		RewriteRuleTokenStream stream_17=new RewriteRuleTokenStream(adaptor,"token 17");
		RewriteRuleTokenStream stream_EOF=new RewriteRuleTokenStream(adaptor,"token EOF");
		RewriteRuleSubtreeStream stream_avExpr=new RewriteRuleSubtreeStream(adaptor,"rule avExpr");

		try {
			// c2ps_opMORPH.g:58:9: ( 'MORPH(' ')' EOF -> ^( OPMORPH EMPTY ) | 'MORPH(' avExpr ( ( '&' )? avExpr )* ')' EOF -> ^( OPMORPH ( avExpr )+ ) )
			int alt3=2;
			int LA3_0 = input.LA(1);
			if ( (LA3_0==17) ) {
				int LA3_1 = input.LA(2);
				if ( (LA3_1==16) ) {
					alt3=1;
				}
				else if ( ((LA3_1 >= AVEXPR1 && LA3_1 <= AVEXPR6)||LA3_1==EXPR||LA3_1==REG_EXPR) ) {
					alt3=2;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 3, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 3, 0, input);
				throw nvae;
			}

			switch (alt3) {
				case 1 :
					// c2ps_opMORPH.g:58:11: 'MORPH(' ')' EOF
					{
					string_literal1=(Token)match(input,17,FOLLOW_17_in_opMORPH425);  
					stream_17.add(string_literal1);

					char_literal2=(Token)match(input,16,FOLLOW_16_in_opMORPH427);  
					stream_16.add(char_literal2);

					EOF3=(Token)match(input,EOF,FOLLOW_EOF_in_opMORPH435);  
					stream_EOF.add(EOF3);


					// AST REWRITE
					// elements: 
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 58:34: -> ^( OPMORPH EMPTY )
					{
						// c2ps_opMORPH.g:58:37: ^( OPMORPH EMPTY )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(OPMORPH, "OPMORPH"), root_1);
						adaptor.addChild(root_1, (Object)adaptor.create(EMPTY, "EMPTY"));
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;
				case 2 :
					// c2ps_opMORPH.g:59:5: 'MORPH(' avExpr ( ( '&' )? avExpr )* ')' EOF
					{
					string_literal4=(Token)match(input,17,FOLLOW_17_in_opMORPH449);  
					stream_17.add(string_literal4);

					pushFollow(FOLLOW_avExpr_in_opMORPH451);
					avExpr5=avExpr();
					state._fsp--;

					stream_avExpr.add(avExpr5.getTree());
					// c2ps_opMORPH.g:59:21: ( ( '&' )? avExpr )*
					loop2:
					while (true) {
						int alt2=2;
						int LA2_0 = input.LA(1);
						if ( ((LA2_0 >= AVEXPR1 && LA2_0 <= AVEXPR6)||LA2_0==EXPR||LA2_0==REG_EXPR||LA2_0==15) ) {
							alt2=1;
						}

						switch (alt2) {
						case 1 :
							// c2ps_opMORPH.g:59:22: ( '&' )? avExpr
							{
							// c2ps_opMORPH.g:59:22: ( '&' )?
							int alt1=2;
							int LA1_0 = input.LA(1);
							if ( (LA1_0==15) ) {
								alt1=1;
							}
							switch (alt1) {
								case 1 :
									// c2ps_opMORPH.g:59:22: '&'
									{
									char_literal6=(Token)match(input,15,FOLLOW_15_in_opMORPH454);  
									stream_15.add(char_literal6);

									}
									break;

							}

							pushFollow(FOLLOW_avExpr_in_opMORPH457);
							avExpr7=avExpr();
							state._fsp--;

							stream_avExpr.add(avExpr7.getTree());
							}
							break;

						default :
							break loop2;
						}
					}

					char_literal8=(Token)match(input,16,FOLLOW_16_in_opMORPH461);  
					stream_16.add(char_literal8);

					EOF9=(Token)match(input,EOF,FOLLOW_EOF_in_opMORPH464);  
					stream_EOF.add(EOF9);


					// AST REWRITE
					// elements: avExpr
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 59:45: -> ^( OPMORPH ( avExpr )+ )
					{
						// c2ps_opMORPH.g:59:48: ^( OPMORPH ( avExpr )+ )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(OPMORPH, "OPMORPH"), root_1);
						if ( !(stream_avExpr.hasNext()) ) {
							throw new RewriteEarlyExitException();
						}
						while ( stream_avExpr.hasNext() ) {
							adaptor.addChild(root_1, stream_avExpr.nextTree());
						}
						stream_avExpr.reset();

						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "opMORPH"


	public static class avExpr_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "avExpr"
	// c2ps_opMORPH.g:61:1: avExpr : ( AVEXPR1 | AVEXPR3 | AVEXPR5 | AVEXPR6 | EXPR | REG_EXPR );
	public final c2ps_opMORPHParser.avExpr_return avExpr() throws RecognitionException {
		c2ps_opMORPHParser.avExpr_return retval = new c2ps_opMORPHParser.avExpr_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token set10=null;

		Object set10_tree=null;

		try {
			// c2ps_opMORPH.g:61:8: ( AVEXPR1 | AVEXPR3 | AVEXPR5 | AVEXPR6 | EXPR | REG_EXPR )
			// c2ps_opMORPH.g:
			{
			root_0 = (Object)adaptor.nil();


			set10=input.LT(1);
			if ( (input.LA(1) >= AVEXPR1 && input.LA(1) <= AVEXPR6)||input.LA(1)==EXPR||input.LA(1)==REG_EXPR ) {
				input.consume();
				adaptor.addChild(root_0, (Object)adaptor.create(set10));
				state.errorRecovery=false;
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				throw mse;
			}
			}

			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "avExpr"

	// Delegated rules



	public static final BitSet FOLLOW_17_in_opMORPH425 = new BitSet(new long[]{0x0000000000010000L});
	public static final BitSet FOLLOW_16_in_opMORPH427 = new BitSet(new long[]{0x0000000000000000L});
	public static final BitSet FOLLOW_EOF_in_opMORPH435 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_17_in_opMORPH449 = new BitSet(new long[]{0x00000000000024F0L});
	public static final BitSet FOLLOW_avExpr_in_opMORPH451 = new BitSet(new long[]{0x000000000001A4F0L});
	public static final BitSet FOLLOW_15_in_opMORPH454 = new BitSet(new long[]{0x00000000000024F0L});
	public static final BitSet FOLLOW_avExpr_in_opMORPH457 = new BitSet(new long[]{0x000000000001A4F0L});
	public static final BitSet FOLLOW_16_in_opMORPH461 = new BitSet(new long[]{0x0000000000000000L});
	public static final BitSet FOLLOW_EOF_in_opMORPH464 = new BitSet(new long[]{0x0000000000000002L});
}
