package com.erbu.myspring.test;

import com.erbu.myspring.bean.UserService;
import org.junit.Test;
import org.myspringframework.core.ApplicationContext;
import org.myspringframework.core.ClassPathXmlApplicationContext;

public class MySpringTest {

    @Test
    public void testMySpring() {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("myspring.xml");
        Object user = applicationContext.getBean("userBean");
        System.out.println(user);

      UserService userService=((UserService)applicationContext.getBean("userServiceBean"));
             userService.save();

    }
}
