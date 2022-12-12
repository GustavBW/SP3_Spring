package gbw.sp3.OpcClient.controllers;

import gbw.sp3.OpcClient.client.*;
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

    /**
     * Will read a list of specified nodes from the OPC UA Server stored in a Map along with a status message, if any.
     * This status message will contain what nodes couldn't be read or if there was a general problem reading ANY of the nodes.
     * @param body containing an UNDERSCORE separated list as a single string in a field called nodeNames.
     * @return A map of the value of the node, or null, as the value and the node name as the key.
     */
    @GetMapping(path=pathRoot + "/read", produces = "application/json")
    public @ResponseBody ResponseEntity<Touple<Map<KnownNodes, DataValue>,String>> readValues(@RequestBody(required = false) String body)
    {
        String errorMessage = "Failed to read nodes: ";
        boolean failedToReadANode = false;

        List<KnownNodes> nodesToRead;
        if (body != null) {
            JSONWrapper wrapped = new JSONWrapper(body);
            ClientRequestValidationService.ClientValidationError requestError = validationService.validateReadRequest(wrapped);
            if(requestError != null){
                return new ResponseEntity<>(
                        new Touple<>(null, requestError.errorMessage() + " Valid node names are: " + ArrayUtil.arrayJoinWith(KnownNodes.getValidNames(),", ")),
                        HttpStatusCode.valueOf(requestError.httpStatus()
                        ));
            }
            nodesToRead = KnownNodes.parseList(wrapped.get("nodeNames").split("_"));
        } else {
            nodesToRead = Arrays.stream(KnownNodes.values()).toList();
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

    @GetMapping(path=pathRoot+"/inventory", produces = "application/json")
    public @ResponseBody ResponseEntity<Touple<Map<KnownNodes, DataValue>,String>> getInventory()
    {
        return readValues("{\"nodeNames\":\"InventoryIsFilling_Barley_Hops_Malt_Wheat_Yeast\"}");
    }

    @GetMapping(path=pathRoot+"/ressource/{name}", produces="application/json")
    public @ResponseBody ResponseEntity<Object[]> getRessource(@PathVariable String name)
    {
        switch (name){
            case "KnownNodes", "knownnodes","knownNodes" -> {
                return new ResponseEntity<>(KnownNodes.values(), HttpStatus.OK);
            }
            case "ProductionState", "productionstate", "productionState" -> {
                return new ResponseEntity<>(ProductionState.values(), HttpStatus.OK);
            }
            case "BatchTypes", "batchtypes", "batchTypes" -> {
                return new ResponseEntity<>(BatchTypes.values(), HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }

    @PostMapping(path=pathRoot+"/execute", produces = "application/json")
    public @ResponseBody ResponseEntity<MachineStatus> executeBatch(@RequestBody(required = false) String body)
    {
        MachineStatus status = OpcClient.status();
        JSONWrapper wrapped = new JSONWrapper(body);
        ClientRequestValidationService.ClientValidationError requestError
                = validationService.validateRequestBody(wrapped, new String[]{"id","beerType","batchSize","speed"});
        if(requestError != null){
            return new ResponseEntity<>(
                    new MachineStatus(status.getMachineStatus(),
                            requestError.errorMessage()),HttpStatus.BAD_REQUEST);
        }

        if(status.isFaulty() || status.getMachineStatus() != ProductionState.IDLE.value){
            return new ResponseEntity<>(status, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        MachineStatus setIdStatus = OpcClient.write(KnownNodes.SetBatchId,new Variant(wrapped.get("id"))),
            setBeerTypeStatus = OpcClient.write(KnownNodes.SetRecipe, new Variant(wrapped.get("beerType"))),
            setBatchSizeStatus = OpcClient.write(KnownNodes.SetQuantity, new Variant(wrapped.get("batchSize"))),
            setSpeedStatus = OpcClient.write(KnownNodes.SetSpeed, new Variant(wrapped.get("speed"))),
            setCMD = OpcClient.write(KnownNodes.SetCommand, new Variant(ControlCommandTypes.START.value)),
            setExecute = OpcClient.write(KnownNodes.ExecuteCommands, new Variant(true));

        Map<String, MachineStatus> writeResults = Map.of(
                "id", setIdStatus,
                "beerType", setBeerTypeStatus,
                "batchSize", setBatchSizeStatus,
                "speed", setSpeedStatus,
                "cmd", setCMD,
                "execute", setExecute
        );

        boolean writeErrorOccoured = false;
        String nodesThatFailed = "";

        for(String s : writeResults.keySet()){
            if(writeResults.get(s).isFaulty()){
                writeErrorOccoured = true;
                nodesThatFailed += s + ",";
            }
        }

        if(writeErrorOccoured){
            return new ResponseEntity<>(
                    new MachineStatus(status.getMachineStatus(), ProductionState.from(status.getMachineStatus()).name(),
                    "Failed to write to nodes: " + nodesThatFailed, true, status.getVibrations()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(status, HttpStatus.OK);
    }

}
