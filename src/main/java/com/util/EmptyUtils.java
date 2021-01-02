package com.util;

import java.util.Collection;
import java.util.Map;

/**
 * <p>判断是否是空的 工具类</p>
 * @author XX
 * @version v1.0
 * @since 2015/5/5
 */
public class EmptyUtils {
    //判空             isEmpty是为空     传入obj任意类型
    public static boolean isEmpty(Object obj){ //obj不知什么类型，通过if判断所以类型
        if (obj == null)
            return true;
        if (obj instanceof CharSequence)
            //instanceof 属于  CharSequence char类型工具类 把obj转换为char
            // 取长度是不是0，是0就没有数据 就为空
            return ((CharSequence) obj).length() == 0;
        if (obj instanceof Collection)
            //Collection  集合父类   判断是否属于集合
            return ((Collection) obj).isEmpty();
        if (obj instanceof Map)//判断map集合是否为空
            return ((Map) obj).isEmpty();
        if (obj instanceof Object[]) { //判断数组是否为空
            Object[] object = (Object[]) obj;//强转为obj类型数组
            if (object.length == 0) {
                return true;
            }
            boolean empty = true;
            for (int i = 0; i < object.length; i++) {
                //取反就是不为空
                if (!isEmpty(object[i])) {//判断obj中的每一个值是否不为空
                    empty = false;//如果不为空是false  是空返回true
                    break;
                }
            }
            return empty;
        }
        return false;
    }
    public static boolean isNotEmpty(Object obj){
        return !isEmpty(obj);
    }



    private boolean validPropertyEmpty(Object ...args) {//传入多个数组
        for (int i = 0; i < args.length; i++) {
            if(EmptyUtils.isEmpty(args[i])){//如果数组的值为空，返回true
                return true;
            }
        }
        return false;
    }
}
