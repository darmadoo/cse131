/**
 * Created by Darmadoo on 10/7/15.
 */
class FloatType extends NumericType {

    public FloatType(){
        super("float", 4);
    }

    public FloatType(String strName, int size)
    {
        super(strName, size);
    }

    public Boolean isAssignableTo(Type t){
        if(t.isFloat()){
            return true;
        }
        if(t.isInt()){
            return true;
        }
        return false;
    }

    public Boolean isEquivalentTo(Type t){
        if(t.isFloat()){
            return true;
        }

        return false;
    }

    public boolean  isFloat()	    { return true; }
}
