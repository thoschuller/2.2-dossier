package nl.mi_x.catch22;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.*;
import java.net.*;
import java.io.*;

@RestController
public class SnomedApiController extends ApiController {
  @GetMapping("/snomed-ct")
  public ResponseEntity<String> snomedId(@RequestParam(value = "term", defaultValue = "") String term) {

    try {
      String requestString = "https://browser.ihtsdotools.org/snowstorm/snomed-ct/MAIN%2FSNOMEDCT-NL/concepts?offset=0&language=nl&term=";
      requestString = requestString.concat(term);

      URL url = new URL(requestString);
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      con.setRequestMethod("GET");
      con.setRequestProperty("Accept-Language", "nl");

      return getApiCallResponse(con);
    } catch (IOException e) {
      e.printStackTrace();

      return new ResponseEntity<String>("FAIL", HttpStatus.valueOf(500));
    }
  }
}