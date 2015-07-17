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

package xolpoc.spi.local;

import java.io.File;

import org.springframework.pipes.module.launcher.ModuleLauncher;
import org.springframework.xd.module.ModuleDescriptor;

import xolpoc.model.ModuleStatus;
import xolpoc.spi.ModuleDeployer;

/**
 * @author Mark Fisher
 */
public class LocalModuleDeployer implements ModuleDeployer {

	ModuleLauncher launcher = new ModuleLauncher(new File("/opt/spring/modules"));

	@Override
	public void deploy(ModuleDescriptor descriptor) {
		// TODO: pass args (from descriptor.getParameters()) in the 2nd arg to launch
		launcher.launch(new String[] { path(descriptor) }, new String[0]);
	}

	@Override
	public void undeploy(ModuleDescriptor descriptor) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ModuleStatus getStatus(ModuleDescriptor descriptor) {
		throw new UnsupportedOperationException();
	}

	private String path(ModuleDescriptor descriptor) {
		return descriptor.getType() + "/" + descriptor.getModuleName();
	}
}
