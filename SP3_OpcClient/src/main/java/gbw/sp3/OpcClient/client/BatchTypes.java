package gbw.sp3.OpcClient.client;

public enum BatchTypes {
    PILSNER(0,"pilsner"),
    WHEAT(1,"wheat"),
    IPA(2,"ipa"),
    STOUT(3,"stout"),
    ALE(4,"ale"),
    ALCOHOL_FREE(5,"alcohol_free"),
    UNKNOWN(6,"unknown");

    final int type;
    final String name;
    BatchTypes(int type, String name)
    {
        this.type = type;
        this.name = name;
    }

    public static BatchTypes from(int i)
    {
        if(i < 0){
            return BatchTypes.UNKNOWN;
        }

        BatchTypes[] values = BatchTypes.values();
        if(i >= values.length){
            return BatchTypes.UNKNOWN;
        }
        return values[i];
    }

    public static BatchTypes parse(String s)
    {
        String normalized = s.replaceAll(" ","_");
        normalized = normalized.replaceAll("-","_");

        for(BatchTypes type : BatchTypes.values()){
            if(type.name().equalsIgnoreCase(normalized)){
                return type;
            }
        }
        return null;
    }

}
