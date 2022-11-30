package gbw.sp3.OpcClient.util;

public class StringUtil {

    public static final char UNDERSCORE = '_', SPACE = ' ', DASH = '-';
    public static final char[] COMMONLY_IGNORED = new char[]{
            UNDERSCORE,SPACE,DASH
    };


    public static String toUnderscored(String string){
        String lowercase = trimUntil(string, COMMONLY_IGNORED).toLowerCase();
        String[] regexed = lowercase.split("[\" _-]");
        return null;
    }

    /**
     * Converts the string to camel-case
     * @param string the string to be converted
     * @return a new string
     */
    public static String toCamel(String string){
        String lowercase = trimUntil(string, COMMONLY_IGNORED).toLowerCase();
        String[] regexed = lowercase.split("[\" _-]");
        String[] resized = ArrayUtil.resizeStringArray(regexed,ArrayUtil.EMPTY_STRING_IGNORE);

        ArrayUtil.print(resized);

        for(int i = 1; i < resized.length; i++){
            regexed[i] = firstLetterUppercase(regexed[i]);
        }

        return ArrayUtil.arrayJoin(regexed);
    }

    public static String firstLetterUppercase(String string){
        //Thank you https://stackoverflow.com/questions/3904579/how-to-capitalize-the-first-letter-of-a-string-in-java
        String remainder = string.length() < 1 ? "" : string.substring(1);
        return string.substring(0, 1).toUpperCase() + remainder;
    }

    /**
     * Takes a string and goes through it. Whenever it meets a character that is NOT
     * within the chars array, it will return the rest of the string.
     * @param string
     * @param chars
     * @return the remaining string.
     */
    public static String trimUntil(String string, char[] chars){
        byte[] asBytes = string.getBytes();
        int index = 0;
        for(; index < asBytes.length; index++){
            if(!isAnyOf(asBytes[index],chars)){
                break;
            }
        }
        return string.substring(index);
    }
    private static boolean isAnyOf(byte b, char[] chars){
        for(char c : chars){
            if(b == c){
                return true;
            }
        }
        return false;
    }


}

