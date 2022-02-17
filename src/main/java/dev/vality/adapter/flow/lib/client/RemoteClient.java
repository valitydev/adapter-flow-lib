package dev.vality.adapter.flow.lib.client;


import dev.vality.adapter.flow.lib.model.BaseRequestModel;
import dev.vality.adapter.flow.lib.model.BaseResponseModel;

/**
 * This interface is used to call an external system in order to perform some operation in the payment flow.
 * Not all methods are required for flows.
 */
public interface RemoteClient {

    default BaseResponseModel auth(BaseRequestModel request) {
        throw new UnsupportedOperationException("This method is unsupported");
    }

    default BaseResponseModel pay(BaseRequestModel request) {
        throw new UnsupportedOperationException("This method is unsupported");
    }

    default BaseResponseModel capture(BaseRequestModel request) {
        throw new UnsupportedOperationException("This method is unsupported");
    }

    default BaseResponseModel cancel(BaseRequestModel request) {
        throw new UnsupportedOperationException("This method is unsupported");
    }

    default BaseResponseModel refund(BaseRequestModel request) {
        throw new UnsupportedOperationException("This method is unsupported");
    }

    default BaseResponseModel finish3ds(BaseRequestModel request) {
        throw new UnsupportedOperationException("This method is unsupported");
    }

    default BaseResponseModel check3dsV2(BaseRequestModel request) {
        throw new UnsupportedOperationException("This method is unsupported");
    }

    default BaseResponseModel finish3dsV2(BaseRequestModel request) {
        throw new UnsupportedOperationException("This method is unsupported");
    }

    default BaseResponseModel status(BaseRequestModel request) {
        throw new UnsupportedOperationException("This method is unsupported");
    }

}
