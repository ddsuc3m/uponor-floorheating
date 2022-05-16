package modbusparser.frames;

import java.util.ArrayList;
import java.util.List;

import configuration.Configuration;
import configuration.SystemConfig;
import modbusparser.comm.commandData.CommandData;

public class ResponseFrame extends Frame {
	
	SystemConfig systemConfig;
	
	public ResponseFrame(Configuration config) {
		super(FrameType.RESPONSE, config);
		this.systemConfig = config.getSystemConfig();
	}
	
	public List<CommandData> commandData;
	
	{
		commandData = new ArrayList<>();
	}

	@Override
	protected String payloadToString() {
		String repr = "";
		
		for(CommandData rD : commandData){
			repr +=  rD.toString();//" ID: " + HexString.convertToHexadecimal(rD.dataId) + " | Val " + HexString.convertToHexadecimal(rD.payload) + " || ";
		};
		return repr;
	}

}
