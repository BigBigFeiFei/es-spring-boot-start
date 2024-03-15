package com.zlf.es.spring.boot.autoconfigure.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author zlf
 * @description:
 * @time: 2022/06/24
 */
@Configuration
public class JacksonConfig {

    /**
     * Date格式化字符串
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    /**
     * DateTime格式化字符串
     */
    private static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    /**
     * Time格式化字符串
     */
    private static final String TIME_FORMAT = "HH:mm:ss";

    @Bean
    @ConditionalOnClass(ObjectMapper.class)
    public Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder() {
        return new Jackson2ObjectMapperBuilder();
    }

    /**
     * 自定义了一个Jackson2ObjectMapperBuilder
     *
     * @return
     */
    @Bean
    @ConditionalOnClass(Jackson2ObjectMapperBuilder.class)
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> {
            builder.serializerByType(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATETIME_FORMAT)));
            builder.deserializerByType(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DATETIME_FORMAT)));
            builder.serializerByType(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern(DATE_FORMAT)));
            builder.deserializerByType(LocalDate.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern(DATE_FORMAT)));
            builder.serializerByType(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern(TIME_FORMAT)));
            builder.deserializerByType(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern(TIME_FORMAT)));
            builder.locale(Locale.SIMPLIFIED_CHINESE);
            /**spring:
             *   jackson:
             *     time-zone: Asia/Shanghai
             *     date-format: yyyy-MM-dd HH:mm:ss
             * 一些常用配置可以配置
             */
            builder.simpleDateFormat(DATETIME_FORMAT);
            builder.timeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        };
    }

    /**
     * 1.禁止springBoot的空对象序列化,否则springMvc返回给前端要被序列化为json的实体中含有空对象就会报错
     * 2.忽略为null的字段
     * 3.反序列化实体中缺少字段不报com.fasterxml.jackson.databind.exc.MismatchedInputException错
     * 4.通过Jackson2ObjectMapperBuilder构建一个单例的ObjectMapper
     * 自定义LocalDateTime类型的编解码
     *
     * @return
     */
    @Bean
    @ConditionalOnClass(Jackson2ObjectMapperBuilder.class)
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder) {
        //针对于Date类型，文本格式化
        //jackson2ObjectMapperBuilder.simpleDateFormat(PATTERN1);
        //针对于JDK新时间类。序列化时带有T的问题，自定义格式化字符串
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATETIME_FORMAT)));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DATETIME_FORMAT)));
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern(DATE_FORMAT)));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern(DATE_FORMAT)));
        javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern(TIME_FORMAT)));
        javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern(TIME_FORMAT)));
        ObjectMapper objectMapper = jackson2ObjectMapperBuilder.build();
        objectMapper.registerModule(javaTimeModule);
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS).setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }

    /*
    jackson消息转换器(默认使用这个消息转换器)
     * 注意：
     *  在配置springBoot支付jakson序列化返回给前端的配置中要配置ObjectMapper的序列化方式的话需要如下这种配置：
     *  在有feigin集成调用的时候然后直接配置一个单例的ObjectMapper就会让fegin反序列化时间字段报错，
     *  原因是由于单例的ObjectMapper会让feigin的解码器和springDecode的jackson支持的解码器相互冲突影响。
     *
     * */
    @Bean
    @ConditionalOnClass(ObjectMapper.class)
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter(
                this.objectMapper(this.jackson2ObjectMapperBuilder()));
        /**设置mediaType支持可以不用设置.默认是这个设置的
         * public MappingJackson2HttpMessageConverter(ObjectMapper objectMapper) {
         *         super(objectMapper, new MediaType[]{MediaType.APPLICATION_JSON, new MediaType("application", "*+json")});
         *     }
         */
       /* List<MediaType> mediaType = new ArrayList<MediaType>();
        mediaType.add(MediaType.APPLICATION_JSON);
        mappingJackson2HttpMessageConverter.setSupportedMediaTypes(mediaType);*/
        return mappingJackson2HttpMessageConverter;
    }

}
