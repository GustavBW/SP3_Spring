package gbw.sp3.OpcClient.controllers;

import gbw.sp3.OpcClient.client.*;
import gbw.sp3.OpcClient.services.ClientRequestValidationService;
import gbw.sp3.OpcClient.util.*;
import org.eclipse.milo.opcua.sdk.client.nodes.UaDataTypeNode;
import org.eclipse.milo.opcua.sdk.client.nodes.UaVariableNode;
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

    /**
     * Checks any connected OPC UA Server for the following values and returns these:
     * <p> machineStatus: int, </p>
     * <p> translation: String (what this status means according to known statuses),</p>
     * <p> errorMessage: String (any notable error encountered),</p>
     * <p> faulty: Boolean (true if there's a fundamental issue with this status. Usually a connection issue.)</p>
     * @return json response containing aforementioned fields.
     */
    @GetMapping(path=pathRoot, produces = "application/json")
    public @ResponseBody ResponseEntity<MachineStatus> status()
    {
        return new ResponseEntity<>(OpcClient.status(),HttpStatus.OK);
    }

    @PostMapping(path=pathRoot+"/{command}", produces = "application/json")
    public @ResponseBody ResponseEntity<MachineStatus> setCommand(@PathVariable String command, @RequestBody(required = false) String body)
    {
        MachineStatus status = OpcClient.status();
        ControlCommandTypes asCmdType = ControlCommandTypes.parse(command);
        JSONWrapper wrapped = new JSONWrapper(body);

        if(body != null){
            ClientRequestValidationService.ClientValidationError requestError = validationService.validateRequestBody(wrapped, new String[]{"autoExecute"});
            if(requestError != null){
                return new ResponseEntity<>(status.setErrorMessage(requestError.errorMessage()),HttpStatus.BAD_REQUEST);
            }
        }

        if(asCmdType == null){
            return new ResponseEntity<>(
                    new MachineStatus(status.getMachineStatus(), "", "unknown command type.", status.isFaulty(), status.getVibrations()),
                    HttpStatus.BAD_REQUEST
            );
        }

        if(status.isFaulty()){
            return new ResponseEntity<>(status, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        MachineStatus result = OpcClient.setCommand(
                asCmdType,
                Boolean.parseBoolean(wrapped.getOr("autoExecute", "false"))
        );
        return new ResponseEntity<>(result, result.isFaulty() ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.OK);
    }

    /**
     * Will read a list of specified nodes from the OPC UA Server stored in a Map along with a status message, if any.
     * This status message will contain what nodes couldn't be read or if there was a general problem reading ANY of the nodes.
     * @param nodeNames containing an UNDERSCORE separated list as a single string in a field called nodeNames.
     * @return A map of the value of the node, or null, as the value and the node name as the key.
     */
    @GetMapping(path=pathRoot + "/read", produces = "application/json")
    public @ResponseBody ResponseEntity<Touple<Map<KnownNodes, DataValue>,String>> readValues(
            @RequestParam(required = false) String[] nodeNames)
    {
        String errorMessage = "Failed to read nodes: ";
        boolean failedToReadANode = false;

        List<KnownNodes> nodesToRead;
        if (nodeNames == null || nodeNames.length == 0) {
            nodesToRead = Arrays.stream(KnownNodes.values()).toList();
        } else {
            nodesToRead = KnownNodes.parseList(nodeNames);
        }

        Map<KnownNodes, DataValue> response = OpcClient.read(nodesToRead);
        if(response == null){
            return new ResponseEntity<>(new Touple<>(null,"Unable to read any nodes. Is the client initialized?"),HttpStatus.INTERNAL_SERVER_ERROR);
        }
        for(KnownNodes node : nodesToRead){
            if(response.get(node) == null){
                errorMessage += node.displayName + ", ";
                failedToReadANode = true;
            }
        }

        return new ResponseEntity<>(
                new Touple<>(response,failedToReadANode ? errorMessage : "Successfully read all valid nodes."), HttpStatus.OK);
    }

     /**
     * Writes a value to a single node in the OPC UA server.
     * @param body json request body expecting fields: "nodeName" and "value".
     * @return the status of the machine and any error encountered
     */
    @PostMapping(path=pathRoot+"/write")
    public @ResponseBody ResponseEntity<OpcClient.InitializationError> writeValue(@RequestBody String body){
        if(body == null){
            return new ResponseEntity<>(
                    new OpcClient.InitializationError(400,"No body found on request"), HttpStatus.BAD_REQUEST);
        }
        
        JSONWrapper wrapped = new JSONWrapper(body);
        ClientRequestValidationService.ClientValidationError requestError = validationService.validateRequestBody(wrapped, new String[]{"nodeName","value","dataType"});
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

        String dataType = wrapped.getOr("dataType", "float");

        MachineStatus status = OpcClient.write(nodeToWriteTo, wrapped.get("value"), dataType);
        return new ResponseEntity<>(
                new OpcClient.InitializationError(status.isFaulty() ? 400 : 200, status.getErrorMessage()),
                HttpStatus.valueOf(status.isFaulty() ? 400 : 200)
        );
    }

    /**
     * Expects a json body containing fields "protocol", "ip" and "port" (optional).
     * Will attempt to initialize a connection opening up for the other client related api calls.
     * @param body json request body
     * @return Any error encountered when initializing
     */
    @PostMapping(path=pathRoot+"/initialize", produces = "application/json")
    public @ResponseBody ResponseEntity<OpcClient.InitializationError> initialize(@RequestBody() String body)
    {
        JSONWrapper wrapped = new JSONWrapper(body);
        ClientRequestValidationService.ClientValidationError requestError = validationService.validateRequestBody
                (wrapped, new String[]{"protocol", "ip"});
        if(requestError != null){
            return new ResponseEntity<>(
                    new OpcClient.InitializationError(requestError.httpStatus(), requestError.errorMessage()),
                    HttpStatusCode.valueOf(requestError.httpStatus())
            );
        }

        OpcClient.InitializationError error = OpcClient.initialize(
                wrapped.getOr("protocol","opc.tcp"),
                wrapped.getOr("ip","999.999.999"),
                IntUtil.parseOr(wrapped.get("port"),-1)
        );
        return new ResponseEntity<>(error, HttpStatusCode.valueOf(error.status()));
    }

    /**
     * Fetches the nodes "InventoryIsFilling", "Barley", "Hops", "Malt", "Wheat" and "Yeast" from the server.
     * @return a json string with a map of the node names and their corresponding values
     */
    @GetMapping(path=pathRoot+"/inventory", produces = "application/json")
    public @ResponseBody ResponseEntity<Touple<Map<KnownNodes, DataValue>,String>> getInventory()
    {
        return readValues(INVENTORY_NODES);
    }
    private static final String[] INVENTORY_NODES = new String[]{"InventoryIsFilling","Barley","Hops","Malt","Wheat","Yeast"};

    /**
     * Fetches the enum values that the Api is using to communicate with the opc ua server.
     * @param name of resource
     * @return A json string with fields "first" and "second", "first" containing the name of
     * the resource and "second" the values of said resource.
     */
    @GetMapping(path=pathRoot+"/resource/{name}", produces="application/json")
    public @ResponseBody ResponseEntity<Touple<String,Object[]>> getResource(@PathVariable String name)
    {
        String[] availableResources = new String[
                ]{"KnownNodes", "ProductionState", "BatchTypes"};
        switch (name){
            case "KnownNodes", "knownnodes","knownNodes" -> {
                return new ResponseEntity<>(
                        new Touple<String,Object[]> ("KnownNodes",KnownNodes.values()), HttpStatus.OK);
            }
            case "ProductionState", "productionstate", "productionState" -> {
                return new ResponseEntity<>(
                        new Touple<String,Object[]> ("ProductionState", ProductionState.values()), HttpStatus.OK);
            }
            case "BatchTypes", "batchtypes", "batchTypes" -> {
                return new ResponseEntity<>(
                        new Touple<String,Object[]> ("BatchTypes", BatchTypes.values()), HttpStatus.OK);
            }

        }
        return new ResponseEntity<>(
                new Touple<String,Object[]>("Error, no such resource.", new Object[]{"Choose from: " + ArrayUtil.arrayJoinWith(availableResources,",")}),
                HttpStatus.BAD_REQUEST
        );
    }


    /**
     * Sets the needed parameters on the server IF the server is in the "idle" state.
     * @param body a json string containing fields: "id", "beerType", "batchSize" and "speed"
     * @return a json string with a MachineStatus with any errors encountered.
     */
    @PostMapping(path=pathRoot+"/execute", produces = "application/json")
    public @ResponseBody ResponseEntity<MachineStatus> executeBatch(@RequestBody(required = false) String body)
    {
        MachineStatus status = OpcClient.status();
        if(status.isFaulty() || status.getMachineStatus() != ProductionState.IDLE.value){
            return new ResponseEntity<>(status, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        JSONWrapper wrapped = new JSONWrapper(body);
        ClientRequestValidationService.ClientValidationError requestError
                = validationService.validateRequestBody(wrapped, new String[]{"id","beerType","batchSize","speed"});

        if(requestError != null){
            return new ResponseEntity<>(
                    new MachineStatus(status.getMachineStatus(),
                            requestError.errorMessage()),HttpStatus.BAD_REQUEST);
        }

        BatchTypes beerType = BatchTypes.parse(wrapped.get("beerType"));
        if(beerType == null){
            return new ResponseEntity<>(
                    new MachineStatus(status.getMachineStatus(), "Invalid beer type"),
                    HttpStatus.BAD_REQUEST
            );
        }

        MachineStatus writeStatus = OpcClient.setBatchDetails(wrapped.get("id"), beerType, wrapped.get("batchSize"), wrapped.get("speed"));
        if(!writeStatus.isFaulty()){
            writeStatus = OpcClient.setCommand(ControlCommandTypes.START, true);
        }

        return new ResponseEntity<>(writeStatus, writeStatus.isFaulty() ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.OK);
    }

    

}
