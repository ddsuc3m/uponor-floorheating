package modbusparser.comm.commandData;

public class UnknownCommandData extends CommandData {

	public UnknownCommandData(byte dataId, byte[] payload) {
		super(dataId, payload);
		this.payload = payload;
	}

	@Override
	public void generatePayload() {
			
	}

}
