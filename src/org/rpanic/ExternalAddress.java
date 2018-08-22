package org.rpanic;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;

public class ExternalAddress {
	
	private static InetAddress address = null; 
	
	public static InetAddress getExternalAddress(){
		if(address == null){
			address = getExternalAddressForceUpdate();
			return address;
		}else{
			return address;
		}
	}
	
	public static InetAddress getExternalAddressForceUpdate(){
		
		String[] arr = new String[]{"http://91.198.22.70", "http://checkip.dyndns.org/", "http://api.ipify.org/", "http://66.171.248.178/", "http://bot.whatismyipaddress.com/", "http://ident.me/"};
		
		for(String add : arr){
			String res = getContents(add);
			if(res != "error"){
				try {
					InetAddress ret = InetAddress.getByName(res);
					return ret;
				} catch (UnknownHostException e) {
					continue;
				}
			}
		}
		return null;
		
	}
	
	public static String getContents(String add){
		
		try{
			HttpURLConnection con = (HttpURLConnection) new URL(add).openConnection();
			con.setConnectTimeout(2000);
			//Socket s = new Socket(add, port);
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
			
			String res = "";
			while(reader.ready()){
				res += reader.readLine();
			}
			
			return res;
			
		}catch(Exception e){
			//e.printStackTrace();
			return "error";
		}
		
	}
	
}
