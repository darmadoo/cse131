/**
 * Created by Darmadoo on 10/7/15.
 */
class IntType extends NumericType{

    public IntType(String strName, int size)
    {
        super(strName, size);
    }

    public boolean  isInt()	    { return false; }
}
