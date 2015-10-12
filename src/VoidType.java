/**
 * Created by Darmadoo on 10/7/15.
 */
class VoidType extends Type{

    public VoidType(String strName, int size)
    {
        super(strName, size);
    }

    public Boolean isAssignableTo(Type t){
        if(t.isVoid()){
            return true;
        }

        return false;
    }

    public Boolean isEquivalentTo(Type t){
        if(t.isVoid()){
            return true;
        }

        return false;
    }

    public boolean  isVoid()	    { return true; }
}
