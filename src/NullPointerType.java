/**
 * Created by Darmadoo on 10/7/15.
 */
class NullPointerType extends PointerType{

    public NullPointerType(String strName, int size)
    {
        super(strName, size);
    }

    public Boolean isAssignableTo(Type t){
        if(t.isNullPointer()){
            return true;
        }

        return false;
    }

    public Boolean isEquivalentTo(Type t){
        if(t.isNullPointer()){
            return true;
        }

        return false;
    }

    public boolean  isNullPointer()	    { return true; }
}
