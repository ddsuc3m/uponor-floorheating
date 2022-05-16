package modbusparser.frames;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import configuration.Configuration;
import modbusparser.comm.commandData.CommandData;

public class CommanDataFrame extends Frame {

	public CommanDataFrame(Configuration config) {
		super(FrameType.COMMANDDATA, config);
	}

	public List<CommandData> commandData;

	{
		commandData = new ArrayList<>();
		this.crc_calc = null;
		this.crc = null;
		this.frame_without_crc = null;
	}

	public List<CommandData> getCommandData() {
		return commandData;
	}

	public void setCommandData(List<CommandData> commandData) {
		this.commandData = commandData;
	}

	public void generateRawFrame() {
		// assumes rawframe possition is 0
		int len_without_crc = config.getSystemConfig().getSYSTEM_HEADER_LEN()
				+ config.getSystemConfig().getADDRESS_LEN()
				+ (config.getSystemConfig().getCOMMANDDATA_LEN() * commandData.size());
		int len_with_crc = len_without_crc + config.getSystemConfig().getCRC_LEN();
		this.frame_without_crc = new byte[len_without_crc];
		this.rawFrame = ByteBuffer.allocate(len_with_crc);
		addSystemHeader();
		addAddressHeader();
		addCommandDataPayload();
		addCRC();
	}

	private void addSystemHeader() {
		this.frame_without_crc[0] = config.getSystemConfig().getSYSTEM_FIRST_BYTE();
		this.rawFrame.put(config.getSystemConfig().getSYSTEM_FIRST_BYTE());
		this.frame_without_crc[1] = config.getSystemConfig().getSYSTEM_SECOND_BYTE();
		this.rawFrame.put(config.getSystemConfig().getSYSTEM_SECOND_BYTE());
	}

	private void addAddressHeader() {
		this.frame_without_crc[2] = address1;
		this.rawFrame.put(address1);
		this.frame_without_crc[3] = address2;
		this.rawFrame.put(address2);
	}

	private void addCommandDataPayload() {
		int pos = this.rawFrame.position();
		for (CommandData cd : commandData) {
			pos = this.rawFrame.position();
			byte[] payload = cd.getEncoding();
			for (int i = 0; i < config.getSystemConfig().getCOMMANDDATA_LEN(); i++) {
				this.frame_without_crc[pos + i] = payload[i];
			}
			this.rawFrame.put(payload);
		}
	}

	private void addCRC() {
		crc_calc = CRC.getCRC(frame_without_crc);
		crc_calculated = true;
		crc = new byte[2];
		crc[0] = crc_calc[0];
		crc[1] = crc_calc[1];
		crc_ok = true;
		this.rawFrame.put(crc_calc);
	}

	@Override
	protected String payloadToString() {
		String repr = "";

		for (CommandData rD : commandData) {
			repr += rD.toString();
		}
		;
		return repr;
	}

	public void addCommandData(CommandData cd) {
		this.commandData.add(cd);
	}

/*	public static void main(String[] args) {
		UnknownCommandData ucd = null;
		CommanDataFrame cdf = null;
		List<CommandData> commandData = null;
		cdf = new CommanDataFrame();
		cdf.setAddress((byte) 0x9E, (byte) 0x8B);
		commandData = new ArrayList<>();
		// Carefull with unknown Command Data since payload is not processed. Should be
		// arranged in reverse order
		ucd = new UnknownCommandData((byte) 0x40, new byte[] { 0x29, 0x03 });
		cdf.addCommandData(ucd);
		ucd = new UnknownCommandData((byte) 0x3E, new byte[] { 0x00, 0x00 });
		cdf.addCommandData(ucd);
		ucd = new UnknownCommandData((byte) 0x3F, new byte[] { 0x02, 0x0C });
		cdf.addCommandData(ucd);
		cdf.generateRawFrame();
		// Should print:
		// -> 11049E8B4003293E00003F0C0208D5
		// 11 04 | 9E 8B | ID: 40 | Val 2903 || ID: 3E | Val 0000 || ID: 3F | Val 020C
		// || CRC 08D5 [ OK ]
		System.out.println(cdf.toString());

		// Other test:
		cdf = new CommanDataFrame();
		cdf.setAddress((byte) 0x9E, (byte) 0x8B);
		commandData = new ArrayList<>();

		// It doues not make sense to send room temperature, but just to test!
		// 0329 -> 809 -> 80,9 F -> 27,1 C
		// Carefull with unknown Command Data since payload is not processed. Should be
		// arranged in reverse order
		RoomTemperature rt = new RoomTemperature();
		rt.setCelsius((float) 27.2);
		cdf.addCommandData(rt);
		ucd = new UnknownCommandData((byte) 0x3D, new byte[] { 0x00, 0x00 });
		cdf.addCommandData(ucd);
		ucd = new UnknownCommandData((byte) 0x0C, new byte[] { 0x24, 0x00 });
		cdf.addCommandData(ucd);
		cdf.generateRawFrame();
		System.out.println(cdf.toString());

		System.out.println("---- Change Setpoint Example frame --- ");
		SetPointTemperature sp0 = new SetPointTemperature();
		cdf = new CommanDataFrame();
		sp0.setPayloadZero();
		cdf.addCommandData(sp0);
		SetPointTemperature spT = new SetPointTemperature();
		spT.setCelsius(20.00f);
		cdf = new CommanDataFrame();
		cdf.setAddress((byte) 0x9E, (byte) 0x8B);
		cdf.addCommandData(sp0);
		cdf.addCommandData(spT);
		cdf.generateRawFrame();
		System.out.println(cdf.toString());

	}*/

}
