/**
 * Created by Darmadoo on 10/7/15.
 */
class BasicType extends Type{

    public BasicType(String strName, int size)
    {
        super(strName, size);
    }

    public Boolean isAssignableTo(Type t){
        if(t.isBasic()){
            return true;
        }

        return false;
    }

    public Boolean isEquivalentTo(Type t){
        if(t.isBasic()){
            return true;
        }

        return false;
    }

    public boolean  isBasic()	    { return true; }
}
