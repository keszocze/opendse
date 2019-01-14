package net.sf.opendse.optimization.encoding;

import org.opt4j.core.start.Constant;

import net.sf.opendse.optimization.SpecificationWrapper;
import net.sf.opendse.optimization.io.IOModule;

/**
 * A simplistic scalable spec for experiments focused on the constraint solving
 * time.
 * 
 * @author Fedor Smirnov
 *
 */
public class ScalableSpecModule extends IOModule {

	@Constant(value = "message number", namespace = ScalableSpec.class)
	protected int messageNum;
	@Constant(value = "switch number", namespace = ScalableSpec.class)
	protected int switchNum;

	public int getMessageNum() {
		return messageNum;
	}

	public void setMessageNum(int messageNum) {
		this.messageNum = messageNum;
	}

	public int getSwitchNum() {
		return switchNum;
	}

	public void setSwitchNum(int switchNum) {
		this.switchNum = switchNum;
	}

	@Override
	protected void config() {
		bind(SpecificationWrapper.class).to(ScalableSpec.class).in(SINGLETON);
	}

}
