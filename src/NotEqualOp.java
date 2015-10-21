/**
 * Created by Darmadoo on 10/9/15.
 */
public class NotEqualOp extends ComparisonOp {

    @Override
    STO checkOperands(STO a, STO b) {
        Type aType = a.getType();
        Type bType = b.getType();
        if (!(aType instanceof BasicType) || !(bType instanceof BasicType)) {
            // error when one of them is not numeric
            if((aType instanceof PointerType) || (bType instanceof PointerType))
            {
                if(!(aType.getName()).equals(bType.getName()))
                    if(!aType.isNullPointer() && !bType.isNullPointer())
                        return new ErrorSTO(Formatter.toString(ErrorMsg.error17_Expr, "==", aType.getName(), bType.getName()));
                    else
                        return new ExprSTO(a.getName() + " == " + b.getName(), new BoolType("bool", 4));
                else
                    return new ExprSTO(a.getName() + " == " + b.getName(), new BoolType("bool", 4));
            }
            else
                return new ErrorSTO(Formatter.toString(ErrorMsg.error1b_Expr, aType.getName(), "==", bType.getName()));
        }
        else if ((aType instanceof NumericType) && (bType instanceof BoolType)) {
            // error when one of them is not Bool
            return new ErrorSTO(Formatter.toString(ErrorMsg.error1b_Expr, aType.getName(), "!=", bType.getName()));
        }
        else if ((aType instanceof BoolType) && (bType instanceof NumericType)) {
            // error when one of them is not Bool
            return new ErrorSTO(Formatter.toString(ErrorMsg.error1b_Expr, aType.getName(), "!=", bType.getName()));
        }
        else {
            if((aType instanceof NumericType) && (bType instanceof NumericType))
            {
                if(a.isConst() && b.isConst())
                    return new ConstSTO(a.getName() + " != " + b.getName(), new BoolType("bool", 4), ((ConstSTO) a).getFloatValue() != ((ConstSTO) b).getFloatValue());
                else
                    return new ExprSTO(a.getName() + " != " + b.getName(), new BoolType("bool", 4));
            }
            else
            {
                if(a.isConst() && b.isConst())
                    return new ConstSTO(a.getName() + " != " + b.getName(), new BoolType("bool", 4), ((ConstSTO) a).getBoolValue() != ((ConstSTO) b).getBoolValue());
                else
                    return new ExprSTO(a.getName() + " != " + b.getName(), new BoolType("bool", 4));
            }
        }
    }

}