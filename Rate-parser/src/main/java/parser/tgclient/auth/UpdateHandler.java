package parser.tgclient.auth;

import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class UpdateHandler implements Client.ResultHandler {
    private final Auth authenticator;

    @Autowired
    public UpdateHandler(Auth authenticator) {
        this.authenticator = authenticator;
    }

    @Override
    public void onResult(TdApi.Object object) {
        if (object.getConstructor() == TdApi.UpdateAuthorizationState.CONSTRUCTOR) {
            authenticator.onAuthorizationStateUpdated(((TdApi.UpdateAuthorizationState) object).authorizationState);
        }
    }
}
