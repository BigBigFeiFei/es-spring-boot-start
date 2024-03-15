package com.zlf.es.spring.boot.autoconfigure.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.SimpleDateFormatSerializer;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;

import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author zlf
 * @description:
 * @time: 2022/06/24
 */
@Data
@Configuration
@RefreshScope
public class FastJsonConfig {

    /**
     * 可以配置为：NO,YES,ALL
     */
    @Value("${isUseFastJsonHMC:NO}")
    private String isUseFastJsonHMC;

    /**
     * jackson的消息解析器优先,同时也支持fastJson的消息解析的
     */
    public static final String ALL = "ALL";

    /**
     * fastJson消息转换器
     *
     * @return
     */
    @Bean
    @ConditionalOnClass(JSON.class)
    public FastJsonHttpMessageConverter fastJsonHttpMessageConverter() {
        FastJsonHttpMessageConverter fastJsonHttpMessageConverter = new FastJsonHttpMessageConverter();
        //1.添加fastJson的配置信息;
        com.alibaba.fastjson.support.config.FastJsonConfig fastJsonConfig = new com.alibaba.fastjson.support.config.FastJsonConfig();
        fastJsonConfig.setCharset(Charset.forName("UTF-8"));
        fastJsonConfig.setSerializerFeatures(
                SerializerFeature.PrettyFormat,//PrettyFormat漂亮的json格式
                SerializerFeature.WriteNonStringKeyAsString, //不是String类型的key转换成String类型，否则前台无法将Json字符串转换成Json对象
                SerializerFeature.WriteMapNullValue,        // 是否输出值为null的字段,默认为false,我们将它打开
                SerializerFeature.WriteNullListAsEmpty,     // 将Collection类型字段的字段空值输出为[]
                SerializerFeature.WriteNullStringAsEmpty,   // 将字符串类型字段的空值输出为空字符串
                SerializerFeature.WriteNullNumberAsZero,    // 将数值类型字段的空值输出为0
                SerializerFeature.WriteDateUseDateFormat,
                SerializerFeature.DisableCircularReferenceDetect    // 禁用循环引用
        );
        // 缺点，指定后，将不会使用@JSONField注解上的format属性，包括并不限于Date类，LocalDateTime类，LocalDate类。（慎用）
        //文本date的格式化方式
        fastJsonConfig.setDateFormat("yyyy-MM-dd HH:mm:ss");
        //2.序列化方式配置
        //SerializeConfig config = SerializeConfig.getGlobalInstance();
        //@JSONField(format = "yyyy-MM-dd HH:mm:ss")注解会失效
        //config.put(LocalDateTime.class, LocalDateTimeSerializer.instance);
        //将数据库的Date格式数据转化为"yyyy-MM-dd"格式
        //config.put(java.sql.Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd"));
        //将数据库的Timestamp格式数据转化为"yyyy-MM-dd HH:mm:ss"格式
        //config.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd"));
        //config.put(Timestamp.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
        //config.put(LocalDateTime.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
        //config.put(LocalDate.class, new SimpleDateFormatSerializer("yyyy-MM-dd"));
        //fastJsonConfig.setSerializeConfig(config);
        SerializeConfig serializeConfig = fastJsonConfig.getSerializeConfig();
        serializeConfig.put(LocalDateTime.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
        serializeConfig.put(Timestamp.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
        serializeConfig.put(LocalDate.class, new SimpleDateFormatSerializer("yyyy-MM-dd"));
        serializeConfig.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd"));
        //3处理中文乱码问题
        List<MediaType> fastMediaTypes = new ArrayList<>();
        fastMediaTypes.add(MediaType.APPLICATION_JSON);
        fastMediaTypes.add(MediaType.parseMediaType(MediaType.TEXT_PLAIN_VALUE + ";charset=ISO-8859-1"));

        //4.在convert中添加配置信息.
        fastJsonHttpMessageConverter.setSupportedMediaTypes(fastMediaTypes);
        fastJsonHttpMessageConverter.setFastJsonConfig(fastJsonConfig);

        return fastJsonHttpMessageConverter;
    }

    /**
     * 遇到的问题：jackson序列化的数据，然后fastJson解析这个LocalDateTime转为Date报错
     * Caused by: com.alibaba.fastjson.JSONException: write javaBean error, fastjson version 1.2.76, class com.dycx.framework.api.response.RestResponse, java.time.LocalDateTime cannot be cast to java.util.Date
     * 1).有这个问题:是由于包冲突导致,统一fast包的版本
     * 2).还有就是上面的配置中配置了下面这几个导致解析不了，注释了就可以解析的
     * //2.序列化方式配置
     * //SerializeConfig config = SerializeConfig.getGlobalInstance();
     * //@JSONField(format = "yyyy-MM-dd HH:mm:ss")注解会失效
     * //config.put(LocalDateTime.class, LocalDateTimeSerializer.instance);
     * //将数据库的Date格式数据转化为"yyyy-MM-dd"格式
     * //config.put(java.sql.Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd"));
     * //将数据库的Timestamp格式数据转化为"yyyy-MM-dd HH:mm:ss"格式
     * //config.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd"));
     * //config.put(Timestamp.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
     * //config.put(LocalDateTime.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
     * //config.put(LocalDate.class, new SimpleDateFormatSerializer("yyyy-MM-dd"));
     * //fastJsonConfig.setSerializeConfig(config);
     */
    /*public static class LocalDateTimeSerializer implements ObjectSerializer {

        public static final LocalDateTimeSerializer instance = new LocalDateTimeSerializer();
        private static final String defaultPattern = "yyyy-MM-dd HH:mm:ss";

        public LocalDateTimeSerializer() {
        }

        @Override
        public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
            SerializeWriter out = serializer.out;
            if (object == null) {
                out.writeNull();
            } else {
                LocalDateTime result = (LocalDateTime) object;
                out.writeString(result.format(DateTimeFormatter.ofPattern(defaultPattern)));
            }
        }
    }*/

}
