package dev.vality.adapter.flow.lib.client;


import dev.vality.adapter.flow.lib.model.BaseRequestModel;
import dev.vality.adapter.flow.lib.model.BaseResponseModel;

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

}
