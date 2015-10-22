public class IncOp extends UnaryOp {

    @Override
    STO checkOperands(STO a) {
        Type aType = a.getType();

        if (!(aType instanceof NumericType) && !(aType instanceof PointerType)) {
            // error when one of them is not numeric
            return new ErrorSTO(Formatter.toString(ErrorMsg.error2_Type, aType.getName(), "++"));
        }
        //check if it's modifiable L value
        if(a.getIsAddressable() && a.getIsModifiable()) {
            if (aType instanceof IntType) {
                // Int++ = Int
                // return ExprSTO of int types
                if(a.isConst())
                    return new ConstSTO(a.getName() + "++", new IntType("int", 4), ((ConstSTO) a).getIntValue() + 1 );
                else
                    return new ExprSTO(a.getName() + "++", new IntType("int", 4));
            } else if(aType instanceof FloatType) {
                // Float++ = Float
                // return ExprSTO of float type
                if(a.isConst())
                    return new ConstSTO(a.getName() + "++", new FloatType("float", 4), ((ConstSTO) a).getFloatValue() + 1 );
                else
                    return new ExprSTO(a.getName() + "++", new FloatType("float", 4));
            } else { // else it's pointer type
                return new ExprSTO(a.getName() + "++", aType);
            }
        }
        else
            return new ErrorSTO(Formatter.toString(ErrorMsg.error2_Lval, "++"));
    }
}
