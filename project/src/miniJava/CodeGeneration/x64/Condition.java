package miniJava.CodeGeneration.x64;

import miniJava.AbstractSyntaxTrees.Operator;

public enum Condition {
	E,
	NE,
	LT,
	LTE,
	GT,
	GTE;
	
	public static Condition getOppositeCond(Operator op) {
		switch( op.spelling ) {
		case ">": return Condition.LTE;
		case ">=": return Condition.LT;
		case "<": return Condition.GTE;
		case "<=": return Condition.GT;
		case "==": return Condition.NE;
		case "!=": return Condition.E;
		};
		
		return null;
	}
	
	public static Condition getCond(Operator op) {
		switch( op.spelling ) {
		case ">": return Condition.GT;
		case ">=": return Condition.GTE;
		case "<": return Condition.LT;
		case "<=": return Condition.LTE;
		case "==": return Condition.E;
		case "!=": return Condition.NE;
		};
		
		return null;
	}
}
