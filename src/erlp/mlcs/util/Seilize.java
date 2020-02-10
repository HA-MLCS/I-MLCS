package erlp.mlcs.util;

import java.io.Externalizable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/***
 * 
 * Reading and Writing of Data
 *
 */

public class Seilize {
	
	public static byte[] serializeObject(Externalizable object, int index)throws Exception {
		
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		byte[] res = null;
		try {
			fos = new FileOutputStream(new File(index + ".ser"));
			oos = new ObjectOutputStream(fos);
			object.writeExternal(oos);
			oos.flush();
			res = object.getClass().getName().getBytes();
		} catch (Exception ex) {
			throw ex;
		} finally {
			try {
				if (oos != null)
					oos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return res;
	}

	public static Externalizable deserializeObject(byte[] rowObject, int index)throws Exception {
		
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		String objectClassName = null;
		Externalizable res = null;
		try {
			fis = new FileInputStream(new File(index + ".ser"));
			objectClassName = new String(rowObject);
			ois = new ObjectInputStream(fis);
			@SuppressWarnings("rawtypes")
			Class objectClass = Class.forName(objectClassName);
			res = (Externalizable) objectClass.newInstance();
			res.readExternal(ois);
		} catch (Exception ex) {
			throw ex;
		} finally {
			try {
				if (ois != null)
					ois.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
				if(fis!=null) fis.close();
		}
		return res;
	}
	
}
