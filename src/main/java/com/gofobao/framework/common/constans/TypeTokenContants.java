package com.gofobao.framework.common.constans;

import com.google.common.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Created by Max on 17/5/26.
 */
public class TypeTokenContants {

    public static  final Type MAP_TOKEN = new TypeToken<Map<String, Object>>(){}.getType() ;
}
