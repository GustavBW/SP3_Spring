package gbw.sp3.OpcClient.controllers;

import gbw.sp3.OpcClient.client.MachineStatus;
import gbw.sp3.OpcClient.client.OpcClient;
import gbw.sp3.OpcClient.util.IntUtil;
import gbw.sp3.OpcClient.util.JSONWrapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
public class ClientController {
    public static final String pathRoot = "/client";

    @GetMapping(path=pathRoot+"/sanity",produces = "application/json",consumes="application/json")
    public @ResponseBody ResponseEntity<String> sanityCheck(@RequestBody(required = false) String body, @RequestHeader(required = false) String headers)
    {
        System.out.println("Sanity Request Recieved: ");
        System.out.println("Headers (if any): ");
        System.out.println(headers);
        System.out.println("Body (if any): ");
        System.out.println(body);
        return new ResponseEntity<>("sane", HttpStatus.OK);
    }

    @GetMapping(path=pathRoot, produces = "application/json", consumes="application/json")
    public @ResponseBody ResponseEntity<MachineStatus> status()
    {
        return new ResponseEntity<>(OpcClient.status(),HttpStatus.OK);
    }

    @PostMapping(path=pathRoot+"/initialize", produces = "application/json", consumes="application/json")
    public @ResponseBody ResponseEntity<OpcClient.InitializationError> initialize(@RequestBody() String body)
    {
        JSONWrapper wrapped = new JSONWrapper(body);
        OpcClient.InitializationError error = OpcClient.initialize(
                wrapped.getOr("protocol","opc.tcp"),
                wrapped.getOr("ip","999.999.999"),
                IntUtil.parseOr(wrapped.getOr("port","6969"),-1)
        );
        return new ResponseEntity<>(error, HttpStatusCode.valueOf(error.status()));
    }

}
