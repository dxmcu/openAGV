package com.robot.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.robot.adapter.model.DeviceAddress;
import com.robot.mvc.core.exceptions.RobotException;
import com.robot.mvc.model.HeadDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by laotang on 2020/1/12.
 */
public class ToolsKit {

    private static final Logger LOG = LoggerFactory.getLogger(ToolsKit.class);
    private static final Set<String> EXCLUDED_METHOD_NAME = new HashSet<>();
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat SDF2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    public static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        /**过滤对象的null属性*/
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        /**过滤map中的null key*/
        objectMapper.getSerializerProvider().setNullKeySerializer(new JsonSerializer<Object>() {
            @Override
            public void serialize(Object value, JsonGenerator generator, SerializerProvider serializers) throws IOException, JsonProcessingException {
                generator.writeFieldName("");
            }
        });
        /**过滤map中的null值*/
        objectMapper.getSerializerProvider().setNullValueSerializer(new JsonSerializer<Object>() {
            @Override
            public void serialize(Object value, JsonGenerator generator, SerializerProvider serializers) throws IOException, JsonProcessingException {
                generator.writeString("");
            }
        });
        //指定遇到date按照这种格式转换
        objectMapper.setDateFormat(SDF);
        //配置该objectMapper在反序列化时，忽略目标对象没有的属性。
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    // 定义一个请求对象安全线程类
    private static DuangThreadLocal<HeadDto> requestHeaderThreadLocal = new DuangThreadLocal<HeadDto>() {
        @Override
        public HeadDto initialValue() {
            return new HeadDto();
        }
    };
    /**
     * 设置请求头DTO到ThreadLocal变量
     * @param headDto       请求头DTO
     */
    public static void setThreadLocalDto(HeadDto headDto) {
        requestHeaderThreadLocal.set(headDto);
    }
    /**
     *  取ThreadLocal里的HeadDto对象
     * @return
     */
    public static HeadDto getThreadLocalDto() {
        return  requestHeaderThreadLocal.get();
    }

    public static <T> T requireNonEmpty(T obj, String message) {
        if (ToolsKit.isEmpty(obj))
            throw new RobotException(message);
        return obj;
    }

    /***
     * 判断传入的对象是否为空
     *
     * @param obj
     *            待检查的对象
     * @return 返回的布尔值, 为空或等于0时返回true
     */
    public static boolean isEmpty(Object obj) {
        return checkObjectIsEmpty(obj, true);
    }

    /***
     * 判断传入的对象是否不为空
     *
     * @param obj
     *            待检查的对象
     * @return 返回的布尔值, 不为空或不等于0时返回true
     */
    public static boolean isNotEmpty(Object obj) {
        return checkObjectIsEmpty(obj, false);
    }

    @SuppressWarnings("rawtypes")
    private static boolean checkObjectIsEmpty(Object obj, boolean bool) {
        if (null == obj) {
            return bool;
        } else if (obj == "" || "".equals(obj) || "null".equalsIgnoreCase(String.valueOf(obj))) {
            return bool;
        } else if (obj instanceof Integer || obj instanceof Long || obj instanceof Double) {
            try {
                Double.parseDouble(obj + "");
            } catch (Exception e) {
                return bool;
            }
        } else if (obj instanceof String) {
            if (((String) obj).length() <= 0) {
                return bool;
            }
            if ("null".equalsIgnoreCase(obj + "")) {
                return bool;
            }
        } else if (obj instanceof Map) {
            if (((Map) obj).size() == 0) {
                return bool;
            }
        } else if (obj instanceof Collection) {
            if (((Collection) obj).size() == 0) {
                return bool;
            }
        } else if (obj instanceof Object[]) {
            if (((Object[]) obj).length == 0) {
                return bool;
            }
        }
        return !bool;
    }


    /**
     * 构建过滤方法名集合，默认包含Object类里公共方法
     *
     * @param excludeMethodClass 如果有指定，则添加指定类下所有方法名
     * @return
     */
    public static Set<String> buildExcludedMethodName(Class<?>... excludeMethodClass) {
        if (EXCLUDED_METHOD_NAME.isEmpty()) {
            Method[] objectMethods = Object.class.getDeclaredMethods();
            for (Method m : objectMethods) {
                EXCLUDED_METHOD_NAME.add(m.getName());
            }
        }
        Set<String> tmpExcludeMethodName = null;
        if (null != excludeMethodClass) {
            tmpExcludeMethodName = new HashSet<>();
            for (Class excludeClass : excludeMethodClass) {
                Method[] excludeMethods = excludeClass.getDeclaredMethods();
                if (null != excludeMethods) {
                    for (Method method : excludeMethods) {
                        tmpExcludeMethodName.add(method.getName());
                    }
                }
            }
            tmpExcludeMethodName.addAll(EXCLUDED_METHOD_NAME);
        }
        return (null == tmpExcludeMethodName) ? EXCLUDED_METHOD_NAME : tmpExcludeMethodName;
    }

    /**
     * 是否正常公用的API方法
     * 正常方法是指访问权限是public的且不是抽像，静态，接口，Final的方法
     *
     * @param mod Modifier的mod
     * @return
     */
    public static boolean isPublicMethod(int mod) {
        return !(Modifier.isAbstract(mod) || Modifier.isStatic(mod) || Modifier.isFinal(mod) || Modifier.isInterface(mod) || Modifier.isPrivate(mod) || Modifier.isProtected(mod));
    }

    /**
     * json字符串转换为对象
     *
     * @param jsonStr json格式的字符串
     * @param clazz   待转换的对象
     * @param <T>     返回泛型值
     * @return
     * @throws Exception
     */
    public static <T> T jsonParseObject(String jsonStr, Class<T> clazz) {
        try {
            return objectMapper.readValue(jsonStr, clazz);
        } catch (Exception e) {
            throw new RobotException(e.getMessage(), e);
        }
    }

    public static <T> List<T> jsonParseArray(String jsonStr, TypeReference<T> typeReference) {
        try {
            return (List<T>) objectMapper.readValue(jsonStr, typeReference);
        } catch (Exception e) {
            throw new RobotException(e.getMessage(), e);
        }
    }


    public static String toJsonString(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RobotException(e.getMessage(), e);
        }
    }


    public static String getDateString(Date date) {
        try {
            return SDF.format(date);
        } catch (Exception e) {
            LOG.warn(e.getMessage(), e);
            return null;
        }
    }

    public static String getCurrentDateString() {
        try {
            return SDF.format(new Date());
        } catch (Exception e) {
            LOG.warn(e.getMessage(), e);
            return "";
        }
    }

    public static void main(String[] args) {
        List<Map<String,Object>> list = new ArrayList<>();
        Map<String,Object> map = new HashMap<>();
        map.put("host","192.168.8.1");
        map.put("port", 2221);
        map.put("name","B003");
        list.add(map);

        map = new HashMap<>();
        map.put("host","192.168.8.2");
        map.put("port",2222);
        map.put("name","B002");
        list.add(map);

        Map<String, List<Map<String, Object>>> aaMap = new HashMap<>();
        aaMap.put("DeviceAddress", list);
        String json = toJsonString(list);
        System.out.println(json);
        TypeReference typeReference = new TypeReference<List<DeviceAddress>>(){};
        List<DeviceAddress>  deviceAddressList = jsonParseArray(json, typeReference);
//        List<DeviceAddress>  deviceAddressList1 = jsonParseArray(json, new TypeReference<List<DeviceAddress>>(){});
        for (DeviceAddress deviceAddress : deviceAddressList) {
            System.out.println(deviceAddress.getName());
        }

    }
}
