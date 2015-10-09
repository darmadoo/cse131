/**
 * Created by Darmadoo on 10/8/15.
 */
public class LessThanOp extends ComparisonOp {

    @Override
    STO checkOperands(STO a, STO b) {
        Type aType = a.getType();
        Type bType = b.getType();
        if (!(aType instanceof NumericType) || !(bType instanceof NumericType)) {
            // error when one of them is not numeric
            return new ErrorSTO(Formatter.toString(ErrorMsg.error1n_Expr, aType.getName(), "<"));
        } else {
            return new ExprSTO("", new BoolType("bool", 4));
        }
    }

}