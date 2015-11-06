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
            if(a.isConst()) {
                ConstSTO sto = new ConstSTO("!" + a.getName(), new BoolType("bool", 4), !((ConstSTO) a).getBoolValue());
                sto.setIsAddressable(false);
                return sto;
            }else
                return new ExprSTO("!" + a.getName(), new BoolType("bool", 4));
        }
    }

}