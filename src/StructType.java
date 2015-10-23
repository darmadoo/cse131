/**
 * Created by Darmadoo on 10/7/15.
 */
class StructType extends CompositeType{

    public StructType(String strName){
        super(strName, 4);
    }

    public StructType(String strName, int size)
    {
        super(strName, size);
    }

    public Boolean isAssignableTo(Type t){
        if(t.isStruct()){
            if((this.getName()).equals(t.getName())){
                return true;
            }
        }

        return false;
    }

    public Boolean isEquivalentTo(Type t){
        if(t.isStruct()){
            if((this.getName()).equals(t.getName())){
                return true;
            }
        }

        return false;
    }

    public boolean  isStruct()	    { return true; }
}
