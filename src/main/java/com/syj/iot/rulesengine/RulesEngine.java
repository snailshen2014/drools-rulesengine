/**
 * 
 */
package com.syj.iot.rulesengine;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
@ImportResource({ "classpath*:META-INF.spring/spring-*.xml" })
@EnableScheduling
public class RulesEngine extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(RulesEngine.class);
	}

	public static void main(String[] args) {
		try {
			SpringApplication app = new SpringApplication(RulesEngine.class);
			app.setBannerMode(Banner.Mode.OFF);
			app.run(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
