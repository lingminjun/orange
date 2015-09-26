package com.ssn.framework.foundation;

/**
 * Created by lingminjun on 15/7/15.
 * 用于一些常用对象变量因为空指针而异常处理
 */
public class TR {
    private static final String EMPTY_STRING = "";

    /**
     * 取字符串
     * @param str
     * @return 返回非空字符串
     */
    public final static String string(String str) {return str != null ? str : EMPTY_STRING;}

    /**
     * 取字符串
     * @param str
     * @param def 默认值
     * @return 返回非空字符串
     */
    public final static String string(String str,String def) {return str != null ? str : (def != null ? def : EMPTY_STRING);}


    /**
     * 取数字
     * @param v
     * @return 返回数字
     */
    public final static int integer(Integer v) {return v != null ? v : 0;}

    /**
     * 取数字
     * @param v
     * @param def 默认值
     * @return 返回数字
     */
    public final static int integer(Integer v, int def) {return v != null ? v : def;}

    /**
     * 取数字
     * @param v
     * @return 返回数字
     */
    public final static long longInteger(Long v) {return v != null ? v : 0;}

    /**
     * 取数字
     * @param v
     * @param def 默认值
     * @return 返回数字
     */
    public final static long longInteger(Long v, long def) {return v != null ? v : def;}


//    public final static Value<? extends Object> value(Object value) {return }
//    private class Value<T extends Object>{
//        private T v(T v) {return v != null ? v : new T();}
//    }

}
