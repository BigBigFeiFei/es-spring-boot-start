package com.zlf.es.spring.boot.autoconfigure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Iterator;
import java.util.List;

/**
 * @author zlf
 * @description:
 * @time: 2022/06/24
 * https://blog.csdn.net/lvyuanj/article/details/108554170
 * 1.WebMvcConfigurerAdapter springBoot1.x
 * springBoot2.x推荐使用WebMvcConfigurer
 * 2.实现WebMvcConfigurer类不会让WebMvcAutoConfiguration自动装配置失效和静态资源访问失效
 * 3.继承WebMvcConfigurationSupport会覆盖原有的WebMvcConfigurationSupport类,、
 * 还会让WebMvcAutoConfiguration自动装配置失效和静态资源访问失效
 */
@Slf4j
@Configuration
public class WebMvcAndJackson2OrFastJsonConfig implements WebMvcConfigurer {

    /*@Autowired(required = false)
    private JacksonConfig jacksonConfig;

    @Autowired(required = false)
    private FastJsonConfig fastJsonConfig;*/

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        JacksonConfig jacksonConfig = new JacksonConfig();
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        /**
         * 此处已经设置了MappingJackson2HttpMessageConverter jackson转换器，但是未设置任何转换格式，
         * 所以当需要反序列化String类型为Timestamp类型时，
         * 用的格式仍然Jackson默认的类型格式-yyyy-MM-dd'T'HH:mm:ss.SSSZ，转
         *  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
         *  private LocalDateTime timestamp 报错
         * 导致反序列化失败，报错，而且此处设置便会覆盖配置文件的 jackson的配置项，配置一直不生效的原因就是这里
         **/
        converters.add(0, jacksonConfig.mappingJackson2HttpMessageConverter());
        /**
         * 如果使用fastJson消息转换器必须把默认的jackson的消息转换器移除,在把fastJson的转换器加入进入,
         * 还是可以自动匹配？
         */
        if (Boolean.valueOf(fastJsonConfig.getIsUseFastJsonHMC())) {
            Iterator<HttpMessageConverter<?>> iterator = converters.iterator();
            while (iterator.hasNext()) {
                HttpMessageConverter<?> next = iterator.next();
                if (next instanceof MappingJackson2HttpMessageConverter) {
                    iterator.remove();
                }
            }
            converters.add(0, fastJsonConfig.fastJsonHttpMessageConverter());
            log.info("==============配置fastJsonHttpMessageConverter成功!===============");
        } else if (String.valueOf(fastJsonConfig.getIsUseFastJsonHMC()).equals(FastJsonConfig.ALL)) {
            converters.add(1, fastJsonConfig.fastJsonHttpMessageConverter());
        }
    }

}