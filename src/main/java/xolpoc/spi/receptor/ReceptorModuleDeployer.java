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

package xolpoc.spi.receptor;

import io.pivotal.receptor.client.ReceptorClient;
import io.pivotal.receptor.commands.ActualLRPResponse;
import io.pivotal.receptor.commands.DesiredLRPCreateRequest;
import io.pivotal.receptor.support.EnvironmentVariable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;
import org.springframework.xd.module.ModuleDescriptor;

import xolpoc.model.ModuleInstanceStatus;
import xolpoc.model.ModuleStatus;
import xolpoc.model.ModuleStatus.ModuleStatusBuilder;
import xolpoc.spi.ModuleDeployer;

/**
 * @author Mark Fisher
 */
public class ReceptorModuleDeployer implements ModuleDeployer {

	public static final String DOCKER_PATH = "docker://192.168.59.103:5000/module-launcher";

	public static final String BASE_ADDRESS = "192.168.11.11.xip.io";

	public static final String ADMIN_GUID = "xd-admin";

	private final ReceptorClient receptorClient = new ReceptorClient();

	@Override
	public void deploy(ModuleDescriptor descriptor) {
		String guid = guid(descriptor);
		DesiredLRPCreateRequest request = new DesiredLRPCreateRequest();
		request.setProcessGuid(guid);
		request.setRootfs(DOCKER_PATH);
		request.runAction().setPath("java");
		request.runAction().addArg("-Djava.security.egd=file:/dev/./urandom");
		request.runAction().addArg("-jar");
		request.runAction().addArg("/module-launcher.jar");
		List<EnvironmentVariable> environmentVariables = new ArrayList<EnvironmentVariable>();
		for (EnvironmentVariable var : request.getEnv()) {
			environmentVariables.add(var);
		}
		environmentVariables.add(new EnvironmentVariable("MODULES", path(descriptor)));
		environmentVariables.add(new EnvironmentVariable("SPRING_PROFILES_ACTIVE", "cloud"));
		Map<String, String> parameters = descriptor.getParameters();
		if (parameters != null && parameters.size() > 0) {
			for (Map.Entry<String, String> option : parameters.entrySet()) {
				environmentVariables.add(new EnvironmentVariable("OPTION_" + option.getKey(), option.getValue()));
			}
		}
		request.setEnv(environmentVariables.toArray(new EnvironmentVariable[environmentVariables.size()]));
		request.setPorts(new int[] {8080, 9000});
		request.addRoute(8080, new String[] {guid + "." + BASE_ADDRESS, guid + "-8080." + BASE_ADDRESS});
		request.addRoute(9000, new String[] {guid + "-9000." + BASE_ADDRESS});
		receptorClient.createDesiredLRP(request);
	}

	@Override
	public void undeploy(ModuleDescriptor descriptor) {
		receptorClient.deleteDesiredLRP(guid(descriptor));
	}

	@Override
	public ModuleStatus getStatus(ModuleDescriptor descriptor) {
		ModuleStatusBuilder builder = ModuleStatus.of(descriptor);
		for (ActualLRPResponse lrp : receptorClient.getActualLRPsByProcessGuid(guid(descriptor))) {
			Map<String, String> attributes = new HashMap<String, String>();
			attributes.put("address", lrp.getAddress());
			attributes.put("cellId", lrp.getCellId());
			attributes.put("domain", lrp.getDomain());
			attributes.put("processGuid", lrp.getProcessGuid());
			attributes.put("index", Integer.toString(lrp.getIndex()));
			attributes.put("ports", StringUtils.arrayToCommaDelimitedString(lrp.getPorts()));
			attributes.put("since", Long.toString(lrp.getSince()));
			builder.with(new ModuleInstanceStatus(lrp.getInstanceGuid(), lrp.getState(), attributes));
		}
		return builder.build();
	}

	private String guid(ModuleDescriptor descriptor) {
		return "xd-" + descriptor.getGroup() + "-" + descriptor.getModuleName() + "-" + descriptor.getIndex();
	}

	private String path(ModuleDescriptor descriptor) {
		return descriptor.getType() + "/" + descriptor.getModuleName();
	}

}
