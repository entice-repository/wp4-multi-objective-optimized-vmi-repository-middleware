
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.io.*;



public class PasswordStorage {
	
	public static Properties properties = new Properties();
	public static Map<String, String> DB = new HashMap<String, String>(); 
	public static final String SALT = "se4>48'V.Pz`QH+&";
	
	
	public static void main(String[] args) throws FileNotFoundException, IOException{
				
		PasswordStorage pass = new PasswordStorage();
		
		String basePath = new File("").getAbsolutePath();
		System.out.println(basePath);
					
		pass.signup("User1", "cVgZzW9Y&*xk%U=9");
		pass.signup("User2", "c3C9^AkGXLKj!VQ9");
		pass.signup("User3", "t-r$$B=v$3?VAFgc");
		pass.signup("User4", "ZMvC^*-8&zy+n9=e");
		pass.signup("User5", "A6?8ND6zG^$myPZ-");
		pass.signup("User6", "hyp!w7cUNkd$rd_V");
		Properties properties = new Properties();
		properties.putAll(DB);	
		properties.store(new FileOutputStream("./Resources/Users/Repo-Guard-Users.properties"), null);	
		
		
	}

	public void signup(String username, String password) {
		String saltedPassword = SALT + password;
		String hashedPassword = generateHash(saltedPassword);
		DB.put(username, hashedPassword);
	}
	
	
	public static String generateHash(String input) {
		StringBuilder hash = new StringBuilder();

		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			byte[] hashedBytes = md5.digest(input.getBytes());
			char[] digits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
					'a', 'b', 'c', 'd', 'e', 'f' };
			for (int idx = 0; idx < hashedBytes.length; ++idx) {
				byte b = hashedBytes[idx];
				hash.append(digits[(b & 0xf0) >> 4]);
				hash.append(digits[b & 0x0f]);
			}
		} catch (NoSuchAlgorithmException e) {
		
		}
	
		return hash.toString();
	}

}