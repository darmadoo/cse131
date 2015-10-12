/**
 * Created by Darmadoo on 10/9/15.
 */
public class NotOp extends UnaryOp {

    // SHOULD ONLY TAKE IN ONE PARAMETER
    @Override
    STO checkOperands(STO a) {
        Type aType = a.getType();
        if (!(aType instanceof BoolType)) {
            // error when one of them is not numeric
            return new ErrorSTO(Formatter.toString(ErrorMsg.error1u_Expr, aType.getName(), "!", new BoolType("bool", 4).getName()));
        }
        else {
            return new ExprSTO("!" + a.getName(), new BoolType("bool", 4));
        }
    }

}