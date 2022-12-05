package gbw.sp3.OpcClient.services;

import ch.qos.logback.core.net.server.Client;
import gbw.sp3.OpcClient.util.JSONWrapper;
import org.springframework.stereotype.Service;

@Service
public class ClientRequestValidationService implements IClientRequestValidationService{

    public static record ClientValidationError(int httpStatus, String errorMessage){}


    public ClientRequestValidationService.ClientValidationError validateWriteRequest(JSONWrapper wrappedRequest){
        if(wrappedRequest.get("nodeName") == null){
            return new ClientValidationError(400,"Expected field nodeName in request body.");
        }
        if(wrappedRequest.get("value") == null){
            return new ClientValidationError(400,"Expected field value in request body.");
        }
        return null;
    }

    /**
     * Validation of OpcClient Initialization request
     * @param wrappedRequest request wrapped in a JSONWrapper
     * @returns null on valid request.
     */
    public ClientValidationError validateInitializeRequest
    (JSONWrapper wrappedRequest, String[] bodyFieldsExpected, String[] headersExpected)
    {
        String errorMessage = "Expected request body to contain fields: ";
        for(String expected : bodyFieldsExpected){
            errorMessage += expected + ",";
        }
        for(String expected : bodyFieldsExpected){
            if(wrappedRequest.get(expected) == null){
                return new ClientValidationError(400, errorMessage);
            }
        }

        errorMessage = "Expected request headers to contain headers: ";
        for(String expected : headersExpected){
            errorMessage += expected + ",";
        }
        for(String expected : headersExpected){
            if(wrappedRequest.get(expected) == null){
                return new ClientValidationError(400, errorMessage);
            }
        }

        return null;
    }

    public ClientValidationError validateReadRequest(JSONWrapper wrappedRequest){
        if(wrappedRequest.get("nodeNames") == null){
            return new ClientValidationError(400, "Expected body field nodeNames to be present in request.");
        }
        if(wrappedRequest.get("nodeNames").isEmpty() || wrappedRequest.get("nodeNames").length() < 5){
            return new ClientValidationError(400, "Invalid value of nodeNames field on request. Expected an underscore-separated string.");
        }
        return null;
    }

}
