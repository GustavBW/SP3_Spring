package gbw.sp3.OpcClient.client;

import gbw.sp3.OpcClient.util.IntUtil;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfigBuilder;
import org.eclipse.milo.opcua.stack.client.DiscoveryClient;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.ReadResponse;
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
    private static InitializationError NO_ERROR = new InitializationError(200,"Client initialization success.");

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
            client.connect().get();
            statusInt = IntUtil.parseOr(client.readValue(1000,TimestampsToReturn.Both,KnownNodes.CurrentState.nodeId).get(), 20);
            vibrations = client.readValue(1000,TimestampsToReturn.Both,KnownNodes.Vibrations.nodeId).get();
        }catch (Exception e){
            return new MachineStatus(20,"", "Client is unable to connect. Has it been initialized?",true,-1);
        }

        return new MachineStatus(statusInt,ProductionState.from(statusInt).name(),"none",false,vibrations);
    }

    public static MachineStatus write(KnownNodes node, Variant variant)
    {
       MachineStatus current = status();
       if(current.isFaulty()){
           return current;
       }
       StatusCode code = null;
       try{
           client.connect().get();
           code = client.writeValue(node.nodeId, new DataValue(variant)).get(1000, TimeUnit.MILLISECONDS);
       }catch (Exception e){
           return new MachineStatus(20,"","unable to write value: " + variant.getValue() + " to node: " + node.displayName);
       }
       if(code != null && !code.isGood()){
           MachineStatus status = status();
           return new MachineStatus(20,"",
                   "Error: " +
                           " isBad: " + code.isBad() + ","+
                   " securityError: " + code.isSecurityError() + ","+
                   " isUncertain: " + code.isUncertain() + ","+
                   " actual value: " + code.getValue(),true,status.getVibrations());
       }

       return current;
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
