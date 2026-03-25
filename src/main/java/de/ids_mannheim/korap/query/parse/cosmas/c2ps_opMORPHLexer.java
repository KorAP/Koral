// $ANTLR 3.5.3 c2ps_opMORPH.g 2026-07-28 12:36:06
package de.ids_mannheim.korap.query.parse.cosmas;

import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("all")
public class c2ps_opMORPHLexer extends Lexer {
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
	// delegators
	public Lexer[] getDelegates() {
		return new Lexer[] {};
	}

	public c2ps_opMORPHLexer() {} 
	public c2ps_opMORPHLexer(CharStream input) {
		this(input, new RecognizerSharedState());
	}
	public c2ps_opMORPHLexer(CharStream input, RecognizerSharedState state) {
		super(input,state);
	}
	@Override public String getGrammarFileName() { return "c2ps_opMORPH.g"; }

	// $ANTLR start "T__15"
	public final void mT__15() throws RecognitionException {
		try {
			int _type = T__15;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// c2ps_opMORPH.g:4:7: ( '&' )
			// c2ps_opMORPH.g:4:9: '&'
			{
			match('&'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__15"

	// $ANTLR start "T__16"
	public final void mT__16() throws RecognitionException {
		try {
			int _type = T__16;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// c2ps_opMORPH.g:5:7: ( ')' )
			// c2ps_opMORPH.g:5:9: ')'
			{
			match(')'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__16"

	// $ANTLR start "T__17"
	public final void mT__17() throws RecognitionException {
		try {
			int _type = T__17;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// c2ps_opMORPH.g:6:7: ( 'MORPH(' )
			// c2ps_opMORPH.g:6:9: 'MORPH('
			{
			match("MORPH("); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__17"

	// $ANTLR start "WS"
	public final void mWS() throws RecognitionException {
		try {
			int _type = WS;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// c2ps_opMORPH.g:25:6: ( ( ' ' | '\\t' | '\\r' | '\\n' )+ )
			// c2ps_opMORPH.g:25:8: ( ' ' | '\\t' | '\\r' | '\\n' )+
			{
			// c2ps_opMORPH.g:25:8: ( ' ' | '\\t' | '\\r' | '\\n' )+
			int cnt1=0;
			loop1:
			while (true) {
				int alt1=2;
				int LA1_0 = input.LA(1);
				if ( ((LA1_0 >= '\t' && LA1_0 <= '\n')||LA1_0=='\r'||LA1_0==' ') ) {
					alt1=1;
				}

				switch (alt1) {
				case 1 :
					// c2ps_opMORPH.g:
					{
					if ( (input.LA(1) >= '\t' && input.LA(1) <= '\n')||input.LA(1)=='\r'||input.LA(1)==' ' ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

				default :
					if ( cnt1 >= 1 ) break loop1;
					EarlyExitException eee = new EarlyExitException(1, input);
					throw eee;
				}
				cnt1++;
			}

			skip();
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "WS"

	// $ANTLR start "EXPR"
	public final void mEXPR() throws RecognitionException {
		try {
			int _type = EXPR;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// c2ps_opMORPH.g:34:7: ( ( 'a' .. 'z' | 'A' .. 'Z' ) ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )* | ( '1' | '2' | '3' ) )
			int alt3=2;
			int LA3_0 = input.LA(1);
			if ( ((LA3_0 >= 'A' && LA3_0 <= 'Z')||(LA3_0 >= 'a' && LA3_0 <= 'z')) ) {
				alt3=1;
			}
			else if ( ((LA3_0 >= '1' && LA3_0 <= '3')) ) {
				alt3=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 3, 0, input);
				throw nvae;
			}

			switch (alt3) {
				case 1 :
					// c2ps_opMORPH.g:34:9: ( 'a' .. 'z' | 'A' .. 'Z' ) ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )*
					{
					if ( (input.LA(1) >= 'A' && input.LA(1) <= 'Z')||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					// c2ps_opMORPH.g:34:30: ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )*
					loop2:
					while (true) {
						int alt2=2;
						int LA2_0 = input.LA(1);
						if ( ((LA2_0 >= '0' && LA2_0 <= '9')||(LA2_0 >= 'A' && LA2_0 <= 'Z')||LA2_0=='_'||(LA2_0 >= 'a' && LA2_0 <= 'z')) ) {
							alt2=1;
						}

						switch (alt2) {
						case 1 :
							// c2ps_opMORPH.g:
							{
							if ( (input.LA(1) >= '0' && input.LA(1) <= '9')||(input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
								input.consume();
							}
							else {
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							}
							break;

						default :
							break loop2;
						}
					}

					}
					break;
				case 2 :
					// c2ps_opMORPH.g:34:69: ( '1' | '2' | '3' )
					{
					if ( (input.LA(1) >= '1' && input.LA(1) <= '3') ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

			}
			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "EXPR"

	// $ANTLR start "REG_EXPR"
	public final void mREG_EXPR() throws RecognitionException {
		try {
			int _type = REG_EXPR;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// c2ps_opMORPH.g:35:10: ( ( 'a' .. 'z' | 'A' .. 'Z' ) ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' | '[' | ']' | '*' | '?' | '+' | '.' | '\\\\' )* )
			// c2ps_opMORPH.g:35:12: ( 'a' .. 'z' | 'A' .. 'Z' ) ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' | '[' | ']' | '*' | '?' | '+' | '.' | '\\\\' )*
			{
			if ( (input.LA(1) >= 'A' && input.LA(1) <= 'Z')||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
				input.consume();
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			// c2ps_opMORPH.g:35:33: ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' | '[' | ']' | '*' | '?' | '+' | '.' | '\\\\' )*
			loop4:
			while (true) {
				int alt4=2;
				int LA4_0 = input.LA(1);
				if ( ((LA4_0 >= '*' && LA4_0 <= '+')||LA4_0=='.'||(LA4_0 >= '0' && LA4_0 <= '9')||LA4_0=='?'||(LA4_0 >= 'A' && LA4_0 <= ']')||LA4_0=='_'||(LA4_0 >= 'a' && LA4_0 <= 'z')) ) {
					alt4=1;
				}

				switch (alt4) {
				case 1 :
					// c2ps_opMORPH.g:
					{
					if ( (input.LA(1) >= '*' && input.LA(1) <= '+')||input.LA(1)=='.'||(input.LA(1) >= '0' && input.LA(1) <= '9')||input.LA(1)=='?'||(input.LA(1) >= 'A' && input.LA(1) <= ']')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

				default :
					break loop4;
				}
			}

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "REG_EXPR"

	// $ANTLR start "OP"
	public final void mOP() throws RecognitionException {
		try {
			int _type = OP;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// c2ps_opMORPH.g:37:5: ( ( '=' | '!=' | '<>' ) )
			// c2ps_opMORPH.g:37:7: ( '=' | '!=' | '<>' )
			{
			// c2ps_opMORPH.g:37:7: ( '=' | '!=' | '<>' )
			int alt5=3;
			switch ( input.LA(1) ) {
			case '=':
				{
				alt5=1;
				}
				break;
			case '!':
				{
				alt5=2;
				}
				break;
			case '<':
				{
				alt5=3;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 5, 0, input);
				throw nvae;
			}
			switch (alt5) {
				case 1 :
					// c2ps_opMORPH.g:37:8: '='
					{
					match('='); 
					}
					break;
				case 2 :
					// c2ps_opMORPH.g:37:14: '!='
					{
					match("!="); 

					}
					break;
				case 3 :
					// c2ps_opMORPH.g:37:21: '<>'
					{
					match("<>"); 

					}
					break;

			}

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "OP"

	// $ANTLR start "B_EXPR"
	public final void mB_EXPR() throws RecognitionException {
		try {
			// c2ps_opMORPH.g:43:17: ( EXPR ':' ( '-' )? REG_EXPR | ( '-' )? REG_EXPR )
			int alt8=2;
			alt8 = dfa8.predict(input);
			switch (alt8) {
				case 1 :
					// c2ps_opMORPH.g:43:19: EXPR ':' ( '-' )? REG_EXPR
					{
					mEXPR(); 

					match(':'); 
					// c2ps_opMORPH.g:43:28: ( '-' )?
					int alt6=2;
					int LA6_0 = input.LA(1);
					if ( (LA6_0=='-') ) {
						alt6=1;
					}
					switch (alt6) {
						case 1 :
							// c2ps_opMORPH.g:43:28: '-'
							{
							match('-'); 
							}
							break;

					}

					mREG_EXPR(); 

					}
					break;
				case 2 :
					// c2ps_opMORPH.g:43:44: ( '-' )? REG_EXPR
					{
					// c2ps_opMORPH.g:43:44: ( '-' )?
					int alt7=2;
					int LA7_0 = input.LA(1);
					if ( (LA7_0=='-') ) {
						alt7=1;
					}
					switch (alt7) {
						case 1 :
							// c2ps_opMORPH.g:43:44: '-'
							{
							match('-'); 
							}
							break;

					}

					mREG_EXPR(); 

					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "B_EXPR"

	// $ANTLR start "AVEXPR1"
	public final void mAVEXPR1() throws RecognitionException {
		try {
			int _type = AVEXPR1;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// c2ps_opMORPH.g:45:9: ( EXPR '/' EXPR OP ( B_EXPR | '\\'' B_EXPR ( ' ' | B_EXPR )* '\\'' | '\"' B_EXPR ( ' ' | B_EXPR )* '\"' ) )
			// c2ps_opMORPH.g:45:11: EXPR '/' EXPR OP ( B_EXPR | '\\'' B_EXPR ( ' ' | B_EXPR )* '\\'' | '\"' B_EXPR ( ' ' | B_EXPR )* '\"' )
			{
			mEXPR(); 

			match('/'); 
			mEXPR(); 

			mOP(); 

			// c2ps_opMORPH.g:45:28: ( B_EXPR | '\\'' B_EXPR ( ' ' | B_EXPR )* '\\'' | '\"' B_EXPR ( ' ' | B_EXPR )* '\"' )
			int alt11=3;
			switch ( input.LA(1) ) {
			case '-':
			case '1':
			case '2':
			case '3':
			case 'A':
			case 'B':
			case 'C':
			case 'D':
			case 'E':
			case 'F':
			case 'G':
			case 'H':
			case 'I':
			case 'J':
			case 'K':
			case 'L':
			case 'M':
			case 'N':
			case 'O':
			case 'P':
			case 'Q':
			case 'R':
			case 'S':
			case 'T':
			case 'U':
			case 'V':
			case 'W':
			case 'X':
			case 'Y':
			case 'Z':
			case 'a':
			case 'b':
			case 'c':
			case 'd':
			case 'e':
			case 'f':
			case 'g':
			case 'h':
			case 'i':
			case 'j':
			case 'k':
			case 'l':
			case 'm':
			case 'n':
			case 'o':
			case 'p':
			case 'q':
			case 'r':
			case 's':
			case 't':
			case 'u':
			case 'v':
			case 'w':
			case 'x':
			case 'y':
			case 'z':
				{
				alt11=1;
				}
				break;
			case '\'':
				{
				alt11=2;
				}
				break;
			case '\"':
				{
				alt11=3;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 11, 0, input);
				throw nvae;
			}
			switch (alt11) {
				case 1 :
					// c2ps_opMORPH.g:45:29: B_EXPR
					{
					mB_EXPR(); 

					}
					break;
				case 2 :
					// c2ps_opMORPH.g:45:38: '\\'' B_EXPR ( ' ' | B_EXPR )* '\\''
					{
					match('\''); 
					mB_EXPR(); 

					// c2ps_opMORPH.g:45:50: ( ' ' | B_EXPR )*
					loop9:
					while (true) {
						int alt9=3;
						int LA9_0 = input.LA(1);
						if ( (LA9_0==' ') ) {
							alt9=1;
						}
						else if ( (LA9_0=='-'||(LA9_0 >= '1' && LA9_0 <= '3')||(LA9_0 >= 'A' && LA9_0 <= 'Z')||(LA9_0 >= 'a' && LA9_0 <= 'z')) ) {
							alt9=2;
						}

						switch (alt9) {
						case 1 :
							// c2ps_opMORPH.g:45:51: ' '
							{
							match(' '); 
							}
							break;
						case 2 :
							// c2ps_opMORPH.g:45:57: B_EXPR
							{
							mB_EXPR(); 

							}
							break;

						default :
							break loop9;
						}
					}

					match('\''); 
					}
					break;
				case 3 :
					// c2ps_opMORPH.g:45:73: '\"' B_EXPR ( ' ' | B_EXPR )* '\"'
					{
					match('\"'); 
					mB_EXPR(); 

					// c2ps_opMORPH.g:45:84: ( ' ' | B_EXPR )*
					loop10:
					while (true) {
						int alt10=3;
						int LA10_0 = input.LA(1);
						if ( (LA10_0==' ') ) {
							alt10=1;
						}
						else if ( (LA10_0=='-'||(LA10_0 >= '1' && LA10_0 <= '3')||(LA10_0 >= 'A' && LA10_0 <= 'Z')||(LA10_0 >= 'a' && LA10_0 <= 'z')) ) {
							alt10=2;
						}

						switch (alt10) {
						case 1 :
							// c2ps_opMORPH.g:45:85: ' '
							{
							match(' '); 
							}
							break;
						case 2 :
							// c2ps_opMORPH.g:45:91: B_EXPR
							{
							mB_EXPR(); 

							}
							break;

						default :
							break loop10;
						}
					}

					match('\"'); 
					}
					break;

			}

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "AVEXPR1"

	// $ANTLR start "AVEXPR3"
	public final void mAVEXPR3() throws RecognitionException {
		try {
			int _type = AVEXPR3;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// c2ps_opMORPH.g:46:9: ( EXPR OP ( B_EXPR | '\\'' B_EXPR ( ' ' | B_EXPR )* '\\'' | '\"' B_EXPR ( ' ' | B_EXPR )* '\"' ) )
			// c2ps_opMORPH.g:46:20: EXPR OP ( B_EXPR | '\\'' B_EXPR ( ' ' | B_EXPR )* '\\'' | '\"' B_EXPR ( ' ' | B_EXPR )* '\"' )
			{
			mEXPR(); 

			mOP(); 

			// c2ps_opMORPH.g:46:28: ( B_EXPR | '\\'' B_EXPR ( ' ' | B_EXPR )* '\\'' | '\"' B_EXPR ( ' ' | B_EXPR )* '\"' )
			int alt14=3;
			switch ( input.LA(1) ) {
			case '-':
			case '1':
			case '2':
			case '3':
			case 'A':
			case 'B':
			case 'C':
			case 'D':
			case 'E':
			case 'F':
			case 'G':
			case 'H':
			case 'I':
			case 'J':
			case 'K':
			case 'L':
			case 'M':
			case 'N':
			case 'O':
			case 'P':
			case 'Q':
			case 'R':
			case 'S':
			case 'T':
			case 'U':
			case 'V':
			case 'W':
			case 'X':
			case 'Y':
			case 'Z':
			case 'a':
			case 'b':
			case 'c':
			case 'd':
			case 'e':
			case 'f':
			case 'g':
			case 'h':
			case 'i':
			case 'j':
			case 'k':
			case 'l':
			case 'm':
			case 'n':
			case 'o':
			case 'p':
			case 'q':
			case 'r':
			case 's':
			case 't':
			case 'u':
			case 'v':
			case 'w':
			case 'x':
			case 'y':
			case 'z':
				{
				alt14=1;
				}
				break;
			case '\'':
				{
				alt14=2;
				}
				break;
			case '\"':
				{
				alt14=3;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 14, 0, input);
				throw nvae;
			}
			switch (alt14) {
				case 1 :
					// c2ps_opMORPH.g:46:29: B_EXPR
					{
					mB_EXPR(); 

					}
					break;
				case 2 :
					// c2ps_opMORPH.g:46:38: '\\'' B_EXPR ( ' ' | B_EXPR )* '\\''
					{
					match('\''); 
					mB_EXPR(); 

					// c2ps_opMORPH.g:46:50: ( ' ' | B_EXPR )*
					loop12:
					while (true) {
						int alt12=3;
						int LA12_0 = input.LA(1);
						if ( (LA12_0==' ') ) {
							alt12=1;
						}
						else if ( (LA12_0=='-'||(LA12_0 >= '1' && LA12_0 <= '3')||(LA12_0 >= 'A' && LA12_0 <= 'Z')||(LA12_0 >= 'a' && LA12_0 <= 'z')) ) {
							alt12=2;
						}

						switch (alt12) {
						case 1 :
							// c2ps_opMORPH.g:46:51: ' '
							{
							match(' '); 
							}
							break;
						case 2 :
							// c2ps_opMORPH.g:46:57: B_EXPR
							{
							mB_EXPR(); 

							}
							break;

						default :
							break loop12;
						}
					}

					match('\''); 
					}
					break;
				case 3 :
					// c2ps_opMORPH.g:46:73: '\"' B_EXPR ( ' ' | B_EXPR )* '\"'
					{
					match('\"'); 
					mB_EXPR(); 

					// c2ps_opMORPH.g:46:84: ( ' ' | B_EXPR )*
					loop13:
					while (true) {
						int alt13=3;
						int LA13_0 = input.LA(1);
						if ( (LA13_0==' ') ) {
							alt13=1;
						}
						else if ( (LA13_0=='-'||(LA13_0 >= '1' && LA13_0 <= '3')||(LA13_0 >= 'A' && LA13_0 <= 'Z')||(LA13_0 >= 'a' && LA13_0 <= 'z')) ) {
							alt13=2;
						}

						switch (alt13) {
						case 1 :
							// c2ps_opMORPH.g:46:85: ' '
							{
							match(' '); 
							}
							break;
						case 2 :
							// c2ps_opMORPH.g:46:91: B_EXPR
							{
							mB_EXPR(); 

							}
							break;

						default :
							break loop13;
						}
					}

					match('\"'); 
					}
					break;

			}

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "AVEXPR3"

	// $ANTLR start "AVEXPR5"
	public final void mAVEXPR5() throws RecognitionException {
		try {
			int _type = AVEXPR5;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// c2ps_opMORPH.g:47:9: ( EXPR ':' ( '-' )? REG_EXPR )
			// c2ps_opMORPH.g:47:28: EXPR ':' ( '-' )? REG_EXPR
			{
			mEXPR(); 

			match(':'); 
			// c2ps_opMORPH.g:47:37: ( '-' )?
			int alt15=2;
			int LA15_0 = input.LA(1);
			if ( (LA15_0=='-') ) {
				alt15=1;
			}
			switch (alt15) {
				case 1 :
					// c2ps_opMORPH.g:47:37: '-'
					{
					match('-'); 
					}
					break;

			}

			mREG_EXPR(); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "AVEXPR5"

	// $ANTLR start "AVEXPR6"
	public final void mAVEXPR6() throws RecognitionException {
		try {
			int _type = AVEXPR6;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// c2ps_opMORPH.g:48:9: ( ( '-' )? REG_EXPR )
			// c2ps_opMORPH.g:48:17: ( '-' )? REG_EXPR
			{
			// c2ps_opMORPH.g:48:17: ( '-' )?
			int alt16=2;
			int LA16_0 = input.LA(1);
			if ( (LA16_0=='-') ) {
				alt16=1;
			}
			switch (alt16) {
				case 1 :
					// c2ps_opMORPH.g:48:17: '-'
					{
					match('-'); 
					}
					break;

			}

			mREG_EXPR(); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "AVEXPR6"

	@Override
	public void mTokens() throws RecognitionException {
		// c2ps_opMORPH.g:1:8: ( T__15 | T__16 | T__17 | WS | EXPR | REG_EXPR | OP | AVEXPR1 | AVEXPR3 | AVEXPR5 | AVEXPR6 )
		int alt17=11;
		alt17 = dfa17.predict(input);
		switch (alt17) {
			case 1 :
				// c2ps_opMORPH.g:1:10: T__15
				{
				mT__15(); 

				}
				break;
			case 2 :
				// c2ps_opMORPH.g:1:16: T__16
				{
				mT__16(); 

				}
				break;
			case 3 :
				// c2ps_opMORPH.g:1:22: T__17
				{
				mT__17(); 

				}
				break;
			case 4 :
				// c2ps_opMORPH.g:1:28: WS
				{
				mWS(); 

				}
				break;
			case 5 :
				// c2ps_opMORPH.g:1:31: EXPR
				{
				mEXPR(); 

				}
				break;
			case 6 :
				// c2ps_opMORPH.g:1:36: REG_EXPR
				{
				mREG_EXPR(); 

				}
				break;
			case 7 :
				// c2ps_opMORPH.g:1:45: OP
				{
				mOP(); 

				}
				break;
			case 8 :
				// c2ps_opMORPH.g:1:48: AVEXPR1
				{
				mAVEXPR1(); 

				}
				break;
			case 9 :
				// c2ps_opMORPH.g:1:56: AVEXPR3
				{
				mAVEXPR3(); 

				}
				break;
			case 10 :
				// c2ps_opMORPH.g:1:64: AVEXPR5
				{
				mAVEXPR5(); 

				}
				break;
			case 11 :
				// c2ps_opMORPH.g:1:72: AVEXPR6
				{
				mAVEXPR6(); 

				}
				break;

		}
	}


	protected DFA8 dfa8 = new DFA8(this);
	protected DFA17 dfa17 = new DFA17(this);
	static final String DFA8_eotS =
		"\1\uffff\1\3\2\uffff\1\3";
	static final String DFA8_eofS =
		"\5\uffff";
	static final String DFA8_minS =
		"\1\55\1\60\2\uffff\1\60";
	static final String DFA8_maxS =
		"\2\172\2\uffff\1\172";
	static final String DFA8_acceptS =
		"\2\uffff\1\1\1\2\1\uffff";
	static final String DFA8_specialS =
		"\5\uffff}>";
	static final String[] DFA8_transitionS = {
			"\1\3\3\uffff\3\2\15\uffff\32\1\6\uffff\32\1",
			"\12\4\1\2\6\uffff\32\4\4\uffff\1\4\1\uffff\32\4",
			"",
			"",
			"\12\4\1\2\6\uffff\32\4\4\uffff\1\4\1\uffff\32\4"
	};

	static final short[] DFA8_eot = DFA.unpackEncodedString(DFA8_eotS);
	static final short[] DFA8_eof = DFA.unpackEncodedString(DFA8_eofS);
	static final char[] DFA8_min = DFA.unpackEncodedStringToUnsignedChars(DFA8_minS);
	static final char[] DFA8_max = DFA.unpackEncodedStringToUnsignedChars(DFA8_maxS);
	static final short[] DFA8_accept = DFA.unpackEncodedString(DFA8_acceptS);
	static final short[] DFA8_special = DFA.unpackEncodedString(DFA8_specialS);
	static final short[][] DFA8_transition;

	static {
		int numStates = DFA8_transitionS.length;
		DFA8_transition = new short[numStates][];
		for (int i=0; i<numStates; i++) {
			DFA8_transition[i] = DFA.unpackEncodedString(DFA8_transitionS[i]);
		}
	}

	protected class DFA8 extends DFA {

		public DFA8(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 8;
			this.eot = DFA8_eot;
			this.eof = DFA8_eof;
			this.min = DFA8_min;
			this.max = DFA8_max;
			this.accept = DFA8_accept;
			this.special = DFA8_special;
			this.transition = DFA8_transition;
		}
		@Override
		public String getDescription() {
			return "43:10: fragment B_EXPR : ( EXPR ':' ( '-' )? REG_EXPR | ( '-' )? REG_EXPR );";
		}
	}

	static final String DFA17_eotS =
		"\3\uffff\1\13\1\uffff\2\13\2\uffff\2\13\1\uffff\1\21\3\uffff\1\13\1\uffff"+
		"\2\13\1\uffff";
	static final String DFA17_eofS =
		"\25\uffff";
	static final String DFA17_minS =
		"\1\11\2\uffff\1\41\1\uffff\2\41\2\uffff\2\41\1\uffff\1\52\3\uffff\1\41"+
		"\1\uffff\2\41\1\uffff";
	static final String DFA17_maxS =
		"\1\172\2\uffff\1\172\1\uffff\1\172\1\75\2\uffff\2\172\1\uffff\1\172\3"+
		"\uffff\1\172\1\uffff\2\172\1\uffff";
	static final String DFA17_acceptS =
		"\1\uffff\1\1\1\2\1\uffff\1\4\2\uffff\1\7\1\13\2\uffff\1\5\1\uffff\1\10"+
		"\1\11\1\12\1\uffff\1\6\2\uffff\1\3";
	static final String DFA17_specialS =
		"\25\uffff}>";
	static final String[] DFA17_transitionS = {
			"\2\4\2\uffff\1\4\22\uffff\1\4\1\7\4\uffff\1\1\2\uffff\1\2\3\uffff\1\10"+
			"\3\uffff\3\6\10\uffff\2\7\3\uffff\14\5\1\3\15\5\6\uffff\32\5",
			"",
			"",
			"\1\16\10\uffff\2\14\2\uffff\1\14\1\15\12\12\1\17\1\uffff\2\16\1\uffff"+
			"\1\14\1\uffff\16\12\1\11\13\12\3\14\1\uffff\1\12\1\uffff\32\12",
			"",
			"\1\16\10\uffff\2\14\2\uffff\1\14\1\15\12\12\1\17\1\uffff\2\16\1\uffff"+
			"\1\14\1\uffff\32\12\3\14\1\uffff\1\12\1\uffff\32\12",
			"\1\16\15\uffff\1\15\12\uffff\1\17\1\uffff\2\16",
			"",
			"",
			"\1\16\10\uffff\2\14\2\uffff\1\14\1\15\12\12\1\17\1\uffff\2\16\1\uffff"+
			"\1\14\1\uffff\21\12\1\20\10\12\3\14\1\uffff\1\12\1\uffff\32\12",
			"\1\16\10\uffff\2\14\2\uffff\1\14\1\15\12\12\1\17\1\uffff\2\16\1\uffff"+
			"\1\14\1\uffff\32\12\3\14\1\uffff\1\12\1\uffff\32\12",
			"",
			"\2\14\2\uffff\1\14\1\uffff\12\14\5\uffff\1\14\1\uffff\35\14\1\uffff"+
			"\1\14\1\uffff\32\14",
			"",
			"",
			"",
			"\1\16\10\uffff\2\14\2\uffff\1\14\1\15\12\12\1\17\1\uffff\2\16\1\uffff"+
			"\1\14\1\uffff\17\12\1\22\12\12\3\14\1\uffff\1\12\1\uffff\32\12",
			"",
			"\1\16\10\uffff\2\14\2\uffff\1\14\1\15\12\12\1\17\1\uffff\2\16\1\uffff"+
			"\1\14\1\uffff\7\12\1\23\22\12\3\14\1\uffff\1\12\1\uffff\32\12",
			"\1\16\6\uffff\1\24\1\uffff\2\14\2\uffff\1\14\1\15\12\12\1\17\1\uffff"+
			"\2\16\1\uffff\1\14\1\uffff\32\12\3\14\1\uffff\1\12\1\uffff\32\12",
			""
	};

	static final short[] DFA17_eot = DFA.unpackEncodedString(DFA17_eotS);
	static final short[] DFA17_eof = DFA.unpackEncodedString(DFA17_eofS);
	static final char[] DFA17_min = DFA.unpackEncodedStringToUnsignedChars(DFA17_minS);
	static final char[] DFA17_max = DFA.unpackEncodedStringToUnsignedChars(DFA17_maxS);
	static final short[] DFA17_accept = DFA.unpackEncodedString(DFA17_acceptS);
	static final short[] DFA17_special = DFA.unpackEncodedString(DFA17_specialS);
	static final short[][] DFA17_transition;

	static {
		int numStates = DFA17_transitionS.length;
		DFA17_transition = new short[numStates][];
		for (int i=0; i<numStates; i++) {
			DFA17_transition[i] = DFA.unpackEncodedString(DFA17_transitionS[i]);
		}
	}

	protected class DFA17 extends DFA {

		public DFA17(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 17;
			this.eot = DFA17_eot;
			this.eof = DFA17_eof;
			this.min = DFA17_min;
			this.max = DFA17_max;
			this.accept = DFA17_accept;
			this.special = DFA17_special;
			this.transition = DFA17_transition;
		}
		@Override
		public String getDescription() {
			return "1:1: Tokens : ( T__15 | T__16 | T__17 | WS | EXPR | REG_EXPR | OP | AVEXPR1 | AVEXPR3 | AVEXPR5 | AVEXPR6 );";
		}
	}

}
