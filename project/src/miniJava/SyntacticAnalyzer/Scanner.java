package miniJava.SyntacticAnalyzer;

import static miniJava.SyntacticAnalyzer.TokenType.*;
import java.io.IOException;
import java.io.InputStream;
import miniJava.ErrorReporter;

public class Scanner {
	private InputStream _in;
	private ErrorReporter _errors;
	private StringBuilder _currentText;
	private char _currentChar;
	private boolean eot;
	
	public Scanner( InputStream in, ErrorReporter errors ) {
		this._in = in;
		this._errors = errors;
		this._currentText = new StringBuilder();
		eot = false;
		
		nextChar();
	}
	
	public Token scan() {
		_currentText.setLength(0);
		//System.out.println("Scan was called");
		// TODO: This function should check the current char to determine what the token could be.
		
		// TODO: Consider what happens if the current char is whitespace
		
		// TODO: Consider what happens if there is a comment (// or /* */)
		
		// TODO: What happens if there are no more tokens?
		
		// TODO: Determine what the token is. For example, if it is a number
		//  keep calling takeIt() until _currentChar is not a number. Then
		//  create the token via makeToken(TokenType.IntegerLiteral) and return it.
		while (!eot && isWhite(_currentChar)) {
			//System.out.println("White While loop" + _currentChar);
			nextChar();
		}
		
		if(eot) {
			return new Token(EOT, "");
		}
		
		if(_currentChar =='/') {
			nextChar();
			
			if(_currentChar == '/') {
				nextChar();
				
				while(!eot && _currentChar != '\n') {
					nextChar();
				}
				
				return scan();
			} else if(_currentChar == '*') {
				nextChar();
				
				while(!eot) {
					if(_currentChar == '*') {
						nextChar();
						if(_currentChar == '/') {
							nextChar();
							return scan();
						}
					} else {
						nextChar();
					}
				}
			} else {
				nextChar();
				return new Token(OPERATOR, "/");
			}
		}
		
		if(isLetter(_currentChar)) {
			takeIt();
			while(isValidIdent(_currentChar)) {
				takeIt();
			}
			
			switch(_currentText.toString()){
				case "class":
					return new Token(CLASS, "class");
				case "void":
					return new Token(VOID, "void");
				case "public":
					return new Token(PUBLIC, "public");
				case "private":
					return new Token(PRIVATE, "private");
				case "static":
					return new Token(STATIC, "static");
				case "int":
					return new Token(INT, "int");
				case "boolean":
					return new Token(BOOLEAN, "boolean");
				case "this":
					return new Token(THIS, "this");
				case "return":
					return new Token(RETURN, "return");
				case "if":
					return new Token(IF, "if");
				case "else":
					return new Token(ELSE, "else");
				case "while":
					return new Token(WHILE, "while");
				case "true":
					return new Token(TRUE, "true");
				case "false":
					return new Token(FALSE, "false");
				case "new":
					return new Token(NEW, "new");
				default:
					return new Token(ID, _currentText.toString());
			}
		} else if(isDigit(_currentChar)) {
			takeIt();
			while(isDigit(_currentChar)) {
				takeIt();
			}
			
			return new Token(INTLITERAL, _currentText.toString());
		} else {
			switch(_currentChar) {
			case '{':
				takeIt();
				return new Token(LBRACE, "{");
			case '(':
				takeIt();
				return new Token(LPAREN, "(");
			case ')':
				takeIt();
				return new Token(RPAREN, ")");
			case '}':
				takeIt();
				return new Token(RBRACE, "}");
			case ';':
				takeIt();
				return new Token(SEMI, ";");
			case '[':
				takeIt();
				return new Token(LBRACKET, "[");
			case ']':
				takeIt();
				return new Token(RBRACKET, "]");
			case ',':
				takeIt();
				return new Token(COMMA, ",");
			case '.':
				takeIt();
				return new Token(DOT, ".");
			case '=':
				takeIt();
				if(_currentChar == '='){
					takeIt();
					return new Token(OPERATOR, "==");
				} else {
					return new Token(EQUALS, "=");
				}
			case '>':
				takeIt();
				if(_currentChar == '='){
					takeIt();
					return new Token(OPERATOR, ">=");
				} else {
					return new Token(OPERATOR, ">");
				}
			case '<':
				takeIt();
				if(_currentChar == '='){
					takeIt();
					return new Token(OPERATOR, "<=");
				} else {
					return new Token(OPERATOR, "<");
				}
			case '!':
				takeIt();
				if(_currentChar == '=') {
					takeIt();
					return new Token(OPERATOR, "!=");
				} else {
					return new Token(OPERATOR, "!");
				}
			case '&':
				takeIt();
				if(_currentChar == '&') {
					takeIt();
					return new Token(OPERATOR, "&&");
				} else {
					return new Token(GARBAGETOKEN, "&");
				}
			case '|':
				takeIt();
				if(_currentChar == '|') {
					takeIt();
					return new Token(OPERATOR, "||");
				} else {
					return new Token(GARBAGETOKEN, "|");
				}
			case '+':
				takeIt();
				return new Token(OPERATOR, "+");
			case '-':
				takeIt();
				return new Token(OPERATOR, "-");
			case '*':
				takeIt();
				return new Token(OPERATOR, "*");
			case '_':
				takeIt();
				return new Token(GARBAGETOKEN, "_");
			}
		}
		takeIt();
		return new Token(GARBAGETOKEN, _currentText.toString());
	}
	
	private boolean isDigit(char c) {
		//System.out.println("isDigit() called");
		if (c == '0' || c == '1' || c == '2' || c == '3' || c == '4' || c == '5' || c == '6' || c == '7' || c == '8' || c == '9') {
			//System.out.println("if is true for digit");
			return true;
		}
		return false;
	}
	
	private boolean isValidIdent(char c) {
		//System.out.println("isValidIdent() called");
		if (isLetter(c) || isDigit(c) || c == '_') {
			//System.out.println("if is true for ident");
			return true;
		}
		return false;
	}
	
	private boolean isLetter(char c) {
		//System.out.println("isLetter() called");
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
	}
	
	private boolean isWhite(char c) {
		return (c == ' ' || c == '\t' || c == '\n' || c == '\r');
	}
	
	private void takeIt() {
		//System.out.println("takeIt() called");
		_currentText.append(_currentChar);
		nextChar();
	}
	
	private void nextChar() {
		//System.out.println("nextChar() called");
		try {
			int c = _in.read();
			
			if(c == -1) {
				eot = true;
			}
			
			_currentChar = (char)c;
			//System.out.println(_currentChar);
			
			// TODO: What happens if c == -1?
			
			// TODO: What happens if c is not a regular ASCII character?
			
		} catch( IOException e ) {
			// TODO: Report an error here
			e.printStackTrace();
			System.exit(1);
		}
	}
}
