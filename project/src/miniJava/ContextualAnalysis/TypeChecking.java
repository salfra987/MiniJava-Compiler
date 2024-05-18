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
		if(decl.type instanceof ClassType && ((ClassType)decl.type).className.spelling.equals("String")){
			reportTypeError(decl, "String is an unsupported type");
			return new BaseType(miniJava.AbstractSyntaxTrees.TypeKind.UNSUPPORTED, decl.posn);}
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

		if (!checkAssignmentCompatibility(declTD, expTD)) {
			//System.out.println(declTD.toString() + " " + expTD.toString());
			reportTypeError(stmt, "Incompatible types in assignment statement");
		}
		
		checkTypeDenoter(stmt, declTD, expTD);
		return null;
	}

	private boolean checkAssignmentCompatibility(TypeDenoter varType, TypeDenoter expType) {
		// Check if the types are exactly the same
		if (varType.equals(expType)) {
			return true;
		}
		
		// Check if both types are class types
		if (varType instanceof ClassType && expType instanceof ClassType) {
			ClassType varClassType = (ClassType) varType;
			ClassType expClassType = (ClassType) expType;
			
			// Check if the classes are exactly the same
			if (varClassType.className.spelling.equals(expClassType.className.spelling)) {
				return true;
			}
		}
		
		if (varType instanceof ClassType && expType == null) {
			// classtype and null type are compatible
			return true;
		} 
		
		if (varType instanceof BaseType && expType instanceof BaseType) {
			BaseType varClassType = (BaseType) varType;
			BaseType expClassType = (BaseType) expType;
			
			// Check if the basetypes are exactly the same
			if (varClassType.typeKind.equals(expClassType.typeKind)) {
				return true;
			}
		}
		
		if (varType instanceof ArrayType && varType instanceof ArrayType) {
			ArrayType varClassType = (ArrayType) varType;
			ArrayType expClassType = (ArrayType) expType;
			
			// Check if the ArrayTypes are exactly the same
			if (varClassType.typeKind.equals(expClassType.typeKind)) {
				return true;
			}
		}
		
		// If none of the above conditions are met, the types are incompatible
		return false;
	}

    @Override
	public TypeDenoter visitAssignStmt(AssignStmt stmt, Object arg){
		if(stmt.ref.declaration == null){
			//System.out.println(stmt.ref);
			_errors.reportError("Error in assignment statement ");
			return new BaseType(miniJava.AbstractSyntaxTrees.TypeKind.ERROR, null);
		}
		if(stmt.ref.declaration.type instanceof BaseType || stmt.ref.declaration.type instanceof ClassType || stmt.ref.declaration.type instanceof ArrayType){
			TypeDenoter refTD = (TypeDenoter)stmt.ref.visit(this, null);
			TypeDenoter expTD = (TypeDenoter)stmt.val.visit(this, null);
			
			checkTypeDenoter(stmt, refTD, expTD);
		}else{
			expectedBaseOrClassType(stmt.ref);
		}
		
		return null;
	}

    @Override
	public TypeDenoter visitIxAssignStmt(IxAssignStmt stmt, Object arg){
		if(stmt.ref.declaration == null){
			return null;
		}
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

					TypeDenoter argType = passedArg.visit(this, null);
					if(!(argType instanceof BaseType)){
                	if ((((ClassType) argType).className.spelling).equals("String")) {
                    	reportTypeError(passedArg, "Unsupported type: String");
                	}}
                
                // Check the type compatibility of arguments
                checkTypeDenoter(passedArg, (TypeDenoter) param.visit(this, null), (TypeDenoter) passedArg.visit(this, null));
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
			if (md.type.typeKind == miniJava.AbstractSyntaxTrees.TypeKind.VOID) {
				reportTypeError(stmt, "Cannot return a value from a void method");
				return null;
			}

			TypeDenoter rTD = (TypeDenoter)stmt.returnExpr.visit(this, null);
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
			case "==":
        	case "!=":
            		if ((left instanceof ClassType || right instanceof ClassType) && !left.equals(right)) {
                		reportTypeError(expr, "Cannot compare objects of different types using == or !=");
            		}

			case ">":
			case "<":
			case ">=":
			case "<=":
				checkTypeKind(expr.left, miniJava.AbstractSyntaxTrees.TypeKind.INT, left.typeKind);
				checkTypeKind(expr.right, miniJava.AbstractSyntaxTrees.TypeKind.INT, right.typeKind);
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
		else{
			reportTypeError(expr,"String is an UNSUPPORTED type");
			return new BaseType(miniJava.AbstractSyntaxTrees.TypeKind.UNSUPPORTED, expr.posn);}
	}

    @Override
	public TypeDenoter visitNewArrayExpr(NewArrayExpr expr, Object arg){
		checkTypeDenoter(expr.sizeExpr, new BaseType(miniJava.AbstractSyntaxTrees.TypeKind.INT, null), (TypeDenoter)expr.sizeExpr.visit(this, null));
		return new ArrayType(expr.eltType, expr.posn);
	}

    @Override
	public TypeDenoter visitThisRef(ThisRef ref, Object arg){
		if(ref.declaration == null){
			return null;
		}
		return ref.declaration.type;
	}

    @Override
	public TypeDenoter visitIdRef(IdRef ref, Object arg){
		if(ref.declaration == null && !(ref.id.spelling.equals("null"))){
			reportTypeError(ref, "Identifier not declared");
			return new BaseType(miniJava.AbstractSyntaxTrees.TypeKind.ERROR, null);
		}
		if (ref.id.spelling.equals("null")){
			return null;
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
			reportTypeError(ast, "(CHECKTYPEKIND)Expected: " + wanted + ", instead found " + found);
			return false;
		}
		return true;
	}

    private void checkTypeDenoter(AST ast, TypeDenoter wanted, TypeDenoter found){
		if(!(wanted.equals(found)) && !(wanted instanceof ClassType && found == null)){
			if(!(wanted.toString().equals(found.toString()))){
			reportTypeError(ast, "(CHECKTYPEDENOTER)Expected: " + wanted + ", instead found " + found);}
		}
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