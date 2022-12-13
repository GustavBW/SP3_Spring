package gbw.sp3.OpcClient.util;

import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;

public class IntUtil {

    public static int parseOr(Object value, int onFail){
        try{
            return Integer.parseInt(String.valueOf(value));
        }catch (NullPointerException | NumberFormatException e){
            return onFail;
        }
    }
    public static int parseOr(Variant variant, int onFail){
        try{
            return Integer.parseInt(String.valueOf(variant.getValue()));
        }catch (NullPointerException | NumberFormatException e){
            return onFail;
        }
    }

}

