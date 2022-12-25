package gbw.sp3.OpcClient.client;

import gbw.sp3.OpcClient.util.IntUtil;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfigBuilder;
import org.eclipse.milo.opcua.stack.client.DiscoveryClient;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.eclipse.milo.opcua.stack.core.util.EndpointUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class OpcClient {

    private static OpcUaClient client;
    public static String currentAddress = "";

    final int NAMESPACEINDEX = 6;
    private static final AtomicLong clientHandles = new AtomicLong(1L);

    public record InitializationError(int status, String error){}
    private static final InitializationError NO_ERROR = new InitializationError(200,"Client initialization success.");

    /**
     * Initializes client on new address. Returns a string ERROR if it fails.
     * @param protocol network protocol e.g. "opc.tcp"
     * @param ip ipv4 address of OPC UA server
     * @param port to address server through
     * @return String error (stack trace)
     */
    public static InitializationError initialize(String protocol, String ip, int port)
    {
        return initialize1(protocol, ip, port);
    }

    public static MachineStatus status()
    {
        if (client == null) {
            return new MachineStatus(20, "","Client is not initialized.",true,-1);
        }
        int statusInt = 20;
        Object vibrations = null;
        try {
            statusInt = IntUtil.parseOr(client.readValue(5000,TimestampsToReturn.Both,KnownNodes.CurrentState.nodeId).get().getValue(), 20);
            vibrations = client.readValue(5000,TimestampsToReturn.Both,KnownNodes.Vibrations.nodeId).get().getValue();
        }catch (Exception e){
            return new MachineStatus(20,"", "Client is unable to connect. Has it been initialized?",true,-1);
        }

        return new MachineStatus(statusInt,ProductionState.from(statusInt).name(),"none",false,vibrations);
    }

    public static MachineStatus write(KnownNodes node, String value, String dataType)
    {
       MachineStatus current = status();
       if(current.isFaulty()){
           return current;
       }
       StatusCode code = null;
       try{
           code = client.writeValue(node.nodeId, DataValue.valueOnly(
                   parseAndGetVariant(value, dataType)
           )).get(5000, TimeUnit.MILLISECONDS);
       }catch (Exception e){
           e.printStackTrace();
           return new MachineStatus(
                   current.getMachineStatus(), "",
                   "unable to write value: " + value + " to node: " + node.displayName,
                   true,current.getVibrations()
           );
       }
       System.out.println(code);
       return code != null && !code.isGood() ? codeAsMachineStatus(code,current) : current;
    }

    private static MachineStatus codeAsMachineStatus(StatusCode code, MachineStatus current)
    {
        return new MachineStatus(current.getMachineStatus(),current.getTranslation(),
                "Error: " +
                        " isBad: " + code.isBad() + "," +
                        " securityError: " + code.isSecurityError() + "," +
                        " isUncertain: " + code.isUncertain() + "," +
                        " actual value: " + code.getValue(),!code.isGood(),current.getVibrations());
    }
    private static Variant parseAndGetVariant(String value, String dataType) throws Exception
    {
        switch (dataType){
            case "double" -> {
                return new Variant(Double.parseDouble(value));
            }
            case "int" -> {
                return new Variant(Integer.parseInt(value));
            }
            case "float" -> {
                return new Variant(Float.parseFloat(value));
            }
            case "long" -> {
                return new Variant(Long.parseLong(value));
            }
            case "short" -> {
                return new Variant(Short.parseShort(value));
            }
            case "byte" -> {
                return new Variant(Byte.parseByte(value));
            }
            case "bool" -> {
                return new Variant(Boolean.parseBoolean(value));
            }
        }
        return new Variant(Float.parseFloat(value));
    }

    public static MachineStatus setCommand(ControlCommandTypes command, boolean autoExecute)
    {
        MachineStatus current = status();
        if(current.isFaulty()){
            return current;
        }
        StatusCode code = null;
        try{
            code = client.writeValue(KnownNodes.SetCommand.nodeId, DataValue.valueOnly(
                    new Variant(command.value)
            )).get(5000, TimeUnit.MILLISECONDS);
            if(autoExecute){
                return triggerCommands();
            }
        }catch (Exception e){
            e.printStackTrace();
            return new MachineStatus(
                    current.getMachineStatus(), "",
                    "unable set SetCommand to: " + command.name,
                    true,current.getVibrations()
            );
        }
        System.out.println(code);
        return code != null && !code.isGood() ? codeAsMachineStatus(code,current) : current;
    }
    public static MachineStatus triggerCommands()
    {
        MachineStatus current = status();
        if(current.isFaulty()){
            return current;
        }
        StatusCode code = null;
        try{
            code = client.writeValue(KnownNodes.ExecuteCommands.nodeId, DataValue.valueOnly(
                    new Variant(true)
            )).get(5000, TimeUnit.MILLISECONDS);
        }catch (Exception e){
            e.printStackTrace();
            return new MachineStatus(
                    current.getMachineStatus(), "",
                    "unable to execute commands",
                    true,current.getVibrations()
            );
        }
        System.out.println(code);
        return code != null && !code.isGood() ? codeAsMachineStatus(code,current) : current;
    }

    public static MachineStatus setBatchDetails(String id, BatchTypes beerType, String batchSize, String speed)
    {
        MachineStatus current = status();
        if(current.isFaulty()){
            return current;
        }
        StatusCode code = null;
        try{

            StatusCode idCode = client.writeValue(KnownNodes.SetBatchId.nodeId, DataValue.valueOnly(parseAndGetVariant(id,"float"))).get(1000, TimeUnit.MILLISECONDS);
            StatusCode recipeCode = client.writeValue(KnownNodes.SetRecipe.nodeId, DataValue.valueOnly(parseAndGetVariant(beerType.type + "","float"))).get(1000, TimeUnit.MILLISECONDS);
            StatusCode sizeCode = client.writeValue(KnownNodes.SetQuantity.nodeId, DataValue.valueOnly(parseAndGetVariant(batchSize,"float"))).get(1000, TimeUnit.MILLISECONDS);
            StatusCode speedCode = client.writeValue(KnownNodes.SetSpeed.nodeId, DataValue.valueOnly(parseAndGetVariant(speed,"float"))).get(1000, TimeUnit.MILLISECONDS);

            for(StatusCode c : new StatusCode[]{idCode, recipeCode, sizeCode, speedCode}){
                if(!c.isGood()){
                    code = c;
                }
            }

        }catch (Exception e){
            e.printStackTrace();
            return new MachineStatus(
                    current.getMachineStatus(), "",
                    "unable to set batch details",
                    true,current.getVibrations()
            );
        }
        System.out.println(code);
        return code != null && !code.isGood() ? codeAsMachineStatus(code,current) : current;
    }

    public static Map<KnownNodes, DataValue> read(List<KnownNodes> nodes)
    {
        if(client == null){
            return null;
        }
        try{
            client.connect().get();
        }catch (Exception e){
            return null;
        }

        Map<KnownNodes, CompletableFuture<DataValue>> loaderMap = new HashMap<>();
        for(KnownNodes node : nodes){ //Filling the loader map.
            try{
                loaderMap.put(node, client.readValue(1000,TimestampsToReturn.Both,node.nodeId));
            }catch (Exception e){
                loaderMap.put(node, null);
            }
        }
        Map<KnownNodes, DataValue> resolvedMap = new HashMap<>();
        for(KnownNodes node : nodes){
            if(loaderMap.get(node) != null){
                try{
                    resolvedMap.put(node, loaderMap.get(node).get());
                }catch (Exception e){
                    resolvedMap.put(node, null);
                }
            }
        }
        return resolvedMap;
    }

    private static InitializationError initialize1(String protocol, String ip, int port)
    {
        String newAddress = protocol + "://" + ip + ":" + port;
        if(port == -1){
            newAddress = protocol + "://" + ip;
        }

        try {
            List<EndpointDescription> endpoints = DiscoveryClient.getEndpoints(newAddress).get();
            EndpointDescription configPoint = EndpointUtil.updateUrl(endpoints.get(0), ip, port);

            OpcUaClientConfigBuilder cfg = new OpcUaClientConfigBuilder();
            cfg.setEndpoint(configPoint);

            client = OpcUaClient.create(cfg.build());
            client.connect().get(1000L, TimeUnit.MILLISECONDS);

        } catch (UaException | CancellationException | TimeoutException uaEx ) {
            return new InitializationError(400, uaEx.getMessage());

        } catch (InterruptedException | ExecutionException intEx){
            return new InitializationError(500, intEx.getMessage());
        }

        return NO_ERROR;
    }


}
