package gbw.sp3.OpcClient.util;

public class IntUtil {

    public static int parseOr(Object value, int onFail){
        try{
            return Integer.parseInt(""+value);
        }catch (NullPointerException | NumberFormatException e){
            return onFail;
        }
    }

}

