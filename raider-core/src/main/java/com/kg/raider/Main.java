package com.kg.raider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import java.lang.management.ManagementFactory;

/**
 * User: kaveh
 * Date: 8/8/13
 * Time: 7:17 PM
 *
 * See Raider.java for internal components. This class
 * just registers a shutdown hook and mbeans
 */
public class Main {

    public static Raider raider;
    private static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("starting raider");

        // this is where all the internal components & services are started
        raider = new Raider();

        logger.info("registering shutdown hook");
        Runtime runtime = Runtime.getRuntime();
        runtime.addShutdownHook(new Thread() {
            public void run() {
                try {
                    raider.shutdown();
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        logger.info("registering mbeans");
        ObjectName raiderJMXObject;
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        try {
            raiderJMXObject = new ObjectName("com.kg.raider:type=RaiderMBean");
            mbs.registerMBean(raider, raiderJMXObject);
        } catch (MalformedObjectNameException |
                 NotCompliantMBeanException |
                 InstanceAlreadyExistsException |
                 MBeanRegistrationException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }

    }
}
