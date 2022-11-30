package gbw.sp3.OpcClient.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static gbw.sp3.OpcClient.util.IntUtil.parseOr;

public class ArrayUtil {

    @FunctionalInterface
    public interface BooleanFunction {
        boolean eval(Object o);
    }

    /**
     * Function checking if a given object is a valid Integer using Integer.parseInt(Obj)
     */
    public static final BooleanFunction INTEGER_INCLUDE = e -> {
        try {
            Integer.parseInt("" + e);
            return true;
        } catch (NullPointerException | NumberFormatException ex) {
            return false;
        }
    };
    public static final BooleanFunction INTEGER_IGNORE = e -> !INTEGER_INCLUDE.eval(e);
    public static final BooleanFunction EMPTY_STRING_IGNORE = e -> e != " ";

    /**
     * Turns a String array into an int array.
     * @param arr Array to parse
     * @return int[]
     */
    public static int[] parseIntArray(String[] arr) throws NumberFormatException, NullPointerException {
        return Stream.of(arr).mapToInt(Integer::parseInt).toArray();
    }
    public static int[] parseIntArray(Object[] arr, BooleanFunction includeFunc){
        arr = resize(arr, includeFunc);
        int[] toReturn = new int[arr.length];

        for(int i = 0; i < toReturn.length; i++){
            toReturn[i] = parseOr(arr[i],-1); //unnecessary parsing, but I know no way around it.
        }

        return toReturn;
    }
    public static Long[] fromIntToLong(int[] intArr){
        Long[] toReturn = new Long[intArr.length];
        for(int i = 0; i < toReturn.length; i++){
            toReturn[i] = (long) intArr[i];
        }
        return toReturn;
    }

    public static void main(String[] args) {
        System.out.println(Arrays.toString(parseIntArray(new Object[]{"1", "2"}, INTEGER_INCLUDE)));
        System.out.println(Arrays.toString(parseIntArray(new Object[]{"1","hi", "", "2"}, INTEGER_INCLUDE)));
        System.out.println(Arrays.toString(parseIntArray(new Object[]{"1000000000","1","hi", "", "-1", "2"}, INTEGER_INCLUDE)));
        System.out.println(Arrays.toString(parseIntArray(new Object[]{"1000000000000000","1000000000","1","hi", "", "-1", "2"}, INTEGER_INCLUDE)));
    }

    public static <T> T[] resize(T[] arr, BooleanFunction includeFunc){
        List<T> toReturn = new ArrayList<>();
        for(T obj : arr){
            if(includeFunc.eval(obj)){
                toReturn.add(obj);
            }
        }
        return (T[]) toReturn.toArray(); //oh java, you poor thing.
    }
    public static String[] resizeStringArray(String[] array, BooleanFunction includeFunc){
        List<String> toReturn = new ArrayList<>();
        for(String s : array){
            if(includeFunc.eval(s)){
                toReturn.add(s);
            }
        }
        return toReturn.toArray(new String[0]); //oh java, you poor thing.
    }

    public static int countLengthIgnore(Object[] arr, BooleanFunction func){
        int count = 0;
        for(Object o : arr){
            if(func.eval(o)){
                count++;
            }
        }
        return count;
    }

    public static String arrayJoin(String[] array){
        StringBuilder sb = new StringBuilder();
        for(String s : array){
            sb.append(s);
        }
        return sb.toString();
    }

    public static void print(String[] array){
        System.out.println("String array " + array + " contains :");
        for(String s : array){
            System.out.println(s);
        }
    }

    /**
     * Joins each index, regardless of content, with the given joint so that
     * for given joint "," array [a][b][c] becomes "a,b,c"
     * @param array
     * @param joint
     * @return the array as a string joined with the given joint
     */
    public static String arrayJoinWith(String[] array, String joint){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < array.length -1; i++){
            sb.append(array[i]).append(joint);
        }
        sb.append(array[array.length-1]);
        return sb.toString();
    }
    /**
     * Joins each index, regardless of content, with the givent joint so that
     * for given joint "," array [a][b][c] becomes "a,b,c"
     * @param array
     * @param joint
     * @return the array as a string joined with the given joint
     */
    public static String arrayJoinWith(int[] array, String joint){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < array.length -1; i++){
            sb.append(array[i]).append(joint);
        }
        sb.append(array[array.length-1]);
        return sb.toString();
    }
    public static boolean contains(String[] array, String string){
        for(String s : array){
            if(s.equals(string)){
                return true;
            }
        }
        return false;
    }
    public static boolean contains(int[] array, int i2){
        for(int i1 : array){
            if(i1 == i2){
                return true;
            }
        }
        return false;
    }

    /**
     * Return a resized array without the indexes matching the given string
     * @param array
     * @param string
     * @return
     */
    public static String[] removeIndexMatching(String[] array, String string){
        List<String> toReturn = new ArrayList<>();
        for(String s : array){
            if(!s.equals(string)){
                toReturn.add(s);
            }
        }
        return toReturn.toArray(new String[0]);
    }
    public static int[] removeIndexMatching(int[] array, int i2){
        List<Integer> toReturn = new ArrayList<>();
        for(int i1 : array){
            if(i1 != i2){
                toReturn.add(i2);
            }
        }
        return toReturn.stream().mapToInt(Integer::intValue).toArray();
    }
}

