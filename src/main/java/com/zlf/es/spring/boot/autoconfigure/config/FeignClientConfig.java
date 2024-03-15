package com.zlf.es.spring.boot.autoconfigure.config;

import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import feign.codec.Decoder;
import feign.codec.Encoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author zlf
 * @description:
 * @time: 2022/06/24
 * fegin的
 */
@Configuration
public class FeignClientConfig {

   /* @Autowired(required = false)
    private JacksonConfig jacksonConfig;

    @Autowired(required = false)
    private FastJsonConfig fastJsonConfig;*/

    @Bean
    public Encoder feignEncoder() {
        return new SpringEncoder(feignHttpMessageConverter());
    }

    @Bean
    public Decoder feignDecoder() {
        return new SpringDecoder(feignHttpMessageConverter());
    }

    private ObjectFactory<HttpMessageConverters> feignHttpMessageConverter() {
        JacksonConfig jacksonConfig = new JacksonConfig();
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        Collection<HttpMessageConverter<?>> converters = new ArrayList<>();
        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = jacksonConfig.mappingJackson2HttpMessageConverter();
        FastJsonHttpMessageConverter fsc = fastJsonConfig.fastJsonHttpMessageConverter();
        converters.add(mappingJackson2HttpMessageConverter);
        if (Boolean.valueOf(fastJsonConfig.getIsUseFastJsonHMC())) {
            converters.add(fsc);
            Iterator<HttpMessageConverter<?>> iterator = converters.iterator();
            while (iterator.hasNext()) {
                HttpMessageConverter<?> next = iterator.next();
                if (next instanceof MappingJackson2HttpMessageConverter) {
                    iterator.remove();
                }
            }
        } else if (String.valueOf(fastJsonConfig.getIsUseFastJsonHMC()).equals(FastJsonConfig.ALL)) {
            converters.add(fastJsonConfig.fastJsonHttpMessageConverter());
        }
        //设置中文编码格式
        List<MediaType> list = new ArrayList<MediaType>();
        list.add(MediaType.APPLICATION_JSON_UTF8);
        list.add(MediaType.APPLICATION_JSON);
        list.add(MediaType.TEXT_PLAIN);
        list.add(MediaType.ALL);
        mappingJackson2HttpMessageConverter.setSupportedMediaTypes(list);
        final HttpMessageConverters httpMessageConverters = new HttpMessageConverters(converters);
        return () -> httpMessageConverters;
    }

}