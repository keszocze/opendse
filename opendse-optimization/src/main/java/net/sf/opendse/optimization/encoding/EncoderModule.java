package net.sf.opendse.optimization.encoding;

import net.sf.opendse.optimization.DesignSpaceExplorationModule;

public class EncoderModule extends DesignSpaceExplorationModule {

	@Override
	protected void config() {
		bind(Encoding.class).to(MyEncoding.class);

	}

}
