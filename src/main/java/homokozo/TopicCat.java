package homokozo;

import org.apache.activemq.ActiveMQConnectionFactory;
import sun.misc.Signal;
import sun.misc.SignalHandler;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.print.attribute.standard.ReferenceUriSchemesSupported;
import java.net.UnknownHostException;
import java.util.Properties;

public class TopicCat {

    // build using mvn clean compile assembly:single

    private String initialContextFactory = "org.apache.activemq.jndi.ActiveMQInitialContextFactory";
    //    private boolean messageReceived = false;

    public static void main(String[] args) {
        SignalHandler handler = sig -> {
            System.out.println("\rReceived SIGINT, goodbye!");
            System.exit(0);
        };
        Signal.handle(new Signal("INT"), handler);
        TopicCat topicCat = new TopicCat();
        topicCat.subscribeWithTopicLookup(args);
        //        topicCat.getFromTopicHopefully(args);
    }

    private void getFromTopicHopefully(String[] connectionStrings) {
        ConnectionFactory factory = new ActiveMQConnectionFactory(connectionStrings[0]);
        try {
            Connection con = factory.createConnection();
            Session session = con.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic topic = session.createTopic(connectionStrings[1]);
            MessageConsumer consumer = session.createConsumer(topic);
            con.start();
            while (true) {
                Message msg = consumer.receive(5000);
                if (msg instanceof TextMessage) {
                    TextMessage tm = (TextMessage) msg;
                    System.out.println(tm.getText());
                } else {
                    System.out.println("Topic Empty");
                    con.stop();
                    break;
                }
            }
            System.exit(0);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    private void subscribeWithTopicLookup(String[] args) {
        Properties properties = new Properties();
        TopicConnection topicConnection = null;
        properties.put("java.naming.factory.initial", initialContextFactory);
        boolean firstArgIsURL = args[0].matches("\\w+://\\w+:\\d+") && args.length > 1;
        String amqAddress = firstArgIsURL ? args[0] : "tcp://localhost:61616"; // just wow
        properties.put(Context.PROVIDER_URL, amqAddress);
        String topicName = firstArgIsURL ? args[1] : args[0];
        properties.put("topic." + topicName, topicName);
        try {
            InitialContext ctx = new InitialContext(properties);
            TopicConnectionFactory topicConnectionFactory = (TopicConnectionFactory) ctx.lookup("TopicConnectionFactory");
            topicConnection = topicConnectionFactory.createTopicConnection();
            System.out.println("Created Topic Connection for Topic " + topicName);

            while (true) {
                try {
                    TopicSession topicSession = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);

                    Topic topic = (Topic) ctx.lookup(topicName);
                    topicConnection.start();
                    javax.jms.TopicSubscriber topicSubscriber = topicSession.createSubscriber(topic);
                    QuickMessageListener quickMessageListener = new QuickMessageListener();
                    topicSubscriber.setMessageListener(quickMessageListener);

                    Thread.sleep(5000); // ???
                    topicSubscriber.close();
                    topicSession.close();
                } catch (JMSException | NamingException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (NamingException e) {
            throw new RuntimeException("Error in initial context lookup", e);
        } catch (JMSException e) {
            Throwable cause = e.getCause();
            if (cause instanceof UnknownHostException) {
                System.out.println("Unknown hostname: " + cause.getMessage());
                System.exit(1);
            }
            throw new RuntimeException("Error in configuring JMS connection", e);
        } finally {
            if (topicConnection != null) {
                try {
                    topicConnection.close();
                } catch (JMSException e) {
                    throw new RuntimeException("Error in closing topic connection", e);
                }
            }
        }
    }

    public class QuickMessageListener implements MessageListener {

        @Override
        public void onMessage(Message message) {
            try {
                System.out.println("This is the message: " + ((TextMessage) message).getText());
                //                messageReceived = true;
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }
}
