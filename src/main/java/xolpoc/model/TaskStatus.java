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

import java.util.HashMap;
import java.util.Map;

import org.springframework.util.StringUtils;

/**
 * @author Michael Minella
 */
public class TaskStatus {

	private final TaskDescriptor descriptor;

	private final String id;

	private final String state;

	private final Map<String, String> attributes = new HashMap<>();

	private final boolean failed;

	private final String failedReason;


	public TaskStatus(TaskDescriptor descriptor, String id, String state, boolean failed, String failedReason) {
		this.descriptor = descriptor;
		this.id = id;
		this.state =  (StringUtils.hasText(state) ? state : "UNKNOWN");
		this.failed = failed;
		this.failedReason = failedReason;
	}

	public void addAttribute(String key, String value) {
		this.attributes.put(key, value);
	}

	public TaskDescriptor getDescriptor() {
		return descriptor;
	}

	public String getId() {
		return id;
	}

	public String getState() {
		return state;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	public Boolean getFailed() {
		return failed;
	}

	public String getFailedReason() {
		return failedReason;
	}
}
