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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.pivotal.receptor.actions.RunAction;
import io.pivotal.receptor.client.ReceptorClient;
import io.pivotal.receptor.commands.TaskCreateRequest;
import io.pivotal.receptor.commands.TaskResponse;
import io.pivotal.receptor.support.EnvironmentVariable;
import xolpoc.model.TaskDescriptor;
import xolpoc.model.TaskStatus;
import xolpoc.spi.TaskDeployer;

/**
 * @author Michael Minella
 */
public class ReceptorTaskDeployer implements TaskDeployer {

	public static final String DOCKER_PATH = "docker://192.168.59.103:5000/module-launcher";

	private final ReceptorClient receptorClient = new ReceptorClient();

	@Override
	public void deploy(TaskDescriptor descriptor) {

		//TODO Add endpoint callback to be able to have lattice notify admin that the task has been completed

		Map<String, RunAction> action = new HashMap<>();
		RunAction runAction = new RunAction();
		runAction.setPath("java");
		runAction.setArgs(new String[] {"-Djava.security.egd=file:/dev/./urandom", "-jar",
				String.format("/opt/spring/modules/%s.jar", descriptor.getModuleName())});
		action.put("run", runAction);

		TaskCreateRequest request = new TaskCreateRequest();
		request.setTaskGuid(guid(descriptor));
		request.setRootfs(DOCKER_PATH);
		request.setLogGuid(request.getTaskGuid());
		request.setAction(action);

		List<EnvironmentVariable> environmentVariables = new ArrayList<>();

		Collections.addAll(environmentVariables, request.getEnv());

		environmentVariables.add(new EnvironmentVariable("SPRING_PROFILES_ACTIVE", "cloud"));
		Map<String, String> parameters = descriptor.getParameters();
		if (parameters != null && parameters.size() > 0) {
			for (Map.Entry<String, String> option : parameters.entrySet()) {
				environmentVariables.add(
						new EnvironmentVariable("OPTION_" + option.getKey(), option.getValue()));
			}
		}
		request.setEnv(environmentVariables.toArray(new EnvironmentVariable[environmentVariables.size()]));

		receptorClient.createTask(request);
	}

	private String guid(TaskDescriptor descriptor) {
		return "xd-" + descriptor.getGroup() + "-" + descriptor.getModuleName();
	}

	private String path(TaskDescriptor descriptor) {
		return descriptor.getGroup() + "." + descriptor.getModuleName();
	}

	@Override
	public void undeploy(TaskDescriptor descriptor) {
		receptorClient.deleteTask(guid(descriptor));
	}

	@Override
	public TaskStatus getStatus(TaskDescriptor descriptor) {
		TaskResponse task = null;
		try {
			task = receptorClient.getTask(guid(descriptor));
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		TaskStatus taskStatus = new TaskStatus(descriptor, task.getTaskGuid(), task.getState(), task.isFailed(), task.getFailureReason());

		taskStatus.addAttribute("cellId", task.getCellId());
		taskStatus.addAttribute("domain", task.getDomain());
		taskStatus.addAttribute("processGuid", task.getTaskGuid());

		return taskStatus;
	}
}
