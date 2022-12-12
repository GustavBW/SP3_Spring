package gbw.sp3.OpcClient.client;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public enum ControlCommandTypes {
    RESET(1,"reset"),
    START(2,"start"),
    STOP(3,"stop"),
    ABORT(4,"abort"),
    CLEAR(5,"clear");

    public final int value;
    public final String name;

    ControlCommandTypes(int value, String name){
        this.name = name;
        this.value = value;
    }

    public static ControlCommandTypes parse(String s)
    {
        for(ControlCommandTypes cmdType: ControlCommandTypes.values()){
            if(cmdType.name().equalsIgnoreCase(s)){
                return cmdType;
            }
        }
        return null;
    }

    public static List<ControlCommandTypes> parse(String[] stringArray)
    {
        ControlCommandTypes[] values = ControlCommandTypes.values();
        List<ControlCommandTypes> parsed = new ArrayList<>();
        for(String s: stringArray){
            for(ControlCommandTypes cmdType: values){
                if(s.equalsIgnoreCase(cmdType.name())){
                    parsed.add(cmdType);
                }
            }
        }
        return parsed;
    }

    public static ControlCommandTypes from(int i)
    {
        if(i < 1 || i > 5){
            return null;
        }
        return ControlCommandTypes.values()[i-1];
    }
}
