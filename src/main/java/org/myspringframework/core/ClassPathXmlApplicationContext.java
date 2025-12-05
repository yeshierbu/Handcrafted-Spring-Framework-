package org.myspringframework.core;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.stax.StAXResult;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ClassPathXmlApplicationContext implements ApplicationContext {

    private static final Logger logger = LoggerFactory.getLogger(ClassPathXmlApplicationContext.class);
    private Map<String, Object> singletonObjects = new HashMap<>();

    /**
     * 解析MySpring配置文件，初始化所有Bean对象
     *
     * @param configLocation xml文件在src根目录
     */
    public ClassPathXmlApplicationContext(String configLocation) {
        try {
            //解析xml文件，初始化bean，并存放到map集合
            //创建dom4j解析xml文件的对象
            SAXReader reader = new SAXReader();
            //获取一个输入流指向xml文件
            InputStream stream = ClassLoader.getSystemClassLoader().getResourceAsStream(configLocation);
            Document document = reader.read(stream);
            //获取所有bean标签
            List<Node> nodes = document.selectNodes("//bean");
            //遍历bean标签
            nodes.forEach(node -> {
                try {
                    //向下转型,为了使用Element更丰富的方法
                    Element nodeElement = (Element) node;
                    //获取id
                    String id = nodeElement.attributeValue("id");
                    //获取value
                    String className = nodeElement.attributeValue("class");
                    logger.info("beanName={}", id);
                    logger.info("beanClass={}", className);
                    //通过反射机制创建对象，将其放到Map集合中提前曝光
                    //获取class
                    Class<?> aClass = Class.forName(className);
                    //获取无参构造方法
                    Constructor<?> defaultConstructor = aClass.getConstructor();
                    //实例化Bean
                    Object bean = defaultConstructor.newInstance();
                    // 加入map集合进行曝光
                    singletonObjects.put(id, bean);
                    logger.info(singletonObjects.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            //遍历所有Bean标签給对象属性赋值
            nodes.forEach(node -> {
                try {
                    //向下转型,为了使用Element更丰富的方法
                    Element beanElement = (Element) node;
                    //获取id
                    String id = beanElement.attributeValue("id");
                    String className = beanElement.attributeValue("class");
                    //获取class
                    Class<?> aClass = Class.forName(className);
                    //获取该bean标签下property属性标签
                    List<Element> properties = beanElement.elements("property");
                    //遍历propertie
                    properties.forEach(property -> {
                        try {
                            String name = property.attributeValue("name");
                            //获取属性类型
                            Field field = aClass.getDeclaredField(name);
                            logger.info("propertyName={}", name);
                            //拼接得到set方法名
                            String setMethodName = "set" + name.toUpperCase().charAt(0) + name.substring(1);
                            //获取set方法
                            Method setMethod = aClass.getDeclaredMethod(setMethodName, field.getType());
                            Object actualValue = null;
                            //获取具体的property的value/ref值
                            String value = property.attributeValue("value");
                            String ref = property.attributeValue("ref");
                            if (value != null) {

                                //调用set方法
                                //只支持以下简单类型:bity short int long  float double char boolean
                                //Byte Short Integer Long Float Double Character Boolean
                                //String
                                //获取属性类型名
                                String propertyTypeSimpleName = field.getType().getSimpleName();
                        /*
                        官方Sring框架通过元数据+类型转换体系来把配置里的字符串转成目标类型
                         */
                                //判断属性类型是什么
                                // 判断属性类型是什么
                                switch (propertyTypeSimpleName) {
                                    case "byte":
                                        actualValue = Byte.parseByte(value);
                                        break;
                                    case "short":
                                        actualValue = Short.parseShort(value);
                                        break;
                                    case "int":
                                        actualValue = Integer.parseInt(value);
                                        break;
                                    case "long":
                                        actualValue = Long.parseLong(value);
                                        break;
                                    case "float":
                                        actualValue = Float.parseFloat(value);
                                        break;
                                    case "double":
                                        actualValue = Double.parseDouble(value);
                                        break;
                                    case "char":
                                        if (value.length() == 1) {
                                            actualValue = value.charAt(0);
                                        } else {
                                            throw new IllegalArgumentException("字符类型必须为单个字符: " + value);
                                        }
                                        break;
                                    case "boolean":
                                        actualValue = Boolean.parseBoolean(value);
                                        break;
                                    case "Byte":
                                        actualValue = Byte.valueOf(value);
                                        break;
                                    case "Short":
                                        actualValue = Short.valueOf(value);
                                        break;
                                    case "Integer":
                                        actualValue = Integer.valueOf(value);
                                        break;
                                    case "Long":
                                        actualValue = Long.valueOf(value);
                                        break;
                                    case "Float":
                                        actualValue = Float.valueOf(value);
                                        break;
                                    case "Double":
                                        actualValue = Double.valueOf(value);
                                        break;
                                    case "Character":
                                        if (value.length() == 1) {
                                            actualValue = Character.valueOf(value.charAt(0));
                                        } else {
                                            throw new IllegalArgumentException("字符类型必须为单个字符: " + value);
                                        }
                                        break;
                                    case "Boolean":
                                        actualValue = Boolean.valueOf(value);
                                        break;
                                    case "String":
                                        actualValue = value;  // String类型直接赋值
                                        break;
                                    default:
                                        throw new IllegalArgumentException("不支持的类型: " + propertyTypeSimpleName);
                                }
                                setMethod.invoke(singletonObjects.get(id), actualValue);
                            }
                            if (ref != null) {
                                //调用set方法
                                setMethod.invoke(singletonObjects.get(id), singletonObjects.get(ref));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public Object getBean(String beanName) {
        Object bean = singletonObjects.get(beanName);
        if (bean == null) {
            throw new IllegalArgumentException("No bean named '" + beanName + "' available");
        }
        return bean;
    }
}
