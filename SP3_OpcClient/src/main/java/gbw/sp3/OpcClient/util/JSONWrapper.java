package gbw.sp3.OpcClient.util;

import gbw.sp3.OpcClient.util.ArrayUtil;
import gbw.sp3.OpcClient.util.JSONModifier;
import gbw.sp3.OpcClient.util.StringUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JSONWrapper {

    private Map<String, String> level1;

    public static String error(int status, String message){
        return "{\n"+
                "\t\"status\" : "+status+",\n"+
                "\t\"message\" : \""+message+"\"\n"+
                "}";
    }
    public static String success(int status, String message){
        return "{\n"+
                "\t\"status\" : "+status+",\n"+
                "\t\"message\" : \""+message+"\"\n"+
                "}";
    }
    public static String custom(int status, Map<String,String> kvPairs){
        String toReturn = "{\"status\" : " +status +"}";
        return JSONModifier.addFields(toReturn, kvPairs);
    }
    public static Map<String, String> parse(String string){
        List<String> asArray = new ArrayList<>(List.of(JSONModifier.unitMask(string,new Character[]{'{','}'}).split(",")));
        Map<String, String> toReturn = new HashMap<>();
        for(String s : asArray){
            String[] kv = JSONModifier.unitMask(s,new Character[]{'\"'}).split(":");
            String value = "unavailable";
            if(kv.length > 1){
                value = kv[1].trim();
            }
            toReturn.put(kv[0].trim(),value);
        }
        return toReturn;
    }

    public JSONWrapper(String string){
        level1 = parse(string);
    }

    public String getOr(String name, String valueOnFail){
        String value = this.get(name);
        if(value == null || value.equals("unavailable") || value.isEmpty()){
            return valueOnFail;
        }
        return value;
    }
    public <T> T getOr(T type, String s, String valueOnFail){
        String value = getOr(s,valueOnFail);
        T asType = null;
        try {
            asType = (T) type.getClass().getConstructor(String.class).newInstance(s);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return asType;
    }

    public String get(String name){
        return level1.get(name);
    }
    public String getLike(String name){
        String nameTest = name;
        if(this.get(nameTest) != null){
            return this.get(nameTest);
        }
        nameTest = name.toLowerCase();
        if(this.get(nameTest) != null){
            return this.get(nameTest);
        }
        nameTest = name.toUpperCase();
        if(this.get(nameTest) != null){
            return this.get(nameTest);
        }
        nameTest = StringUtil.toCamel(name);
        if(this.get(nameTest) != null){
            return this.get(nameTest);
        }
        return null;
    }



}

