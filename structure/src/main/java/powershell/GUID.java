package powershell;

import com.fasterxml.jackson.annotation.JsonCreator;

public class GUID{

	private final String guid;
	public static final String EMPTY_ID = "00000000-0000-0000-0000-000000000000"; //$NON-NLS-1$

	@JsonCreator
	public GUID(String guid) {
		this.guid = (guid.matches("\\{.+\\}") ? guid : ('{' + (guid.matches("/Guid\\(.+\\)/") ? guid.substring(6, 42) : guid) + '}')).toLowerCase();
	}
	
	public byte[] getBytes(){
		byte bytes[] = new byte[16];
		String str = getTrimGuid().replaceAll("-", "");
		int pos = 2;
		do{
			int in = Integer.decode("0x"+str.substring(pos-2, pos));
			bytes[(pos/2)-1] = (byte)in;
		}while((pos+=2)<=str.length());

		return bytes;
	}

	public String getGuid() {
		return guid;
	}

	@Override
	public String toString() {
		return guid;
	}

	public String getTrimGuid(){
		return guid.startsWith("{") && guid.endsWith("}")?guid.substring(1, guid.length()-1):guid;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj!=null && (obj instanceof GUID) && ((this == obj)||this.getGuid().equals(((GUID)obj).getGuid()))){
			return true;
		}
		
		return false;
	}
}