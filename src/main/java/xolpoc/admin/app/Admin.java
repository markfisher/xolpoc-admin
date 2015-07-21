/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xolpoc.admin.app;

import org.apache.catalina.webresources.TomcatURLStreamHandlerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.config.java.AbstractCloudConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.util.ObjectUtils;

import xolpoc.admin.web.StreamController;
import xolpoc.admin.web.TaskController;

/**
 * @author Mark Fisher
 */
@SpringBootApplication
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})
@Import({StreamController.class, TaskController.class})
public class Admin {

	public static void main(String[] args) throws Exception {
		TomcatURLStreamHandlerFactory.disable();
		SpringApplication.run(Admin.class, ObjectUtils.addObjectToArray(args, "--spring.config.name=admin"));
	}

	@Configuration
	@ConditionalOnProperty("PROCESS_GUID")
	protected static class LatticeConfig extends AbstractCloudConfig {

		@Bean
		RedisConnectionFactory redisConnectionFactory() {
			return connectionFactory().redisConnectionFactory();
		}
	}
}
