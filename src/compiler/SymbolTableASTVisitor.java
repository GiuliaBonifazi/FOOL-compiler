package compiler;

import java.util.*;
import compiler.AST.*;
import compiler.exc.VoidException;
import compiler.lib.*;

public class SymbolTableASTVisitor extends BaseASTVisitor<Void, VoidException> {
	
	private final List<Map<String, STentry>> symTable = new ArrayList<>();
	private final Map<String, Map<String, STentry>> classTable = new HashMap<>();
	private int nestingLevel=0; // current nesting level
	private int decOffset=-2; // counter for offset of local declarations at current nesting level 
	int stErrors=0;

	SymbolTableASTVisitor() {}
	SymbolTableASTVisitor(boolean debug) {super(debug);} // enables print for debugging

	private STentry stLookup(String id) {
		int j = nestingLevel;
		STentry entry = null;
		while (j >= 0 && entry == null) 
			entry = symTable.get(j--).get(id);	
		return entry;
	}

	@Override
	public Void visitNode(ProgLetInNode n) {
		if (print) printNode(n);
		Map<String, STentry> hm = new HashMap<>();
		symTable.add(hm);
	    for (Node dec : n.declist) visit(dec);
		visit(n.exp);
		symTable.remove(0);
		return null;
	}

	@Override
	public Void visitNode(ProgNode n) {
		if (print) printNode(n);
		visit(n.exp);
		return null;
	}

	@Override
	public Void visitNode(ClassNode n) {
		if (print) printNode(n);
		Map<String, STentry> hm = symTable.get(nestingLevel);
		ClassTypeNode classType = new ClassTypeNode(new ArrayList<>(), new ArrayList<>());
		STentry classEntry = new STentry(nestingLevel, classType, decOffset);
		decOffset--;

		// Provo ad inserire l'id della classe nella Symtable; se esiste già, errore
		if (hm.put(n.id, classEntry) != null) {
			System.out.println("Class id " + n.id + " at line " + n.getLine() + " already declared.");
			stErrors++;
		}

		// Creo nuova entry per la ClassTable
		Map<String, STentry> virtualTable = new HashMap<>();
		classTable.put(n.id, virtualTable);

		nestingLevel++;
		symTable.add(virtualTable);
		int fieldOffset = -1;
		for (FieldNode field : n.fields) {
			if (print) printNode(field);
			STentry fieldEntry = new STentry(nestingLevel, field.getType(), fieldOffset);
			fieldOffset--;
			classType.allFields.add(fieldEntry.type);
			if (virtualTable.put(field.id, fieldEntry) != null) {
				System.out.println("Field id " + field.id + " at line " + field.getLine() + " already declared.");
				stErrors++;
			}
		}
		int offsetBeforeMethodDecl = decOffset;
		decOffset = 0;
		for (MethodNode method: n.methods) {
			visit(method);
			classType.allMethods.add(
				method.getOffset(),
				(ArrowTypeNode) virtualTable.get(method.id).type
			);
		}
		decOffset = offsetBeforeMethodDecl;
		symTable.remove(nestingLevel);
		nestingLevel--;

		return null;
	}

	@Override
	public Void visitNode(MethodNode n) {
		if (print) printNode(n);
		Map<String, STentry> virtualTable = symTable.get(nestingLevel);
		List<TypeNode> parameterTypes = new ArrayList<>();
		for (ParNode parameter : n.parameters) parameterTypes.add(parameter.getType());
		STentry entry = new STentry(
			nestingLevel,
			new ArrowTypeNode(parameterTypes, n.returnType),
			decOffset
		);
		n.setOffset(decOffset);
		// Gli offset dei metodi iniziano da 0 e aumentano con ogni dichiarazione
		decOffset++;

		//Inserisco ID nella symtable/virtual table
		if (virtualTable.put(n.id, entry) != null) {
			System.out.println("Method id " + n.id + " at line "+ n.getLine() +" already declared.");
			stErrors++;
		}

		// Creo nuova hashmap per il corpo del metodo
		nestingLevel++;
		Map<String, STentry> methodLevel = new HashMap<>();
		symTable.add(methodLevel);
		int prevOffset = decOffset;
		decOffset = -2;
		int parameterOffset = 1;
		for (ParNode parameter : n.parameters) {
			STentry parameterEntry = new STentry(nestingLevel, parameter.getType(), parameterOffset);
			if (methodLevel.put(parameter.id, parameterEntry) != null) {
				System.out.println("Parameter id " + parameter.id + " at line " + n.getLine() + " already declared.");
				stErrors++;
			}
			parameterOffset++;
		}
		for (Node dec : n.declarations) visit(dec);
		visit(n.exp);

		// Rimuovo symbol table
		symTable.remove(nestingLevel);
		decOffset = prevOffset;
		nestingLevel--;
		return null;
	}
	
	@Override
	public Void visitNode(FunNode n) {
		if (print) printNode(n);
		Map<String, STentry> hm = symTable.get(nestingLevel);
		List<TypeNode> parTypes = new ArrayList<>();
		for (ParNode par : n.parlist) parTypes.add(par.getType()); 
		STentry entry = new STentry(nestingLevel, new ArrowTypeNode(parTypes,n.retType),decOffset--);
		//inserimento di ID nella symtable
		if (hm.put(n.id, entry) != null) {
			System.out.println("Fun id " + n.id + " at line "+ n.getLine() +" already declared");
			stErrors++;
		} 
		//creare una nuova hashmap per la symTable
		nestingLevel++;
		Map<String, STentry> hmn = new HashMap<>();
		symTable.add(hmn);
		int prevNLDecOffset=decOffset; // stores counter for offset of declarations at previous nesting level 
		decOffset=-2;
		
		int parOffset=1;
		for (ParNode par : n.parlist)
			if (hmn.put(par.id, new STentry(nestingLevel,par.getType(),parOffset++)) != null) {
				System.out.println("Par id " + par.id + " at line "+ n.getLine() +" already declared");
				stErrors++;
			}
		for (Node dec : n.declist) visit(dec);
		visit(n.exp);
		//rimuovere la hashmap corrente poiche' esco dallo scope               
		symTable.remove(nestingLevel--);
		decOffset=prevNLDecOffset; // restores counter for offset of declarations at previous nesting level 
		return null;
	}
	
	@Override
	public Void visitNode(VarNode n) {
		if (print) printNode(n);
		visit(n.exp);
		Map<String, STentry> hm = symTable.get(nestingLevel);
		STentry entry = new STentry(nestingLevel,n.getType(),decOffset--);
		//inserimento di ID nella symtable
		if (hm.put(n.id, entry) != null) {
			System.out.println("Var id " + n.id + " at line "+ n.getLine() +" already declared");
			stErrors++;
		}
		return null;
	}

	@Override
	public Void visitNode(PrintNode n) {
		if (print) printNode(n);
		visit(n.exp);
		return null;
	}

	@Override
	public Void visitNode(IfNode n) {
		if (print) printNode(n);
		visit(n.cond);
		visit(n.th);
		visit(n.el);
		return null;
	}
	
	@Override
	public Void visitNode(EqualNode n) {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(GreaterEqualNode n) {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(LessEqualNode n) {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(NotNode n) {
		if (print) printNode(n);
		visit(n.exp);
		return null;
	}

	@Override
	public Void visitNode(AndNode n) {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(OrNode n) {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(TimesNode n) {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(DivNode n) {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}
	
	@Override
	public Void visitNode(PlusNode n) {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(MinusNode n) {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(CallNode n) {
		if (print) printNode(n);
		STentry entry = stLookup(n.id);
		if (entry == null) {
			System.out.println("Fun id " + n.id + " at line "+ n.getLine() + " not declared");
			stErrors++;
		} else {
			n.entry = entry;
			n.nl = nestingLevel;
		}
		for (Node arg : n.arglist) visit(arg);
		return null;
	}

	@Override
	public Void visitNode(IdNode n) {
		if (print) printNode(n);
		STentry entry = stLookup(n.id);
		if (entry == null) {
			System.out.println("Var or Par id " + n.id + " at line "+ n.getLine() + " not declared");
			stErrors++;
		} else {
			n.entry = entry;
			n.nl = nestingLevel;
		}
		return null;
	}

	@Override
	public Void visitNode(BoolNode n) {
		if (print) printNode(n, n.val.toString());
		return null;
	}

	@Override
	public Void visitNode(IntNode n) {
		if (print) printNode(n, n.val.toString());
		return null;
	}
}
