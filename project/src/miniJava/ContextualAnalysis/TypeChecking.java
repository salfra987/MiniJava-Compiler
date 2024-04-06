package miniJava.ContextualAnalysis;

import javax.lang.model.type.TypeKind;

import miniJava.ErrorReporter;
import miniJava.AbstractSyntaxTrees.*;
import miniJava.AbstractSyntaxTrees.BaseType;
import miniJava.AbstractSyntaxTrees.Package;
import miniJava.ContextualAnalysis.Identification.IdentificationError;
import miniJava.SyntacticAnalyzer.SourcePosition;

public class TypeChecking implements Visitor<Object, TypeDenoter> {
	private ErrorReporter _errors;
	
	public TypeChecking(ErrorReporter errors) {
		this._errors = errors;
	}
	
	public void parse(Package prog) {
		try {
			visitPackage(prog,null);
		} catch( IdentificationError e ) {
			_errors.reportError(e.toString());
		}
	}

    @Override
    public TypeDenoter visitPackage(Package prog, Object arg){
		prog.classDeclList.forEach(cd -> cd.visit(this, null));
		return null;
	}

    @Override
	public TypeDenoter visitClassDecl(ClassDecl cd, Object arg){
		cd.fieldDeclList.forEach(fd -> fd.visit(this, null));
		cd.methodDeclList.forEach(md -> md.visit(this, null));
		return null;
	}

    @Override
	public TypeDenoter visitFieldDecl(FieldDecl fd, Object arg){
		return fd.type;
	}

    @Override
	public TypeDenoter visitMethodDecl(MethodDecl md, Object arg){
		md.statementList.forEach(sl -> sl.visit(this, md));
		return md.type;
	}

    @Override
	public TypeDenoter visitParameterDecl(ParameterDecl pd, Object arg){
		return pd.type;
	}

    @Override
	public TypeDenoter visitVarDecl(VarDecl decl, Object arg){
		if(decl.type instanceof ClassType && ((ClassType)decl.type).className.spelling.equals("String"))
			return new BaseType(miniJava.AbstractSyntaxTrees.TypeKind.ERROR, decl.posn);
		else
			return decl.type;
	}

    @Override
	public TypeDenoter visitBaseType(BaseType type, Object arg){
		return null;
	}

    @Override
	public TypeDenoter visitClassType(ClassType type, Object arg){
		return null;
	}

    @Override
	public TypeDenoter visitArrayType(ArrayType type, Object arg){
		return null;
	}

    @Override
	public TypeDenoter visitBlockStmt(BlockStmt stmt, Object arg){
		MethodDecl md = (MethodDecl)arg;
		stmt.sl.forEach(s -> s.visit(this, md));
		return null;
	}

    @Override
	public TypeDenoter visitVardeclStmt(VarDeclStmt stmt, Object arg){
		TypeDenoter declTD = (TypeDenoter)stmt.varDecl.visit(this, null);
		TypeDenoter	expTD = (TypeDenoter)stmt.initExp.visit(this, null);
		//System.out.println("hello");
		checkTypeDenoter(stmt, declTD, expTD);
		return null;
	}

    @Override
	public TypeDenoter visitAssignStmt(AssignStmt stmt, Object arg){
		if(stmt.ref.declaration == null){
			_errors.reportError("Error in assignment statement ");
			return new BaseType(miniJava.AbstractSyntaxTrees.TypeKind.ERROR, null);
		}
		if(stmt.ref.declaration.type instanceof BaseType || stmt.ref.declaration.type instanceof ClassType){
			TypeDenoter refTD = (TypeDenoter)stmt.ref.visit(this, null);
			TypeDenoter expTD = (TypeDenoter)stmt.val.visit(this, null);
			
			//System.out.println("hello2");
			checkTypeDenoter(stmt, refTD, expTD);
		}else{
			expectedBaseOrClassType(stmt.ref);
		}
		
		return null;
	}

    @Override
	public TypeDenoter visitIxAssignStmt(IxAssignStmt stmt, Object arg){
		if(!(stmt.ref.declaration.type instanceof ArrayType)){
			expectedArrayType(stmt.ref);
			return null;
		}
		
		TypeDenoter ixTD = (TypeDenoter)stmt.ix.visit(this, null);
		checkTypeKind(stmt, ixTD.typeKind, miniJava.AbstractSyntaxTrees.TypeKind.INT);
		
		TypeDenoter expTD = (TypeDenoter)stmt.exp.visit(this, null);
		//System.out.println("hello3");
		checkTypeDenoter(expTD, ((ArrayType)stmt.ref.declaration.type).eltType, expTD);
		
		return null;
	}

    @Override
    public TypeDenoter visitCallStmt(CallStmt stmt, Object arg){
		if(stmt.methodRef.declaration instanceof MethodDecl){
			MethodDecl md = (MethodDecl)stmt.methodRef.declaration;
			
			if(md.parameterDeclList.size() != stmt.argList.size()){
				expectedArgs(101010, md, stmt.argList.size());
			}else{				
				for(int i = 0; i < md.parameterDeclList.size(); i++){
					Expression passedArg = stmt.argList.get(i);
					ParameterDecl param = md.parameterDeclList.get(i);
					
					//System.out.println("hello4");
					checkTypeDenoter(passedArg, (TypeDenoter)param.visit(this, null), (TypeDenoter)passedArg.visit(this, null));
				}
			}
		}else{
			expectedMethod(101010);
		}
		
		return null;
	}

    @Override
	public TypeDenoter visitReturnStmt(ReturnStmt stmt, Object arg){
		MethodDecl md = (MethodDecl)arg;
		
		if(stmt.returnExpr != null) {
			TypeDenoter rTD = (TypeDenoter)stmt.returnExpr.visit(this, null);
			//System.out.println("hello5");
			checkTypeDenoter(stmt.returnExpr, md.type, rTD);
		}else
			checkTypeKind(stmt, md.type.typeKind, miniJava.AbstractSyntaxTrees.TypeKind.VOID);
		
		return null;
	}

    @Override
	public TypeDenoter visitIfStmt(IfStmt stmt, Object arg){
		MethodDecl md = (MethodDecl)arg;
		
		TypeDenoter condTD = (TypeDenoter)stmt.cond.visit(this, null);
		checkTypeKind(stmt.cond, miniJava.AbstractSyntaxTrees.TypeKind.BOOLEAN, condTD.typeKind);
		
		stmt.thenStmt.visit(this, md);
		
		if(stmt.elseStmt != null)
			stmt.elseStmt.visit(this, md);
		
		return null;
	}

    @Override
	public TypeDenoter visitWhileStmt(WhileStmt stmt, Object arg){
		MethodDecl md = (MethodDecl)arg;
		
		TypeDenoter condTD = (TypeDenoter)stmt.cond.visit(this, null);
		checkTypeKind(stmt.cond, miniJava.AbstractSyntaxTrees.TypeKind.BOOLEAN, condTD.typeKind);

		stmt.body.visit(this, md);
		
		return null;
	}

    @Override
	public TypeDenoter visitUnaryExpr(UnaryExpr expr, Object arg){
		TypeDenoter e = (TypeDenoter)expr.expr.visit(this, null);
		
		switch(expr.operator.spelling){
			case "!":
				checkTypeKind(e, miniJava.AbstractSyntaxTrees.TypeKind.BOOLEAN, e.typeKind);
				return new BaseType(miniJava.AbstractSyntaxTrees.TypeKind.BOOLEAN, expr.posn);
			case "-":
				checkTypeKind(e, miniJava.AbstractSyntaxTrees.TypeKind.INT, e.typeKind);
				return new BaseType(miniJava.AbstractSyntaxTrees.TypeKind.INT, expr.posn);
				
			default:
				expectedValidOperator(expr.operator);
				return new BaseType(miniJava.AbstractSyntaxTrees.TypeKind.ERROR, expr.posn);
		}
	}

    @Override
    public TypeDenoter visitBinaryExpr(BinaryExpr expr, Object arg){
		TypeDenoter left = (TypeDenoter)expr.left.visit(this, null);
		TypeDenoter right = (TypeDenoter)expr.right.visit(this, null);
		
		switch(expr.operator.spelling){
			case ">":
			case "<":
			case ">=":
			case "<=":
			case "!=":
				checkTypeKind(expr.left, miniJava.AbstractSyntaxTrees.TypeKind.INT, left.typeKind);
				checkTypeKind(expr.right, miniJava.AbstractSyntaxTrees.TypeKind.INT, right.typeKind);
				return new BaseType(miniJava.AbstractSyntaxTrees.TypeKind.BOOLEAN, expr.posn);
				
			case "==":
				//System.out.println("hello6");
				checkTypeDenoter(expr, left, right);
				return new BaseType(miniJava.AbstractSyntaxTrees.TypeKind.BOOLEAN, expr.posn);
				
			case "&&":
			case "||":
				checkTypeKind(expr.left, miniJava.AbstractSyntaxTrees.TypeKind.BOOLEAN, left.typeKind);
				checkTypeKind(expr.right, miniJava.AbstractSyntaxTrees.TypeKind.BOOLEAN, right.typeKind);
				return new BaseType(miniJava.AbstractSyntaxTrees.TypeKind.BOOLEAN, expr.posn);
			
			case "+":
			case "-":
			case "*":
			case "/":
				checkTypeKind(expr.left, miniJava.AbstractSyntaxTrees.TypeKind.INT, left.typeKind);
				checkTypeKind(expr.right, miniJava.AbstractSyntaxTrees.TypeKind.INT, right.typeKind);
				return new BaseType(miniJava.AbstractSyntaxTrees.TypeKind.INT, expr.posn);
			
			default:
				expectedValidOperator(expr.operator);
				return new BaseType(miniJava.AbstractSyntaxTrees.TypeKind.ERROR, expr.posn);
		}
	}

    @Override
	public TypeDenoter visitRefExpr(RefExpr expr, Object arg){
		return expr.ref.visit(this, null);
	}

    @Override
	public TypeDenoter visitIxExpr(IxExpr expr, Object arg){
		TypeDenoter refType = (TypeDenoter)expr.ref.visit(this, null);
		
		if(!(refType instanceof ArrayType)){
			expectedArrayType(expr.ref);
			return new BaseType(miniJava.AbstractSyntaxTrees.TypeKind.ERROR, expr.posn);
		}
		
		ArrayType at = (ArrayType)refType;
		
		checkTypeKind(expr.ixExpr, ((TypeDenoter)expr.ixExpr.visit(this, null)).typeKind, miniJava.AbstractSyntaxTrees.TypeKind.INT);
		
		return at.eltType;
	}

    @Override
	public TypeDenoter visitCallExpr(CallExpr expr, Object arg){
		if(expr.functionRef.declaration instanceof MethodDecl){
			MethodDecl md = (MethodDecl)expr.functionRef.declaration;
			
			if(md.parameterDeclList.size() != expr.argList.size()){
				expectedArgs(101010, md, expr.argList.size());
			}else{				
				for(int i = 0; i < md.parameterDeclList.size(); i++){
					Expression passedArg = expr.argList.get(i);
					ParameterDecl param = md.parameterDeclList.get(i);
					
					//System.out.println("hello7");
					checkTypeDenoter(passedArg, (TypeDenoter)param.visit(this, null), (TypeDenoter)passedArg.visit(this, null));
				}
				
				return md.type;
			}
		}else{
			expectedMethod(101010);
		}
		
		return new BaseType(miniJava.AbstractSyntaxTrees.TypeKind.ERROR, expr.posn);
	}

    @Override
	public TypeDenoter visitLiteralExpr(LiteralExpr expr, Object arg){
		return expr.lit.visit(this, null);
	}

    @Override
	public TypeDenoter visitNewObjectExpr(NewObjectExpr expr, Object arg){
		if(!expr.classtype.className.spelling.equals("String"))
			return expr.classtype;
		else
			return new BaseType(miniJava.AbstractSyntaxTrees.TypeKind.ERROR, expr.posn);
	}

    @Override
	public TypeDenoter visitNewArrayExpr(NewArrayExpr expr, Object arg){
		//System.out.println("hello8");
		checkTypeDenoter(expr.sizeExpr, new BaseType(miniJava.AbstractSyntaxTrees.TypeKind.INT, null), (TypeDenoter)expr.sizeExpr.visit(this, null));
		return new ArrayType(expr.eltType, expr.posn);
	}

    @Override
	public TypeDenoter visitThisRef(ThisRef ref, Object arg){
		return ref.declaration.type;
	}

    @Override
	public TypeDenoter visitIdRef(IdRef ref, Object arg){
		if(ref.declaration == null){
			reportTypeError(ref, "Identifier not declared");
			return new BaseType(miniJava.AbstractSyntaxTrees.TypeKind.ERROR, null);
		}
		return ref.declaration.type;
	}

    @Override
	public TypeDenoter visitQRef(QualRef ref, Object arg){
		if(ref.declaration == null){
			reportTypeError(ref, "Qref does not exist");
			return new BaseType(miniJava.AbstractSyntaxTrees.TypeKind.ERROR, null);
		}
		return ref.declaration.type;
	}

    @Override
	public TypeDenoter visitIdentifier(Identifier id, Object arg){
		return id.decl.visit(this, null);
	}

    @Override
	public TypeDenoter visitOperator(Operator op, Object arg){
		return null;
	}

    @Override
	public TypeDenoter visitIntLiteral(IntLiteral num, Object arg){
		return new BaseType(miniJava.AbstractSyntaxTrees.TypeKind.INT, num.posn);
	}

    @Override
	public TypeDenoter visitBooleanLiteral(BooleanLiteral bool, Object arg){
		return new BaseType(miniJava.AbstractSyntaxTrees.TypeKind.BOOLEAN, bool.posn);
	}

    @Override
	public TypeDenoter visitNullLiteral(NullLiteral nil, Object arg){
		return new BaseType(miniJava.AbstractSyntaxTrees.TypeKind.CLASS, nil.posn);
	}

    //---------------HELPER METHODS---------------------

    private boolean checkTypeKind(AST ast, miniJava.AbstractSyntaxTrees.TypeKind wanted, miniJava.AbstractSyntaxTrees.TypeKind found){
		if(wanted != found){
			//System.out.println("hello10");
			reportTypeError(ast, "Expected: " + wanted + ", instead found " + found);
			return false;
		}
		return true;
	}

    private void checkTypeDenoter(AST ast, TypeDenoter wanted, TypeDenoter found){
		if(wanted.equals(found))
		//System.out.println("hello9");
			reportTypeError(ast, "Expected: " + wanted + ", instead found " + found);
	}

    private void expectedMethod(int lineNum){
		reportTypeError(null, "Line number " + lineNum + " needs a method and does not have one.");
	}

    private void expectedArgs(int lineNum, MethodDecl md, int attemptedNumOfArgs){
		if(md == null){
			_errors.reportError("Error in expected args ");
		} else {
		reportTypeError(null, "Line number " + lineNum + " tried to call " + md.name + " with " + attemptedNumOfArgs + " arguments, but it needed " + md.parameterDeclList.size());}
	}

    private void expectedArrayType(Reference ref){
		if(ref.declaration == null){
			_errors.reportError("Error in expected arraytype ");
		} else {
		reportTypeError(null, "Just referenced " + ref.declaration.name + " but meant to reference an array, which this is not.");}
	}

    private void expectedBaseOrClassType(Reference ref){
		if(ref.declaration == null){
			_errors.reportError("Error in expected baseclassorclasstype");
		} else {
		reportTypeError(null, "Just Referenced " + ref.declaration.name + " is not a base type or class type, which is what you meant to reference.");}
	}

    private void expectedValidOperator(Operator operator){
		if(operator == null){
			_errors.reportError("Error in expected operator ");
		} else {
		reportTypeError(null, "Unexpected operator " + operator.spelling + " used.");}
	}

	private void reportTypeError(AST ast, String errMsg) {
		_errors.reportError(errMsg);
	}
}