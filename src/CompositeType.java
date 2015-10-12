/**
 * Created by Darmadoo on 10/7/15.
 */
class CompositeType extends Type{

    public CompositeType(String strName, int size)
    {
        super(strName, size);
    }

    public Boolean isAssignableTo(Type t){
        if(t.isComposite()){
            return true;
        }

        return false;
    }

    public Boolean isEquivalentTo(Type t){
        if(t.isComposite()){
            return true;
        }

        return false;
    }

    public boolean  isComposite()	    { return true; }
}
