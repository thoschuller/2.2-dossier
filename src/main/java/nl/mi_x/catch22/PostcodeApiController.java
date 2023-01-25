package nl.mi_x.catch22;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.*;
import java.net.*;
import java.io.*;

@RestController
public class PostcodeApiController extends ApiController {
  @GetMapping("/postcode")
  public ResponseEntity<String> postcode(@RequestParam(value = "postcode", defaultValue = "110AZ") String postcode,
      @RequestParam(name = "huisnummer", defaultValue = "9") String huisnummer) {

    try {
      String requestString = "https://api.overheid.io/bag?filters[postcode]=";
      requestString = requestString.concat(postcode).concat("&filters[huisnummer]=").concat(huisnummer)
          .concat("&ovio-api-key=".concat(System.getenv("postcodeApiKey")));

      URL url = new URL(requestString);
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      con.setRequestMethod("GET");

      return getApiCallResponse(con);
    } catch (IOException e) {
      e.printStackTrace();

      // Throw a server error
      return new ResponseEntity<String>("FAIL", HttpStatus.valueOf(500));
    }
  }
}