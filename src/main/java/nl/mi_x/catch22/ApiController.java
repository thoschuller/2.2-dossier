package nl.mi_x.catch22;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.*;
import java.net.*;
import java.io.*;

@RestController
abstract class ApiController {
  protected ResponseEntity<String> getApiCallResponse(HttpURLConnection con) {
    try {
      con.connect();

      try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
        StringBuilder response = new StringBuilder();
        String responseLine = null;
        while ((responseLine = br.readLine()) != null) {
          response.append(responseLine.trim());
        }
        System.out.println(response.toString());
        return new ResponseEntity<String>(response.toString(), HttpStatus.valueOf(200));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    // Throw a server error
    return new ResponseEntity<String>("FAIL", HttpStatus.valueOf(501));
  }
}