package net.tangentmc.nmsUtils.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.bukkit.Bukkit;

import net.tangentmc.nmsUtils.NMSUtil;
import net.tangentmc.nmsUtils.NMSUtils;

public class ReflectionManager {
	
	public static boolean load() {
		String nmsVersion = Bukkit.getServer().getClass().getPackage().getName()
				.split("\\.")[3];
		try {
			NMSUtil nmsUtil = (NMSUtil) Class.forName(NMSUtils.class.getPackage().getName()+"."+nmsVersion+".NMSUtilImpl").newInstance();
			NMSUtils.getInstance().setUtil(nmsUtil);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			System.out.print("NMSUtil does not appear to be complied for this version of bukkit. Disabling NMS functions.");
			return false;
		}
		return true;
	}
	public static void setFinalStatic(Field field, Object obj, Object newValue) throws Exception {
	      field.setAccessible(true);

	      Field modifiersField = Field.class.getDeclaredField("modifiers");
	      modifiersField.setAccessible(true);
	      modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

	      field.set(obj, newValue);
	   }


	/**
	 * A convenience method.
	 * @param clazz The class.
	 * @param f The string representation of the private static field.
	 * @return The object found
	 * @throws Exception if unable to get the object.
	 */
	public static Object getPrivateStatic(Class<?> clazz, String f) throws Exception {
		Field field = clazz.getDeclaredField(f);
		field.setAccessible(true);
		return field.get(null);
	}
}
