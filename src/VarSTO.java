//---------------------------------------------------------------------
// CSE 131 Reduced-C Compiler Project
// Copyright (C) 2008-2015 Garo Bournoutian and Rick Ord
// University of California, San Diego
//---------------------------------------------------------------------

import java.math.BigDecimal;

class VarSTO extends STO
{
	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	private BigDecimal		m_value;

	private int offset;
	private String insideStruct;
	private boolean isSet;

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

	public VarSTO(String strName, int val)
	{
		super(strName, new IntType());
		m_value = new BigDecimal(val);
		// You may want to change the isModifiable and isAddressable
		// fields as necessary
		super.setIsAddressable(true);
		super.setIsModifiable(true);
	}

	public VarSTO(String strName, double val)
	{
		super(strName, new FloatType());
		m_value = new BigDecimal(val);
		// You may want to change the isModifiable and isAddressable
		// fields as necessary
		super.setIsAddressable(true);
		super.setIsModifiable(true);
	}

	public VarSTO(String strName, boolean val)
	{
		super(strName, new BoolType());
		if(val == true) {
			m_value = new BigDecimal(1);
		}
		else
			m_value = new BigDecimal(0);
		// You may want to change the isModifiable and isAddressable
		// fields as necessary
		super.setIsAddressable(true);
		super.setIsModifiable(true);
	}

	public VarSTO(String strName, Type typ, int val)
	{
		super(strName, typ);
		m_value = new BigDecimal(val);
		// You may want to change the isModifiable and isAddressable
		// fields as necessary
		super.setIsAddressable(true);
		super.setIsModifiable(true);
	}

	public VarSTO(String strName, Type typ, double val)
	{
		super(strName, typ);
		m_value = new BigDecimal(val);
		// You may want to change the isModifiable and isAddressable
		// fields as necessary
		super.setIsAddressable(true);
		super.setIsModifiable(true);
	}

	public VarSTO(String strName, Type typ, boolean val)
	{
		super(strName, typ);
		if(val == true) {
			m_value = new BigDecimal(1);
		}
		else
			m_value = new BigDecimal(0);
		// You may want to change the isModifiable and isAddressable
		// fields as necessary
		super.setIsAddressable(true);
		super.setIsModifiable(true);
	}

	public BigDecimal getM_value(){
		return m_value;
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

	public void setInsideStruct(String t){
		insideStruct = t;
	}

	public String getInsideStruct(){
		return insideStruct;
	}

	public void setStructOffset(int i){
		offset = i;
		isSet = true;
	}

	public int getStructOffset(){
		return offset;
	}

	public boolean getisSet(){
		return isSet;
	}

	public void setisSet(boolean flag){
		isSet = flag;
	}
	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public BigDecimal getValue()
	{
		return m_value;
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public int getIntValue()
	{
		return m_value.intValue();
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public float getFloatValue()
	{
		return m_value.floatValue();
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public boolean getBoolValue()
	{
		return !BigDecimal.ZERO.equals(m_value);
	}

}
