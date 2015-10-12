/**
 * Created by Darmadoo on 10/9/15.
 */
public class OrOp extends BooleanOp {

    @Override
    STO checkOperands(STO a, STO b) {
        Type aType = a.getType();
        Type bType = b.getType();
        if (!(aType instanceof BoolType) || !(bType instanceof BoolType)) {
            // error when one of them is not numeric
            if(!(aType instanceof BoolType) && !(bType instanceof BoolType)){
                return new ErrorSTO(Formatter.toString(ErrorMsg.error1w_Expr, aType.getName(), "||", new BoolType("bool", 4).getName()));
            }
            else if(!(bType instanceof BoolType)){
                return new ErrorSTO(Formatter.toString(ErrorMsg.error1w_Expr, bType.getName(), "||", aType.getName()));
            }
            else{
                return new ErrorSTO(Formatter.toString(ErrorMsg.error1w_Expr, aType.getName(), "||", bType.getName()));
            }
        }
        else {
            return new ExprSTO(a.getName() + " || " + b.getName(), new BoolType("bool", 4));
        }
    }
}