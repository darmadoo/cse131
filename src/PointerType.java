/**
 * Created by Darmadoo on 10/7/15.
 */
class PointerType extends CompositeType{

    public PointerType(String strName, int size)
    {
        super(strName, size);
    }

    public Boolean isAssignableTo(Type t){
        if(t.isPointer()){
            return true;
        }

        return false;
    }

    public Boolean isEquivalentTo(Type t){
        if(t.isPointer()){
            return true;
        }

        return false;
    }

    public boolean  isPointer()	    { return true; }
}
