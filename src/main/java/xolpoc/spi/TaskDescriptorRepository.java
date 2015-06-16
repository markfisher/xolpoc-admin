package xolpoc.spi;

import java.util.Map;

import xolpoc.model.TaskDescriptor;

/**
 * @author Michael Minella
 */
public interface TaskDescriptorRepository {

	Map<String, TaskDescriptor> findAll();

	TaskDescriptor find(String name);

	TaskDescriptor create(String name, String dsl);

	boolean delete(String name);

}
