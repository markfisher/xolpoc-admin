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
package xolpoc.admin.web;

import java.util.HashMap;
import java.util.Map;

import xolpoc.model.TaskDescriptor;
import xolpoc.model.TaskStatus;
import xolpoc.spi.TaskDeployer;
import xolpoc.spi.TaskDescriptorRepository;
import xolpoc.spi.defaults.InMemoryTaskDescriptorRepository;
import xolpoc.spi.receptor.ReceptorTaskDeployer;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Michael Minella
 */
@RestController
@RequestMapping("/tasks")
public class TaskController {

	private final TaskDeployer deployer = new ReceptorTaskDeployer();

	private final TaskDescriptorRepository repository =
			new InMemoryTaskDescriptorRepository();

	@RequestMapping
	public Map<String, TaskStatus> listTasks() {
		Map<String, TaskStatus> results = new HashMap<>();

		for (Map.Entry<String, TaskDescriptor> entry : repository.findAll().entrySet()) {
			TaskStatus status = deployer.getStatus(entry.getValue());

			if(status == null) {
				repository.delete(entry.getKey());
			}
			else {
				results.put(entry.getKey(), status);
			}
		}

		return results;
	}

	@RequestMapping(value = "/{name}", method = RequestMethod.POST)
	public void createTask(@PathVariable("name") String name, @RequestBody String dsl) {
		TaskDescriptor definition = repository.create(name, dsl);
		deployer.deploy(definition);
	}

	@RequestMapping(value = "/{name}", method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.OK)
	public void destroyTask(@PathVariable("name") String name) {
		TaskDescriptor definition = repository.find(name);
		if (definition == null) {
			throw new IllegalArgumentException("unable to find definition for: " + name);
		}

		deployer.undeploy(definition);
		repository.delete(name);
	}
}
