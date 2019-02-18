package sg.edu.nus.logbase.crindex;

import java.util.HashMap;

public class LogIndexConfigurationSet {
	private HashMap<String,String> m_configSet = new HashMap<String,String>();
	
	public String getString(String config, String defaultValue)
	{
		String var = m_configSet.get(config);
		String ret;
		if (var == null) ret = defaultValue;
		else ret = var;
		LogIndexDebug.printInfo("configuration fetched: " + config+" = "+ret);
		return ret;
	}
	
	public int getInt(String config, int defaultValue)
	{
		String var = m_configSet.get(config);
		int ret;
		if (var == null) ret = defaultValue;
		else ret = Integer.valueOf(var);
		LogIndexDebug.printInfo("configuration fetched: "+config+" = "+ret);
		return ret;
	}
	
	public long getLong(String config, long defaultValue)
	{
		String var = m_configSet.get(config);
		long ret;
		if (var == null) ret = defaultValue;
		else ret = Long.valueOf(var);
		LogIndexDebug.printInfo("configuration fetched: "+config+" = "+ret);
		return ret;
	}
	
	public double getDouble(String config, double defaultValue)
	{
		String var = m_configSet.get(config);
		double ret;
		if (var == null) ret = defaultValue;
		else ret = Double.valueOf(var);
		LogIndexDebug.printInfo("configuration fetched: "+config+" = "+ret);
		return ret;
	}
	
	public boolean getBoolean(String config, boolean defaultValue)
	{
		String var = m_configSet.get(config);
		boolean ret;
		if (var == null) ret = defaultValue;
		else ret = Boolean.valueOf(var);
		LogIndexDebug.printInfo("configuration fetched: "+config+" = "+ret);
		return ret;
	}
	
	public void setConfiguration(String config, String o)
	{
		m_configSet.put(config, o);
		LogIndexDebug.printInfo("new configuration added: "+config+" = "+o);
	}
}
