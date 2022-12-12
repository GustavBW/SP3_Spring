package gbw.sp3.OpcClient.services;

import gbw.sp3.OpcClient.util.JSONWrapper;

public interface IClientRequestValidationService {

    ClientRequestValidationService.ClientValidationError validateRequestBody
            (JSONWrapper wrappedRequest, String[] bodyFieldsExpected);

    ClientRequestValidationService.ClientValidationError validateReadRequest(JSONWrapper wrappedRequest);

    ClientRequestValidationService.ClientValidationError validateWriteRequest(JSONWrapper wrappedRequest);
}
