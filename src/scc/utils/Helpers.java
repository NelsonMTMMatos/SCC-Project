package scc.utils;

import com.azure.cosmos.util.CosmosPagedIterable;

import java.util.Iterator;

public class Helpers {

    private Helpers(){}

    public static <T> T getItem(CosmosPagedIterable<T> resGet){
        Iterator<T> it = resGet.stream().iterator();
        return it.hasNext() ? it.next() : null;
    }
}
