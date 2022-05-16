package modbusparser.comm.commandData;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.EnumSet;

import modbusparser.util.HexString;

public abstract class CommandData {
	
	public byte dataId;
	public byte[] payload = new byte[2];
	
	public CommandData(byte dataId)
	{
		this.dataId = dataId;
	}
	
	public CommandData(byte dataId, byte[] payload) {
		super();
		this.dataId = dataId;
		this.payload = payload;
	}

	@Override
	public String toString()
	{
		String repr = " ID: " + HexString.convertToHexadecimal(dataId) + " | Val " + HexString.convertToHexadecimal(payload) + " || ";
		return repr;
	}
	public static CommandData getResponseData(byte dataId, byte[] payload)
	{
	
		for(CommandDataTypeCollection dt : EnumSet.allOf(CommandDataTypeCollection.class))
		{
			if(dt.getDataId()==dataId)
			{
				try {
					Constructor<?> constructor = dt.getInstantiatingClass().getConstructor(byte[].class);
					Object instance = constructor.newInstance(payload);
					return (CommandData) instance;
				} catch (NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					e.printStackTrace();
				} 
			}
		}
		return new UnknownCommandData(dataId, payload);
	}

	public byte getDataId() {
		return dataId;
	}

	public byte[] getPayload() {
		return payload;
	}
	
	public abstract void generatePayload();
	
	
	public byte[] getEncoding()
	{
		generatePayload();
		byte[] enconding = new byte[]{ dataId, payload[1], payload[0]};
		return enconding;
	}
		

	

}
