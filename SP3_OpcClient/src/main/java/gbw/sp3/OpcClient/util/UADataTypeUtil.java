package gbw.sp3.OpcClient.util;

import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;
import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.ushort;

public class UADataTypeUtil {


    public static <T extends Object> Object asType(String s, String val)
    {
        new Variant(ushort(1));
        switch (s){
            case "short" -> {
                return (val);
            }
            case "integer" -> {
                return uint(val);
            }
            case "bool" -> {
                return  uint(val);
            }
        }
        return uint(val);
    }

}
