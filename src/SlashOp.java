/**
 * Created by Darmadoo on 10/8/15.
 */
public class SlashOp extends ArithmeticOp {

    @Override
    STO checkOperands(STO a, STO b) {
        Type aType = a.getType();
        Type bType = b.getType();
        if (!(aType instanceof NumericType) || !(bType instanceof NumericType)) {
            // error when one of them is not numeric
            return new ErrorSTO(Formatter.toString(ErrorMsg.error1n_Expr, aType.getName(), "/"));
        } else if (aType instanceof IntType && bType instanceof IntType) {
            // Int + Int = Int
            // return ExprSTO of int types
            return new ExprSTO(a.getName() + " / " + b.getName(), new IntType("int", 4));
        } else {
            // Float + int = Float
            // Float + Float = Float
            // return ExprSTO of float type
            return new ExprSTO(a.getName() + " / " + b.getName(), new IntType("float", 4));
        }
    }

}

