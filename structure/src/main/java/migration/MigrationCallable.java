package migration;

import java.util.concurrent.Callable;

import objects.GenericObject;
import structure.FileBuilder;

public class MigrationCallable implements Callable<GenericObject>{
	private GenericObject source;
	private GenericObject target;
	
	public MigrationCallable(GenericObject source, GenericObject target){
		this.source=source;
		this.target=target;
	}

	@Override
	public GenericObject call() throws Exception {
		GenericObject newFolder = new FileBuilder().parent(target).path(source.getName()).statistics(target.getStatistics()).configuration(target.getConfiguration()).build();
		if (newFolder.container()){
			for (GenericObject child:source.children()){
				//child.migrate(executor, child, newFolder);
			}
			return newFolder;
		}
		
		return null;
	}
}