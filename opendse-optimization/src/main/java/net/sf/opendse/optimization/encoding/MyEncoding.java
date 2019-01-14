package net.sf.opendse.optimization.encoding;

import static edu.uci.ics.jung.graph.util.EdgeType.UNDIRECTED;
import static net.sf.opendse.model.Models.filterCommunications;
import static net.sf.opendse.model.Models.filterProcesses;
import static net.sf.opendse.model.Models.getInLinks;
import static net.sf.opendse.model.Models.getLinks;
import static net.sf.opendse.model.Models.getOutLinks;
import static net.sf.opendse.model.Models.isProcess;
import static net.sf.opendse.optimization.encoding.variables.Variables.p;
import static net.sf.opendse.optimization.encoding.variables.Variables.var;

import java.util.List;

import org.opt4j.satdecoding.Clause;
import org.opt4j.satdecoding.Constraint;
import org.opt4j.satdecoding.Implies;
import org.opt4j.satdecoding.Literal;
import org.opt4j.satdecoding.Nand;

import com.google.inject.Inject;

import edu.uci.ics.jung.graph.util.Pair;
import net.sf.opendse.model.Application;
import net.sf.opendse.model.Architecture;
import net.sf.opendse.model.Dependency;
import net.sf.opendse.model.Edge;
import net.sf.opendse.model.Link;
import net.sf.opendse.model.Mapping;
import net.sf.opendse.model.Models.DirectedLink;
import net.sf.opendse.model.Resource;
import net.sf.opendse.model.Specification;
import net.sf.opendse.model.Task;
import net.sf.opendse.optimization.constraints.SpecificationConstraints;

public class MyEncoding extends Encoding {

	@Inject
	public MyEncoding(SpecificationConstraints specificationConstraints, RoutingEncoding routingEncoding) {
		super(specificationConstraints, routingEncoding);
		// intentionally left blank
	}

	/**
	 * A resource which is the mapping target of an activated mapping edge also has
	 * to be activated in a valid implementation.
	 * 
	 * @param constraints
	 * @param specification
	 */
	@Override
	protected void EQ2(List<Constraint> constraints, Specification specification) {
		for (Mapping<Task, Resource> m : specification.getMappings()) {
			Resource r = m.getTarget();
			Constraint cons = new Implies(p(m), p(r));
			constraints.add(cons);
		}
	}

	@Override
	protected void EQ3EQ4(List<Constraint> constraints, Specification specification) {
		for (Dependency dependency : specification.getApplication().getEdges()) {
			Task p0 = specification.getApplication().getSource(dependency);
			Task p1 = specification.getApplication().getDest(dependency);

			if (isProcess(p0) && isProcess(p1)) {
				// both tasks are processes
				for (Mapping<Task, Resource> m0 : specification.getMappings().get(p0)) {
					for (Mapping<Task, Resource> m1 : specification.getMappings().get(p1)) {
						Resource r0 = m0.getTarget();
						Resource r1 = m1.getTarget();

						Edge l = specification.getArchitecture().findEdge(r0, r1);

						if (l != null) {
							// case where there is a link connecting the mapping
							// targets of both processes
							constraints.add(new Implies(p(m0), p(m1), p(l)));
						} else if (!r0.equals(r1)) {
							// the processes are mapped to two different
							// resources that are not connected by a link => at
							// most one of the considered mapping edges may be
							// activated at the same time
							constraints.add(new Nand(p(m0), p(m1)));
						}
					}
				}
			}
		}
	}

	
	// TODO EQ 5 bauen
	// es wäre (r1 r2 ~l) (r1 ~r2 ~l) (~r1 r2 ~l)
	
	@Override
	protected void EQ6(List<Constraint> constraints, Specification specification) {
		for (Task c : filterCommunications(specification.getApplication())) {
			Architecture<Resource, Link> routing = specification.getRoutings().get(c);
			for (Resource r : routing) {
				Literal cr = p(var(c, r));
				constraints.add(new Implies(cr, p(r)));
			}
		}
	}

	@Override
	protected void EQ7(List<Constraint> constraints, Specification specification) {
		for (Task c : filterCommunications(specification.getApplication())) {
			Architecture<Resource, Link> routing = specification.getRoutings().get(c);
			for (DirectedLink lrr : getLinks(routing)) {
				Literal l = p(lrr.getLink());
				Literal cl = p(var(c, lrr));
				constraints.add(new Implies(cl, l));
			}
		}
	}
	
	// TODO EQ 8 bauen

	@Override
	protected void EQ9(List<Constraint> constraints, Specification specification) {
		for (Task c : filterCommunications(specification.getApplication())) {
			Architecture<Resource, Link> routing = specification.getRoutings().get(c);

			// TODO herausfinden, welche EQ das eigentlich ist (EQ 11 ist zumindest ein
			// NAND)

			for (Link l : routing.getEdges()) {
				if (routing.getEdgeType(l) == UNDIRECTED) {
					Pair<Resource> endpoints = routing.getEndpoints(l);
					Resource r0 = endpoints.getFirst();
					Resource r1 = endpoints.getSecond();

					Literal a = p(var(c, l, r0, r1));
					Literal b = p(var(c, l, r1, r0));
					constraints.add(new Nand(a, b));
				}
			}
		}
	}

	@Override
	protected void EQ10EQ11(List<Constraint> constraints, Specification specification) {
		// iterate over all communications
		for (Task c : filterCommunications(specification.getApplication())) {
			// iterate over all flows of the current communication
			for (Task p : filterProcesses(specification.getApplication().getNeighbors(c))) {
				// iterate over all possible mapping targets of the current
				// communication flow
				for (Mapping<Task, Resource> m : specification.getMappings().get(p)) {
					Resource r = m.getTarget();
					if (specification.getRoutings().get(c).containsVertex(r)) {
						// case where the resource is part of the routing graph
						// : if the mapping of the comm flow on the resource is
						// activated, the communication has to be routed over
						// the resource

						// scheint EQ 9 zu sein
						Literal cr = p(var(c, r));
						constraints.add(new Implies(p(m), cr));
					} else {

						// in echt EQ 10

						// case where the resource is not in the routing graph :
						// the mapping must not be activated
						Clause not = new Clause();
						not.add(p(m).negate());
						System.out.println(not);
						constraints.add(not);
					}
				}
			}
		}
	}

	@Override
	protected void EQ12(List<Constraint> constraints, Specification specification) {
		// scheint EQ 11 zu sein

		for (Task c : filterCommunications(specification.getApplication())) {
			for (Task p : filterProcesses(specification.getApplication().getPredecessors(c))) {
				for (Mapping<Task, Resource> m : specification.getMappings().get(p)) {
					Resource r0 = m.getTarget();
					Architecture<Resource, Link> routing = specification.getRoutings().get(c);

					for (DirectedLink lrr : getInLinks(routing, r0)) {
						Literal clrr = p(var(c, lrr));
						constraints.add(new Nand(p(m), clrr));
					}

				}
			}
		}
	}

	@Override
	protected void EQ14(List<Constraint> constraints, Specification specification) {
		// EQ 13
		for (Task c : filterCommunications(specification.getApplication())) {
			Architecture<Resource, Link> routing = specification.getRoutings().get(c);

			for (Resource r0 : routing) {
				Clause clause = new Clause();
				Literal cr = p(var(c, r0));
				clause.add(cr.negate());

				for (Task p : filterProcesses(specification.getApplication().getSuccessors(c))) {
					for (Mapping<Task, Resource> m : specification.getMappings().get(p, r0)) {
						clause.add(p(m));
					}
				}
				for (DirectedLink lrr : getOutLinks(routing, r0)) {
					Literal clrr = p(var(c, lrr));
					clause.add(clrr);
				}
				constraints.add(clause);
			}
		}
	}

	@Override
	protected void EQ15(List<Constraint> constraints, Specification specification) {
		// EQ 14
		for (Task c : filterCommunications(specification.getApplication())) {
			Architecture<Resource, Link> routing = specification.getRoutings().get(c);

			for (Resource r0 : routing) {
				Clause clause = new Clause();
				Literal cr = p(var(c, r0));
				clause.add(cr.negate());

				for (Task p : filterProcesses(specification.getApplication().getPredecessors(c))) {
					for (Mapping<Task, Resource> m : specification.getMappings().get(p, r0)) {
						clause.add(p(m));
					}
				}
				for (DirectedLink lrr : getInLinks(routing, r0)) {
					Literal clrr = p(var(c, lrr));
					clause.add(clrr);
				}

				constraints.add(clause);
			}
		}
	}

	@Override
	protected void EQ18(List<Constraint> constraints, Specification specification) {
		final Application<Task, Dependency> application = specification.getApplication();

		for (Task c : filterCommunications(application)) {
			for (Task p0 : filterProcesses(application.getPredecessors(c))) {
				for (Task p1 : filterProcesses(application.getSuccessors(c))) {
					for (Mapping<Task, Resource> m : specification.getMappings().get(p0)) {
						Resource r0 = m.getTarget();
						Architecture<Resource, Link> routing = specification.getRoutings().get(c);

						for (DirectedLink lrr : getInLinks(routing, r0)) {
							Literal clrr = p(var(c, lrr, p1));
							constraints.add(new Nand(p(m), clrr));
						}
					}
				}
			}
		}
	}

}
