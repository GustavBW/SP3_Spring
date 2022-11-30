package gbw.sp3.OpcClient.util;


import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static gbw.sp3.OpcClient.util.ArrayUtil.arrayJoinWith;

/**
 * Works with standard JSON format purely by String operations
 * expects input json to be the same format as when an object is converted
 * using jackson.
 */
public class JSONModifier {

    /**
     * Removes the field and value of specified attribute
     * @param attributeName name of attribute without quotation-marks
     * @param json Full json string
     * @return A new json string with that field removed.
     */
    public static String removeField(String attributeName, String json){
        String[] asArray = unitMask(json,new Character[]{'{','}'}).split(",");
        asArray = arrayFilterAttribute(asArray, attributeName);
        return "{" + arrayJoinWith(asArray,",") + "}";
    }

    /**
     * Replaces the value of a json attribute
     * @param attributeName what attribute
     * @param json complete json string
     * @param mask Replace string without quotation-marks
     * @return A new json string with that field masked.
     */
    public static String maskField(String attributeName, String json, String mask){
        String[] asArray = unitMask(json,new Character[]{'{','}'}).split(",");
        List<String> toReturn = new ArrayList<>();

        for(String s : asArray){
            if(s.contains("\""+attributeName+"\"")){
                String[] temp = s.split(":");
                toReturn.add(temp[0] + ":" + "\"" + mask + "\"");
            }else {
                toReturn.add(s);
            }
        }

        return "{" + arrayJoinWith(toReturn.toArray(new String[0]),",") + "}";
    }
    public static String addField(String json, String key, String value) {
        List<String> toReturn = new ArrayList<>(List.of(unitMask(json,new Character[]{'{','}'}).split(",")));

        String nullChecked = value == null ? "" : value;
        toReturn.add("\""+key+"\" : \"" + nullChecked + "\"");

        return "{" + arrayJoinWith(toReturn.toArray(new String[0]),",") + "}";
    }
    public static String addFields(String json, Map<String,String> kvPais){
        List<String> toReturn = new ArrayList<>(List.of(unitMask(json,new Character[]{'{','}'}).split(",")));

        for(String key: kvPais.keySet()) {
            String nullChecked = kvPais.get(key) == null ? "" : kvPais.get(key);
            toReturn.add("\"" + key + "\" : \"" + nullChecked + "\"");
        }

        return "{" + arrayJoinWith(toReturn.toArray(new String[0]),",") + "}";
    }

    private static String[] arrayFilterAttribute(String[] array, String attribute){
        List<String> toReturn = new ArrayList<>();

        for(String s : array){
            if(!s.contains("\""+attribute+"\"")){
                toReturn.add(s);
            }
        }

        return toReturn.toArray(new String[0]);
    }

    public static String unitMask(String string, Character[] mask){
        for(Character c : mask){
            string = string.replace("" + c,"");
        }
        return string;
    }

    public static void main(String[] args){
        String json = "{\"id\":1,\"firstName\":\"Gustav\",\"lastName\":\"Wanscher\",\"initials\":\"GUWA\",\"email\":\"ishoulvePutSomethinghere\",\"password\":\"passwordHERE\",\"admin\":\"unknown\"}";
        System.out.println("-----||NOW TESTING: JSONModifier.maskField()||-----");
        System.out.println(json);
        System.out.println(maskField("firstName",json,"||||||||||1||||||||"));
        System.out.println(maskField("admin",json,"|||||||2||||||"));
        //System.out.println(new UserService().maskDetails(json));

        System.out.println("\n\n");

        System.out.println("-----||NOW TESTING: JSONModifier.removeField()||-----");
        System.out.println(json);
        System.out.println(removeField("firstName",json));
        System.out.println(removeField("admin",json));
       // System.out.println(new UserService().eraseDetails(json));

        System.out.println("\n\n");

        System.out.println("-----||NOW TESTING: JSONModifier.addField()||-----");
        System.out.println(addField(json,"TestField_0","TestValue_0"));
        System.out.println(addField(json,"TestField_1",""));
        System.out.println(addField(json,"TestField_2",null));
        System.out.println("Testing keys: ");
        System.out.println(addField(json,"", "TestValue_0"));
        System.out.println(addField(json,null, "TestValue_1"));

        System.out.println("\n\n");

        System.out.println("-----||NOW TESTING: JSONModifier.addFields()||-----");
        System.out.println(addFields(json, Map.of(
                "TestField_0","TestValue_0",
                "TestField_1","TestValue_1",
                "TestField_2","TestValue_2")
        ));
        System.out.println(addFields(json, Map.of(
                "TestField_0","",
                "TestField_1","-1",
                "TestField_2","null")
        ));
        System.out.println("Testing keys: ");
        System.out.println(addFields(json, Map.of(
                "2","TestValue_0",
                "","TestValue_1",
                "\"TestField_3\"\n\t hi","TestValue_2")
        ));


        System.out.println("\n\n");
    }


}
