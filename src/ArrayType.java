/**
 * Created by Darmadoo on 10/7/15.
 */
class ArrayType extends CompositeType {
    private int dimensions;
    private Type next;

    public ArrayType()
    {
        super("array",0);
        dimensions = 0;
    }

    public ArrayType(String strName, int size)
    {
        super(strName, size);
        dimensions = size;
        next = null;
    }

    public void setDimension(int size)
    {
        dimensions = size;
    }

    public int getDimension()
    {
        return dimensions;
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
