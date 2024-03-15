package com.zlf.es.spring.boot.autoconfigure.config;

import com.sun.istack.Nullable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.Formatter;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

/**
 * @author zlf
 * @description:
 * @time: 2022/06/24
 * 时间全局处理
 * 可以使用@DateTimeFormat(pattern = "xxxx")标注在指定的字段上
 */
@Configuration
public class LocalDateTimeFormatConfig {

    @Bean
    public Formatter<LocalDate> localDateFormatter() {
        return new Formatter<LocalDate>() {
            @Override
            public @Nullable
            String print(@Nullable LocalDate object, @Nullable Locale locale) {
                if (Objects.isNull(object)) {
                    return null;
                }
                return object.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }

            @Override
            public @Nullable
            LocalDate parse(@Nullable String text, @Nullable Locale locale) {
                if (!StringUtils.hasText(text)) {
                    return null;
                }
                return LocalDate.parse(text, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }
        };
    }

    @Bean
    public Formatter<LocalDateTime> localDateTimeFormatter() {
        return new Formatter<LocalDateTime>() {
            @Override
            public @Nullable
            String print(@Nullable LocalDateTime object, @Nullable Locale locale) {
                if (Objects.isNull(object)) {
                    return null;
                }
                return object.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }

            @Override
            public @Nullable
            LocalDateTime parse(@Nullable String text, @Nullable Locale locale) {
                if (!StringUtils.hasText(text)) {
                    return null;
                }
                return LocalDateTime.parse(text, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
        };
    }

    @Bean
    public Formatter<LocalTime> localTimeFormatter() {
        return new Formatter<LocalTime>() {
            @Override
            public @Nullable
            String print(@Nullable LocalTime object, @Nullable Locale locale) {
                if (Objects.isNull(object)) {
                    return null;
                }
                return object.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            }

            @Override
            public @Nullable
            LocalTime parse(@Nullable String text, @Nullable Locale locale) {
                if (!StringUtils.hasText(text)) {
                    return null;
                }
                return LocalTime.parse(text, DateTimeFormatter.ofPattern("HH:mm:ss"));
            }
        };
    }

}
