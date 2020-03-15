package cn.wangkf.common.utils;

import org.dozer.Mapper;
import org.dozer.MappingException;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.List;
import java.util.stream.Collectors;

public class DozerUtils implements ApplicationContextAware
{
    private static Mapper mapper;

    /**
     * 容器初始化获取mapper
     *
     * @param applicationContext
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.mapper = applicationContext.getBean(Mapper.class);
    }

    /**
     * Constructs new instance of destinationClass and performs mapping between from source
     *
     * @param source
     * @param destinationClass
     * @param <T>
     * @return
     * @throws MappingException
     */
    public static <T> T map(Object source, Class<T> destinationClass) throws MappingException
    {
        if(source == null)
        {
            return null;
        }

        return mapper.map(source, destinationClass);
    }

    /**
     * Performs mapping between source and destination objects
     *
     * @param source
     * @param destination
     * @throws MappingException
     */
    public static void map(Object source, Object destination) throws MappingException
    {
        mapper.map(source, destination);
    }

    /**
     * Constructs new instance of destinationClass and performs mapping between from source
     *
     * @param source
     * @param destinationClass
     * @param mapId
     * @param <T>
     * @return
     * @throws MappingException
     */
    public static <T> T map(Object source, Class<T> destinationClass, String mapId) throws MappingException
    {
        if(source == null)
        {
            return null;
        }

        return mapper.map(source, destinationClass, mapId);
    }

    /**
     * Performs mapping between source and destination objects
     *
     * @param source
     * @param destination
     * @param mapId
     * @throws MappingException
     */
    public static void map(Object source, Object destination, String mapId) throws MappingException
    {
        mapper.map(source, destination, mapId);
    }

    /**
     * 对象集合映射
     *
     * @param sources
     * @param destinationClass
     * @param <T>
     * @return
     * @throws MappingException
     */
    public static <T> List<T> mapList(List<?> sources, Class<T> destinationClass) throws MappingException
    {
        return sources.stream().map(source -> mapper.map(source, destinationClass)).collect(Collectors.toList());
    }

}
