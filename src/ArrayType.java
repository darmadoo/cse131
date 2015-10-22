/**
 * Created by Darmadoo on 10/7/15.
 */
class ArrayType extends CompositeType {
    private int dimensions;
    private Type child;

    public ArrayType()
    {
        super("array",0);
        dimensions = 0;
        child = null;
    }

    public ArrayType(String strName, int size, int dimensions)
    {
        super(strName, size);
        this.dimensions = dimensions;
        child = null;
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
    public int getDimensions()
    {
        return dimensions;
    }
    public Boolean isAssignableTo(Type t){
        if(t.isArray() && t.getName() == this.getName()){
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
