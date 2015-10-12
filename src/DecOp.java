/**
 * Created by clapichacu on 10/9/15.
 */
public class DecOp extends UnaryOp {

    @Override
    STO checkOperands(STO a, STO b) {
        Type aType = a.getType();
        return new ErrorSTO(Formatter.toString(ErrorMsg.error2_Type, aType.getName(), "++"));
    }

    STO checkOperands(STO a) {
        Type aType = a.getType();
        if (!(aType instanceof NumericType)) {
            // error when one of them is not numeric
            return new ErrorSTO(Formatter.toString(ErrorMsg.error2_Type, aType.getName(), "--"));
        } else if (aType instanceof IntType) {
            // Int-- = Int
            // return ExprSTO of int types
            return new ExprSTO(a.getName() + "--" , new IntType("int", 4));
        } else {
            // Float-- = Float
            // return ExprSTO of float type
            return new ExprSTO(a.getName() + "--", new IntType("int", 4));
        }
    }

} 