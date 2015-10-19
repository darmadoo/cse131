/**
 * Created by Darmadoo on 10/7/15.
 */
class PointerType extends CompositeType{
    private Type child;

    public PointerType(String strName, int size)
    {
        super(strName, size);
    }

    public boolean hasNext()
    {
        if(child != null)
            return true;
        else
            return false;
    }

    public void setChild(Type next)
    {
        child = next;
    }

    public Type next()
    {
        return child;
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
