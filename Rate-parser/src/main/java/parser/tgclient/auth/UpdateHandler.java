package parser.tgclient.auth;

import lombok.AllArgsConstructor;
import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;
import org.springframework.stereotype.Component;
import parser.rest.RateUpdater;
import parser.tgclient.parser.RateParser;

import java.util.Locale;

@Component
@AllArgsConstructor
class UpdateHandler implements Client.ResultHandler {
    private final Auth authenticator;
    private final RateParser rateParser;
    private final RateUpdater rateUpdater;

    @Override
    public void onResult(TdApi.Object object) {
        if (object.getConstructor() == TdApi.UpdateAuthorizationState.CONSTRUCTOR) {
            authenticator.onAuthorizationStateUpdated(((TdApi.UpdateAuthorizationState) object).authorizationState);
        } else if (object.getConstructor() == TdApi.UpdateChatLastMessage.CONSTRUCTOR) {
            TdApi.UpdateChatLastMessage updateChat = (TdApi.UpdateChatLastMessage) object;
            if (updateChat.lastMessage.content instanceof TdApi.MessageText) {
                String text = ((TdApi.MessageText) updateChat.lastMessage.content).text.text.toLowerCase(Locale.ROOT);
                if (text.contains("vkursedpua")) {
                    RateParser.MenorahRates rates = rateParser.getRates(text);
                    if (rates.getCurrencies().size() > 0) {
                        rateUpdater.updateRates(rates);
                    }
                }
            }
        }
    }
}
