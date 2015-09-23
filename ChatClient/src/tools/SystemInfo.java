package tools;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SystemInfo {
	
	public static final DateFormat dateFormat = new SimpleDateFormat("MMMMMMMMM dd, HH:mm a");
	public static final Date date = new Date();
	public static final String a = "";
	
	public static String getDate() {
		return dateFormat.format(date);
	}
	
	public static String getTime() {
		return new SimpleDateFormat("HH:mm a").format(new Date());
	}
	
}