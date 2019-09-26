package com.openagv.core;

import cn.hutool.core.util.ReflectUtil;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.openagv.core.interfaces.IHandler;
import com.openagv.core.interfaces.ITelegram;
import com.openagv.plugins.udp.UdpServerChannelManager;
import com.openagv.route.Route;
import com.openagv.tools.SettingUtils;
import com.openagv.tools.ToolsKit;
import gnu.io.SerialPort;
import org.opentcs.contrib.tcp.netty.TcpClientChannelManager;

import java.util.*;
import java.util.function.Consumer;

/**
 * 上下文及映射容器
 *
 * @author Laotang
 */
public class AppContext {

    /**要进行依赖反转的类*/
    private final static Set<Class<?>> INJECT_CLASS_SET = new HashSet<>();
    /**要进行依赖反转的对象*/
    private final static Set<Object> INJECT_OBJECT_SET = new HashSet<>();
    /**路由映射*/
    private final static Map<String, Route> ROUTE_MAP = new HashMap<>();
    /**在执行Controller前的处理器链*/
    public static List<IHandler> BEFORE_HEANDLER_LIST = new ArrayList<>();
    /**在执行Controller后的处理器链*/
    public static List<IHandler> AFTER_HEANDLER_LIST = new ArrayList<>();
    /**guice的injector*/
    private static Injector injector;
    /**injector的Module集合*/
    private final static Set<Module> MODULES = new HashSet<>();


    public static void setGuiceInjector(Injector injector) {
        AppContext.injector = injector;
    }

    public static Injector getGuiceInjector() {
        return injector;
    }

    public static Set<Module> getModules() {
        return MODULES;
    }

    public static Set<Class<?>> getInjectClassSet() {
        return INJECT_CLASS_SET;
    }

    public static Set<Object> getInjectClassObjectSet() {
        if(INJECT_OBJECT_SET.isEmpty()){
            getInjectClassSet().forEach(new Consumer<Class<?>>() {
                @Override
                public void accept(Class<?> clazz) {
                    Object injectObj = AppContext.getGuiceInjector().getInstance(clazz);
                    String key ="";
                    ROUTE_MAP.put(key, new Route(key, injectObj));
                    INJECT_OBJECT_SET.add(injectObj);
                }
            });
        }
        return INJECT_OBJECT_SET;
    }


    public static Map<String, Route> getRouteMap() {
        return ROUTE_MAP;
    }

    public static List<IHandler> getBeforeHeandlerList() {
        return BEFORE_HEANDLER_LIST;
    }

    public static void setBeforeHeandlerList(List<IHandler> beforeHeandlerList) {
        BEFORE_HEANDLER_LIST = beforeHeandlerList;
    }

    public static List<IHandler> getAfterHeandlerList() {
        return AFTER_HEANDLER_LIST;
    }

    public static void setAfterHeandlerList(List<IHandler> afterHeandlerList) {
        AFTER_HEANDLER_LIST = afterHeandlerList;
    }

    /**
     * 串口
     */
    private static SerialPort serialPort;
    public static void setSerialPort(SerialPort serialPort) {
        AppContext.serialPort = serialPort;
    }
    public static SerialPort getSerialPort() {
        return serialPort;
    }



    private static final String TELEGRAM_SETTING_FIELD = "telegram.impl";
    private static ITelegram TELEGRAM;
    public static ITelegram getTelegram(){
        if(ToolsKit.isEmpty(TELEGRAM)) {
            String telegramImpl = SettingUtils.getString(TELEGRAM_SETTING_FIELD);
            if(ToolsKit.isEmpty(telegramImpl)) {
                throw new NullPointerException("请先实现"+ ITelegram.class.getName()+"接口，并在app.setting文件里["+TELEGRAM_SETTING_FIELD+"]添加接口的实现类路径");
            }
            TELEGRAM = ReflectUtil.newInstance(telegramImpl);
        }
        return TELEGRAM;
    }


    private static Object channelManagerObj;
    public static void setChannelManager(Object channelManager) {
        channelManagerObj =channelManager;
    }
    // 初始化车辆渠道管理器
    public static void channelManagerInitialize(){
        java.util.Objects.requireNonNull(channelManagerObj, "渠道管理对象不能为空");
        if(channelManagerObj instanceof TcpClientChannelManager) {
            TcpClientChannelManager channelManager = (TcpClientChannelManager)channelManagerObj;
            if(!channelManager.isInitialized()) {
                channelManager.initialize();
            }
        }
        else if(channelManagerObj instanceof UdpServerChannelManager) {
            UdpServerChannelManager channelManager = (UdpServerChannelManager)channelManagerObj;
            if(!channelManager.isInitialized()) {
                channelManager.initialize();
            }
        }
        else {

        }
    }
}