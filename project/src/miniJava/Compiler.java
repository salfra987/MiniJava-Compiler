package miniJava;

import java.io.FileInputStream;
import java.lang.System;

import miniJava.AbstractSyntaxTrees.*;
import miniJava.AbstractSyntaxTrees.Package;
import miniJava.ContextualAnalysis.Identification;
import miniJava.ContextualAnalysis.TypeChecking;

import java.io.FileNotFoundException;

import miniJava.SyntacticAnalyzer.Scanner;
import miniJava.SyntacticAnalyzer.Parser;

public class Compiler {
	// Main function, the file to compile will be an argument.
	public static void main(String[] args) {
		// TODO: Instantiate the ErrorReporter object
		ErrorReporter eReporter = new ErrorReporter();
		//System.out.println("ErrorReporter instantiation successful");
		
		// TODO: Check to make sure a file path is given in args
		if(args.length == 0) {
			throw new IllegalArgumentException("No file given");
		}
		
		FileInputStream inputStream = null;
		//System.out.println("inputStream instantiation successful");
		
		// TODO: Create the inputStream using new FileInputStream
		try {
			inputStream = new FileInputStream(args[0]);
		} catch (FileNotFoundException e) {
			System.out.println("No such file " + args[0] + " exists");
			System.exit(1);
		}
		//System.out.println("we have a file");
		
		// TODO: Instantiate the scanner with the input stream and error object
		Scanner scanner = new Scanner(inputStream, eReporter);
		//System.out.println("scanner instantiation successful");
		
		// TODO: Instantiate the parser with the scanner and error object
		Parser parser = new Parser(scanner, eReporter);
		//System.out.println("parser instantiation successful");
		
		// TODO: Call the parser's parse function
		AST ast = parser.parse();
		//System.out.println("done parsing");
		
		// TODO: Check if any errors exist, if so, println("Error")
		//  then output the errors
		if(eReporter.hasErrors()) {
			System.out.println("Error");
			eReporter.outputErrors();
		}
		
		// TODO: If there are no errors, println("Success")
		//else {
		//	ASTDisplay display = new ASTDisplay();
		//	display.showTree(ast);
		//}

		Identification id = new Identification((Package)ast, eReporter);
		id.parse((Package)ast);
		int x = 0;
		
		if(eReporter.hasErrors()){
			System.out.println("Error");
			eReporter.outputErrors();
			x = 1;
		}

		TypeChecking tc = new TypeChecking(eReporter);
		tc.parse((Package)ast);
		
		if(eReporter.hasErrors()){
			System.out.println("Error");
			eReporter.outputErrors();
		}else{
			if(x == 0){
				System.out.println("Success");
			}
		}
	}
}