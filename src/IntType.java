/**
 * Created by Darmadoo on 10/7/15.
 */
class IntType extends NumericType{

    public IntType(String strName, int size)
    {
        super(strName, size);
    }

    public Boolean isAssignableTo(Type t){
        if(t.isInt()){
            return true;
        }

        return false;
    }

    public Boolean isEquivalentTo(Type t){
        if(t.isInt()){
            return true;
        }

        return false;
    }

    public boolean  isInt()	    { return true; }
}
