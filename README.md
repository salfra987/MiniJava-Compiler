PA5-Guide

PA1: Syntactic Analysis
For syntactic analysis, I used recursive descent parsing. Everything was tokenized so the number of tokens was not minimized. This step occurs first and the relevant files are found in the SyntaticAnalysis directory.

PA2: AST Generation
 We assume general familiarity with ASTs. See ASTChanges.txt for data beyond syntax that is stored in ASTs. The relevant files are found in the AbstractSyntaxTrees directory.
 
PA3: Contextual Analysis
I used two traversals for contextual analysis, the first traversal is for identification and the second traversal is for type checking. Scoped Identification took place in the first traversal, so all of the relevant methods are in the identification.java and IDTable.java.

PA4: Code Generation
This portion of the project is incomplete, the code for each instruction and encoding registers is complete. When the Code Generation files are complete, it will translate miniJava to x86/x64 processors and target the Linux operating system. The relevant files are found in the CodeGeneration directory.

Other Things to look out for:
The Contextual Analysis as it stands is not fully passing all the correctness tests and the Code Generation is also not complete. The entry point for the code is in Compiler.java.
