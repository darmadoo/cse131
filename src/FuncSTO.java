//---------------------------------------------------------------------
// CSE 131 Reduced-C Compiler Project
// Copyright (C) 2008-2015 Garo Bournoutian and Rick Ord
// University of California, San Diego
//---------------------------------------------------------------------

import java.util.Vector;

class FuncSTO extends STO
{
	private Type m_returnType;
	private Vector<STO> currentFunc;
	private int level;
	private boolean overloaded = false;

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public FuncSTO(String strName)
	{
		super (strName);
		setReturnType(null);
		// You may want to change the isModifiable and isAddressable
		// fields as necessary
		super.setIsAddressable(true);
		super.setIsModifiable(false);
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public FuncSTO(String strName, Type typ)
	{
		super (strName, typ);
		setReturnType(typ);
		// You may want to change the isModifiable and isAddressable
		// fields as necessary
		super.setIsAddressable(true);
		super.setIsModifiable(false);
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public boolean isFunc() 
	{ 
		return true;
		// You may want to change the isModifiable and isAddressable                      
		// fields as necessary
	}

	//----------------------------------------------------------------
	// This is the return type of the function. This is different from 
	// the function's type (for function pointers - which we are not 
	// testing in this project).
	//----------------------------------------------------------------
	public void setReturnType(Type typ)
	{
		m_returnType = typ;
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public Type getReturnType ()
	{
		return m_returnType;
	}

	public void setParams(Vector<STO> vec){
		currentFunc = vec;
	}

	public Vector<STO> getParams(){
		return currentFunc;
	}

	public int getParamSize(){
		return currentFunc.size();
	}

	private boolean rbr = false;

	public void setRbr(boolean p){
		rbr = p;
	}

	public boolean getRbr(){
		return rbr;
	}

	// 6.3
	public int getLevel(){
		return level;
	}

	// 6.3
	public void setLevel(int currentLevel){
		level = currentLevel;
	}

	// 9b
	public void setOverloaded(boolean x){ overloaded = x; }

	// 9b
	public boolean getOverloaded() { return  overloaded; }
}
