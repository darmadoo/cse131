/**
 * Created by Darmadoo on 10/9/15.
 */
public class EqualOp extends ComparisonOp {

    @Override
    STO checkOperands(STO a, STO b) {
        Type aType = a.getType();
        Type bType = b.getType();

        if (!(aType instanceof BasicType) || !(bType instanceof BasicType)) {
            // error when one of them is not numeric
            if((aType instanceof PointerType) || (bType instanceof PointerType))
            {
                if((aType.isNullPointer() && bType.isPointer()) || (aType.isPointer() && bType.isNullPointer()))
                    return new ExprSTO(a.getName() + " == " + b.getName(), new BoolType("bool", 4));
                if(!(aType.isAssignableTo(bType)))
                    return new ErrorSTO(Formatter.toString(ErrorMsg.error17_Expr, "==", aType.getName(), bType.getName()));
                else
                    return new ExprSTO(a.getName() + " == " + b.getName(), new BoolType("bool", 4));
            }
            else
                return new ErrorSTO(Formatter.toString(ErrorMsg.error1b_Expr, aType.getName(), "==", bType.getName()));
        }

        if (!(aType instanceof BasicType) || !(bType instanceof BasicType)) {
            // error when one of them is not numeric
            if((aType instanceof PointerType) || (bType instanceof PointerType))
            {
                if((aType.isNullPointer() && bType.isPointer()) || (aType.isPointer() && bType.isNullPointer()))
                    return new ExprSTO("(" + a.getName() + " == " + b.getName() + ")", new BoolType("bool", 4));
                if(!(aType.isAssignableTo(bType)))
                    return new ErrorSTO(Formatter.toString(ErrorMsg.error17_Expr, "==", aType.getName(), bType.getName()));
                else
                    return new ExprSTO("(" + a.getName() + " == " + b.getName() + ")", new BoolType("bool", 4));
            }
            else
                return new ErrorSTO(Formatter.toString(ErrorMsg.error1b_Expr, aType.getName(), "==", bType.getName()));
        }
        else if ((aType instanceof NumericType) && (bType instanceof BoolType)) {
            // error when one of them is not Bool
            return new ErrorSTO(Formatter.toString(ErrorMsg.error1b_Expr, aType.getName(), "==", bType.getName()));
        }
        else if ((aType instanceof BoolType) && (bType instanceof NumericType)) {
            // error when one of them is not Bool
            return new ErrorSTO(Formatter.toString(ErrorMsg.error1b_Expr, aType.getName(), "==", bType.getName()));
        }
        else {
            if((aType instanceof NumericType) && (bType instanceof NumericType))
            {
                if(a.isConst() && b.isConst()) {
                    ConstSTO sto = new ConstSTO("(" + a.getName() + " == " + b.getName() + ")", new BoolType("bool", 4), ((ConstSTO) a).getFloatValue() == ((ConstSTO) b).getFloatValue());
                    sto.setIsAddressable(false);
                    return sto;
                }
                else
                    return new ExprSTO("(" + a.getName() + " == " + b.getName() + ")", new BoolType("bool", 4));
            }
            else
            {
                if(a.isConst() && b.isConst()) {
                    ConstSTO sto = new ConstSTO("(" + a.getName() + " == " + b.getName() + ")", new BoolType("bool", 4), ((ConstSTO) a).getBoolValue() == ((ConstSTO) b).getBoolValue());
                    sto.setIsAddressable(false);
                    return sto;
                }
                else
                    return new ExprSTO("(" + a.getName() + " == " + b.getName() + ")", new BoolType("bool", 4));
            }
        }
    }

}