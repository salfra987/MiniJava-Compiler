package miniJava.ContextualAnalysis;

import static miniJava.SyntacticAnalyzer.TokenType.ID;

import java.lang.System;
import java.lang.reflect.Method;

import miniJava.ErrorReporter;
import miniJava.AbstractSyntaxTrees.Package;
import miniJava.SyntacticAnalyzer.Token;
import miniJava.SyntacticAnalyzer.TokenType;
import miniJava.AbstractSyntaxTrees.*;

public class Identification implements Visitor<Object,Object> {
	private ErrorReporter _errors;
    
	public Identification(Package ast, ErrorReporter errors) {
		this._errors = errors;
		// TODO: predefined names
        //string
        IDTable.openScope();

        //printStream
        MethodDeclList printStreamMDL = new MethodDeclList();
        ParameterDeclList printStreamPDL = new ParameterDeclList();
        printStreamPDL.add(new ParameterDecl(new BaseType(TypeKind.INT, null), "n", null));
        printStreamMDL.add(new MethodDecl(new FieldDecl(false, false, new BaseType(TypeKind.VOID, null), "println", null), printStreamPDL, new StatementList(), null));
        if(IDTable.addDeclaration(new ClassDecl("_PrintStream", new FieldDeclList(), printStreamMDL, null)) == 0){
            throw new IdentificationError(ast, "You shouldn't recieve this error.");
        };

        //system
        Token sysTok = new Token(TokenType.ID, "_PrintStream");
        Identifier sysIden = new Identifier(sysTok);
        FieldDeclList sysFDL = new FieldDeclList();
        sysFDL.add(new FieldDecl(false, true, new ClassType(new Identifier(sysTok, IDTable.findDeclaration(sysIden)), null), "out", null));
        if(IDTable.addDeclaration(new ClassDecl("System", sysFDL, new MethodDeclList(), null)) == 0){
            throw new IdentificationError(ast, "You shouldn't recieve this error.");
        };

        //String
        if(IDTable.addDeclaration(new ClassDecl("String", new FieldDeclList(), new MethodDeclList(), null)) == 0){
            throw new IdentificationError(ast, "You shouldn't recieve this error.");
        };
	}
	
	public void parse( Package prog ) {
		try {
			visitPackage(prog,null);
		} catch( IdentificationError e ) {
			_errors.reportError(e.toString());
		}
	}
	
	public Object visitPackage(Package prog, Object arg) throws IdentificationError {
        IDTable.openScope();

        for(int i = 0; i < prog.classDeclList.size(); i++){
            int result = IDTable.addDeclaration(prog.classDeclList.get(i));
            if(result == 0){
                throw new IdentificationError(prog.classDeclList.get(i), "This class declaration already exists " + prog.classDeclList.get(i).name );
            }
        }

        prog.classDeclList.forEach(cd -> cd.visit(this, null));

        IDTable.closeScope();
        return null;
	}

    public Object visitClassDecl(ClassDecl clas, Object arg){
        if(arg == null){
            IDTable.openScope();
            for(int i = 0; i < clas.fieldDeclList.size(); i++){
                int result = IDTable.addDeclaration(clas.fieldDeclList.get(i));
                if(result == 0){
                    throw new IdentificationError(clas.fieldDeclList.get(i), "This field declaration already exists " + clas.fieldDeclList.get(i).name );
                }
            }
            for(int i = 0; i < clas.methodDeclList.size(); i++){
                int result = IDTable.addDeclaration(clas.methodDeclList.get(i));
                if(result == 0){
                    throw new IdentificationError(clas.methodDeclList.get(i), "This field declaration already exists " + clas.methodDeclList.get(i).name );
                }
            }

            clas.fieldDeclList.forEach(fd -> fd.visit(this, null));
            clas.methodDeclList.forEach(md -> md.visit(this, clas));
            IDTable.closeScope();
            return null;
        } else{
            Identifier want = (Identifier)arg;
            for(FieldDecl fd:clas.fieldDeclList){
                if(fd.name.equals(want.spelling)){
                    return fd;
                }
            }
            for(MethodDecl md:clas.methodDeclList){
                if(md.name.equals(want.spelling)){
                    return md;
                }
            }
            return null;
        }
    }

    public Object visitFieldDecl(FieldDecl fd, Object arg){
        fd.type.visit(this, null);
        return null;
    }

    public Object visitMethodDecl(MethodDecl md, Object arg){
        md.type.visit(this, null);
        ClassDecl myContext = (ClassDecl) arg;
        md.classContained = myContext;

        IDTable.openScope();
        md.parameterDeclList.forEach(pdl -> pdl.visit(this, null));
        IDTable.openScope();
        md.statementList.forEach(sl -> sl.visit(this, md));
        IDTable.closeScope();
        IDTable.closeScope();
        return null;
    }

    public Object visitParameterDecl(ParameterDecl pd, Object arg){
        pd.type.visit(this, null);
        if(IDTable.addDeclaration(pd) == 0){
            throw new IdentificationError(pd,"This parameter declaration already exists " + pd.name);
        };
        return null;
    }

    public Object visitVarDecl(VarDecl vd, Object arg){
        vd.type.visit(this, null);
        int scopeNum = IDTable.findDeclarationsLocation() - 3;
        //System.out.println(scopeNum);
        if(IDTable.scopeContainsIfStatement(scopeNum) || IDTable.scopeContainsWhileStatement(scopeNum)) {
            //System.out.println(vd.name);
            throw new IdentificationError(vd, "solitary variable declaration statement not permitted here");
        }
        if(IDTable.addDeclaration(vd) == 0){
            throw new IdentificationError(vd, "This variable declaration already exists " + vd.name);
        };
        return null;
    }

    public Object visitBaseType(BaseType type, Object arg){
        return null;
    }

    public Object visitClassType(ClassType type, Object arg){
        //System.out.println(IDTable.findDeclaration(type.className));
        //System.out.println(type.className.spelling);
        Declaration obj = IDTable.findDeclaration(type.className, "ClassDecl");
        //System.out.println(obj.toString());
        //System.out.println(type.className.spelling);
        if(obj == null){
            throw new IdentificationError(type, "Class does not exist");
        }
        //System.out.println(obj.toString());
        if(!obj.toString().equals("ClassDecl")){
            //System.out.println(obj.toString());
            //System.out.println(obj.toString().length());
            throw new IdentificationError(type, "The class type: " + type.className.spelling + " does not exist.");
        }
        return null;
    }
	
    public Object visitArrayType(ArrayType type, Object arg){
        type.eltType.visit(this, null);
        return null;
    }

    public Object visitBlockStmt(BlockStmt bs, Object arg){
        MethodDecl md = (MethodDecl)arg;
        IDTable.openScope();
        bs.sl.forEach(s -> s.visit(this, md));
        IDTable.closeScope();
        return null;
    }

    public Object visitVardeclStmt(VarDeclStmt vds, Object arg){
        MethodDecl md = (MethodDecl)arg;
        if(vds.initExp instanceof BinaryExpr){
            BinaryExpr binEx = ((BinaryExpr) vds.initExp);
            if(binEx.left instanceof RefExpr && binEx.right instanceof RefExpr){if(((IdRef) ((RefExpr) binEx.left).ref).id.spelling.equals(vds.varDecl.name) || ((IdRef) ((RefExpr) binEx.right).ref).id.spelling.equals(vds.varDecl.name)){
                throw new IdentificationError(vds, "This varDeclStmt references itself while being initialized");
            }}
        }

        if(vds.initExp instanceof RefExpr){
            RefExpr myExpr = ((RefExpr) vds.initExp);
            if(myExpr.ref instanceof IdRef){
                IdRef myID = ((IdRef) myExpr.ref);
                if(IDTable.findDeclaration(myID.id) != null){
                    if(!(IDTable.findDeclaration(myID.id) instanceof VarDecl) && !(IDTable.findDeclaration(myID.id) instanceof ParameterDecl) && !(IDTable.findDeclaration(myID.id) instanceof FieldDecl)){
                        throw new IdentificationError(vds, "reference " + myID.id.spelling + " is not a variable");
                    }
                }
            }
        }

        vds.varDecl.visit(this, null);
        vds.initExp.visit(this, md);
        return null;
    }

    public Object visitAssignStmt(AssignStmt as, Object arg){
        MethodDecl md = (MethodDecl)arg;
        as.ref.visit(this, md);
        as.val.visit(this, md);
        if(as.val instanceof RefExpr){
            RefExpr epx = ((RefExpr) as.val);
            if(epx.ref instanceof IdRef){
                IdRef iddd = ((IdRef) epx.ref);
                if(IDTable.findDeclaration(iddd.id) != null && IDTable.findDeclaration(iddd.id) instanceof MethodDecl){
                    throw new IdentificationError(as, "The right side of this assignment statement is an Identifier for a variable that does not exist.");
                }
                if(IDTable.findDeclaration(iddd.id) != null && !(IDTable.findDeclaration(iddd.id) instanceof VarDecl) && !(IDTable.findDeclaration(iddd.id) instanceof FieldDecl) && !(IDTable.findDeclaration(iddd.id) instanceof ParameterDecl)){
                    throw new IdentificationError(as, "The right side of this assignment statment is not a field or variable");
                }
            }
        }
        return null;
    }

    public Object visitIxAssignStmt(IxAssignStmt ias, Object arg){
        MethodDecl md = (MethodDecl)arg;
        ias.ref.visit(this,md);
        ias.ix.visit(this, md);
        ias.exp.visit(this, md);
        return null;
    }

    public Object visitCallStmt(CallStmt cs, Object arg){
        MethodDecl md = (MethodDecl)arg;
        cs.methodRef.visit(this, md);
        cs.argList.forEach(e -> e.visit(this, md));
        return null;
    }

    public Object visitReturnStmt(ReturnStmt rs, Object arg){
        MethodDecl md = (MethodDecl)arg;
        if(rs.returnExpr != null){
            rs.returnExpr.visit(this, md);
        }
        return null;
    }

    public Object visitIfStmt(IfStmt is, Object arg){
        MethodDecl md = (MethodDecl)arg;
        is.cond.visit(this, md);
        is.thenStmt.visit(this, md);

        if(is.elseStmt != null){
            is.elseStmt.visit(this, md);
        }
        return null;
    }

    public Object visitWhileStmt(WhileStmt ws, Object arg){
		MethodDecl md = (MethodDecl)arg;
		ws.cond.visit(this, md);
		ws.body.visit(this, md);
		return null;
	}

    public Object visitUnaryExpr(UnaryExpr ue, Object arg){
		MethodDecl md = (MethodDecl)arg;
		ue.operator.visit(this, null);
		ue.expr.visit(this, md);
		return null;
	}

    public Object visitBinaryExpr(BinaryExpr be, Object arg){
		MethodDecl md = (MethodDecl)arg;
		be.operator.visit(this, null);
		be.left.visit(this, md);
		be.right.visit(this, md);
		return null;
	}

    public Object visitRefExpr(RefExpr re, Object arg){
		MethodDecl md = (MethodDecl)arg;
		re.ref.visit(this, md);
		return null;
	}

    public Object visitIxExpr(IxExpr iex, Object arg){
		MethodDecl md = (MethodDecl)arg;
		iex.ref.visit(this, md);
		iex.ixExpr.visit(this, md);
		return null;
	}

    public Object visitCallExpr(CallExpr cex, Object arg){
		MethodDecl md = (MethodDecl)arg;
		cex.functionRef.visit(this, md);
		cex.argList.forEach(al -> al.visit(this, md));
		return null;
	}

    public Object visitLiteralExpr(LiteralExpr lex, Object arg){
		return null;
	}

    public Object visitNewObjectExpr(NewObjectExpr nox, Object arg){
		nox.classtype.visit(this, null);
		return null;
	}

    public Object visitNewArrayExpr(NewArrayExpr nax, Object arg){
		MethodDecl md = (MethodDecl)arg;
		nax.eltType.visit(this, null);
		nax.sizeExpr.visit(this, md);
		return null;
	}

    public Object visitThisRef(ThisRef thRef, Object arg){
		MethodDecl md = (MethodDecl)arg;
        if(md == null){
            throw new IdentificationError(md, "Method declaration does not exist");
        }
		if(md.isStatic){
			throw new IdentificationError(md, "method declaration is static");
        }
		thRef.declaration = md.classContained;
		return null;
	}

    public Object visitIdRef(IdRef iRef, Object arg){
		MethodDecl md = (MethodDecl)arg;
		Declaration decl = (Declaration)iRef.id.visit(this, md);

        if (!(decl instanceof VarDecl) && !(decl instanceof FieldDecl) && !(decl instanceof ParameterDecl) && !(iRef.id.spelling.equals("System")) && !(decl instanceof MethodDecl) && !(decl instanceof ClassDecl) && !(iRef.id.spelling.equals("null"))) {
            throw new IdentificationError(iRef, "Reference " + iRef.id.spelling + " cannot be resolved to a variable");
        }

		iRef.declaration = decl;
		return null;
	}

    public Object visitQRef(QualRef qre, Object arg){
		MethodDecl md = (MethodDecl)arg;
		qre.ref.visit(this, md);
		Declaration context = qre.ref.declaration;
		if(context == null){
			throw new IdentificationError(context, "qual ref context is null");
		}

        // if(IDTable.findDeclaration(qre.id) instanceof MethodDecl){
        //     throw new IdentificationError(qre, "referencing a method decl");
        // }

		if(context instanceof ClassDecl){
			ClassDecl cd = (ClassDecl)context;
			MemberDecl d = (MemberDecl)cd.visit(this, qre.id);
			
			if(d == null){
                throw new IdentificationError(d, "member decl is null");
			}
			
			if(md.isStatic && !d.isStatic){
				throw new IdentificationError(d, "method decl is static and member is not");
			}
			
			if(d.isPrivate && !(cd.name.equals(md.name))){
                System.out.println(cd.name);
                System.out.println(md.name);
				throw new IdentificationError(d, "member decl is private");
            }

			qre.id.decl = d;
			qre.declaration = qre.id.decl;
		}else if(context instanceof LocalDecl){
			LocalDecl ld = (LocalDecl)context;
			
			switch(ld.type.typeKind){
				case CLASS:
					ClassType ct = (ClassType)ld.type;
					ClassDecl cd = (ClassDecl)IDTable.findDeclaration(ct.className);
					MemberDecl d = (MemberDecl)cd.visit(this, qre.id);
					
					if(d == null){
						throw new IdentificationError(d, "THIS MEMBER DECL IS NULL");
					}
					
					// if(d.isPrivate){
					// 	throw new IdentificationError(d, "THIS MEMBER DECL IS PRIVATE");
                    // }
					
					qre.id.decl = d;
					qre.declaration = qre.id.decl;
                    // if(qre.id.decl instanceof MethodDecl){
                    //     System.out.println(qre.id.decl.name);
                    //     throw new IdentificationError(qre, "Cannot use this member of the class as a qualifier");
                    // }
					break;
				default:
					throw new IdentificationError(qre, "DEFAULTING ERROR");
			}
		}else if(context instanceof MemberDecl){
			MemberDecl memd = (MemberDecl)context;
			
			switch(memd.type.typeKind){
				case CLASS:
					ClassType ct = (ClassType)memd.type;
					ClassDecl cd = (ClassDecl)IDTable.findDeclaration(ct.className);
					MemberDecl d = (MemberDecl)cd.visit(this, qre.id);
					
					if(d == null){
						throw new IdentificationError(qre, "instance of member decl and d == null");
					}
					
					//if(d.isPrivate){
                        //System.out.println(context);
						//throw new IdentificationError(d, "instance of member decl and d is private");
                    //}

					qre.id.decl = d;
					qre.declaration = qre.id.decl;
					break;
				default:
					throw new IdentificationError(qre, "instance of member decl default");
			}
		}
		
		return null;
	}

    public Object visitIdentifier(Identifier id, Object arg){
		MethodDecl md = (MethodDecl)arg;
		return IDTable.findDeclaration(id);
	}

    public Object visitOperator(Operator op, Object arg){
		return null;
	}

    public Object visitIntLiteral(IntLiteral num, Object arg){
		return null;
	}

    public Object visitBooleanLiteral(BooleanLiteral bool, Object arg){
		return null;
	}

    public Object visitNullLiteral(NullLiteral nil, Object arg){
		return null;
	}

	class IdentificationError extends Error {
		private static final long serialVersionUID = -441346906191470192L;
		private String _errMsg;
		
		public IdentificationError(AST ast, String errMsg) {
			super();
			this._errMsg = errMsg;
		}
		
		@Override
		public String toString() {
			return _errMsg;
		}
	}
}