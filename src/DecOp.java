public class DecOp extends UnaryOp {

    @Override
    STO checkOperands(STO a) {
        Type aType = a.getType();

        //check if it's modifiable L value
        if(a.getIsAddressable() && a.getIsModifiable()) {
            if (!(aType instanceof NumericType)) {
                // error when one of them is not numeric
                return new ErrorSTO(Formatter.toString(ErrorMsg.error2_Type, aType.getName(), "--"));
            } else if (aType instanceof IntType) {
                // Int++ = Int
                // return ExprSTO of int types
                return new ExprSTO(a.getName() + "--", new IntType("int", 4));
            } else {
                // Float++ = Float
                // return ExprSTO of float type
                return new ExprSTO(a.getName() + "--", new IntType("int", 4));
            }
        }
        //else return error message
        else
            return new ErrorSTO(Formatter.toString(ErrorMsg.error2_Lval,  "++"));
    }
}