package net.sf.opendse.optimization.encoding;

import org.opt4j.core.start.Constant;

import com.google.inject.Inject;

import edu.uci.ics.jung.graph.util.EdgeType;
import net.sf.opendse.model.Application;
import net.sf.opendse.model.Architecture;
import net.sf.opendse.model.Communication;
import net.sf.opendse.model.Dependency;
import net.sf.opendse.model.Link;
import net.sf.opendse.model.Mapping;
import net.sf.opendse.model.Mappings;
import net.sf.opendse.model.Resource;
import net.sf.opendse.model.Specification;
import net.sf.opendse.model.Task;
import net.sf.opendse.optimization.SpecificationWrapper;
import net.sf.opendse.visualization.SpecificationViewer;

/**
 * A {@link SpecificationWrapper} yielding a simplistic spec built according to
 * two parameters, the number of communication nodes and the number of fully
 * connected switches.
 * 
 * @author Fedor Smirnov
 *
 */
public class ScalableSpec implements SpecificationWrapper {

	protected final Specification spec;
	protected int currentId = 0;
	protected Task srcTask = new Task("src");
	protected Task destTask = new Task("dest");
	protected Resource srcRes = new Resource("srcRes");
	protected Resource destRes = new Resource("destRes");

	@Inject
	public ScalableSpec(@Constant(value = "message number", namespace = ScalableSpec.class) int messageNum, 
			@Constant(value = "switch number", namespace = ScalableSpec.class) int switchNum) {
		this.spec = buildSpec(messageNum, switchNum);
	}

	protected Specification buildSpec(int messageNum, int switchNum) {
		return new Specification(getAppl(messageNum), getArch(switchNum), getMappings());
	}
	
	protected Application<Task, Dependency> getAppl(int messageNum){
		Application<Task, Dependency> result = new Application<Task, Dependency>();
		for (int i = 0; i < messageNum; i++) {
			Communication comm = new Communication("comm_" + currentId++);
			Dependency dep1 = new Dependency("dep_" + currentId++);
			Dependency dep2 = new Dependency("dep_" + currentId++);
			result.addEdge(dep1, srcTask, comm, EdgeType.DIRECTED);
			result.addEdge(dep2, comm, destTask, EdgeType.DIRECTED);
		}
		return result;
	}
	
	protected Architecture<Resource, Link> getArch(int switchNum){
		Architecture<Resource, Link> result = new Architecture<Resource, Link>();
		for (int i = 0; i < switchNum; i++) {
			Resource sw = new Resource("switch_" + currentId++);
			Link l1 = new Link("link_" + currentId++);
			Link l2 = new Link("link_" + currentId++);
			result.addEdge(l1, srcRes, sw, EdgeType.UNDIRECTED);
			result.addEdge(l2, sw, destRes, EdgeType.UNDIRECTED);
		}
		return result;
	}
	
	protected Mappings<Task, Resource> getMappings(){
		Mappings<Task, Resource> result = new Mappings<Task, Resource>();
		Mapping<Task, Resource> src = new Mapping<Task, Resource>("srcMapping", srcTask, srcRes);
		Mapping<Task, Resource> dest = new Mapping<Task, Resource>("destMapping", destTask, destRes);
		result.add(src);
		result.add(dest);
		return result;
	}

	@Override
	public Specification getSpecification() {
		return spec;
	}
	
	public static void main(String[] args) {
		ScalableSpec spec = new ScalableSpec(100, 100);
		SpecificationViewer.view(spec.getSpecification());
	}
}
