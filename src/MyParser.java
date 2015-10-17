//---------------------------------------------------------------------
// CSE 131 Reduced-C Compiler Project
// Copyright (C) 2008-2015 Garo Bournoutian and Rick Ord
// University of California, San Diego
//---------------------------------------------------------------------

import java_cup.runtime.*;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

class MyParser extends parser
{
	private Lexer m_lexer;
	private ErrorPrinter m_errors;
	private boolean m_debugMode;
	private int m_nNumErrors;
	private String m_strLastLexeme;
	private boolean m_bSyntaxError = true;
	private int m_nSavedLineNum;

	private SymbolTable m_symtab;

	// SELF-DEFINED VARIABLES
	private HashMap<String, FuncSTO> map = new HashMap<String, FuncSTO>();
	// Check 6.3
	private boolean topLevelFlag = false;
	// Check 12.2
	private int breakCounter = 0;

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public MyParser(Lexer lexer, ErrorPrinter errors, boolean debugMode)
	{
		m_lexer = lexer;
		m_symtab = new SymbolTable();
		m_errors = errors;
		m_debugMode = debugMode;
		m_nNumErrors = 0;
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public boolean Ok()
	{
		return m_nNumErrors == 0;
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public Symbol scan()
	{
		Token t = m_lexer.GetToken();

		//	We'll save the last token read for error messages.
		//	Sometimes, the token is lost reading for the next
		//	token which can be null.
		m_strLastLexeme = t.GetLexeme();

		switch (t.GetCode())
		{
			case sym.T_ID:
			case sym.T_ID_U:
			case sym.T_STR_LITERAL:
			case sym.T_FLOAT_LITERAL:
			case sym.T_INT_LITERAL:
				return new Symbol(t.GetCode(), t.GetLexeme());
			default:
				return new Symbol(t.GetCode());
		}
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public void syntax_error(Symbol s)
	{
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public void report_fatal_error(Symbol s)
	{
		m_nNumErrors++;
		if (m_bSyntaxError)
		{
			m_nNumErrors++;

			//	It is possible that the error was detected
			//	at the end of a line - in which case, s will
			//	be null.  Instead, we saved the last token
			//	read in to give a more meaningful error 
			//	message.
			m_errors.print(Formatter.toString(ErrorMsg.syntax_error, m_strLastLexeme));
		}
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public void unrecovered_syntax_error(Symbol s)
	{
		report_fatal_error(s);
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public void DisableSyntaxError()
	{
		m_bSyntaxError = false;
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public void EnableSyntaxError()
	{
		m_bSyntaxError = true;
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public String GetFile()
	{
		return m_lexer.getEPFilename();
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public int GetLineNum()
	{
		return m_lexer.getLineNumber();
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public void SaveLineNum()
	{
		m_nSavedLineNum = m_lexer.getLineNumber();
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public int GetSavedLineNum()
	{
		return m_nSavedLineNum;
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	void DoProgramStart()
	{
		// Opens the global scope.
		m_symtab.openScope();
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	void DoProgramEnd()
	{
		m_symtab.closeScope();
	}


	String GetType(Vector<STO> arguments)
	{
		String result = "";
		for(STO x : arguments) {
			if(x.isConst())
				result = result + "[" + ((ConstSTO)x).getIntValue() + "]";
		}
		return result;
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	VarSTO DoDeclArray(String id, Type t, Vector<STO> arguments)
	{
		ArrayType head = null;
		ArrayType pointer = null;
		ArrayType temp;
		while(!arguments.isEmpty()) {
			STO x = arguments.firstElement();
			if (x.isError())
				return new VarSTO(id);

			x = DoDesignator2_Array(x);

			if (x.isError())
				return new VarSTO(id);

			if (x.isConst())
			{
				temp = new ArrayType(t.getName() + GetType(arguments), ((ConstSTO) x).getIntValue());
				if(head == null) {
					head = temp;
				}
				else {
					pointer = head;
					while (pointer.hasNext()) {
						pointer = (ArrayType) pointer.next();
					}
					pointer.setChild(temp);
				}
			}

			arguments.remove(x);
		}

		pointer = head;
		while (pointer.hasNext()) {
			pointer = (ArrayType) pointer.next();
		}
		if(pointer != null){
			pointer.setChild(t);
		}

		//pointer.setChild(t);

		pointer= head;
		while(pointer.hasNext())
		{
			if(pointer.next().isArray())
				pointer = (ArrayType)pointer.next();
			else
				break;
		}
		VarSTO sto = new VarSTO(id, head);
		//sto.setIsModifiable(false);
		return sto;
	}
	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	void DoVarDecl(String id, Type t, STO expr, Vector<STO> arguments, String rtType)
	{
		if (m_symtab.accessLocal(id) != null)
		{
			m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.redeclared_id, id));
		}

		else if(arguments != null)
		{
			ArrayType head = null;
			ArrayType pointer = null;
			ArrayType temp;
			while(!arguments.isEmpty()) {
				STO x = arguments.firstElement();
				if (x.isError())
					return;

				x = DoDesignator2_Array(x);

				if (x.isError())
					return;

				if (x.isConst())
				{
					temp = new ArrayType(t.getName() + GetType(arguments), ((ConstSTO) x).getIntValue());
					if(head == null) {
						head = temp;
					}
					else {
						pointer = head;
						while (pointer.hasNext()) {
							pointer = (ArrayType) pointer.next();
						}
						pointer.setChild(temp);
					}
				}

				arguments.remove(x);
			}

			pointer = head;
			while (pointer.hasNext()) {
				pointer = (ArrayType) pointer.next();
			}
			if(pointer != null){
				pointer.setChild(t);
			}

			//pointer.setChild(t);

			pointer= head;
			while(pointer.hasNext())
			{
				if(pointer.next().isArray())
					pointer = (ArrayType)pointer.next();
				else
					break;
			}
			VarSTO sto = new VarSTO(id, head);

			m_symtab.insert(sto);
		}
		else if(expr != null && !t.isAssignableTo(expr.getType()))
		{
			if (expr.isError()) {
				return;
			} else {
				m_nNumErrors++;
				m_errors.print(Formatter.toString(ErrorMsg.error8_Assign, expr.getType().getName(), t.getName()));
			}
		}
		else if(expr != null && expr.isConst())
		{
			if (expr.getType().isInt()) {
				VarSTO sto = new VarSTO(id, t, ((ConstSTO) expr).getIntValue());
				m_symtab.insert(sto);
			} else if (expr.getType().isFloat()) {
				VarSTO sto = new VarSTO(id, t, ((ConstSTO) expr).getFloatValue());
				m_symtab.insert(sto);
			} else {
				VarSTO sto = new VarSTO(id, t, ((ConstSTO) expr).getBoolValue());
				m_symtab.insert(sto);
			}
		}
		else
		{
			VarSTO sto = new VarSTO(id, t);
			m_symtab.insert(sto);
		}
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	void DoVarDecl(String id)
	{
		if (m_symtab.accessLocal(id) != null)
		{
			m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.redeclared_id, id));
		}

		VarSTO sto = new VarSTO(id);
		m_symtab.insert(sto);
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	void DoAutoDecl(String id,STO expr){
		//create a new STO and assign it the type of exp
		// proceed as normal
		if(expr != null)
		{
			VarSTO sto = new VarSTO(id, expr.getType());
			m_symtab.insert(sto);
		}
		else
		{
			VarSTO sto = new VarSTO(id);
			m_symtab.insert(sto);
		}

	}
	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	void DoExternDecl(String id)
	{
		if (m_symtab.accessLocal(id) != null)
		{
			m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.redeclared_id, id));
		}

		VarSTO sto = new VarSTO(id);
		m_symtab.insert(sto);
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	void DoConstDecl(String id, Type t, STO expr)
	{
		if (m_symtab.accessLocal(id) != null)
		{
			m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.redeclared_id, id));
		}
		else if(expr != null && !t.isAssignableTo(expr.getType()))
		{
			if (expr.isError()) {
				return;
			} else {
				m_nNumErrors++;
				m_errors.print(Formatter.toString(ErrorMsg.error8_Assign, expr.getType().getName(), t.getName()));
			}
		}
		else if(!expr.isConst() && expr != null)
		{
			m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.error8_CompileTime, id));
		}
		else if(expr !=null)
		{
			if(expr.getType().isInt())
			{
				ConstSTO sto = new ConstSTO(id, t, ((ConstSTO) expr).getIntValue());
				m_symtab.insert(sto);
			}
			else if(expr.getType().isFloat())
			{
				ConstSTO sto = new ConstSTO(id, t, ((ConstSTO) expr).getFloatValue());
				m_symtab.insert(sto);
			}
			else
			{
				ConstSTO sto = new ConstSTO(id, t, ((ConstSTO) expr).getBoolValue());
				m_symtab.insert(sto);
			}
		}
		else
		{
			ConstSTO sto = new ConstSTO(id, t);
			m_symtab.insert(sto);
		}
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	void DoConstDecl(String id)
	{
		if (m_symtab.accessLocal(id) != null)
		{
			m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.redeclared_id, id));
		}
		
		ConstSTO sto = new ConstSTO(id, null, 0);   // fix me
		m_symtab.insert(sto);
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	void DoAutoConstDecl(String id,STO expr){
		//create a new STO and assign it the type of exp
		// proceed as normal
		if(expr != null)
		{
			if(expr.getType().isInt())
			{
				ConstSTO sto = new ConstSTO(id, ((ConstSTO) expr).getIntValue());
				m_symtab.insert(sto);
			}
			else if(expr.getType().isFloat())
			{
				ConstSTO sto = new ConstSTO(id, ((ConstSTO) expr).getFloatValue());
				m_symtab.insert(sto);
			}
			else
			{
				ConstSTO sto = new ConstSTO(id, ((ConstSTO) expr).getBoolValue());
				m_symtab.insert(sto);
			}
		}
		else
		{
			ConstSTO sto = new ConstSTO(id);
			m_symtab.insert(sto);
		}

	}
	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	void DoStructdefDecl(String id)
	{
		if (m_symtab.accessLocal(id) != null)
		{
			m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.redeclared_id, id));
		}
		
		StructdefSTO sto = new StructdefSTO(id);
		m_symtab.insert(sto);
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	void DoFuncDecl_1(String id)
	{
		if (m_symtab.accessLocal(id) != null)
		{
			m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.redeclared_id, id));
		}

	
		FuncSTO sto = new FuncSTO(id);
		m_symtab.insert(sto);

		m_symtab.openScope();
		// Check 6.3
		sto.setLevel(m_symtab.getLevel());
		m_symtab.setFunc(sto);
	}

	//----------------------------------------------------------------
	// New overloaded function
	//----------------------------------------------------------------
	void DoFuncDecl_1(String id, Type typ)
	{
		/*
		if (m_symtab.accessLocal(id) != null)
		{
			m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.redeclared_id, id));
		}
		*/

		FuncSTO sto = new FuncSTO(id, typ);
		m_symtab.insert(sto);

		m_symtab.openScope();
		// Check 6.3
		sto.setLevel(m_symtab.getLevel());
		m_symtab.setFunc(sto);
	}

	//----------------------------------------------------------------
	//	New overloaded function
	//----------------------------------------------------------------
	void DoFuncDecl_1(String id, Type typ, String rtType)
	{
		/*
		if (m_symtab.accessLocal(id) != null)
		{
			m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.redeclared_id, id));
		}
		*/

		FuncSTO sto = new FuncSTO(id, typ);

		// Check 6.2
		if(rtType == "&"){
			sto.setRbr(true);
		}
		else{
			sto.setRbr(false);
		}

		m_symtab.insert(sto);

		m_symtab.openScope();
		// Check 6.3
		sto.setLevel(m_symtab.getLevel());

		m_symtab.setFunc(sto);
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	void DoFuncDecl_2()
	{
		FuncSTO temp = m_symtab.getFunc();
		String hashKey = buildHashMap(temp, temp.getParams());

		// Check 6.3
		if(!(temp.getReturnType() instanceof VoidType)) {
			if (!topLevelFlag) {
				m_nNumErrors++;
				m_errors.print(ErrorMsg.error6c_Return_missing);
				return;
			}
		}

		// Check 9.1
		if(map.containsKey(hashKey)){
			m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.error9_Decl, temp.getName()));
			return;
		}

		map.put(hashKey, m_symtab.getFunc());

		m_symtab.closeScope();
		// Check 6.3
		topLevelFlag = false;
		m_symtab.setFunc(null);
	}

	//----------------------------------------------------------------
	// New helper function to generate the string for the hash map
	//----------------------------------------------------------------
	String buildHashMap(FuncSTO curr, Vector<STO> currParam){
		String name = curr.getName();
		String params = "";
		Iterator<STO> itr = currParam.iterator();

		while(itr.hasNext()){
				params += ("." + itr.next().getType().getName());
		}

		return name + params;
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	void DoFormalParams(Vector<STO> params)
	{
		if (m_symtab.getFunc() == null)
		{
			m_nNumErrors++;
			m_errors.print ("internal: DoFormalParams says no proc!");
		}

		// insert parameters here
		if(params == null){
			params = new Vector<STO>();
		}

		m_symtab.getFunc().setParams(params);

		for(int i = 0;i < params.size(); i++){
			m_symtab.insert(params.get(i));
		}
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	void DoBlockOpen()
	{
		// Open a scope.
		m_symtab.openScope();
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	void DoBlockClose()
	{
		m_symtab.closeScope();
	}

	//----------------------------------------------------------------
	// Check 12.2
	//----------------------------------------------------------------
	void incrementBreakCounter(){
		breakCounter++;
	}

	//----------------------------------------------------------------
	// Check 12.2
	//----------------------------------------------------------------
	void decrementBreakCounter(){
		breakCounter--;
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	STO DoBoolCheck(STO expr){
		Type varType = expr.getType();

		if(!(varType instanceof BoolType)){
			// handle propagating errors
			m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.error4_Test, varType.getName()));
		}

		return expr;
	}

	//----------------------------------------------------------------
	// Check 7
	//----------------------------------------------------------------
	STO DoExitCheck(STO expr){
		if(expr != null)
		{
			Type varType = expr.getType();
			if (!(varType instanceof IntType)) {
				// handle propagating errors
				m_nNumErrors++;
				m_errors.print(Formatter.toString(ErrorMsg.error7_Exit, varType.getName()));
			}
		}
		return expr;
	}
	
	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	STO DoAssignExpr(STO stoDes, STO expr)
	{
		//System.out.println("StoDes Type: " + stoDes.getIsModifiable());
		//System.out.println("Expr Type: " + expr.getName());
		if(stoDes.isError())
		{
			return new ErrorSTO(stoDes.getName());
		}
		else if(expr.isError())
		{
			return new ErrorSTO(stoDes.getName());
		}
		else if(!stoDes.isModLValue())
		{
			m_nNumErrors++;
			m_errors.print(ErrorMsg.error3a_Assign);
			return new ErrorSTO(stoDes.getName());
		}
		else if(!expr.isFunc() && !(stoDes.getType().isAssignableTo(expr.getType())))
		{
			//check if expression is array
			m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.error3b_Assign, expr.getType().getName(), stoDes.getType().getName()));
			return new ErrorSTO(stoDes.getName());
		}

		return stoDes;
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	STO DoBinaryExpr(STO a, BinaryOp o, STO b) {

		if(a instanceof ErrorSTO){
			return a;
		}

		if(b instanceof ErrorSTO){
			return b;
		}

		STO result = o.checkOperands(a, b);
		if (result instanceof ErrorSTO) {
			// handle propagating errors
			m_nNumErrors++;
			m_errors.print(result.getName());
		}
			//do stuff...
		return result ;
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	STO DoUnaryExpr(STO a, UnaryOp o){
		if(a instanceof ErrorSTO){
			return a;
		}

		STO result = o.checkOperands(a);
		if (result instanceof ErrorSTO) {
			// handle propagating errors
			m_nNumErrors++;
			m_errors.print(result.getName());
		}
		//do stuff...
		return result ;
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	STO DoFuncCall(STO sto, Vector<STO> arguments)
	{
		if (!sto.isFunc())
		{
			m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.not_function, sto.getName()));
			return new ErrorSTO(sto.getName());
		}

		if(sto instanceof ErrorSTO){
			return sto;
		}

		FuncSTO temp = (FuncSTO) sto;
		String hashKey = buildHashMap(temp, arguments);
		int size = temp.getParamSize();
		int paramSize = arguments.size();

		// Check 5.1
		if(size != paramSize){
			m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.error5n_Call, paramSize, size));
			return new ErrorSTO(sto.getName());
		}

		// Check if the function declared is in the hash map
		if(!map.containsKey(hashKey)){
			for(int i = 0; i < size; i++){
				// Get the STO and the type
				STO argumentSTO = arguments.get(i);
				Type argumentType = argumentSTO.getType();

				VarSTO parameterSTO = (VarSTO)temp.getParams().get(i);
				Type parameterType = parameterSTO.getType();

				if(parameterSTO.getPbr()){
					// MAYBE NEED TO MAKE IT IS EQUIVALENT
					if(!parameterType.isEquivalentTo(argumentType)){
						m_nNumErrors++;
						m_errors.print(Formatter.toString(ErrorMsg.error5r_Call, argumentType.getName(), parameterSTO.getName(), parameterType.getName()));
					}
					else if(!argumentSTO.isModLValue()){
						m_nNumErrors++;
						m_errors.print(Formatter.toString(ErrorMsg.error5c_Call, parameterSTO.getName(), parameterType.getName()));
					}
				}
				else{
					if(!parameterType.isAssignableTo(argumentType)){
						m_nNumErrors++;
						m_errors.print(Formatter.toString(ErrorMsg.error5a_Call, argumentType.getName(), parameterSTO.getName(), parameterType.getName()));
					}
				}
			}

			return new ErrorSTO(sto.getName());
		}
		else{
			for(int j = 0; j < size; j++){
				// Get the STO and the type
				STO argumentSTO = arguments.get(j);

				VarSTO parameterSTO = (VarSTO)temp.getParams().get(j);
				Type parameterType = parameterSTO.getType();

				if(parameterSTO.getPbr()){
					if(!argumentSTO.isModLValue()){
						m_nNumErrors++;
						m_errors.print(Formatter.toString(ErrorMsg.error5c_Call, parameterSTO.getName(), parameterType.getName()));
						sto = new ErrorSTO(sto.getName());
					}
				}
			}
		}

		return sto;
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	STO DoReturnCheck(){
		FuncSTO temp = m_symtab.getFunc();
		Type returnType = temp.getReturnType();

		if(temp.getLevel() == m_symtab.getLevel()){
			// Check 6.3
			topLevelFlag = true;
		}

		if(!(returnType instanceof VoidType)){
			m_nNumErrors++;
			m_errors.print(ErrorMsg.error6a_Return_expr);
			return new ErrorSTO("Return type not void");
		}

		return temp;
	}

	//----------------------------------------------------------------
	// Check 6.2
	//----------------------------------------------------------------
	STO DoReturnCheck(STO a){

		if(a instanceof ErrorSTO){
			// Check 6.3
			topLevelFlag = true;
			return a;
		}

		FuncSTO temp = m_symtab.getFunc();
		Type returnType = temp.getReturnType();
		Type returnStmt = a.getType();
		STO var;

		// Check 6.3
		if(temp.getLevel() == m_symtab.getLevel()){
			topLevelFlag = true;
		}

		if(a instanceof ConstSTO){
			var = a;
		}
		else{
			var = m_symtab.access(a.getName());
		}

		// Is passByRef
		if(temp.getRbr()){
			// Check if they are equivalent
			if(!returnType.isEquivalentTo(returnStmt)){
				m_nNumErrors++;
				m_errors.print(Formatter.toString(ErrorMsg.error6b_Return_equiv, returnStmt.getName(), returnType.getName()));
				return new ErrorSTO(a.getName());
			}
			// Check if the return type is a modifiable L-value
			else if(!var.isModLValue()){
				m_nNumErrors++;
				m_errors.print(ErrorMsg.error6b_Return_modlval);
				return new ErrorSTO(a.getName());
			}
		}
		// Not passByRef
		else{
			if(!returnType.isAssignableTo(returnStmt)){
				m_nNumErrors++;
				m_errors.print(Formatter.toString(ErrorMsg.error6a_Return_type, returnStmt.getName(), returnType.getName()));
				return new ErrorSTO(a.getName());
			}
		}

		return a;
	}


	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	STO DoDesignator2_Dot(STO sto, String strID)
	{
		// Good place to do the struct checks

		return sto;
	}

	//----------------------------------------------------------------
	// haven't do the pointer check
	//----------------------------------------------------------------
	STO DoDesignator2_Arrays(STO des, STO expr)
	{
		//System.out.println(((ConstSTO)sto).getIntValue());
		// Good place to do the array checks
		//System.out.println(expr.isExpr());
		if(des.isError() || expr.isError())
		{
			return new ErrorSTO(expr.getName());
		}
		//if the designator preceding [] is not array or pointer return error
		if(des != null && !des.getType().isArray() && !des.getType().isPointer())
		{
			m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.error11t_ArrExp, expr.getType().getName()));
			return new ErrorSTO(expr.getName());
		}
		// else if the index expression is not equivalent to int
		else if(!expr.getType().isInt())
		{
			m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.error11i_ArrExp, expr.getType().getName()));
			return new ErrorSTO(expr.getName());
		}
		else if(des.getType().isArray())
		{

			ArrayType temp = (ArrayType) des.getType();

			if (expr.isConst() && (((ConstSTO) expr).getIntValue() >= temp.getDimensions() || ((ConstSTO) expr).getIntValue() < 0)) {
				m_nNumErrors++;
				m_errors.print(Formatter.toString(ErrorMsg.error11b_ArrExp, ((ConstSTO) expr).getIntValue(), temp.getDimensions()));
				return new ErrorSTO(expr.getName());
			}

			Type next = temp.next();
			if (next.isArray()) {
				//System.out.println("1");
				VarSTO sto = new VarSTO(next.getName(), (ArrayType)next);
				return sto;
			} else if(next.isInt()){
				//System.out.println("2");
				return new VarSTO(des.getName(), new IntType());
			} else if(next.isBool())
			{
				//System.out.println("3");
				return new VarSTO(des.getName(), new BoolType());
			}
			else {
				//System.out.println("4");
				return new VarSTO(des.getName(), new FloatType());

			}
		}
		return des;
	}
	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	STO DoDesignator2_Array(STO sto)
	{
		//System.out.println(((ConstSTO)sto).getIntValue());
		// Good place to do the array checks
		if(!sto.getType().isInt())
		{
			m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.error10i_Array, sto.getType().getName()));
			return new ErrorSTO(sto.getName());
		}
		else if(!sto.isConst())
		{
			m_nNumErrors++;
			m_errors.print(ErrorMsg.error10c_Array);
			return new ErrorSTO(sto.getName());
		}
		else if(((ConstSTO)sto).getIntValue() <= 0 )
		{
			m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.error10z_Array, ((ConstSTO)sto).getIntValue()));
			return new ErrorSTO(sto.getName());
		}
		return sto;
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	STO DoDesignator3_ID(String strID)
	{
		STO sto;

		if ((sto = m_symtab.access(strID)) == null)
		{
			m_nNumErrors++;
		 	m_errors.print(Formatter.toString(ErrorMsg.undeclared_id, strID));
			sto = new ErrorSTO(strID);
		}

		return sto;
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	STO DoDesignator4_ID(String strID)
	{
		STO sto;

		if ((sto = m_symtab.accessGlobal(strID)) == null)
		{
			m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.error0g_Scope, strID));
			sto = new ErrorSTO(strID);
		}

		return sto;
	}


	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	Type DoStructType_ID(String strID)
	{
		STO sto;

		if ((sto = m_symtab.access(strID)) == null)
		{
			m_nNumErrors++;
		 	m_errors.print(Formatter.toString(ErrorMsg.undeclared_id, strID));
			return new ErrorType();
		}

		if (!sto.isStructdef())
		{
			m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.not_type, sto.getName()));
			return new ErrorType();
		}

		return sto.getType();
	}

	//----------------------------------------------------------------
	// Check 12.2
	//----------------------------------------------------------------
	void DoBreakCheck(){
		if(breakCounter == 0){
			m_nNumErrors++;
			m_errors.print(ErrorMsg.error12_Break);
		}
	}

	//----------------------------------------------------------------
	// Check 12.2
	//----------------------------------------------------------------
	void DoContinueCheck() {
		if (breakCounter == 0) {
			m_nNumErrors++;
			m_errors.print(ErrorMsg.error12_Continue);
		}
	}

	//----------------------------------------------------------------
	// Check 13.1
	//----------------------------------------------------------------
	void DoDuplicateCheck(Vector<STO> members){
		Iterator<STO> itr = members.iterator();
		Vector<String> tempVec = new Vector<>();

		while(itr.hasNext()){
			STO cur = itr.next();
			if(tempVec.contains(cur.getName())){
				m_nNumErrors++;
				m_errors.print(Formatter.toString(ErrorMsg.error13a_Struct, cur.getName()));
			} else {
				tempVec.add(cur.getName());
			}
		}
	}
}
