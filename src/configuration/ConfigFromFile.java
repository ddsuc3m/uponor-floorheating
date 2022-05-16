package configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

public class ConfigFromFile {
	
	Yaml yaml;
	Configuration configuration;
	
	public ConfigFromFile(String path) throws FileNotFoundException
	{
		FileInputStream io;
		io = new FileInputStream(new File(path));
		yaml = new Yaml(new Constructor(Configuration.class));
		configuration = yaml.load(io);
		
	}
	
	public Configuration getConfiguration()
	{
		return configuration;
	}


}
