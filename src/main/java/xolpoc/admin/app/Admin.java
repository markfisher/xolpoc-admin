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

import xolpoc.admin.web.AdminController;
import xolpoc.admin.web.StreamController;
import xolpoc.admin.web.TaskController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.config.java.AbstractCloudConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.xd.analytics.metrics.core.AggregateCounterRepository;
import org.springframework.xd.analytics.metrics.core.FieldValueCounterRepository;
import org.springframework.xd.dirt.rest.TempAccessControlInterceptor;
import org.springframework.xd.dirt.rest.metrics.AggregateCountersController;
import org.springframework.xd.dirt.rest.metrics.FieldValueCountersController;

/**
 * @author Mark Fisher
 */
@SpringBootApplication
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})
@ImportResource("classpath*:/META-INF/spring-xd/analytics/redis-analytics.xml")
@Import({AdminController.class, StreamController.class, TaskController.class})
public class Admin {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(Admin.class, args);
	}

	@Autowired
	FieldValueCounterRepository fieldValueCounterRepository;

	@Autowired
	AggregateCounterRepository aggregateCounterRepository;

	@Bean
	public FieldValueCountersController fieldValueCountersController() {
		return new FieldValueCountersController(fieldValueCounterRepository);
	}

	@Bean
	public AggregateCountersController aggregateCountersController() {
		return new AggregateCountersController(aggregateCounterRepository);
	}

	@Bean
	public WebMvcConfigurer configurer() {
		return new WebMvcConfigurerAdapter() {

			@Value("${xd.ui.allow_origin:http://localhost:9889}")
			private String allowedOrigin;

			@Override
			public void addInterceptors(InterceptorRegistry registry) {
				registry.addInterceptor(new TempAccessControlInterceptor(allowedOrigin));
			}
		};
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
