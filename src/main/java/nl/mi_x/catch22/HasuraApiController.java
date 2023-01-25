package nl.mi_x.catch22;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.json.JSONObject;
import org.json.JSONException;
import org.springframework.http.*;
import java.net.*;

import javax.crypto.SecretKey;

import java.io.*;

/**
*
*  This class contains methods to control the site's internally-used API for Hasura
*
*  @author MI-X
*
*  @author MI-X 2022 course 2.2 project group 6
*  @author Thomas Schuller
*  @author Melle Wels
*  @author Nienke Heemskerk
*  @author Rosan van der Linden
*
*/
@RestController
public class HasuraApiController extends nl.mi_x.catch22.ApiController {


  // Function below is a "stub"
  @GetMapping("/hasura")
  public ResponseEntity<String> hasura(
      @RequestParam(value = "query") String query) {

    try {
      URL url = new URL(System.getenv("hasuraUrl"));
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      con.setRequestMethod("POST");
      con.setRequestProperty("Content-Type", "application/json");
      con.setRequestProperty("x-hasura-admin-secret", System.getenv("hasuraApiKey"));
      con.setDoOutput(true);

      System.out.println("Sending this query:");
      System.out.println(query);

      JSONObject jsonObject = new JSONObject();
      jsonObject.put("query", query);
      String jsonInputString = jsonObject.toString();

      try (OutputStream os = con.getOutputStream()) {
        byte[] input = jsonInputString.getBytes("utf-8");
        os.write(input, 0, input.length);
        os.close();
      }

      return getApiCallResponse(con);
    } catch (Exception e) {
      e.printStackTrace();

      // Throw a server error
      return new ResponseEntity<String>("FAIL", HttpStatus.valueOf(500));
    }
  }





  
    @GetMapping("/encryptedHasura")
  public ResponseEntity<byte[]> encryptedHasura(@RequestParam(value = "query") byte[] encryptedQuery, @RequestParam(value = "session_ID") String session_ID){
    String keyQuery = "query MyQuery {  sessie_by_pk(sessie_ID: \"" + session_ID + "\") {    AES_key }}";
    ResponseEntity<String> keyResult = hasura(keyQuery);
    byte[] encryptedKey;
    try{
      encryptedKey = new JSONObject(keyResult.toString()).getJSONObject("data").getJSONObject("sessie_by_pk").getString("AES_key").getBytes();
    }catch (JSONException jse){
      System.out.println("er ging iets mis bij het zoeken van de sessie + key: " + jse);
      return new ResponseEntity<byte[]>(HttpStatus.valueOf(404));
    }
    try{
      String keyString = Crypto.decryptOwnKey(encryptedKey, true);
      SecretKey key = Crypto.getSecretKey(keyString);
      String query = Crypto.decrypt(encryptedQuery, key, "AES/CTR/NoPadding");
      ResponseEntity<String> hasuraResponse = hasura(query);
      return new ResponseEntity<byte[]>(Crypto.encrypt(hasuraResponse.toString(), key, "AES/CTR/NoPadding"), HttpStatus.valueOf(200));
    }catch(Exception e){
      System.err.println("Something went very wrong trying execute a query: " + e);
      return new ResponseEntity<byte[]>(HttpStatus.valueOf(500));
    }
  }










  
  @GetMapping("/login")
  public ResponseEntity<String> loginAttempt(@RequestParam(value = "username") byte[] encryptedUsername, @RequestParam(value = "password") byte[] encryptedPassword, @RequestParam(value = "key") byte[] encryptedKey){

    try{
    String keyString = Crypto.decryptOwnKey(encryptedKey, true);
    SecretKey key = Crypto.getSecretKey(keyString);
    String username = Crypto.decrypt(encryptedUsername, key, "AES/CTR/NoPadding");
    String password = Crypto.decrypt(encryptedPassword, key, "AES/CTR/NoPadding"); //password will still be MD5-hashed

    String inlogQuery = "query MyQuery {  gebruiker(where: {gebruikersnaam: {_eq: \"" 
            + username + "\"}, wachtwoord: {_eq: \"" + password + "\"}}) {    gebruikersnaam  }}";
     ResponseEntity<String> loginResult = hasura(inlogQuery);
String gebruikersnaam;
    try{
    gebruikersnaam = new JSONObject(loginResult.toString()).getJSONObject("data").getJSONObject("gebruiker").getString("gebruikersnaam");
    }catch(JSONException jse){
      System.out.println("waarschijnlijk een verkeerde gebruikersnaam/wachtwoord: " + jse);
      return new ResponseEntity<String>("failed login", HttpStatus.valueOf(401));
    }
      if(username.equals(gebruikersnaam)){

        String sessieMutation = "mutation MyMutation {  insert_sessie_one(object: {AES_key: \"" + keyString + "\", gebruikersnaam: \"" + gebruikersnaam + "\"}) {    sessie_ID  }}";

        ResponseEntity<String> sessionResult = hasura(sessieMutation);
        String sessieId = new JSONObject(sessionResult.toString()).getJSONObject("data").getJSONObject("sessie").getString("sessie_ID");
        return new ResponseEntity<String>(sessieId, HttpStatus.valueOf(200));
      }else{
        return new ResponseEntity<String>("failed login", HttpStatus.valueOf(401));
      }
      
    }catch(Exception e){
      System.err.println("Something went very wrong trying to log in: " + e);
      return new ResponseEntity<String>("FAIL", HttpStatus.valueOf(500));
    }
    
  }
}