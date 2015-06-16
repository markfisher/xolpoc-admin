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
package xolpoc.model;

import java.util.Map;

/**
 * @author Michael Minella
 */
public class TaskDescriptor {
	private final String name;
	private final String taskLabel;
	private final String group;
	private final Map<String, String> parameters;
	private final String moduleName;

	public TaskDescriptor(String name, String taskLabel, String group, Map<String, String> parameters, String moduleName) {
		this.name = name;
		this.taskLabel = taskLabel;
		this.group = group;
		this.parameters = parameters;
		this.moduleName = moduleName;
	}

	public String getTaskLabel() {
		return taskLabel;
	}

	public String getGroup() {
		return group;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public String getModuleName() {
		return moduleName;
	}

	public String getName() {
		return name;
	}
}
