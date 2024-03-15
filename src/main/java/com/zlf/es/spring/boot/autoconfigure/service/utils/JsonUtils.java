package com.zlf.es.spring.boot.autoconfigure.service.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author zlf
 * @description:
 * @time: 2022/06/24
 */
@Slf4j
public class JsonUtils<T> {

    public JsonUtils() {
    }

    public final static ObjectMapper MAPPER = new ObjectMapper();

    public static String getJsonByObject(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T getObjectByJson(String jsonText, Class<T> beanClass) {
        try {
            return MAPPER.readValue(jsonText, beanClass);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 需要序列化一个对象
     *
     * @param jsonText
     * @param beanClass
     * @return
     */
    public List<T> getListByJson(String jsonText, Class<T> beanClass) {
        try {
            List<T> t = MAPPER.readValue(jsonText, new TypeReference<List<T>>() {

            });
            return t;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object getListByJson(String jsonText, Class collectionClass, Class beanClass) {
        try {
            JavaType javaType = MAPPER.getTypeFactory().constructParametricType(collectionClass, beanClass);
            return MAPPER.readValue(jsonText, javaType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object getMapByJson(String jsonText, Class collectionClass, Class keyClass, Class valueClass) {
        try {
            JavaType javaType = MAPPER.getTypeFactory().constructParametricType(collectionClass, keyClass, valueClass);
            return MAPPER.readValue(jsonText, javaType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 创建一个JsonNode
     *
     * @param rootNode
     * @param fieldName
     * @param value
     */
    public static void createJsonObj(JsonNode rootNode, String fieldName, String value) {
        ((ObjectNode) rootNode).put(fieldName, value);
    }

    /**
     * 将jsonNode转换为一个String
     *
     * @param rootNode
     * @return
     */
    public static String JsonObjToString(JsonNode rootNode) {
        try {
            String jsonString = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
            return jsonString;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "";
    }

}