/**
 * Created by Darmadoo on 10/7/15.
 */
class BoolType extends BasicType{

    public BoolType(){
        super("bool", 4);
    }

    public BoolType(String strName, int size)
    {
        super(strName, size);
    }

    public Boolean isAssignableTo(Type t){
        if(t.isBool()){
            return true;
        }

        return false;
    }

    public Boolean isEquivalentTo(Type t){
        if(t.isBool()){
            return true;
        }

        return false;
    }

    public boolean  isBool()	    { return true; }
}
