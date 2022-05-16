package entities;

import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.annotation.Nullable;

import configuration.Configuration;
import modbusparser.exception.EntityAlreadyExists;
import modbusparser.frames.Frame;
import modbusparser.frames.FrameType;
import modbusparser.frames.ResponseFrame;

public class EntityCollection {

	private HashMap<Integer, Entity> entityCollection = new LinkedHashMap<>();
	private Configuration config;
	
	public EntityCollection(Configuration config)
	{
		this.config = config;
	}
	
	
	
	public HashMap<Integer, Entity> getEntityCollection() {
		return entityCollection;
	}



	public @Nullable Entity lookUp(int id)
	{
		return entityCollection.get(id);
		
	}
	public @Nullable Entity lookUp(byte addr1, byte addr2)
	{
		return lookUp(EntityID.getIDIntegerFromAddress(addr1, addr2));
		
	}
	public void addNewEntity(Entity ent) throws EntityAlreadyExists
	{
		if(lookUp(ent.getID().getIDInt())!=null) { throw new EntityAlreadyExists();}
		entityCollection.put(ent.getID().getIDInt(), ent);
	}
	
	
	
	
	public Entity frameBroker(Frame frame)
	{
		Entity ent = lookUp(frame.getAddress1(), frame.getAddress2());
		if(ent == null)
		{
			//entity does not exist, let's create it
			EntityID entID = new EntityID(frame.getAddress1(), frame.getAddress2(), config.getSystemConfig());
			ent = new Entity(entID, config);
			try {
				addNewEntity(ent);
			} catch (EntityAlreadyExists e) {
				e.printStackTrace();
				return null;
			}
		}
		if(frame.getFrameType() == FrameType.REQUEST) {
			ent.updateLastRequested();
		}
		if(frame.getFrameType() == FrameType.RESPONSE) {
			ent.updateValues((ResponseFrame) frame);	
		}
		return ent;
		
	}
	public String getStatusString() {
		String res = "";
		for(Entity e : entityCollection.values())
		{
			res += e.getStatusString();
			res += "\n";
			res += e.getJSON();
			res += "\n";
		}
		return res;
	}
}
