package gbw.sp3.OpcClient.controllers;

import gbw.sp3.OpcClient.client.KnownNodes;
import gbw.sp3.OpcClient.client.MachineStatus;
import gbw.sp3.OpcClient.client.OpcClient;
import gbw.sp3.OpcClient.services.ClientRequestValidationService;
import gbw.sp3.OpcClient.util.ArrayUtil;
import gbw.sp3.OpcClient.util.IntUtil;
import gbw.sp3.OpcClient.util.JSONWrapper;
import gbw.sp3.OpcClient.util.Touple;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


@RestController
public class ClientController {
    public static final String pathRoot = "/client";
    @Autowired
    private ClientRequestValidationService validationService;

    @GetMapping(path=pathRoot, produces = "application/json")
    public @ResponseBody ResponseEntity<MachineStatus> status()
    {
        return new ResponseEntity<>(OpcClient.status(),HttpStatus.OK);
    }


    @GetMapping(path=pathRoot + "/read", produces = "application/json")
    public @ResponseBody ResponseEntity<Touple<Map<KnownNodes, DataValue>,String>> readValues(@RequestBody(required = false) String body)
    {
        String errorMessage = "Failed to read nodes: ";
        boolean failedToReadANode = false;

        List<KnownNodes> nodesToRead;
        if(body == null){
            nodesToRead = Arrays.stream(KnownNodes.values()).toList();
        }else{
            JSONWrapper wrapped = new JSONWrapper(body);
            ClientRequestValidationService.ClientValidationError requestError = validationService.validateReadRequest(wrapped);
            if(requestError != null){
                return new ResponseEntity<>(
                        new Touple<>(null, requestError.errorMessage() + " Valid node names are: " + ArrayUtil.arrayJoinWith(KnownNodes.getValidNames(),", ")),
                        HttpStatusCode.valueOf(requestError.httpStatus()
                        ));
            }
            nodesToRead = KnownNodes.parseList(wrapped.get("nodeNames").split("_"));
        }

        Map<KnownNodes, DataValue> response = OpcClient.read(nodesToRead);
        if(response == null){
            return new ResponseEntity<>(new Touple<>(null,"Unable to read any nodes. Is the client initialized?"),HttpStatus.INTERNAL_SERVER_ERROR);
        }
        for(KnownNodes node : KnownNodes.values()){
            if(response.get(node) == null){
                errorMessage += node.displayName + ",";
                failedToReadANode = true;
            }
        }

        return new ResponseEntity<>(
                new Touple<>(response,failedToReadANode ? errorMessage : "Successfully read all valid nodes."), HttpStatus.OK);
    }

    @PostMapping(path=pathRoot+"/write")
    public @ResponseBody ResponseEntity<OpcClient.InitializationError> writeValue(@RequestBody String body){
        if(body == null){
            return new ResponseEntity<>(
                    new OpcClient.InitializationError(400,"No body found on request"), HttpStatus.BAD_REQUEST);
        }
        
        JSONWrapper wrapped = new JSONWrapper(body);
        ClientRequestValidationService.ClientValidationError requestError = validationService.validateWriteRequest(wrapped);
        if(requestError != null){
            return new ResponseEntity<>(
                    new OpcClient.InitializationError(requestError.httpStatus(), requestError.errorMessage()), HttpStatus.BAD_REQUEST);
        }

        KnownNodes nodeToWriteTo = KnownNodes.parse(wrapped.get("nodeName"));
        if(nodeToWriteTo == null){
            return new ResponseEntity<>(
                    new OpcClient.InitializationError(400, "Invalid node name. Valid node names are: " + ArrayUtil.arrayJoinWith(KnownNodes.getValidNames(),", ")),
                    HttpStatus.BAD_REQUEST);
        }

        MachineStatus status = OpcClient.write(nodeToWriteTo, new Variant(wrapped.get("value")));
        return new ResponseEntity<>(
                new OpcClient.InitializationError(status.isFaulty() ? 400 : 200, status.getErrorMessage()),
                HttpStatus.valueOf(status.isFaulty() ? 400 : 200)
        );
    }

    @PostMapping(path=pathRoot+"/initialize", produces = "application/json")
    public @ResponseBody ResponseEntity<OpcClient.InitializationError> initialize(@RequestBody() String body)
    {
        JSONWrapper wrapped = new JSONWrapper(body);
        ClientRequestValidationService.ClientValidationError requestError = validationService.validateInitializeRequest
                (wrapped, new String[]{"protocol", "ip","port"}, new String[0]);
        if(requestError != null){
            return new ResponseEntity<>(
                    new OpcClient.InitializationError(requestError.httpStatus(), requestError.errorMessage()),
                    HttpStatusCode.valueOf(requestError.httpStatus())
            );
        }

        OpcClient.InitializationError error = OpcClient.initialize(
                wrapped.getOr("protocol","opc.tcp"),
                wrapped.getOr("ip","999.999.999"),
                IntUtil.parseOr(wrapped.getOr("port","6969"),-1)
        );
        return new ResponseEntity<>(error, HttpStatusCode.valueOf(error.status()));
    }

}
