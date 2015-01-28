package co.rumpy.database.test;

import java.util.ArrayList;

import co.rumpy.database.ConnectionManager;
import co.rumpy.database.ResourceDatabase;
import co.rumpy.database.UserDatabase;
import co.rumpy.stanza.iq.Roster;

public class DatabaseTest {

	
	public static void main(String[] args) {
		
		new ConnectionManager();
		
		String signum = "bagas@rumpy.co/android";
		String password = "asdf";
		String fullname = "Bagas Abisena";
		String resource = "android";
		
		UserDatabase userDB = new UserDatabase();
		ResourceDatabase resDB = new ResourceDatabase();
		
		//userDB.register(signum, password, fullname);
		//resDB.registerResource(signum);
		
		//boolean isSuccess = resDB.registerChannel(signum, 12345678);
		//resDB.registerChannel("bagas@rumpy.co/browser", 12121212);
		//boolean isSuccess = resDB.unregisterChannel(signum);
		//boolean isSuccess = resDB.unregisterChannel(12121212);
		//System.out.println(isSuccess);
		
		ArrayList<Integer> channels = resDB.getActiveChannels("bagas@rumpy.co");
		
		if (channels != null) {
			for (Integer i : channels) {
				System.out.println(i);
			}
		} else {
			System.out.println("BOO");
		}
		
		/*boolean isSuccess = userDB.updateRelation("bagas@rumpy.co", "adiskaf@rumpy.co", UserDatabase.RELATION_FRIEND);
		System.out.println(isSuccess);*/
		
		//---------------------------------
		
		/*Roster r = userDB.getUser("bagas@rumpy.co");
		if (r == null) {
			System.out.println("NULL");
		} else {
			r.print();
		}*/
		
		// -------------------------------------------
		
		
		
		
		
		
	}

}
