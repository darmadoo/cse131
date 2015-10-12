/**
 * Created by Darmadoo on 10/7/15.
 */
class StructType extends CompositeType{

    public StructType(String strName, int size)
    {
        super(strName, size);
    }

    public Boolean isAssignableTo(Type t){
        if(t.isStruct()){
            return true;
        }

        return false;
    }

    public Boolean isEquivalentTo(Type t){
        if(t.isStruct()){
            return true;
        }

        return false;
    }

    public boolean  isStruct()	    { return true; }
}
