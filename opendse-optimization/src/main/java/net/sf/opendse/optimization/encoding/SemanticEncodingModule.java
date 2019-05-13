package net.sf.opendse.optimization.encoding;

import org.opt4j.core.config.annotations.Info;

import net.sf.opendse.optimization.DesignSpaceExplorationModule;


@Info("Uses semantic encoding for the constraints (right now this is just syntactic sugar).")
public class SemanticEncodingModule extends DesignSpaceExplorationModule {

	@Override
	protected void config() {
		bind(Encoding.class).to(MyEncoding.class);

	}

}
