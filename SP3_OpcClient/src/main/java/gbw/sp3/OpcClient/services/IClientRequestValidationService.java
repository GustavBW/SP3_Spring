package gbw.sp3.OpcClient.services;

import gbw.sp3.OpcClient.util.JSONWrapper;

public interface IClientRequestValidationService {

    ClientRequestValidationService.ClientValidationError validateInitializeRequest
            (JSONWrapper wrappedRequest, String[] bodyFieldsExpected, String[] headersExpected);

    ClientRequestValidationService.ClientValidationError validateReadRequest(JSONWrapper wrappedRequest);

    ClientRequestValidationService.ClientValidationError validateWriteRequest(JSONWrapper wrappedRequest);
}
