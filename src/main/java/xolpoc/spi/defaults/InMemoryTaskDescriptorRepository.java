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
package xolpoc.spi.defaults;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import xolpoc.model.TaskDescriptor;
import xolpoc.spi.TaskDescriptorRepository;

/**
 * @author Michael Minella
 */
public class InMemoryTaskDescriptorRepository implements TaskDescriptorRepository {

	private final Map<String, TaskDescriptor> repository = new ConcurrentHashMap<>();

	@Override
	public Map<String, TaskDescriptor> findAll() {
		return Collections.unmodifiableMap(repository);
	}

	@Override
	public TaskDescriptor find(String name) {
		return repository.get(name);
	}

	@Override
	public TaskDescriptor create(String name, String dsl) {
		String[] nameAndOptions = dsl.split("\\s", 2);
		Map<String, String> parameters = new HashMap<>();

		if (nameAndOptions.length == 2) {
			String optionsString = nameAndOptions[1];
			String[] optionTokens = optionsString.split("\\s");
			for (String s : optionTokens) {
				String[] kv = s.split("=", 2);
				parameters.put(kv[0].replaceFirst("--", ""), kv[1]);
			}
		}

		TaskDescriptor taskDescriptor = new TaskDescriptor(name, null, name, parameters, nameAndOptions[0]);

		repository.put(name, taskDescriptor);

		return taskDescriptor;
	}

	@Override
	public boolean delete(String name) {
		return repository.remove(name) != null;
	}
}
