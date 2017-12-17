package objects;

import configuration.Configuration;
import structure.Statistics;

public class ExchangeObject extends GenericObject {
	private static final long serialVersionUID = 1433225130072719515L;

	public ExchangeObject(String pathname, String target, int size, Statistics statistics, Configuration configuration) {
		super(pathname, target, size, statistics, configuration);
	}

	@Override
	public boolean createContainer() throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean createContent() throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean createTarget() throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean checkTarget() throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeTarget() throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean retrieveChildren() throws Exception {
		// TODO Auto-generated method stub
		return false;
	}
}