package gbw.sp3.OpcClient.util;


import java.io.Serializable;

public record Touple<T,R>(T first, R second) implements Serializable {
    public static final Long serialVersionUID = 8947828190L;
}
