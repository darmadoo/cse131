/**
 * Created by Darmadoo on 10/7/15.
 */
class ArrayType extends CompositeType {

    public ArrayType(String strName, int size)
    {
        super(strName, size);
    }

    public Boolean isAssignableTo(Type t){
        if(t.isArray()){
            return true;
        }

        return false;
    }

    public Boolean isEquivalentTo(Type t){
        if(t.isArray()){
            return true;
        }

        return false;
    }

    public boolean  isArray()	    { return true; }
}
