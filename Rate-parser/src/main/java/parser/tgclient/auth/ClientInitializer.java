//
// Copyright Aliaksei Levin (levlam@telegram.org), Arseny Smirnov (arseny30@gmail.com) 2014-2022
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
package parser.tgclient.auth;

import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;

/**
 * Example class for TDLib usage from Java.
 */
@Slf4j
@Configuration
public class ClientInitializer {

    static {
        try {
            System.loadLibrary("tdjni");
        } catch (UnsatisfiedLinkError e) {
            throw new RuntimeException(e);
        }
    }

    private final UpdateHandler handler;
    private final Auth authenticator;
    private Client client = null;

    @Autowired
    public ClientInitializer(UpdateHandler handler,
                             Auth authenticator) {
        this.handler = handler;
        this.authenticator = authenticator;
    }

    @Bean
    public Client initializeClient() {
        Client.execute(new TdApi.SetLogVerbosityLevel(2));
        client = Client.create(handler, null, null);
        authenticator.setClient(client);
        return client;
    }

    @PreDestroy
    private void destroy() {
        client.send(new TdApi.Close(), handler);
    }
}
