//---------------------------------------------------------------------
// CSE 131 Reduced-C Compiler Project
// Copyright (C) 2008-2015 Garo Bournoutian and Rick Ord
// University of California, San Diego
//---------------------------------------------------------------------

import java_cup.runtime.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

class MyParser extends parser
{
	private AssemblyGenerator m_writer;
	private Lexer m_lexer;
	private ErrorPrinter m_errors;
	private boolean m_debugMode;
	private int m_nNumErrors;
	private String m_strLastLexeme;
	private boolean m_bSyntaxError = true;
	private int m_nSavedLineNum;

	private SymbolTable m_symtab;

	// SELF-DEFINED VARIABLES
	private String delimiter = "%";
	private HashMap<String, STO> map = new HashMap<>();
	// Check 6.3
	private boolean topLevelFlag = false;
	// Check 9b
	private HashMap<String, Vector<FuncSTO>> funcMap = new HashMap<>();
	// Check 12.2
	private int breakCounter = 0;
	// Check 13.1
	private boolean isStruct = false;
	private FuncSTO functionSTO;
	private String currentStructName;
	// Check 14.2
	private boolean recursiveFunc = false;

	private boolean isStaticFlag = false;
	private boolean isPre = false;

	private int offset = 0;
	// Project 2 phase 1 check 5
	private int cmpCount = 0;
	private boolean ifFlag = false;

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
		m_writer = new AssemblyGenerator("rc.s");
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
		m_writer.writeRodata();
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	void DoProgramEnd()
	{
		m_symtab.closeScope();
		// CALL WRITE ASSEMBLY
		m_writer.writeToFile();
		m_writer.dispose();
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
				temp = new ArrayType(t.getName() + GetType(arguments), GetSize(arguments) * t.getSize(), ((ConstSTO) x).getIntValue());
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
		if(pointer != null) {
			pointer.setChild(t);
		}

		pointer= head;
		while(pointer.hasNext())
		{
			if(pointer.next().isArray())
				pointer = (ArrayType)pointer.next();
			else
				break;
		}
		VarSTO sto = new VarSTO(id, head);
		sto.setIsModifiable(false);
		return sto;
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	void DoForEachVarDecl(String id, Type t, STO expr, String rtType)
	{

		if (m_symtab.accessLocal(id) != null)
		{
			m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.redeclared_id, id));
		}
		if(expr != null)
		{
			if(expr.isError()) {
				return;
			}
			//if it's not array
			else if(!expr.getType().isArray())
			{
				m_nNumErrors++;
				m_errors.print(ErrorMsg.error12a_Foreach);
			}
			//else it's array
			else
			{
				//access the array element
				ArrayType temp = (ArrayType) expr.getType();
				Type next = temp.next();
				if(rtType != "&" && !t.isAssignableTo(next))
				{
					m_nNumErrors++;
					m_errors.print(Formatter.toString(ErrorMsg.error12v_Foreach, next.getName(), id, t.getName()));
				}
				else if(rtType == "&" && !t.isEquivalentTo(next))
				{
					m_nNumErrors++;
					m_errors.print(Formatter.toString(ErrorMsg.error12r_Foreach, next.getName(), id, t.getName()));
				}
			}
		}
		VarSTO sto = new VarSTO(id, t);
		m_symtab.insert(sto);
	}

	//----------------------------------------------------------------
	//
	//
	//----------------------------------------------------------------
	void DoVarDecl(String id, Type t, STO expr, Vector<STO> arguments, String rtType)
	{
		boolean global = true;
		FuncSTO fun = m_symtab.getFunc();
		if(fun != null)
		{
			global = false;
		}

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
					temp = new ArrayType(t.getName() + GetType(arguments), GetSize(arguments) * t.getSize(), ((ConstSTO) x).getIntValue());
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

			pointer= head;
			while(pointer.hasNext())
			{
				if(pointer.next().isArray())
					pointer = (ArrayType)pointer.next();
				else
					break;
			}
			VarSTO sto = new VarSTO(id, head);
			sto.setIsModifiable(false);
			m_symtab.insert(sto);
		}
		else if(expr != null && !t.isAssignableTo(expr.getType()))
		{
			if (expr.isError() || expr.getType().isError()) {
				VarSTO sto = new VarSTO(id, t);
				m_symtab.insert(sto);
			} else {
				m_nNumErrors++;
				m_errors.print(Formatter.toString(ErrorMsg.error8_Assign, expr.getType().getName(), t.getName()));
				VarSTO sto = new VarSTO(id, t);
				m_symtab.insert(sto);
			}
		}
		else if(expr != null && expr.isConst())
		{
			if (expr.getType().isInt()) {
				VarSTO sto = new VarSTO(id, t, ((ConstSTO) expr).getIntValue());
				// Check I.1
				if(global) {
					sto.setBase("%g0");
					sto.setOffset(id);
					m_writer.writeGlobalInit(expr, id, t, isStaticFlag);
				}
				else
				{
					//local and static
					if(isStaticFlag)
					{
						sto.setBase("%g0");
						sto.setOffset(fun.getName() + "." + fun.getType().getName() + "." + id);
						m_writer.writeGlobalInit(expr, sto.getOffset(), t, isStaticFlag);
					}
					//local not static
					else
					{
						sto.setBase("%fp");
						offset -= t.getSize();
						sto.setOffset(Integer.toString(offset));
						if(t.isInt())
							m_writer.writeLocalInitWithConst(sto, expr);
						else // assign int to float
						{
							expr.setBase("%fp");
							offset -= expr.getType().getSize();
							expr.setOffset(Integer.toString(offset));
							m_writer.writeLocalInitWithConst(sto, expr);
						}
					}
				}

				m_symtab.insert(sto);
			} else if (expr.getType().isFloat()) {
				VarSTO sto = new VarSTO(id, t, ((ConstSTO) expr).getFloatValue());
				// Check I.1

				if(global) {
					sto.setBase("%g0");
					sto.setOffset(id);
					m_writer.writeGlobalInit(expr, id, t, isStaticFlag);
				}
				else
				{
					//local and static
					if(isStaticFlag)
					{
						sto.setBase("%g0");
						sto.setOffset(fun.getName() + "." + fun.getType().getName() + "." + id);
						m_writer.writeGlobalInit(expr, sto.getOffset(), t, isStaticFlag);
					}
					//local not static
					else
					{
						sto.setBase("%fp");
						offset -= t.getSize();
						sto.setOffset(Integer.toString(offset));
						m_writer.writeLocalInitWithConst(sto, expr);
					}
				}

				m_symtab.insert(sto);
			} else {
				VarSTO sto = new VarSTO(id, t, ((ConstSTO) expr).getBoolValue());
				// Check I.1
				if(global) {
					sto.setBase("%g0");
					sto.setOffset(id);
					m_writer.writeGlobalInit(expr, id, t, isStaticFlag);
				}
				else
				{
					//local and static
					if(isStaticFlag)
					{
						sto.setBase("%g0");
						sto.setOffset(fun.getName() + "." + fun.getType().getName() + "." + id);
						m_writer.writeGlobalInit(expr, sto.getOffset(), t, isStaticFlag);
					}
					//local not static
					else
					{
						sto.setBase("%fp");
						offset -= t.getSize();
						sto.setOffset(Integer.toString(offset));
						m_writer.writeLocalInitWithConst(sto, expr);
					}
				}

				m_symtab.insert(sto);
			}
		}
		else if(expr != null && ( expr.isVar() || expr.isExpr()))
		{
			VarSTO sto = new VarSTO(id, t);

			if(global) {
				sto.setBase("%g0");
				sto.setOffset(id);
				// call global writer here
				m_writer.writeGlobalNonInit(id, isStaticFlag);

				if(!isStaticFlag){
					m_writer.initGlobal(id, expr, offset);
				}
			}
			else
			{
				//local and static
				if(isStaticFlag)
				{
					sto.setBase("%g0");
					sto.setOffset(fun.getName() + "." + fun.getType().getName() + "." + id);
					m_writer.writeLocalStaticInitWithVar(sto, expr);
				}
				//local not static
				else
				{
					sto.setBase("%fp");
					offset -= t.getSize();
					sto.setOffset(Integer.toString(offset));
					m_writer.writeLocalInitWithVar(sto, expr);
				}
			}

			m_symtab.insert(sto);

		}
		else{
			VarSTO sto = new VarSTO(id, t);

			if(global) {
				sto.setBase("%g0");
				sto.setOffset(id);
				m_writer.writeGlobalNonInit(id, isStaticFlag);
			}
			else
			{
				//local and static
				if(isStaticFlag)
				{
					sto.setBase("%g0");
					sto.setOffset(fun.getName() + "." + fun.getType().getName() + "." + id);
					m_writer.writeGlobalNonInit(fun.getName() + "." + fun.getType().getName() + "." + id, isStaticFlag);
				}
				//local not static
				else
				{
					sto.setBase("%fp");
					offset -= t.getSize();
					sto.setOffset(Integer.toString(offset));
					//uninitialize local variable do nothing in the assembly code
				}
			}

			if(expr instanceof VarSTO){
				// Call writer to int
			}

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
	// Overloaded function for structs
	//----------------------------------------------------------------
	void DoVarDecl(String id, Type typ, Vector<STO> parameters)
	{
		if (m_symtab.accessLocal(id) != null)
		{
			m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.redeclared_id, id));
		}

		StructdefSTO tempSTO = (StructdefSTO) m_symtab.accessGlobal(typ.getName());

		DoFuncCall(tempSTO.getCtorDtorsList().firstElement(), parameters);

		VarSTO sto = new VarSTO(id, tempSTO.getType());
		m_symtab.insert(sto);
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	void DoAutoDecl(String id,STO expr){
		boolean global = true;
		FuncSTO fun = m_symtab.getFunc();
		if(fun != null)
		{
			global = false;
		}

		//create a new STO and assign it the type of exp
		// proceed as normal
		if(expr != null && !expr.getType().isNullPointer())
		{
			VarSTO sto = new VarSTO(id, expr.getType());
			//local and static
			if(expr.isConst()) {
				if(global) {
					sto.setBase("%g0");
					sto.setOffset(id);
					// call global writer here
					m_writer.writeGlobalInit(expr, id, expr.getType(), isStaticFlag);
				}
				else {
					if (isStaticFlag) {
						sto.setBase("%g0");
						sto.setOffset(fun.getName() + "." + fun.getType().getName() + "." + id);
						m_writer.writeGlobalInit(expr, sto.getOffset(), expr.getType(), isStaticFlag);
					}
					//local not static
					else {
						sto.setBase("%fp");
						offset -= expr.getType().getSize();
						sto.setOffset(Integer.toString(offset));
						m_writer.writeLocalInitWithConst(sto, expr);
					}
				}
			}
			else // variable initialization
			{
				if(global) {
					sto.setBase("%g0");
					sto.setOffset(id);
					// call global writer here
					m_writer.writeGlobalNonInit(id, isStaticFlag);

					if(!isStaticFlag){
						m_writer.initGlobal(id, expr, offset);
					}
				}
				else
				{
					//local and static
					if(isStaticFlag)
					{
						sto.setBase("%g0");
						sto.setOffset(fun.getName() + "." + fun.getType().getName() + "." + id);
						m_writer.writeLocalStaticInitWithVar(sto, expr);
					}
					//local not static
					else
					{
						sto.setBase("%fp");
						offset -= expr.getType().getSize();
						sto.setOffset(Integer.toString(offset));
						m_writer.writeLocalInitWithVar(sto, expr);
					}
				}
			}
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
		boolean exprIsConstVar = expr.getIsAddressable();
		boolean global = true;
		FuncSTO fun = m_symtab.getFunc();
		if(fun != null)
		{
			global = false;
		}

		if (m_symtab.accessLocal(id) != null)
		{
			m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.redeclared_id, id));
		}
		else if(!expr.isConst() && expr != null)
		{
			m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.error8_CompileTime, id));
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
		else if(expr !=null)
		{
			if(expr.getType().isInt())
			{
				ConstSTO sto = new ConstSTO(id, t, ((ConstSTO) expr).getIntValue());
				if(global) {
					sto.setBase("%g0");
					sto.setOffset(id);
					//global constant writer here
					m_writer.writeGlobalInit(expr, id, expr.getType(), isStaticFlag);
				}
				else
				{
					//local and static
					if(isStaticFlag)
					{
						sto.setBase("%g0");
						sto.setOffset(fun.getName() + "." + fun.getType().getName() + "." + id);
						m_writer.writeGlobalInit(expr, sto.getOffset(), t, isStaticFlag);
					}
					else {
						sto.setBase("%fp");
						offset -= t.getSize();
						sto.setOffset(Integer.toString(offset));
						if (exprIsConstVar)
							m_writer.writeLocalInitWithVar(sto, expr);
						else
							m_writer.writeLocalInitWithConst(sto, expr);
					}
				}
				m_symtab.insert(sto);
			}
			else if(expr.getType().isFloat())
			{
				ConstSTO sto = new ConstSTO(id, t, ((ConstSTO) expr).getFloatValue());
				if(global) {
					sto.setBase("%g0");
					sto.setOffset(id);
					//global constant writer here
				}
				else
				{
					//local and static
					if(isStaticFlag)
					{
						sto.setBase("%g0");
						sto.setOffset(fun.getName() + "." + fun.getType().getName() + "." + id);
						m_writer.writeGlobalInit(expr, sto.getOffset(), t, isStaticFlag);
					}
					else {
						sto.setBase("%fp");
						offset -= t.getSize();
						sto.setOffset(Integer.toString(offset));
						if (exprIsConstVar)
							m_writer.writeLocalInitWithVar(sto, expr);
						else
							m_writer.writeLocalInitWithConst(sto, expr);
					}
				}
				m_symtab.insert(sto);
			}
			else // else boolean
			{
				ConstSTO sto = new ConstSTO(id, t, ((ConstSTO) expr).getBoolValue());
				if(global) {
					sto.setBase("%g0");
					sto.setOffset(id);
					//global constant writer here
				}
				else
				{
					//local and static
					if(isStaticFlag)
					{
						sto.setBase("%g0");
						sto.setOffset(fun.getName() + "." + fun.getType().getName() + "." + id);
						m_writer.writeGlobalInit(expr, sto.getOffset(), t, isStaticFlag);
					}
					else {
						sto.setBase("%fp");
						offset -= t.getSize();
						sto.setOffset(Integer.toString(offset));
						if (exprIsConstVar)
							m_writer.writeLocalInitWithVar(sto, expr);
						else
							m_writer.writeLocalInitWithConst(sto, expr);
					}
				}
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
		boolean exprIsConstVar = expr.getIsAddressable();
		boolean global = true;
		FuncSTO fun = m_symtab.getFunc();
		if(fun != null)
		{
			global = false;
		}
		//create a new STO and assign it the type of exp
		// proceed as normal
		if(expr != null && expr.isConst())
		{
			if(expr.getType().isInt())
			{
				ConstSTO sto = new ConstSTO(id, ((ConstSTO) expr).getIntValue());
				if(global) {
					sto.setBase("%g0");
					sto.setOffset(id);
					//global constant writer here
				}
				else
				{
					//local and static
					if(isStaticFlag)
					{
						sto.setBase("%g0");
						sto.setOffset(fun.getName() + "." + fun.getType().getName() + "." + id);
						m_writer.writeGlobalInit(expr, sto.getOffset(), expr.getType(), isStaticFlag);
					}
					else {
						sto.setBase("%fp");
						offset -= expr.getType().getSize();
						sto.setOffset(Integer.toString(offset));
						if (exprIsConstVar)
							m_writer.writeLocalInitWithVar(sto, expr);
						else
							m_writer.writeLocalInitWithConst(sto, expr);
					}
				}
				m_symtab.insert(sto);
			}
			else if(expr.getType().isFloat())
			{
				ConstSTO sto = new ConstSTO(id, ((ConstSTO) expr).getFloatValue());
				if(global) {
					sto.setBase("%g0");
					sto.setOffset(id);
					//global constant writer here
				}
				else
				{
					//local and static
					if(isStaticFlag)
					{
						sto.setBase("%g0");
						sto.setOffset(fun.getName() + "." + fun.getType().getName() + "." + id);
						m_writer.writeGlobalInit(expr, sto.getOffset(), expr.getType(), isStaticFlag);
					}
					else {
						sto.setBase("%fp");
						offset -= expr.getType().getSize();
						sto.setOffset(Integer.toString(offset));
						if (exprIsConstVar)
							m_writer.writeLocalInitWithVar(sto, expr);
						else
							m_writer.writeLocalInitWithConst(sto, expr);
					}
				}
				m_symtab.insert(sto);
			}
			else
			{
				ConstSTO sto = new ConstSTO(id, ((ConstSTO) expr).getBoolValue());
				if(global) {
					sto.setBase("%g0");
					sto.setOffset(id);
					//global constant writer here
				}
				else
				{
					//local and static
					if(isStaticFlag)
					{
						sto.setBase("%g0");
						sto.setOffset(fun.getName() + "." + fun.getType().getName() + "." + id);
						m_writer.writeGlobalInit(expr, sto.getOffset(), expr.getType(), isStaticFlag);
					}
					else {
						sto.setBase("%fp");
						offset -= expr.getType().getSize();
						sto.setOffset(Integer.toString(offset));
						if (exprIsConstVar)
							m_writer.writeLocalInitWithVar(sto, expr);
						else
							m_writer.writeLocalInitWithConst(sto, expr);
					}
				}
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
	void DoStructdefDecl(String id, Vector<STO> varList, Vector<STO> funcList, Vector<STO> ctorDtorList)
	{
		if (m_symtab.accessLocal(id) != null)
		{
			m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.redeclared_id, id));
		}

		int count = 0;
		for(int i = 0; i < varList.size(); i++){
			count += varList.get(i).getType().getSize();
		}

		Type type = new StructType(id, count);
		StructdefSTO sto = new StructdefSTO(id, type, varList, funcList, ctorDtorList);

		m_symtab.insert(sto);
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	void DoFuncDecl_1(String id)
	{
		if (m_symtab.accessGlobal(id) != null) {
			if (!funcMap.containsKey(id)) {
				{
					m_nNumErrors++;
					m_errors.print(Formatter.toString(ErrorMsg.redeclared_id, id));
				}
			}
		}

		FuncSTO sto = new FuncSTO(id);
		m_symtab.insert(sto);

		m_symtab.openScope();

		offset = 0;

		// Check 6.3
		sto.setLevel(m_symtab.getLevel());
		m_symtab.setFunc(sto);
	}

	//----------------------------------------------------------------
	// New overloaded function
	//----------------------------------------------------------------
	void DoFuncDecl_1(String id, Type typ)
	{
		if (m_symtab.accessGlobal(id) != null) {
			if (!funcMap.containsKey(id)) {
				{
					m_nNumErrors++;
					m_errors.print(Formatter.toString(ErrorMsg.redeclared_id, id));
				}
			}
		}

		FuncSTO sto = new FuncSTO(id, typ);
		m_symtab.insert(sto);

		m_symtab.openScope();

		offset = 0;
		// Check 6.3
		sto.setLevel(m_symtab.getLevel());
		m_symtab.setFunc(sto);
	}

	//----------------------------------------------------------------
	//	New overloaded function
	//----------------------------------------------------------------
	void DoFuncDecl_1(String id, Type typ, String rtType)
	{
		if (m_symtab.accessGlobal(id) != null) {
			if (!funcMap.containsKey(id)) {
				{
					m_nNumErrors++;
					m_errors.print(Formatter.toString(ErrorMsg.redeclared_id, id));
				}
			}
		}

		FuncSTO sto = new FuncSTO(id, typ);

		// Check 6.2
		if(rtType == "&"){
			sto.setRbr(true);
			//passed by references, so the returned stuff is modifiable
			sto.setIsModifiable(true);
		}
		else{
			sto.setRbr(false);
			sto.setIsAddressable(false);
			sto.setIsModifiable(false);
		}

		m_symtab.insert(sto);

		m_symtab.openScope();

		offset = 0;

		// Check 6.3
		sto.setLevel(m_symtab.getLevel());

		m_symtab.setFunc(sto);
	}

	void writeFuncDecl(String id, Vector<STO> param){
		m_writer.writeFuncDecl(id, param);
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	void DoFuncDecl_2(String id, Vector<STO> params)
	{
		FuncSTO temp = m_symtab.getFunc();
		String hashKey = generateUniqueKey(temp, temp.getParams());

		// Check 13.1
		if(isStruct){
			// Save the structure when it is a struct
			functionSTO = temp;
			if(!(temp.getReturnType() instanceof VoidType)) {
				if (!topLevelFlag) {
					m_nNumErrors++;
					m_errors.print(ErrorMsg.error6c_Return_missing);
					return;
				}
			}
		}
		else{
			// Check 9.1
			if(map.containsKey(hashKey)){
				m_nNumErrors++;
				m_errors.print(Formatter.toString(ErrorMsg.error9_Decl, temp.getName()));
				return;
			}

			// Insert the function into the hashmap
			map.put(hashKey, m_symtab.getFunc());

			// Check 9.2
			// Get the name of the function
			String funcName = temp.getName();
			buildOverloadedHashMap(temp, funcName);

			// Check 6.3
			if(!(temp.getReturnType() instanceof VoidType)) {
				if (!topLevelFlag) {
					m_nNumErrors++;
					m_errors.print(ErrorMsg.error6c_Return_missing);
					return;
				}
			}
		}

		//TODO end of function, reset the local variables offset;
		m_writer.writeFuncDecl2(id, params, offset);

		//cmpCount = 0;
		offset = 0;
		m_symtab.closeScope();

		// Check 6.3
		topLevelFlag = false;

		m_symtab.setFunc(null);
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
			params = new Vector();
		}

		m_symtab.getFunc().setParams(params);

		// Check 14.1
		if(isStruct && recursiveFunc){
			functionSTO = m_symtab.getFunc();
			DoDuplicateFuncCheck();
		}

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
	//
	//----------------------------------------------------------------
	STO DoAssignExpr(STO stoDes, STO expr)
	{
		boolean global = true;
		FuncSTO fun = m_symtab.getFunc();
		if(fun != null)
		{
			global = false;
		}

		//System.out.println("StoDes Type: " + stoDes.getType());
		//System.out.println("Expr Type: " + expr.getType());
		if(stoDes.isError() || stoDes.getType().isError())
		{
			return new ErrorSTO(stoDes.getName());
		}
		else if(expr.getType().isError() || expr.isError())
		{
			return new ErrorSTO(stoDes.getName());
		}
		else if(!stoDes.isModLValue())
		{
			m_nNumErrors++;
			m_errors.print(ErrorMsg.error3a_Assign);
			return new ErrorSTO(stoDes.getName());
		}
		else if(!(stoDes.getType().isAssignableTo(expr.getType())))
		{
			if((expr.getName()).equals("this")){
				//check if expression is this
				m_nNumErrors++;
				m_errors.print(Formatter.toString(ErrorMsg.error3b_Assign, currentStructName, stoDes.getType().getName()));
				return new ErrorSTO(stoDes.getName());
			}
			else{
				//check if expression is array
				m_nNumErrors++;
				m_errors.print(Formatter.toString(ErrorMsg.error3b_Assign, expr.getType().getName(), stoDes.getType().getName()));
				return new ErrorSTO(stoDes.getName());
			}
		}

		if(global)
		{
			//do your stuff here
		}
		else
		{
			if (!expr.isConst())
				m_writer.writeLocalInitWithVar(stoDes, expr);
			else {
				if(stoDes.getType().isFloat() && expr.getType().isInt())
				{
					expr.setBase("%fp");
					offset -= expr.getType().getSize();
					expr.setOffset(Integer.toString(offset));
					m_writer.writeLocalInitWithConst(stoDes, expr);
				}
				else
					m_writer.writeLocalInitWithConst(stoDes, expr);
			}
		}

		return stoDes;
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
	STO DoDesignator4_ID(String strID) {
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
	// Check 1
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

		//if not a constant folding
		if(!a.isConst() || !b.isConst()) {
			// check I.4
			result.setBase("%fp");
			offset -= result.getType().getSize();
			result.setOffset(Integer.toString(offset));

			if (a.getType().isInt() && b.getType().isInt()) {
				cmpCount++;
				m_writer.writeIntegerBinaryArithmeticExpression(a, o, b, result, cmpCount, ifFlag);
			}
			else if(a.getType().isFloat() || b.getType().isFloat())
			{
				//check whether one of them are int
				STO temp = new VarSTO("Temp");

				//if one of them are int
				if(a.getType().isInt() || b.getType().isInt())
				{
					temp.setBase("%fp");
					offset -= a.getType().getSize();
					temp.setOffset(Integer.toString(offset));
				}

				m_writer.writeFloatBinaryArithmeticExpression(a, o, b, result, temp);
			}
		}
			//do stuff...
		return result ;
	}

	void setIfFlag(boolean flag){
		ifFlag = flag;
	}

	//----------------------------------------------------------------
	// Check 1
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

		result.setBase("%fp");
		offset -= result.getType().getSize();
		result.setOffset(Integer.toString(offset));

		if(a.getType().isInt()) {
			m_writer.writeIntegerUnaryArithmeticExpression(a, o, isPre, result);
		}
		else if(a.getType().isFloat()) {
			m_writer.writeFloatUnaryArithmeticExpression(a, o, isPre, result);
		}
		//do stuff...
		return result ;
	}

	//----------------------------------------------------------------
	// Check 4
	//----------------------------------------------------------------
	STO DoBoolCheck(STO expr){

		if(expr instanceof ErrorSTO || expr.getType().isError()) {
			return expr;
		}
		Type varType = expr.getType();
		if(!(varType instanceof BoolType)){
			// handle propagating errors
			m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.error4_Test, varType.getName()));
		}

		m_writer.writeEndofIf(cmpCount);
		return expr;
	}

	public void elseBlock(){
		m_writer.writeAssembly(AssemlyString.PREFIX + "endif." + cmpCount + ":\n\n");
	}


	//----------------------------------------------------------------
	// New helper function to generate the string for the hash map
	// CHECK 5.1
	//----------------------------------------------------------------
	String generateUniqueKey(STO curr, Vector<STO> currParam){
		String name = curr.getName() + delimiter;
		String params = "";

		Iterator<STO> itr = currParam.iterator();
		while(itr.hasNext()){
			params += (itr.next().getType().getName() + delimiter);
		}

		return name + params;
	}

	//----------------------------------------------------------------
	// Check 5
	//----------------------------------------------------------------
	STO DoFuncCall(STO sto, Vector<STO> arguments)
	{
		// Check if the STO coming in is and ErrorSTO
		if(sto instanceof ErrorSTO){
			return sto;
		}

		if (!sto.isFunc())
		{
			m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.not_function, sto.getName()));
			return new ErrorSTO(sto.getName());
		}
		else{
			// Get the function
			FuncSTO temp = (FuncSTO) sto;
			// Build the name mingling
			String hashKey = generateUniqueKey(temp, arguments);
			// Store the size of the argument and the parameters
			int size = temp.getParamSize();
			int paramSize = arguments.size();

			// Check 9b
			if(temp.getOverloaded()){
				Vector<FuncSTO> func = funcMap.get(temp.getName());
				if(func != null){
					// Do error checking for overloaded function
					STO isOverloaded = functionOverloadedCheck(temp, arguments, funcMap.get(temp.getName()));
					if(!(isOverloaded instanceof ErrorSTO)){
						return isOverloaded;
					}
				}
			}
			// Not overloaded
			else{
				// Check 5.1
				// Check if the parameters passed in is equals to the arguments
				if(size != paramSize){
					m_nNumErrors++;
					m_errors.print(Formatter.toString(ErrorMsg.error5n_Call, paramSize, size));
					return new ErrorSTO(sto.getName());
				}
				else{
					// Check if the function declared is in the hash map
					if(!map.containsKey(hashKey)){
						int errorCount = 0;
						for(int i = 0; i < size; i++){
							// Make sure that the argument is valid
							if(arguments.get(i) instanceof ErrorSTO){
								continue;
							}

							// Check for pass by reference
							if(((VarSTO)temp.getParams().get(i)).getPbr()){
								// Check for equivalence
								if(!((temp.getParams().get(i)).getType()).isEquivalentTo(arguments.get(i).getType())){
									m_nNumErrors++;
									m_errors.print(Formatter.toString(ErrorMsg.error5r_Call, arguments.get(i).getType().getName(), (temp.getParams().get(i)).getName(), ((temp.getParams().get(i)).getType()).getName()));
									errorCount++;
								}
								// Check for Modifiable L value
								// exception to array that is passed by references
								else if(!arguments.get(i).isModLValue() && !arguments.get(i).getType().isArray()){
									m_nNumErrors++;
									m_errors.print(Formatter.toString(ErrorMsg.error5c_Call, (temp.getParams().get(i)).getName(), ((temp.getParams().get(i)).getType()).getName()));
									errorCount++;
								}
							}
							else{
								// Pass by value, so check for assignability (Type promotion)
								if(!((temp.getParams().get(i)).getType()).isAssignableTo(arguments.get(i).getType())){
									m_nNumErrors++;
									m_errors.print(Formatter.toString(ErrorMsg.error5a_Call, arguments.get(i).getType().getName(), (temp.getParams().get(i)).getName(), ((temp.getParams().get(i)).getType()).getName()));
									errorCount++;
								}
							}
						}
						// Here if anyone of the variables causes an error, return
						if(errorCount > 0){
							return new ErrorSTO(sto.getName());
						}
						else{
							// Continue
						}
					}
					// Already inside the namespace
					else{
						for(int j = 0; j < size; j++){
							if(((VarSTO)temp.getParams().get(j)).getPbr()){
								// check whether is modifiable, exception to array
								if(!arguments.get(j).isModLValue()){
									if(!arguments.get(j).getType().isArray()){
										m_nNumErrors++;
										m_errors.print(Formatter.toString(ErrorMsg.error5c_Call, (temp.getParams().get(j)).getName(), (temp.getParams().get(j)).getType().getName()));
										sto = new ErrorSTO(sto.getName());
									}
								}
							}
						}
					}
				}
			}
		}

		return generateExpr(sto);
	}

	//----------------------------------------------------------------
	// Check 5 and 9
	//----------------------------------------------------------------
	STO generateExpr(STO sto){
		// Solving pass by reference issue where functions are non modif L val
		ExprSTO exprSTO = new ExprSTO(sto.getName(), sto.getType());
		// Set the addressabilty and modifiability to the current functions's
		exprSTO.setIsAddressable(sto.getIsAddressable());
		exprSTO.setIsModifiable(sto.getIsModifiable());

		return exprSTO;
	}

	//----------------------------------------------------------------
	// Check 7
	//----------------------------------------------------------------
	STO DoExitCheck(STO expr){
		if(expr instanceof ErrorSTO){
			return expr;
		}

		if(expr != null)
		{
			Type varType = expr.getType();
			if (!(varType instanceof IntType)) {
				// handle propagating errors
				m_nNumErrors++;
				m_errors.print(Formatter.toString(ErrorMsg.error7_Exit, varType.getName()));
			}
			m_writer.writeExit(expr);
		}
		return expr;
	}

	//----------------------------------------------------------------
	// Check 9
	//----------------------------------------------------------------
	STO functionOverloadedCheck(FuncSTO curSTO, Vector<STO> arguments, Vector<FuncSTO> functions){
		int found = -1;

		for(int i = 0; i < functions.size(); i++){
			Vector<STO> parameters = functions.get(i).getParams();

			// Check if the number of parameters matches the number of arguments
			if(parameters.size() == arguments.size()){
				if(parameters.size() == 0){
					found = i;
					break;
				}

				// Loop through all the parameters
				for(int j = 0; j < parameters.size(); j++){
					// Make sure that the types do match, else break out of the function
					if(!(parameters.get(j).getType().isEquivalentTo(arguments.get(j).getType()))){
						break;
					}

					int argSize = j + 1;

					// After we validated that the no of params matches the arguments and the type
					if(argSize == parameters.size()){
						// Check if the parameters is a pass by reference
						if(((VarSTO)parameters.get(j)).getPbr()){
							// Check if it is L-modif
							if(arguments.get(j).isModLValue()){
								found = i;
								break;
							}
						}
						// Pass by value
						else{
							found = i;
							break;
						}
					}
				}

			}
		}

		// If after all the checks there are no suitable functions then we print the error
		if(found < 0){
			m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.error9_Illegal, curSTO.getName()));
			return new ErrorSTO(curSTO.getName());
		}
		else{
			// Solving pass by reference issue where functions are non modif L val
			return generateExpr(functions.get(found));
		}
	}

	//----------------------------------------------------------------
	// Check 6.1
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

		m_writer.writeReturn(null, cmpCount);
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
		// Get the return type of the function
		Type returnType = temp.getReturnType();
		// Get the return type of the return stmt
		Type returnStmt = a.getType();
		// Variable to check store the return value.
		STO var;

		// Check 6.3
		if(temp.getLevel() == m_symtab.getLevel()){
			topLevelFlag = true;
		}

		// Need to look up the variable if it is not a constant
		if(a instanceof ConstSTO || a instanceof ExprSTO){
			var = a;
		}
		else{
			//this thing always return VarSTO......
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
			else if(var != null && !var.isModLValue()){
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

		m_writer.writeReturn(a, cmpCount);
		return a;
	}

	//----------------------------------------------------------------
	// New helper function to add functions into hashmap
	// CHECK 9.2
	//----------------------------------------------------------------
	void buildOverloadedHashMap(FuncSTO cur, String curName){
		// Check if the function name exists inside the function hash map
		if(funcMap.containsKey(curName)){
			// If Exists, retrieve from the hash map
			Vector<FuncSTO> v = funcMap.get(curName);
			if(v != null){
				// Set the functions to overloaded
				if(v.firstElement().getOverloaded() != true){
					v.firstElement().setOverloaded(true);
				}

				if(cur.getOverloaded() != true){
					cur.setOverloaded(true);
				}

				v.add(cur);

				// Put the function back into the map
				funcMap.put(curName, v);
			}
		}
		else {
			// If does not exists, then create a new vector with the function name in it
			Vector<FuncSTO> v = new Vector();
			v.add(cur);
			funcMap.put(curName, v);
		}
	}

	//----------------------------------------------------------------
	// Check 10/11
	//----------------------------------------------------------------
	STO DoDesignator2_Arrays(STO des, STO expr)
	{
		// Good place to do the array checks
		if(des.isError() || des.getType().isError() || expr.getType().isError() || expr.isError())
		{
			return new ErrorSTO(des.getName());
		}
		if(des.getType().isNullPointer())
		{
			m_nNumErrors++;
			m_errors.print(ErrorMsg.error15_Nullptr);
			return new ErrorSTO(des.getName());
		}
		//if the designator preceding [] is not array or pointer return error
		if(des != null && !des.getType().isArray() && !des.getType().isPointer())
		{
			m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.error11t_ArrExp, des.getType().getName()));
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
				VarSTO sto = new VarSTO(next.getName(), (ArrayType) next);
				return sto;
			} else if (next.isPointer()){
				VarSTO sto = new VarSTO(next.getName(), (PointerType) next);
				return sto;
			}
			else if(next.isInt()){
				return new VarSTO(des.getName(), new IntType());
			} else if(next.isBool())
			{
				return new VarSTO(des.getName(), new BoolType());
			}
			else {
				return new VarSTO(des.getName(), new FloatType());

			}
		}
		//else it's a pointer no need to do anything lah
		if(des.getType().isPointer())
		{
			Type newType;
			PointerType temp = (PointerType) des.getType();
			if(temp.hasNext()) {
				newType = temp.next();
				return new VarSTO(newType.getName(), newType);
			}
		}
		return des;
	}

	//----------------------------------------------------------------
	// Check 10/11
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
	// Check 12.2
	//----------------------------------------------------------------
	void incrementBreakCounter(){
		// Do keep track of the number of open braces
		breakCounter++;
	}

	//----------------------------------------------------------------
	// Check 12.2
	//----------------------------------------------------------------
	void decrementBreakCounter(){
		// Do keep track of the number of close braces
		breakCounter--;
	}

	//----------------------------------------------------------------
	// Check 12.2
	//----------------------------------------------------------------
	void DoBreakCheck(){
		// Check if the number of open braces matches up
		if(breakCounter == 0){
			m_nNumErrors++;
			m_errors.print(ErrorMsg.error12_Break);
		}
	}

	//----------------------------------------------------------------
	// Check 12.2
	//----------------------------------------------------------------
	void DoContinueCheck() {
		// Check if the number of open braces matches up
		if (breakCounter == 0) {
			m_nNumErrors++;
			m_errors.print(ErrorMsg.error12_Continue);
		}
	}

	//----------------------------------------------------------------
	// Check 13.1
	//----------------------------------------------------------------
	void SetStructFlagAndLoad(String curName){
		// Set the current name
		currentStructName = curName;
		isStruct = true;
	}

	//----------------------------------------------------------------
	// Check 13.1
	//----------------------------------------------------------------
	void ResetStruct(){
		// Should reset the global, for the next struct or function
		currentStructName = "";
		isStruct = false;
	}

	//----------------------------------------------------------------
	// Check 13.1
	//----------------------------------------------------------------
	void DoDuplicateVarCheck(VarSTO curVar){
		String structName = currentStructName + delimiter + curVar.getName();

		// Check if the variable already exists inside the scope
		if(map.containsKey(structName)){
			m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.error13a_Struct, curVar.getName()));
		}
		else{
			// Put it inside the scope otherwise
			map.put(structName, curVar);
		}
	}

	//----------------------------------------------------------------
	// Check 13.2
	//----------------------------------------------------------------
	STO DoCtorDtorCheck(){
		String hashKey = generateUniqueKey(functionSTO, functionSTO.getParams());

		// ctor
		if(!(functionSTO.getName()).startsWith("~")){
			if(!(currentStructName.equals(functionSTO.getName()))){
				m_nNumErrors++;
				m_errors.print(Formatter.toString(ErrorMsg.error13b_Ctor, functionSTO.getName(), currentStructName));
			}
			else{
				if(map.containsKey(hashKey)){
					m_nNumErrors++;
					m_errors.print(Formatter.toString(ErrorMsg.error9_Decl, functionSTO.getName()));
				}
				else{
					map.put(hashKey, functionSTO);
					buildOverloadedHashMap(functionSTO, functionSTO.getName());
				}
			}
		}
		// dtor
		else{
			if(!(("~" + currentStructName).equals(functionSTO.getName()))){
				m_nNumErrors++;
				m_errors.print(Formatter.toString(ErrorMsg.error13b_Dtor, functionSTO.getName(), currentStructName));
			}
			else{
				if(map.containsKey(hashKey)){
					m_nNumErrors++;
					m_errors.print(Formatter.toString(ErrorMsg.error9_Decl, functionSTO.getName()));
				}
				else{
					map.put(hashKey, functionSTO);
					buildOverloadedHashMap(functionSTO, functionSTO.getName());
				}
			}
		}

		FuncSTO temp = functionSTO;
		functionSTO = null;
		return temp;
	}

	//----------------------------------------------------------------
	// Check 13.2
	//----------------------------------------------------------------
	Vector CtorCheck(Vector<STO> ctorDtorList, String structName){
		// Function to check if the struct does not have a constructor
		// If the structs do not contain any ctor or dtor
		if(ctorDtorList.isEmpty()){
			makeCtor(structName, ctorDtorList);
		}
		// if it contains only one dtor
		else if (ctorDtorList.size() == 1){
			if((ctorDtorList.firstElement().getName()).equals("~" + structName)){
				makeCtor(structName, ctorDtorList);
			}
		}
		else{
			// Do nothing
		}

		return ctorDtorList;
	}

	//----------------------------------------------------------------
	// Check 13.2
	//----------------------------------------------------------------
	void makeCtor(String id, Vector<STO> list){
		FuncSTO emptyCtor = new FuncSTO(id);
		emptyCtor.setParams(new Vector());
		list.add(emptyCtor);
	}

	//----------------------------------------------------------------
	// Check 13.1
	//----------------------------------------------------------------
	STO DoDuplicateFuncCheck(){
		String hashKey = currentStructName + delimiter + generateUniqueKey(functionSTO, functionSTO.getParams());

		// Check if the function already inside the global scope
		if(map.containsKey(currentStructName + delimiter + functionSTO.getName())){
			m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.error13a_Struct, functionSTO.getName()));
		}
		// Check our overloaded hash map
		else if(map.containsKey(hashKey)){
			m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.error9_Decl, functionSTO.getName()));
		}
		else{
			// Insert the function inside the global scope and the overloaded hashmap
			map.put(hashKey, functionSTO);
			buildOverloadedHashMap(functionSTO, currentStructName + delimiter + functionSTO.getName());
		}

		FuncSTO temp = functionSTO;
		functionSTO = null;
		return temp;
	}

	//----------------------------------------------------------------
	// Check 14
	//----------------------------------------------------------------
	Type DoStructType_ID(String strID)
	{
		STO sto;

		if(!strID.equals(currentStructName)){
			if ((sto = m_symtab.access(strID)) == null)
			{
				//System.out.println(strID);
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
		// Calls within itself
		else{
		}

		// prob wrong
		return new StructType(strID);

	}

	//----------------------------------------------------------------
	// CHECK 14.1
	//----------------------------------------------------------------
	VarSTO VarTypeDec(Type typ, String id, Vector<STO> arrayList){
		VarSTO temp;

		if (arrayList != null)
		{
			// construct array
			temp = DoDeclArray(id, typ, arrayList);
		}
		else{
			// Construct variable
			temp = new VarSTO(id, typ);
		}

		return temp;
	}

	//----------------------------------------------------------------
	//Check 14.2
	//----------------------------------------------------------------
	FuncSTO getStruct(){
		FuncSTO temp = functionSTO;
		functionSTO = null;
		return temp;
	}

	//----------------------------------------------------------------
	// Check 14.2 | 14.3
	//----------------------------------------------------------------
	STO DoDesignator2_Dot(STO sto, String strID)
	{
		if(sto instanceof ErrorSTO){
			return new ErrorSTO(sto.getName());
		}

		// Check 14.3
		// Check if the keyword is equals to "this" keyword
		if((sto.getName()).equals("this")){
			// Check the hashmap
			if(map.containsKey(currentStructName + delimiter + strID)){
				return map.get(currentStructName + delimiter + strID);
			}
			// Check the overloaded hash map for functions
			else if(funcMap.containsKey(currentStructName + delimiter + strID)){
				return funcMap.get(currentStructName + delimiter + strID).firstElement();
			}
			else{
				// error
				m_nNumErrors++;
				m_errors.print(Formatter.toString(ErrorMsg.error14c_StructExpThis, strID));
				return new ErrorSTO(sto.getName());
			}
		}
		// Not this
		else{
			// Check 14.2
			// Check if the variable is a struct type
			if(!(sto.getType() instanceof StructType)){
				m_nNumErrors++;
				m_errors.print(Formatter.toString(ErrorMsg.error14t_StructExp, sto.getType().getName()));
				return new ErrorSTO(sto.getName());
			}
			// It is a struct type
			else{
				boolean found = false;
				// Retrieve the current struct
				StructdefSTO tempSTO = (StructdefSTO) m_symtab.accessGlobal(sto.getType().getName());

				Vector<STO> vars = tempSTO.getVarList();
				// Iterate the variables
				Iterator<STO> itr = vars.iterator();
				while (itr.hasNext()){
					STO var = itr.next();
					if((var.getName()).equals(strID)){
						sto = var;
						found = true;
					}
				}
				// If we already found the variable, we should return it
				if(found){
					return sto;
				}
				else{
					Vector<STO> funcs = tempSTO.getFuncList();
					// Loop through the method
					itr = funcs.iterator();
					while(itr.hasNext()){
						STO func = itr.next();
						if((func.getName()).equals(strID)){
							sto = func;
							found = true;
						}
					}
					// If even after functions there are no match, throw the error
					if(!found){
						m_nNumErrors++;
						m_errors.print(Formatter.toString(ErrorMsg.error14f_StructExp, strID, sto.getType().getName()));
						return new ErrorSTO(sto.getName());
					}
				}
			}
		}
		return sto;
	}

	//----------------------------------------------------------------
	//Check 14.2
	//----------------------------------------------------------------
	void setRecursiveFuncFalse(){
		recursiveFunc = false;
	}

	//----------------------------------------------------------------
	//Check 14.2
	//----------------------------------------------------------------
	void setRecursiveFuncTrue(){
		recursiveFunc = true;
	}

	//----------------------------------------------------------------
	// Check 15
	//----------------------------------------------------------------
	String GetPointerType(Vector<String> arguments)
	{
		String result = "";
		for(int i = 0; i < arguments.size(); i++) {
			result = result + "*";
		}
		return result;
	}

	//----------------------------------------------------------------
	// Check 15
	//----------------------------------------------------------------
	Type DoPointerType(Type t, Vector<String> arguments)
	{
		PointerType head = null;
		PointerType pointer = null;
		PointerType temp;
		while(!arguments.isEmpty()) {
			String x = arguments.firstElement();
			if (x == "*")
			{
				temp = new PointerType(t.getName() + GetPointerType(arguments), 4);
				if(head == null) {
					head = temp;
				}
				else {
					pointer = head;
					while (pointer.hasNext()) {
						pointer = (PointerType) pointer.next();
					}
					pointer.setChild(temp);
				}
			}

			arguments.remove(x);
		}

		pointer = head;
		while (pointer.hasNext()) {
			pointer = (PointerType) pointer.next();
		}
		if(pointer != null){
			pointer.setChild(t);
		}

		pointer = head;
		while(pointer.hasNext())
		{
			if(pointer.next().isPointer())
				pointer = (PointerType)pointer.next();
			else
				break;
		}
		return head;
	}

	//----------------------------------------------------------------
	//Check 15.1
	//----------------------------------------------------------------
	STO DoArrowCheck(STO sto, String id){
		// Make sure the sto coming in is not an error sto
		if(sto instanceof ErrorSTO){
			return sto;
		}

		// Check 15.1
		if(sto.getType() instanceof NullPointerType) {
			m_nNumErrors++;
			m_errors.print(ErrorMsg.error15_Nullptr);
			return new ErrorSTO(sto.getName());
		}

		// Check if the sto is a struct type. The left side
		if(!(sto.getType().isPointer())){
			m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.error15_ReceiverArrow, sto.getType().getName()));
			return new ErrorSTO(sto.getName());
		}
		// Need to check for variables inside the struct
		// else it's a pointer
		else
		{
			Type nextType;
			// check whether it's pointer to a struct
			PointerType temp = (PointerType)sto.getType();
			if(temp.hasNext())
			{
				nextType = temp.next();
				// if the pointer is not pointing to a struct
				if (!nextType.isStruct())
				{
					m_nNumErrors++;
					m_errors.print(Formatter.toString(ErrorMsg.error15_ReceiverArrow, sto.getType().getName()));
					return new ErrorSTO(sto.getName());
				}
				// Is a structure
				else{
					StructdefSTO struct = (StructdefSTO) m_symtab.accessGlobal(((PointerType) sto.getType()).next().getName());
					Vector<STO> varList = struct.getVarList();
					Vector<STO> funcList = struct.getFuncList();
					boolean found = false;
					int index = -1;

					for(int i = 0; i < varList.size(); i++){
						STO curVar = varList.get(i);
						if((curVar.getName()).equals(id)){
							found = true;
							index = i;
						}
					}

					if(found){
						return varList.get(index);
					}
					else{
						for(int j = 0; j < funcList.size(); j++){
							STO curFunc = funcList.get(j);
							if((curFunc.getName()).equals(id)){
								found = true;
								index = j;
							}
						}

						if(!found) {
							m_nNumErrors++;
							m_errors.print(Formatter.toString(ErrorMsg.error14f_StructExp, id, struct.getName()));
							return new ErrorSTO(sto.getName());
						}
						else{
							return funcList.get(index);
						}
					}
				}
			}
		}
		return sto;
	}

	//----------------------------------------------------------------
	//Check 15.1
	//----------------------------------------------------------------
	STO DoAmpersand(STO sto) {
		if(sto.getType().isError() || sto.isError())
		{
			return sto;
		}
		if (sto.getIsAddressable() == false) {
			m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.error18_AddressOf, sto.getType().getName()));
			return new ErrorSTO(sto.getName());
		}

		Type t = sto.getType();

		PointerType temp = new PointerType(t.getName() + '*', t.getSize());
		temp.setChild(t);

		return new ExprSTO(sto.getName(), temp);

	}

	//----------------------------------------------------------------
	//Check 15.1
	//----------------------------------------------------------------
	STO DoStarCheck(STO sto){
		// Make sure the sto coming in is not an ERRORSTO
		if(sto instanceof ErrorSTO){
			return sto;
		}

		// Check if we trying to derefrenece nullptr
		if(sto.getType() instanceof NullPointerType){
			m_nNumErrors++;
			m_errors.print(ErrorMsg.error15_Nullptr);
			return new ErrorSTO(sto.getName());
		}

		// Check if the refrenced var is a pointer type
		if(!(sto.getType() instanceof PointerType)){
			m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.error15_Receiver, sto.getType().getName()));
			return new ErrorSTO(sto.getName());
		}
		//else it's a pointer type
		else
		{
			Type newType;
			PointerType temp = (PointerType) sto.getType();
			if(temp.hasNext()) {
				newType = temp.next();
				return new VarSTO(newType.getName(), newType);
			}
		}
		return sto;
	}

	//----------------------------------------------------------------
	// Check 16
	//----------------------------------------------------------------
	void DoNewStatement(STO des, Vector<STO> temp)
	{
		if(des.isError())
		{
			return;
		}

		if(temp.isEmpty()){
			if(des.getType() instanceof PointerType && !des.getType().isNullPointer()){
				if(((PointerType) des.getType()).next() instanceof StructType){
					map.size();
					// Find the struct
					StructType struct = (StructType)((PointerType) des.getType()).next();
					StructdefSTO findStruct =(StructdefSTO) m_symtab.accessGlobal(struct.getName());
					DoFuncCall(findStruct.getCtorDtorsList().firstElement(), temp);
				}
				else {
					// Check for L modif if it is a pointer but not struct
					if(!(des.getIsAddressable() && des.getIsModifiable())){
						m_nNumErrors++;
						m_errors.print(ErrorMsg.error16_New_var);
					}
				}
			}
			//if it's modifiable l value
			else if (des.getIsAddressable() && des.getIsModifiable())
			{
				if (!des.getType().isPointer())
				{
					m_nNumErrors++;
					m_errors.print(Formatter.toString(ErrorMsg.error16_New, des.getType().getName()));
				}
			}
			else
			{
				m_nNumErrors++;
				m_errors.print(ErrorMsg.error16_New_var);
			}
		}
		else{
			if(des.getType() instanceof PointerType && !des.getType().isNullPointer()){
				if(((PointerType) des.getType()).next() instanceof StructType){
					// Find the struct
					StructType struct = (StructType)((PointerType) des.getType()).next();
					StructdefSTO findStruct =(StructdefSTO) m_symtab.accessGlobal(struct.getName());
					DoFuncCall(findStruct.getCtorDtorsList().firstElement(), temp);
				}
				else{
					m_nNumErrors++;
					m_errors.print(Formatter.toString(ErrorMsg.error16b_NonStructCtorCall, des.getType().getName()));
				}
			}
			//if it's modifiable l value
			else if (des.getIsAddressable() && des.getIsModifiable())
			{
				if (!des.getType().isPointer())
				{
					m_nNumErrors++;
					m_errors.print(Formatter.toString(ErrorMsg.error16_New, des.getType().getName()));
				}
			}
			else
			{
				m_nNumErrors++;
				m_errors.print(ErrorMsg.error16_New_var);
			}
		}

	}

	//----------------------------------------------------------------
	// Check 16
	//----------------------------------------------------------------
	void DoDeleteStatement(STO des) {
		if (des.isError()) {
			return;
		}
		//if it's modifiable l value
		if (des.getIsAddressable() && des.getIsModifiable())
		{
			if (!des.getType().isPointer())
			{
				m_nNumErrors++;
				m_errors.print(Formatter.toString(ErrorMsg.error16_Delete, des.getType().getName()));
			}
		}
		else
		{
			m_nNumErrors++;
			m_errors.print(ErrorMsg.error16_Delete_var);
		}
	}

	//----------------------------------------------------------------
	//----------------------------------------------------------------
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
	// Check 19
	//----------------------------------------------------------------
	int GetSize(Vector<STO> arguments)
	{
		int result = 1;
		for(STO x : arguments) {
			if(x.isConst())
				result = result * ((ConstSTO)x).getIntValue();
		}
		return result;
	}

	//----------------------------------------------------------------
	//Check 19
	//----------------------------------------------------------------
	STO DoSizeCheck(STO sto){

		if(!(sto.getType() instanceof Type)){
			m_nNumErrors++;
			m_errors.print(ErrorMsg.error19_Sizeof);
			return new ErrorSTO(sto.getName());
		}

		if(!sto.getIsAddressable()){
			m_nNumErrors++;
			m_errors.print(ErrorMsg.error19_Sizeof);
			return new ErrorSTO(sto.getName());
		}

		// Make an R val constant
		ConstSTO size = new ConstSTO("sizeof(" + sto.getType().getName() + ")", sto.getType().getSize());
		size.setIsAddressable(false);
		size.setIsModifiable(false);

		return size;

	}

	//----------------------------------------------------------------
	//Check 19
	//----------------------------------------------------------------
	STO DoSizeCheck(Type typ, Vector<STO> arguments) {
		int size = 1;
		if (arguments != null)
			size = GetSize(arguments);

		//if the type is pointer get the size of what it's pointing to
		ConstSTO sto = new ConstSTO("sizeof", new IntType(), size * typ.getSize());
		sto.setIsAddressable(false);
		return sto;
	}

	//----------------------------------------------------------------
	//Check 20
	//----------------------------------------------------------------
	STO DoTypeCast(Type t, STO sto)
	{
		//basic types
		if(sto.getType().isBasic() && t.isBasic())
		{
			//bool -> int / float
			if(sto.getType().isBool() && t.isNumeric())
			{
				if(sto.isConst())
				{
					//if false
					if (((ConstSTO)sto).getBoolValue() == false)
						return new ConstSTO(sto.getName(), t, 0);
					//else true
					else
						return new ConstSTO(sto.getName(), t, 1);
				}
				//not constant
				else
					return new ExprSTO(sto.getName(), t);
			}
			//float / int -> bool
			else if(sto.getType().isNumeric() && t.isBool())
			{
				if(sto.isConst())
				{
					//float -> bool
					if(sto.getType().isFloat())
					{
						if(((ConstSTO)sto).getFloatValue() == 0.0)
							return new ConstSTO(sto.getName(), t, false);
						else
							return new ConstSTO(sto.getName(), t, true);
					}
					//int -> bool
					else
					{
						if(((ConstSTO)sto).getIntValue() == 0)
							return new ConstSTO(sto.getName(), t, false);
						else
							return new ConstSTO(sto.getName(), t, true);
					}
				}
				else
					return new ExprSTO(sto.getName(), t);
			}
			else if(sto.getType().isInt() && t.isFloat())
			{
				if(sto.isConst())
					return new ConstSTO(sto.getName(), t, (float) ((ConstSTO)sto).getIntValue());
				//not constant
				else
					return new ExprSTO(sto.getName(), t);
			}
			else if(sto.getType().isFloat() && t.isInt())
			{
				if(sto.isConst())
					return new ConstSTO(sto.getName(), t, (int) ((ConstSTO)sto).getFloatValue());
					//not constant
				else
					return new ExprSTO(sto.getName(), t);
			}
			// int -> int
			// float -> float
			return new ExprSTO(sto.getName(), t);
		}
		// pointers can be cast to any type except nullpointer
		// make sure sto is pointer AND not nullptr
		// make sure t is not nullptr
		else if(sto.getType().isPointer() && !sto.getType().isNullPointer() && !t.isNullPointer())
		{
			return new ExprSTO(sto.getName(), t);
		}
		//sto is not basic type and not pointer type.
		//sto basic type, t pointer type
		else if (sto.getType().isBasic() && !t.isNullPointer() && t.isPointer())
		{
			return new ExprSTO(sto.getName(), t);
		}

		m_nNumErrors++;
		m_errors.print(Formatter.toString(ErrorMsg.error20_Cast, sto.getType().getName(), t.getName()));
		return new ErrorSTO(sto.getName());
	}

	void isStatic(boolean flag){
		isStaticFlag = flag;
	}

	void isPre(boolean flag){
		isPre = flag;
	}


	//////////////////// PROJECT 2 STUFF ///////////////////////
	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	void DoCout(STO sto)
	{
		if(sto.isConst())
		{
			//handle literal -> const that is not addressable
			if(!sto.getIsAddressable()) {
				//if type is null then it's string literal
				if (sto.getType() == null) {
					m_writer.writeStringCout(sto.getName());
				}
				else if (sto.getType().isInt()) {
					m_writer.writeIntLiteralCout(sto.getName(), Integer.toString(((ConstSTO)sto).getIntValue()));
				}
				else if (sto.getType().isFloat()) {
					m_writer.writeFloatLiteralCout(sto.getName(), Float.toString(((ConstSTO) sto).getFloatValue()));
				}
				else // boolean
				{
					if(((ConstSTO)sto).getBoolValue())
					{
						//if true
						m_writer.writeBoolLiteralCout(sto.getName(), "1");
					}
					else
					{
						m_writer.writeBoolLiteralCout(sto.getName(), "0");
					}
				}
			}
			else
				m_writer.writeVarCout(sto);
		}
		else
			m_writer.writeVarCout(sto);
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	void DoEndlCout()
	{
		m_writer.writeEndlCout();
	}
}
