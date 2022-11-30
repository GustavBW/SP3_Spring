package gbw.sp3.OpcClient.client;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfigBuilder;
import org.eclipse.milo.opcua.stack.client.DiscoveryClient;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.eclipse.milo.opcua.stack.core.util.EndpointUtil;
import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

public class OpcClient {

    private static OpcUaClient client;
    public static String currentAddress = "";

    final int NAMESPACEINDEX = 6;
    private static final AtomicLong clientHandles = new AtomicLong(1L);

    public record InitializationError(int status, String error){}
    private static InitializationError NO_ERROR = new InitializationError(200,"You're fine :)");

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

    public static MachineStatus status() {
        return new MachineStatus(200,"meh","meh");
    }

    private static InitializationError initialize1(String protocol, String ip, int port)
    {
        String newAddress = protocol + "://" + ip + ":" + port;
        try {
            List<EndpointDescription> endpoints = DiscoveryClient.getEndpoints(newAddress).get();
            EndpointDescription configPoint = EndpointUtil.updateUrl(endpoints.get(0), ip, port);

            OpcUaClientConfigBuilder cfg = new OpcUaClientConfigBuilder();
            cfg.setEndpoint(configPoint);

            client = OpcUaClient.create(cfg.build());
            client.connect().get();

        } catch (UaException | CancellationException uaEx) {
            return new InitializationError(400, uaEx.getMessage());
        } catch (InterruptedException | ExecutionException intEx){
            return new InitializationError(500, intEx.getMessage());
        }
        return NO_ERROR;
    }


}
