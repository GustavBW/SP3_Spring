package gbw.sp3.OpcClient.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class SanityController {
    private static final String pathRoot = "sanity";

    @GetMapping(path=pathRoot,produces = "application/json")
    public @ResponseBody ResponseEntity<String> sanityCheck(@RequestBody(required = false) String body, @RequestHeader(required = false) String headers)
    {
        System.out.println("Sanity Request Recieved: ");
        System.out.println("Headers (if any): ");
        System.out.println(headers);
        System.out.println("Body (if any): ");
        System.out.println(body);
        String toReturn = "<h1>You're sane</h1>. \nYour request consisted of headers: \n" + headers + "\nAnd body: \n" + body;
        return new ResponseEntity<>(toReturn, HttpStatus.OK);
    }

}
