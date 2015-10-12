/**
 * Created by Darmadoo on 10/7/15.
 */
class NumericType extends BasicType{

    public NumericType(String strName, int size)
    {
        super(strName, size);
    }

    public Boolean isAssignableTo(Type t){
        if(t.isNumeric()){
            return true;
        }

        return false;
    }

    public Boolean isEquivalentTo(Type t){
        if(t.isNumeric()){
            return true;
        }

        return false;
    }

    public boolean  isNumeric()	    { return true; }
}
