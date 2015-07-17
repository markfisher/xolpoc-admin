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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.xd.module.ModuleDescriptor;

/**
 * @author Mark Fisher
 */
public class ModuleStatus {
//TODO: make this generic and/or push down the receptor specifics into the ReceptorModuleDeployer's getModuelStatus method
// we should define an enum (failed, incomplete, deploying, deployed) at the SPI level
// all that impls do is map to those enum values from whatever their system provides

	private final ModuleDescriptor descriptor;

	private final Map<String, ModuleInstanceStatus> instances = new HashMap<String, ModuleInstanceStatus>();

	private ModuleStatus(ModuleDescriptor descriptor) {
		this.descriptor = descriptor;
	}

	public String getName() {
		return descriptor.getModuleLabel();
	}

	public String getState() {
		Set<String> instanceStates = new HashSet<String>();
		for (Map.Entry<String, ModuleInstanceStatus> entry : instances.entrySet()) {
			instanceStates.add(entry.getValue().getState());
		}
		String state = "failed";
		if (instanceStates.size() == 1 && "RUNNING".equals(instanceStates.iterator().next())) {
			state = "deployed";
		}
		if (instanceStates.contains("UNCLAIMED")) {
			state = (instanceStates.size() == 1 ? "failed" : "incomplete");
		}
		if (instanceStates.contains("CLAIMED")) {
			state = "deploying";
		}
		return state;
	}

	public Map<String, ModuleInstanceStatus> getInstances() {
		return instances;
	}

	private void addInstance(String id, ModuleInstanceStatus status) {
		this.instances.put(id, status);
	}

	public static ModuleStatusBuilder of(ModuleDescriptor descriptor) {
		return new ModuleStatusBuilder(descriptor);
	}

	public static class ModuleStatusBuilder {

		private final ModuleStatus status;

		private ModuleStatusBuilder(ModuleDescriptor descriptor) {
			this.status = new ModuleStatus(descriptor);
		}

		public ModuleStatusBuilder with(ModuleInstanceStatus instance) {
			status.addInstance(instance.getId(), instance);
			return this;
		}

		public ModuleStatus build() {
			return status;
		}
	}
}
