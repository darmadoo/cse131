/**
 * Created by Darmadoo on 10/8/15.
 */
public class ModOp extends ArithmeticOp {

    @Override
    STO checkOperands(STO a, STO b) {
        Type aType = a.getType();
        Type bType = b.getType();
        if (!(aType instanceof IntType) || !(bType instanceof IntType)) {
            // error when one of them is not numeric
            if(!(aType instanceof  IntType)){
                return new ErrorSTO(Formatter.toString(ErrorMsg.error1w_Expr, aType.getName(), "%", "int"));
            }
            else{
                return new ErrorSTO(Formatter.toString(ErrorMsg.error1w_Expr, bType.getName(), "%", "int"));
            }
        } else {
            // Int % Int = Int
            if(a.isConst() && b.isConst()) {
                if (((ConstSTO) b).getIntValue() == 0)
                    return new ErrorSTO(ErrorMsg.error8_Arithmetic);
                else
                    return new ConstSTO(a.getName() + " % " + b.getName(), new IntType("int", 4), ((ConstSTO) a).getIntValue() % ((ConstSTO) b).getIntValue());
            }
            else
                return new ExprSTO(a.getName() + " % " + b.getName(), new IntType("int", 4));
        }
    }

}
