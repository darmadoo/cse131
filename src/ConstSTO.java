//---------------------------------------------------------------------
// CSE 131 Reduced-C Compiler Project
// Copyright (C) 2008-2015 Garo Bournoutian and Rick Ord
// University of California, San Diego
//---------------------------------------------------------------------

import java.math.BigDecimal;

class ConstSTO extends STO
{
    //----------------------------------------------------------------
    //	Constants have a value, so you should store them here.
    //	Note: We suggest using Java's BigDecimal class, which can hold
    //	floats and ints. You can then do .floatValue() or 
    //	.intValue() to get the corresponding value based on the
    //	type. Booleans/Ptrs can easily be handled by ints.
    //	Feel free to change this if you don't like it!
    //----------------------------------------------------------------
    private BigDecimal		m_value;

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public ConstSTO(String strName)
	{
		super(strName);
		m_value = null; // fix this
		// You may want to change the isModifiable and isAddressable
		// fields as necessary
		super.setIsAddressable(false);
		super.setIsModifiable(false);
	}

	/*
	// Set R Type
	public ConstSTO(String strName, Type typ, int val, boolean rtype){
		super(strName);
		m_value = null; // fix this
		// You may want to change the isModifiable and isAddressable
		// fields as necessary
		super.setIsAddressable(false);
		super.setIsModifiable(false);
	}
*/
	public ConstSTO(String strName, Type typ)
	{
		super(strName, typ);
		m_value = null; // fix this
		// You may want to change the isModifiable and isAddressable
		// fields as necessary
		super.setIsAddressable(true);
		super.setIsModifiable(false);
	}

	public ConstSTO(String strName, Type typ, int val)
	{
		super(strName, typ);
		m_value = new BigDecimal(val);
		// You may want to change the isModifiable and isAddressable
		// fields as necessary
		super.setIsAddressable(true);
		super.setIsModifiable(false);
	}

	public ConstSTO(String strName, Type typ, double val)
	{
		super(strName, typ);
		m_value = new BigDecimal(val);
		// You may want to change the isModifiable and isAddressable
		// fields as necessary
		super.setIsAddressable(true);
		super.setIsModifiable(false);
	}

	public ConstSTO(String strName, Type typ, boolean val)
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
		super.setIsModifiable(false);
	}

	public ConstSTO(String strName, int val)
	{
		super(strName, new IntType());
		m_value = new BigDecimal(val);
		// You may want to change the isModifiable and isAddressable
		// fields as necessary
		super.setIsAddressable(true);
		super.setIsModifiable(false);
	}

	public ConstSTO(String strName, double val)
	{
		super(strName, new FloatType());
		m_value = new BigDecimal(val);
		// You may want to change the isModifiable and isAddressable
		// fields as necessary
		super.setIsAddressable(true);
		super.setIsModifiable(false);
	}

	public ConstSTO(String strName, boolean val)
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
		super.setIsModifiable(false);
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public boolean isConst() 
	{
		return true;
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
