package miniJava.SyntacticAnalyzer;

import miniJava.ErrorReporter;
import static miniJava.SyntacticAnalyzer.TokenType.*;
import miniJava.AbstractSyntaxTrees.Package;
import miniJava.AbstractSyntaxTrees.*;

public class Parser {
	private Scanner _scanner;
	private ErrorReporter _errors;
	private Token _currentToken;
	
	public Parser( Scanner scanner, ErrorReporter errors ) {
		this._scanner = scanner;
		this._errors = errors;
		this._currentToken = this._scanner.scan();
	}
	
	class SyntaxError extends Error {
		private static final long serialVersionUID = -6461942006097999362L;
	}
	
	public AST parse() {
		try {
			// The first thing we need to parse is the Program symbol
			//System.out.println("parse() was called");
			return parseProgram();
		} catch( SyntaxError e ) { 
			//TODO: print out error messages
		}
		
		return null;
	}
	
	// Program ::= (ClassDeclaration)* eot
	private Package parseProgram() throws SyntaxError {
		// TODO: Keep parsing class declarations until eot
		ClassDeclList cdl = new ClassDeclList();
		while(_currentToken.getTokenType() != EOT) {
			cdl.add(parseClassDeclaration());
		}
		accept(EOT);
		return new Package(cdl, null);
	}
	
	// ClassDeclaration ::= class identifier { (FieldDeclaration|MethodDeclaration)* }
	private ClassDecl parseClassDeclaration() throws SyntaxError {
		// TODO: Take in a "class" token (check by the TokenType)
		//  What should be done if the first token isn't "class"?
		accept(CLASS);
		
		// TODO: Take in an identifier token
		String cn = _currentToken.getTokenText();
		accept(ID);
		
		// TODO: Take in a {
		accept(LBRACE);
		
		FieldDeclList fdl = new FieldDeclList();
		MethodDeclList mdl = new MethodDeclList();
		
		TypeDenoter typeTok;
		String mn;
		
		// TODO: Parse either a FieldDeclaration or MethodDeclaration
		while(_currentToken.getTokenType() != RBRACE) {
			boolean isPrivate = parseVisibility();
			boolean isStatic = parseAccess();
			if(_currentToken.getTokenType() == VOID) {
				accept(VOID);
				mn = _currentToken.getTokenText();
				accept(ID);
				mdl.add(parseMethodDeclaration(isPrivate, isStatic, new BaseType(TypeKind.VOID, null), mn));
			} else {
				typeTok = parseType();
				mn = _currentToken.getTokenText();
				accept(ID);
				if(_currentToken.getTokenType() == SEMI) {
					fdl.add(parseFieldDeclaration(isPrivate, isStatic, typeTok, mn));
				} else {
					mdl.add(parseMethodDeclaration(isPrivate, isStatic, typeTok, mn));
				}
			}
		}
		// TODO: Take in a }
		accept(RBRACE);
		return new ClassDecl(cn, fdl, mdl, null);
	}
	
	//Visibility ::= (public|private)?
	private boolean parseVisibility() throws SyntaxError {
		if(_currentToken.getTokenType() == PUBLIC) {
			accept(PUBLIC);
			return false;
		} else if(_currentToken.getTokenType() == PRIVATE) {
			accept(PRIVATE);
			return true;
		}
		return false;
	}
	
	//Access ::= static?
	private boolean parseAccess() throws SyntaxError {
		if(_currentToken.getTokenType() == STATIC) {
			accept(STATIC);
			return true;
		}
		return false;
	}
	
	//Type ::= int | boolean | id | (int|id)[]
	private TypeDenoter parseType() throws SyntaxError{
		if(_currentToken.getTokenType() == BOOLEAN) {
			accept(BOOLEAN);
			return new BaseType(TypeKind.BOOLEAN, null);
		} else if(_currentToken.getTokenType() == INT) {
			accept(INT);
			if(_currentToken.getTokenType() == LBRACKET) {
				accept(LBRACKET);
				accept(RBRACKET);
				return new ArrayType(new BaseType(TypeKind.INT, null), null);
			}
			return new BaseType(TypeKind.INT, null);
		} else {
			Token namey = _currentToken;
			accept(ID);
			if(_currentToken.getTokenType() == LBRACKET) {
				accept(LBRACKET);
				accept(RBRACKET);
				return new ArrayType(new ClassType(new Identifier(namey), null), null);
			}
			return new ClassType(new Identifier(namey), null);
		}
	}
	
	//FieldDeclaration ::= Visibility Access Type id; ~ACCEPTED EVERYTHING BUT SEMI BEFORE CALLED
	private FieldDecl parseFieldDeclaration(boolean isPrivate, boolean isStatic, TypeDenoter td, String cn) throws SyntaxError{
		accept(SEMI);
		return new FieldDecl(isPrivate, isStatic, td, cn, null);
	}
	
	//MethodDeclaration ::= Visibility Access (Type|void) id ~ACCEPTED ALL BEFORE THIS~ ( ParameterList? ){ Statement* }
	private MethodDecl parseMethodDeclaration(boolean isPrivate, boolean isStatic, TypeDenoter td, String cn) throws SyntaxError{
		accept(LPAREN);
		ParameterDeclList pl = new ParameterDeclList();
		if (_currentToken.getTokenType() != RPAREN) {
			pl = parseParameterList();
		}
		accept(RPAREN);
		accept(LBRACE);
		StatementList sl = new StatementList();
		while (_currentToken.getTokenType() != RBRACE) {
			sl.add(parseStatement());
		}
		accept(RBRACE);
		return new MethodDecl(new FieldDecl(isPrivate, isStatic, td, cn, null), pl, sl, null);
	}
	
	//ParameterList ::= Type id (, Type id)*
	private ParameterDeclList parseParameterList() throws SyntaxError{
		ParameterDeclList plInside = new ParameterDeclList();
		TypeDenoter td = parseType();
		String pn = _currentToken.getTokenText();
		accept(ID);
		plInside.add(new ParameterDecl(td, pn, null));
		while (_currentToken.getTokenType() == COMMA) {
			accept(COMMA);
			td = parseType();
			pn = _currentToken.getTokenText();
			accept(ID);
			plInside.add(new ParameterDecl(td, pn, null));
		}
		return plInside;
	}
	
	//ArgumentList ::= Expression (, Expression)*
	private ExprList parseArgumentList() throws SyntaxError{
		ExprList el = new ExprList();
		el.add(parseExpression());
		while (_currentToken.getTokenType() == COMMA) {
			accept(COMMA);
			el.add(parseExpression());
		}
		return el;
	}
	
	//Reference ::= id | this | Reference . id ~CONVERTS TO~ (id|this)(.id)*
	private Reference parseReference() throws SyntaxError{
		Reference ref;
		if(_currentToken.getTokenType() == THIS) {
			ref = new ThisRef(null);
			accept(THIS);
		} else {
			ref = new IdRef(new Identifier(_currentToken), null);
			accept(ID);
		}
		Identifier idKeeper;
		Reference refx;
		while(_currentToken.getTokenType() == DOT) {
			accept(DOT);
			idKeeper = (new Identifier(_currentToken));
			accept(ID);
			refx = ref;
			ref = new QualRef(refx, idKeeper, null);
		}
		return ref;
	}
	
	/*Statement ::=  { Statement* }  
	 * | Type id = Expression ;  
	 * | Reference = Expression ;  
	 * | Reference [ Expression ] = Expression ;  
	 * | Reference ( ArgumentList? ) ;  
	 * | return Expression? ;  
	 * | if ( Expression ) Statement (else Statement)?  
	 * | while ( Expression ) Statement
	*/
	private Statement parseStatement() throws SyntaxError{
		// {
		if(_currentToken.getTokenType() == LBRACE) {
			accept(LBRACE);
			StatementList sl = new StatementList();
			while(_currentToken.getTokenType() != RBRACE) {
				sl.add(parseStatement());
			}
			accept(RBRACE);
			return new BlockStmt(sl, null);
		}
		
		// id ~could be Reference or Type
		else if(_currentToken.getTokenType() == ID) {
			Token bigIdentity = _currentToken;
			accept(ID);
			if(_currentToken.getTokenType() == ID) {
				String vn = _currentToken.getTokenText();
				accept(ID);
				accept(EQUALS);
				Expression e = parseExpression();
				accept(SEMI);
				return new VarDeclStmt(new VarDecl(new ClassType(new Identifier(bigIdentity), null), vn, null), e, null);
			} else if(_currentToken.getTokenType() == LBRACKET) {
				accept(LBRACKET);
				if(_currentToken.getTokenType() == RBRACKET) {
					accept(RBRACKET);
					String vnTwo = _currentToken.getTokenText();
					accept(ID);
					accept(EQUALS);
					Expression e = parseExpression();
					accept(SEMI);
					return new VarDeclStmt(new VarDecl (new ArrayType(new ClassType(new Identifier(bigIdentity), null), null), vnTwo, null), e, null);
				} else {
					Expression i = parseExpression();
					accept(RBRACKET);
					accept(EQUALS);
					Expression e = parseExpression();
					accept(SEMI);
					return new IxAssignStmt(new IdRef(new Identifier(bigIdentity), null), i, e, null);
				}
			} else if(_currentToken.getTokenType() == EQUALS) {
				accept(EQUALS);
				Expression e = parseExpression();
				accept(SEMI);
				return new AssignStmt(new IdRef(new Identifier(bigIdentity), null), e, null);
			} else if(_currentToken.getTokenType() == LPAREN) {
				accept(LPAREN);
				ExprList el = new ExprList();
				if(_currentToken.getTokenType() != RPAREN) {
					el = parseArgumentList();
				}
				accept(RPAREN);
				accept(SEMI);
				return new CallStmt(new IdRef(new Identifier(bigIdentity), null), el, null);
			} else {
				
				Reference bigIdentTwo = new IdRef(new Identifier(bigIdentity), null);
				
				Identifier idKeeper;
				Reference refx;
				
				while(_currentToken.getTokenType() == DOT) {
					accept(DOT);
					idKeeper = (new Identifier(_currentToken));
					accept(ID);
					refx = bigIdentTwo;
					bigIdentTwo = new QualRef(refx, idKeeper, null);
				}
				if(_currentToken.getTokenType() == EQUALS) {
					accept(EQUALS);
					//System.out.println("CORRECT BRANCH");
					Expression e = parseExpression();
					accept(SEMI);
					return new AssignStmt(bigIdentTwo, e, null);
				} else if(_currentToken.getTokenType() == LBRACKET) {
					accept(LBRACKET);
					Expression i = parseExpression();
					accept(RBRACKET);
					accept(EQUALS);
					Expression e = parseExpression();
					accept(SEMI);
					return new IxAssignStmt(bigIdentTwo, i, e, null);
				} else {
					accept(LPAREN);
					ExprList al = new ExprList();
					if(_currentToken.getTokenType() != RPAREN) {
						al = parseArgumentList();
					}
					accept(RPAREN);
					accept(SEMI);
					return new CallStmt(bigIdentTwo, al, null);
				}
			}
		}
		
		// int or boolean means Type id = Expression;
		else if(_currentToken.getTokenType() == INT || _currentToken.getTokenType() == BOOLEAN) {
			TypeDenoter td = parseType();
			String idStringer = _currentToken.getTokenText();
			accept(ID);
			accept(EQUALS);
			Expression e = parseExpression();
			accept(SEMI);
			return new VarDeclStmt(new VarDecl(td, idStringer, null), e, null);
		}
		
		// this means Reference but could still be one of three options
		else if(_currentToken.getTokenType() == THIS) {
			Reference thisQualRef = parseReference();
			if(_currentToken.getTokenType() == EQUALS) {
				accept(EQUALS);
				Expression e = parseExpression();
				accept(SEMI);
				return new AssignStmt(thisQualRef, e, null);
			} else if(_currentToken.getTokenType() == LBRACKET) {
				accept(LBRACKET);
				Expression i = parseExpression();
				accept(RBRACKET);
				accept(EQUALS);
				Expression e = parseExpression();
				accept(SEMI);
				return new IxAssignStmt(thisQualRef, i, e, null);
			} else {
				accept(LPAREN);
				ExprList al = new ExprList();
				if(_currentToken.getTokenType() != RPAREN) {
					al = parseArgumentList();
				}
				accept(RPAREN);
				accept(SEMI);
				return new CallStmt(thisQualRef, al, null);
			}
		}
		
		// return means return Expression? ;
		else if(_currentToken.getTokenType() == RETURN) {
			accept(RETURN);
			Expression e = null;
			if(_currentToken.getTokenType() != SEMI) {
				e = parseExpression();
			}
			accept(SEMI);
			return new ReturnStmt(e, null);
		}
		
		// if means if ( Expression ) Statement (else Statement)?
		else if(_currentToken.getTokenType() == IF) {
			accept(IF);
			accept(LPAREN);
			Expression b = parseExpression();
			accept(RPAREN);
			Statement t = parseStatement();
			if(_currentToken.getTokenType() == ELSE) {
				accept(ELSE);
				Statement e = parseStatement();
				return new IfStmt(b, t, e, null);
			}
			return new IfStmt(b, t, null);
		}
		
		// if none of the other if statements are true then it must be while ( Expression ) Statement
		else {
			accept(WHILE);
			accept(LPAREN);
			Expression e = parseExpression();
			accept(RPAREN);
			Statement s = parseStatement();
			return new WhileStmt(e, s, null);
		}
	}
	
	/* Expression ::=  Reference  
	 * | Reference [ Expression ]  
	 * | Reference ( ArgumentList? )  
	 * | unop Expression  
	 * | Expression binop Expression  
	 * | ( Expression )  
	 * | num | true | false  
	 * | new ( id() | int [ Expression ] | id [ Expression ] ) 
	 */
	
	//OPERATOR PRECEDENCE HANDLING, Disjunction First
	private Expression parseExpression() throws SyntaxError{
		Expression e1 = parseConj();
		Operator op;
		while (_currentToken.getTokenText().equals("||")) {
			op = new Operator(_currentToken);
			accept(OPERATOR);
			Expression e2 = parseConj();
			e1 = new BinaryExpr(op, e1, e2, null);
		}
		return e1;
	}
	
	private Expression parseConj() throws SyntaxError{
		Expression e3 = parseEquality();
		Operator op1;
		while (_currentToken.getTokenText().equals("&&")) {
			op1 = new Operator(_currentToken);
			accept(OPERATOR);
			Expression e4 = parseEquality();
			e3 = new BinaryExpr(op1, e3, e4, null);
		}
		return e3;
	}
	
	private Expression parseEquality() throws SyntaxError{
		Expression e5 = parseRelational();
		Operator op2;
		while (_currentToken.getTokenText().equals("==") || _currentToken.getTokenText().equals("!=")) {
			op2 = new Operator(_currentToken);
			accept(OPERATOR);
			Expression e6 = parseRelational();
			e5 = new BinaryExpr(op2, e5, e6, null);
		}
		return e5;
	}
	
	private Expression parseRelational() throws SyntaxError{
		Expression e7 = parseAdditive();
		Operator op3;
		while (_currentToken.getTokenText().equals("<=") || _currentToken.getTokenText().equals("<") || _currentToken.getTokenText().equals(">") || _currentToken.getTokenText().equals(">=")) {
			op3 = new Operator(_currentToken);
			accept(OPERATOR);
			Expression e8 = parseAdditive();
			e7 = new BinaryExpr(op3, e7, e8, null);
		}
		return e7;
	}
	
	private Expression parseAdditive() throws SyntaxError{
		Expression e9 = parseMultiplicative();
		Operator op4;
		while (_currentToken.getTokenText().equals("+") || _currentToken.getTokenText().equals("-")){
			op4 = new Operator(_currentToken);
			accept(OPERATOR);
			Expression e10 = parseMultiplicative();
			e9 = new BinaryExpr(op4, e9, e10, null);
		}
		return e9;
	}
	
	private Expression parseMultiplicative() throws SyntaxError{
		Expression e11 = parseUnary();
		Operator op5;
		while (_currentToken.getTokenText().equals("*") || _currentToken.getTokenText().equals("/")) {
			op5 = new Operator(_currentToken);
			//System.out.println(_currentToken.getTokenText());
			accept(OPERATOR);
			//System.out.println(_currentToken.getTokenText());
			Expression e12 = parseUnary();
			e11 = new BinaryExpr(op5, e11, e12, null);
		}
		return e11;
	}
	
	private Expression parseUnary() throws SyntaxError{
		Operator op6;
		if (_currentToken.getTokenText().equals("-") || _currentToken.getTokenText().equals("!")) {
			op6 = new Operator(_currentToken);
			accept(OPERATOR);
			return new UnaryExpr(op6, parseUnary(), null);
		}
		return parseE();
	}
	
	private Expression parseE() throws SyntaxError{
		if(_currentToken.getTokenType() == ID) {
		//starts with Reference
			Reference r = parseReference();
			if(_currentToken.getTokenType() == LBRACKET) {
				accept(LBRACKET);
				Expression e = parseExpression();
				accept(RBRACKET);
				return new IxExpr(r, e, null);
			} else if(_currentToken.getTokenType() == LPAREN) {
				accept(LPAREN);
				ExprList el = new ExprList();
				if(_currentToken.getTokenType() != RPAREN) {
					el = parseArgumentList();
				}
				//System.out.println("hello");
				accept(RPAREN);
				return new CallExpr(r, el, null);
			} else {
				return new RefExpr(r, null);
			}
		} else if(_currentToken.getTokenType() == THIS) {
			Reference r = parseReference();
			if(_currentToken.getTokenType() == LBRACKET) {
				accept(LBRACKET);
				Expression e = parseExpression();
				accept(RBRACKET);
				return new IxExpr(r, e, null);
			} else if(_currentToken.getTokenType() == LPAREN) {
				ExprList el = new ExprList();
				accept(LPAREN);
				if(_currentToken.getTokenType() != RPAREN) {
					el = parseArgumentList();
				}
				accept(RPAREN);
				return new CallExpr(r, el, null);
			} else {
				return new RefExpr(r, null);
			}
		} else if(_currentToken.getTokenType() == LPAREN) {
			
		//( Expression )
			accept(LPAREN);
			Expression e = parseExpression();
			accept(RPAREN);
			return e;
		} else if (_currentToken.getTokenType() == INTLITERAL) {
			
		//num
			Token t = _currentToken;
			accept(INTLITERAL);
			return new LiteralExpr(new IntLiteral(t), null);
		} else if(_currentToken.getTokenType() == TRUE) {
			
		//true
		//case TRUE:
			//System.out.println(_currentToken.getTokenType().toString());
			Token t = _currentToken;
			accept(TRUE);
			return new LiteralExpr(new BooleanLiteral(t), null);
		} else if(_currentToken.getTokenType() == FALSE) {
			
		//false
		//case FALSE:
			//System.out.println(_currentToken.getTokenType().toString());
			Token t = _currentToken;
			accept(FALSE);
			return new LiteralExpr(new BooleanLiteral(t), null);
		} else {
			
		//new (id() | int [ Expression ] | id [ Expression ])
		//default:
			//System.out.println(_currentToken.getTokenType().toString());
			accept(NEW);
			if(_currentToken.getTokenType() == INT) {
				accept(INT);
				accept(LBRACKET);
				Expression e = parseExpression();
				accept(RBRACKET);
				return new NewArrayExpr(new BaseType(TypeKind.INT, null), e, null);
			} else {
				Token t = _currentToken;
				accept(ID);
				if(_currentToken.getTokenType() == LPAREN) {
					accept(LPAREN);
					accept(RPAREN);
					return new NewObjectExpr(new ClassType(new Identifier(t), null), null);
				} else {
					accept(LBRACKET);
					Expression e = parseExpression();
					accept(RBRACKET);
					return new NewArrayExpr(new ClassType(new Identifier(t), null), e, null);
				}
			}
		}
	}
	
	// This method will accept the token and retrieve the next token.
	//  Can be useful if you want to error check and accept all-in-one.
	private void accept(TokenType expectedType) throws SyntaxError {
		//System.out.println(_currentToken.getTokenText());
		//System.out.println(expectedType);
		//System.out.println(_currentToken.getTokenText());

		if( _currentToken.getTokenType() == expectedType ) {
			//System.out.println(_currentToken.getTokenType().toString() + _currentToken.getTokenText() + "\n");
			_currentToken = _scanner.scan();
			//System.out.println(_currentToken.getTokenType().toString() + _currentToken.getTokenText() + "\n");
			return;
		}
		
		// TODO: Report an error here.
		//  "Expected token X, but got Y"
		_errors.reportError("Expected token " + expectedType + ", but got " + _currentToken.getTokenType());
		throw new SyntaxError();
	}
}
