/**
 * Created by Darmadoo on 10/9/15.
 */
public class NotEqualOp extends ComparisonOp {

    @Override
    STO checkOperands(STO a, STO b) {
        Type aType = a.getType();
        Type bType = b.getType();
        if (!(aType instanceof NumericType) || !(bType instanceof NumericType)) {
            // error when one of them is not numeric
            return new ErrorSTO(Formatter.toString(ErrorMsg.error1n_Expr, aType.getName(), "!="));
        }
        else if (!(aType instanceof BoolType) || !(bType instanceof BoolType)) {
            // error when one of them is not Bool
            return new ErrorSTO(Formatter.toString(ErrorMsg.error1n_Expr, aType.getName(), "!="));
        }
        else {
            return new ExprSTO(a.getName() + " != " + b.getName(), new BoolType("bool", 4));
        }
    }

}