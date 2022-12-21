package gbw.sp3.OpcClient.services;

import ch.qos.logback.core.net.server.Client;
import gbw.sp3.OpcClient.util.JSONWrapper;
import org.springframework.stereotype.Service;

@Service
public class ClientRequestValidationService implements IClientRequestValidationService{

    public static record ClientValidationError(int httpStatus, String errorMessage){}

    /**
     * Validation of any request body
     * @param wrappedRequest request wrapped in a JSONWrapper
     * @param bodyFieldsExpected a string array with what fields are expected
     * @returns null on valid request.
     */
    public ClientValidationError validateRequestBody(JSONWrapper wrappedRequest, String[] bodyFieldsExpected)
    {
        String errorMessage = "Expected request body to contain fields: ";
        boolean isFaulty = false;
        for(String expected : bodyFieldsExpected){
            if(wrappedRequest.get(expected) == null){
                isFaulty = true;
                errorMessage += expected + ", ";
            }
        }

        if(isFaulty){
            return new ClientValidationError(400, errorMessage);
        }

        return null;
    }


}
