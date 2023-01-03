package bot.tgclient.auth;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Auth {

    @Getter
    protected final String newLine = System.getProperty("line.separator");
    private final AuthState externalAuthorizationState;
    private TdApi.AuthorizationState internalAuthorizationState;
    @Setter
    private Client client;

    @Autowired
    public Auth(AuthState externalAuthorizationState) {
        this.externalAuthorizationState = externalAuthorizationState;
    }

    public void authorize(String code) {
        client.send(new TdApi.CheckAuthenticationCode(code),
                new AuthorizationRequestHandler());
    }

    public void enterPassword(String password) {
        client.send(new TdApi.CheckAuthenticationPassword(password), new AuthorizationRequestHandler());
    }

    protected void onAuthorizationStateUpdated(TdApi.AuthorizationState authorizationState) {
        if (authorizationState != null) {
            this.internalAuthorizationState = authorizationState;
        }
        switch (this.internalAuthorizationState.getConstructor()) {
            case TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR:
                TdApi.TdlibParameters parameters = new TdApi.TdlibParameters();
                parameters.databaseDirectory = "tdlib";
                parameters.useMessageDatabase = true;
                parameters.useSecretChats = true;
                parameters.apiId = 94575;
                parameters.apiHash = "a3406de8d171bb422bb6ddf3bbd800e2";
                parameters.systemLanguageCode = "en";
                parameters.deviceModel = "Desktop";
                parameters.applicationVersion = "1.0";
                parameters.enableStorageOptimizer = true;

                client.send(new TdApi.SetTdlibParameters(parameters),
                        new AuthorizationRequestHandler());
                break;
            case TdApi.AuthorizationStateWaitEncryptionKey.CONSTRUCTOR:
                client.send(new TdApi.CheckDatabaseEncryptionKey(),
                        new AuthorizationRequestHandler());
                break;
            case TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR: {
                log.info("Seeking phone number.");
                client.send(new TdApi.SetAuthenticationPhoneNumber("+380634410488", null),
                        new AuthorizationRequestHandler());
                break;
            }
            case TdApi.AuthorizationStateWaitOtherDeviceConfirmation.CONSTRUCTOR: {
                String link = ((TdApi.AuthorizationStateWaitOtherDeviceConfirmation) this.internalAuthorizationState).link;
                System.out.println("Please confirm this login link on another device: " + link);
                break;
            }
            case TdApi.AuthorizationStateWaitCode.CONSTRUCTOR: {
                log.info("Awaiting authorization code.");
                break;
            }
            case TdApi.AuthorizationStateWaitPassword.CONSTRUCTOR: {
                log.info("Password was requested in Auth.");
                break;
            }
            case TdApi.AuthorizationStateReady.CONSTRUCTOR:
                externalAuthorizationState.release();
                break;
            case TdApi.AuthorizationStateLoggingOut.CONSTRUCTOR:
                log.info("Logging out");
                break;
            case TdApi.AuthorizationStateClosing.CONSTRUCTOR:
                log.info("Closing");
                break;
            case TdApi.AuthorizationStateClosed.CONSTRUCTOR:
                log.info("Closed");
                break;
            default:
                log.error("Unsupported authorization state:" + newLine + this.internalAuthorizationState);
        }
    }

    private class AuthorizationRequestHandler implements Client.ResultHandler {

        @Override
        public void onResult(TdApi.Object object) {
            switch (object.getConstructor()) {
                case TdApi.Error.CONSTRUCTOR:
                    log.error("Receive an error:" + newLine + object);
                    onAuthorizationStateUpdated(null); // repeat last action
                    break;
                case TdApi.Ok.CONSTRUCTOR:
                    // result is already received through UpdateAuthorizationState, nothing to do
                    break;
                default:
                    log.error("Receive wrong response from TDLib:" + newLine + object);
            }
        }
    }
}
