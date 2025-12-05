package org.myspringframework.core;

/**
 * MySpring框架应用上下文接口
 */
public interface ApplicationContext {

    /**
     * 根据Bean名称获取Bean对象
     * beanName MySpring框架xml配置文件中bean的id
     * @return 对应的bean对象
     */
     Object getBean(String beanName);
}
