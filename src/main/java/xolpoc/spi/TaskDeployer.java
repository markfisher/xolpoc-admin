package xolpoc.spi;

import xolpoc.model.TaskDescriptor;
import xolpoc.model.TaskStatus;

/**
 * @author Michael Minella
 */
public interface TaskDeployer {

	void deploy(TaskDescriptor descriptor);

	void undeploy(TaskDescriptor descriptor);

	TaskStatus getStatus(TaskDescriptor descriptor);
}
