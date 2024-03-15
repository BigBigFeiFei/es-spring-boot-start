package com.alibaba.fastjson.serializer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * 1）.jackson和fastjson序列化和反序列化混用,
 * 只要json的格式都是一个标准都可以解析的
 * （除非两个库的编码实现上有差异,有特特殊的字符或类型的序列化和反序列化有差异的话,就会不能相互解析
 * <p>
 * 2）.fastJson的bug
 * fastJson的JSON.toJSONString(object obj)序列化
 * 只要实体中含有LocalDateTime的字段，在fastJson没有指定SerializeConfig序列化时间的方式时会默认的序列化为java的UTC时间类型。格式为：
 * yyyy-MM-dd'T' HH:mm:ss,如果指定了SerializeConfig序列化LocalDateTime的方式时，fastJson默认支持的时间字段累类型是Date,所以指定
 * LocalDateTime的序列化方式的时候，JSON.toJSONString(object obj)会执行失败，失败报错就是LocalDateTime不能直接转为Date类型，所以我们要重写fastJson的时间格式化解析器的源码
 */

/**
 * @author zlf
 * @description:
 * @time: 2022/06/24
 */
public class SimpleDateFormatSerializer implements ObjectSerializer {

    private final String pattern;

    public SimpleDateFormatSerializer(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        if (object == null) {
            serializer.out.writeNull();
            return;
        }
        Date date = null;
        if (object instanceof LocalDateTime) {
            LocalDateTime localDateTime = (LocalDateTime) object;
            date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        } else if (object instanceof LocalDate) {
            LocalDate localDate = (LocalDate) object;
            date = Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        } else {
            date = (Date) object;
        }
        SimpleDateFormat format = new SimpleDateFormat(pattern, serializer.locale);
        format.setTimeZone(serializer.timeZone);

        String text = format.format(date);
        serializer.write(text);
    }

}
