package com.mucot.timetracer;
import java.text.*;

public class Define
{
	final public static String FileName = "TimeTraceResult.txt";
	final public static String captionFile = "Caption.txt";
	final public static String BR = System.getProperty("line.separator");
	final public static DateFormat df = new SimpleDateFormat("HH:mm:ss.SSS");
	
	final public static int SERVICE_STOP = 1;
}