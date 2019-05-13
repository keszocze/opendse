package net.sf.opendse.optimization.encoding.common;

import net.sf.opendse.optimization.DesignSpaceExplorationModule;

public class ConstraintPreprocessingSemanticModule extends DesignSpaceExplorationModule {

	@Override
	protected void config() {
		bind(ConstraintPreprocessing.class).to(ConstraintPreprocessingSemantic.class);
	}
}
