//---------------------------------------------------------------------
// CSE 131 Reduced-C Compiler Project
// Copyright (C) 2008-2015 Garo Bournoutian and Rick Ord
// University of California, San Diego
//---------------------------------------------------------------------

//---------------------------------------------------------------------
// For structdefs
//---------------------------------------------------------------------

import java.util.Iterator;
import java.util.Vector;

class StructdefSTO extends STO
{
	// Check 14.1
	private Vector<STO> vars, funcs, ctors;
	private FuncSTO dtor;
	private int size;
	//------------------
	// ----------------------------------------------
	//
	//----------------------------------------------------------------
	public StructdefSTO(String strName)
	{
		super(strName);
		super.setIsModifiable(true);
		super.setIsAddressable(true);
	}

	public StructdefSTO(String strName, Type typ, Vector<STO> varList, Vector<STO> funcList, Vector<STO> ctorDtorList)
	{
		super(strName, typ);
		vars = varList;
		funcs = funcList;
		size = getStructSize(varList);
		// Check 14.1
		parseDtor(ctorDtorList, strName);
		super.setIsModifiable(true);
		super.setIsAddressable(true);
	}

	int getStructSize(Vector<STO> var){
		int count = 0;
		for(int i = 0; i < var.size(); i++){
			count += var.get(i).getType().getSize();
		}
		return count;
	}

	// Check 14.1
	void parseDtor(Vector<STO> ctorDtors, String curStrucName){
		Iterator<STO> tempItr = ctorDtors.iterator();
		ctors = new Vector<>();

		while(tempItr.hasNext()){
			STO temp = tempItr.next();

			if((temp.getName()).startsWith("~")){
				if(("~" + curStrucName).equals(temp.getName()) && dtor == null){
					dtor = (FuncSTO)temp;
				}
			}
			else{
				if((temp.getName().equals(curStrucName))){
					ctors.add(temp);
				}
			}
		}
	}

	Vector<STO> getVarList(){
		return vars;
	}

	Vector<STO> getFuncList(){
		return funcs;
	}

	Vector<STO> getCtorDtorsList(){
		return ctors;
	}

	STO getDtorsList(){
		return dtor;
	}

	int getSize(){
		return size;
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public boolean isStructdef()
	{
		return true;
	}
}
