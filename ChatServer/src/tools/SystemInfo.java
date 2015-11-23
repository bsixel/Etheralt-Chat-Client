package tools;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class SystemInfo {
	
	public static final DateFormat dateFormat = new SimpleDateFormat("MMMMMMMMM dd, hh:mm a");
	public static final DateFormat milDateFormat = new SimpleDateFormat("MMMMMMMMM dd, HH:mm");
	public static final DateFormat expMilDateFormat = new SimpleDateFormat("MMMMMMMMM dd, YYYY HH:mm");
	public static final Date date = new Date();
	public static final String a = "";
	
	public static String getDate() {
		return milDateFormat.format(new Date());
	}
	
	public static String getTime() {
		return new SimpleDateFormat("HH:mm a").format(new Date());
	}

	public static String getFullDate() {
		return expMilDateFormat.format(new Date());
	}
	
}