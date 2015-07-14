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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.xd.module.ModuleDescriptor;

import xolpoc.model.ModuleStatus;
import xolpoc.model.StreamDefinition;
import xolpoc.spi.ModuleDeployer;
import xolpoc.spi.StreamDefinitionRepository;
import xolpoc.spi.defaults.InMemoryStreamDefinitionRepository;
import xolpoc.spi.local.LocalModuleDeployer;

/**
 * @author Mark Fisher
 */
@RestController
@RequestMapping("/streams")
public class StreamController {

	private final StreamDefinitionRepository repository = new InMemoryStreamDefinitionRepository();

	// TODO: inject profile-driven deployer
	//private final ModuleDeployer deployer = new ReceptorModuleDeployer();
	private final ModuleDeployer deployer = new LocalModuleDeployer();

	@RequestMapping
	public Map<String, List<ModuleStatus>> listStreams() {
		Map<String, List<ModuleStatus>> results = new HashMap<String, List<ModuleStatus>>();
		for (Map.Entry<String, StreamDefinition> entry : repository.findAll().entrySet()) {
			List<ModuleStatus> moduleStates = new ArrayList<ModuleStatus>();
			for (ModuleDescriptor descriptor : entry.getValue().getModuleDescriptors()) {
				moduleStates.add(deployer.getStatus(descriptor));
			}
			results.put(entry.getKey(), moduleStates);
		}
		return results;
	}

	@RequestMapping(value = "/{name}", method = RequestMethod.POST)
	public void createStream(@PathVariable("name") String name, @RequestBody String dsl) {
		StreamDefinition definition = repository.create(name, dsl);
		List<ModuleDescriptor> modules = definition.getModuleDescriptors();
		for (int i = modules.size() - 1; i >= 0; i--) {
			deployer.deploy(modules.get(i));
		}
	}

	@RequestMapping(value = "/{name}", method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.OK)
	public void destroyStream(@PathVariable("name") String name) {
		StreamDefinition definition = repository.find(name);
		if (definition == null) {
			throw new IllegalArgumentException("unable to find definition for: " + name);
		}
		List<ModuleDescriptor> modules = definition.getModuleDescriptors();
		for (ModuleDescriptor module : modules) {
			deployer.undeploy(module);
		}
		repository.delete(name);
	}

}
