package cn.wangkf.listener;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import cn.wangkf.kafka.KafkaConsumer;
import cn.wangkf.rebuild.RebuildCacheThread;
import cn.wangkf.util.SpringContext;
import cn.wangkf.zk.ZooKeeperClient;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;


/**
 * 系统初始化的监听器
 *
 */
public class InitListener implements ServletContextListener {
	
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext sc = sce.getServletContext();
		ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(sc);
		SpringContext.setApplicationContext(context);
		
		new Thread(new KafkaConsumer("cache-message")).start();
		new Thread(new RebuildCacheThread()).start();
		
		ZooKeeperClient.init();
	}
	
	public void contextDestroyed(ServletContextEvent sce) {
		
	}

}
