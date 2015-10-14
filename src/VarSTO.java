//---------------------------------------------------------------------
// CSE 131 Reduced-C Compiler Project
// Copyright (C) 2008-2015 Garo Bournoutian and Rick Ord
// University of California, San Diego
//---------------------------------------------------------------------

class VarSTO extends STO
{
	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public VarSTO(String strName)
	{
		super(strName);
		// You may want to change the isModifiable and isAddressable 
		// fields as necessary
		super.setIsAddressable(true);
		super.setIsModifiable(true);
	}

	public VarSTO(String strName, Type typ)
	{
		super(strName, typ);
		// You may want to change the isModifiable and isAddressable 
		// fields as necessary
		super.setIsAddressable(true);
		super.setIsModifiable(true);
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public boolean isVar() 
	{
		return true;
	}

	private boolean pbr = false;

	public void setPbr(boolean p){
		pbr = p;
	}

	public boolean getPbr(){
		return pbr;
	}
}
