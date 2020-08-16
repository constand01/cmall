package com.cmall.payment;

import com.cmall.mq.ActiveMQUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

@SpringBootTest
class CmallPaymentApplicationTests {

    @Autowired
    ActiveMQUtil  activeMQUtil;

    @Test
    void contextLoads() throws JMSException {
        ConnectionFactory connectionFactory= activeMQUtil.getConnectionFactory();
       Connection connection= connectionFactory.createConnection();

       System.out.print(connection);

    }

}
