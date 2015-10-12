/**
 * Created by Darmadoo on 10/9/15.
 */
public class BarOp extends BitwiseOp {

    @Override
    STO checkOperands(STO a, STO b) {
        Type aType = a.getType();
        Type bType = b.getType();
        if (!(aType instanceof IntType) || !(bType instanceof IntType)) {
            // error when one of them is not numeric
            if(!(aType instanceof IntType && !(bType instanceof IntType))){
                return new ErrorSTO(Formatter.toString(ErrorMsg.error1w_Expr, aType.getName(), "|", new IntType("int", 4).getName()));
            }
            else if(!(bType instanceof IntType)){
                return new ErrorSTO(Formatter.toString(ErrorMsg.error1w_Expr, bType.getName(), "|", aType.getName()));
            }
            else{
                return new ErrorSTO(Formatter.toString(ErrorMsg.error1w_Expr, aType.getName(), "|", bType.getName()));
            }
        }
        else {
            return new ExprSTO(a.getName() + " | " + b.getName(), new IntType("int", 4));
        }
    }
}
