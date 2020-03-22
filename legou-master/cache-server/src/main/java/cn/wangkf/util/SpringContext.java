package cn.wangkf.util;

import org.springframework.context.ApplicationContext;

/**
 * spring上下文：用来获取 bean
 *
 *
 */
public class SpringContext {

	private static ApplicationContext applicationContext;

	public static ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public static void setApplicationContext(ApplicationContext applicationContext) {
		SpringContext.applicationContext = applicationContext;
	}
	
}
