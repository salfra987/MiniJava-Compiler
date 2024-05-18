package miniJava.ContextualAnalysis;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import miniJava.ErrorReporter;
import miniJava.AbstractSyntaxTrees.*;
import miniJava.ContextualAnalysis.Identification.IdentificationError;
import miniJava.SyntacticAnalyzer.Token;
import miniJava.SyntacticAnalyzer.TokenType;


public class IDTable {
    public static Stack<HashMap<String, Declaration>> table = new Stack<>();
    private ErrorReporter reporter;
    public static int scoper = 0;


    public IDTable(){
        openScope();
    }


    public static void openScope(){
        table.add(new HashMap<>());
        scoper += 1;
    }


    public static void closeScope(){
        table.pop();
        scoper--;
    }


    public static int addDeclaration(Declaration decl){
        //System.out.println(decl.name);
        //System.out.println(decl.toString());

        for(int i = table.size() - 1; i >= 0; i--){
            if(table.get(i).containsKey(decl.name) && (decl.toString().equals(table.get(i).get(decl.name).toString()) || isVarButAlsoAlreadyParam(decl, i) || isMethodButAlsoAlreadyField(decl, i))){
                //System.out.println(decl.name);
                //System.out.println(decl.toString());
                //System.out.println(table.get(i).get(decl.name).toString());
                //System.out.println(isVarButAlsoAlreadyParam(decl, i));
                //System.out.println(isOtherMember(decl, i));
                return 0;
            }
        }
        table.peek().put(decl.name, decl);
        return 1;
    }

    //private static boolean isOtherMember(Declaration decl, int i){
        //if(!table.get(i).get(decl.name).toString().equals("ParameterDecl")){
         //   return true;
        //}
        //return false;
    //}

    private static boolean isVarButAlsoAlreadyParam(Declaration decl, int i){
        if(table.get(i).get(decl.name).toString().equals("ParameterDecl") && decl.toString().equals("VarDecl")){
            return true;
        }
        return false;
    }

    private static boolean isMethodButAlsoAlreadyField(Declaration decl, int i){
        if(table.get(i).get(decl.name).toString().equals("FieldDecl") && decl.toString().equals("MethodDecl")){
            return true;
        }
        return false;
    }


    public static Declaration findDeclaration(Identifier id){
        //System.out.println(table.peek().get(id.spelling));
        //System.out.println(table.peek().get(id.spelling));
        //System.out.println(id.spelling);

        //System.out.println("loop time");
        for(int i = table.size() - 1; i >= 0; i--){
            Declaration decl = table.get(i).get(id.spelling);
            if(decl != null){
                return decl;
            }
        }
            return null;
    }

    public static Declaration findDeclaration(Identifier id, String lookingFor){
        for(int i = table.size() - 1; i >= 0; i--){
            Declaration decl = table.get(i).get(id.spelling);
            if(decl != null && decl.toString().equals(lookingFor)){
                return decl;
            }
        }
            return null;
    }

    public static int findDeclarationsLocation(){
        return scoper;
    }

    public static boolean scopeContainsIfStatement(int scopeIndex) {
        HashMap<String, Declaration> scope = table.get(scopeIndex);
        for (Declaration decl : scope.values()) {
            if (decl instanceof MethodDecl) {
                MethodDecl methodDecl = (MethodDecl) decl;
                if (methodDecl.statementList != null) {
                    for (Statement stmt : methodDecl.statementList) {
                        if (stmt instanceof IfStmt) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    public static boolean currentScopeContainsIfStatement() {
        for (HashMap<String, Declaration> scope : table) {
            for (Declaration decl : scope.values()) {
                if (decl instanceof MethodDecl) {
                    MethodDecl methodDecl = (MethodDecl) decl;
                    if (methodDecl.statementList != null) {
                        for (Statement stmt : methodDecl.statementList) {
                            if (stmt instanceof IfStmt) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public static boolean scopeContainsWhileStatement(int scopeIndex) {
        HashMap<String, Declaration> scope = table.get(scopeIndex);
        for (Declaration decl : scope.values()) {
            if (decl instanceof MethodDecl) {
                MethodDecl methodDecl = (MethodDecl) decl;
                if (methodDecl.statementList != null) {
                    for (Statement stmt : methodDecl.statementList) {
                        if (stmt instanceof WhileStmt) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    public static boolean currentScopeContainsWhileStatement() {
        for (HashMap<String, Declaration> scope : table) {
            for (Declaration decl : scope.values()) {
                if (decl instanceof MethodDecl) {
                    MethodDecl methodDecl = (MethodDecl) decl;
                    if (methodDecl.statementList != null) {
                        for (Statement stmt : methodDecl.statementList) {
                            if (stmt instanceof WhileStmt) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
}