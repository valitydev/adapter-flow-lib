package dev.vality.adapter.flow.lib.client;


import dev.vality.adapter.flow.lib.model.BaseRequestModel;
import dev.vality.adapter.flow.lib.model.BaseResponseModel;

/**
 * This interface is used to call an external system in order to perform some operation in the payment flow.
 * Not all methods are required for flows.
 */
public interface RemoteClient {

    default BaseResponseModel preAuth(BaseRequestModel request) {
        throw new UnsupportedOperationException();
    }

    default BaseResponseModel auth(BaseRequestModel request) {
        throw new UnsupportedOperationException();
    }

    default BaseResponseModel generateToken(BaseRequestModel request) {
        throw new UnsupportedOperationException();
    }

    default BaseResponseModel pay(BaseRequestModel request) {
        throw new UnsupportedOperationException();
    }

    default BaseResponseModel capture(BaseRequestModel request) {
        throw new UnsupportedOperationException();
    }

    default BaseResponseModel cancel(BaseRequestModel request) {
        throw new UnsupportedOperationException();
    }

    default BaseResponseModel refund(BaseRequestModel request) {
        throw new UnsupportedOperationException();
    }

    default BaseResponseModel finish3ds(BaseRequestModel request) {
        throw new UnsupportedOperationException();
    }

    default BaseResponseModel check3dsV2(BaseRequestModel request) {
        throw new UnsupportedOperationException();
    }

    default BaseResponseModel finish3dsV2(BaseRequestModel request) {
        throw new UnsupportedOperationException();
    }

    default BaseResponseModel status(BaseRequestModel request) {
        throw new UnsupportedOperationException();
    }

    default BaseResponseModel recurringGenerateToken(BaseRequestModel request) {
        throw new UnsupportedOperationException();
    }

    default BaseResponseModel recurringPay(BaseRequestModel request) {
        throw new UnsupportedOperationException();
    }

}
