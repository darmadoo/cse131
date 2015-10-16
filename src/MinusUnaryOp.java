/**
 * Created by clapichacu on 10/15/15.
 */
public class MinusUnaryOp extends UnaryOp {

    @Override
    STO checkOperands(STO a) {
        Type aType = a.getType();

        if (!(aType instanceof NumericType)) {
            // error when one of them is not numeric
            return new ErrorSTO(Formatter.toString(ErrorMsg.error2_Type, aType.getName(), "-"));
        }
        if (aType instanceof IntType) {
            // Int++ = Int
            // return ExprSTO of int types
            if(a.isConst())
                return new ConstSTO(a.getName() + "-", new IntType("int", 4), 0 - ((ConstSTO) a).getIntValue());
            else
                return new ExprSTO(a.getName() + "-", new IntType("int", 4));
        } else {
            // Float++ = Float
            // return ExprSTO of float type
            if(a.isConst()) {
                return new ConstSTO(a.getName() + "-", new FloatType("float", 4), 0.0 - ((ConstSTO) a).getFloatValue());
            }
            else
                return new ExprSTO(a.getName() + "-", new FloatType("float", 4));
        }

    }
}