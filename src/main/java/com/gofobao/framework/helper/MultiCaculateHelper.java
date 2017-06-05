package com.gofobao.framework.helper;

import com.gofobao.framework.system.entity.Statistic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by Max on 17/6/2.
 */
@Slf4j
public class MultiCaculateHelper {

    public static <T> void caculate(Class<T> clazz, T org, T change) throws Exception{
            Field[] fields = clazz.getDeclaredFields();
            String fieldName = null ;
            Method readMethod = null ;
            Method writeMethod = null ;
            Long lDbValue = null ;
            Long lChangeValue = null ;
            Integer iDbValue = null ;
            Integer iChangeValue = null ;
            String typeName = null ;
            for(Field field: fields){
                fieldName = field.getName();
                PropertyDescriptor pd = new PropertyDescriptor(fieldName, clazz);
                field.setAccessible(true);
                readMethod = pd.getReadMethod();
                writeMethod =  pd.getWriteMethod();
                typeName = field.getGenericType().getTypeName();
                if(typeName.equals(Long.class.getTypeName())){
                    lChangeValue = (Long) ClassHelper.getValueByRefler(clazz, change, fieldName);
                    if(ObjectUtils.isEmpty(lChangeValue)){
                        continue;
                    }
                    lDbValue = (Long) readMethod.invoke(org);
                    lDbValue = lDbValue == null ? 0 : lDbValue ;
                    writeMethod.invoke(org, lDbValue + lChangeValue) ;
                }else if(typeName.equals(Integer.class.getTypeName())){
                    iChangeValue = (Integer) ClassHelper.getValueByRefler(clazz, change, fieldName);
                    if(ObjectUtils.isEmpty(lChangeValue)){
                        continue;
                    }
                    iDbValue = (Integer) readMethod.invoke(org);
                    iDbValue = iDbValue == null ? 0 : iDbValue ;
                    writeMethod.invoke(org, iDbValue + iChangeValue) ;
                }
            }
    }
}
