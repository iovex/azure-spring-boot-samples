// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.servicebus.topic.binder;

import com.azure.spring.integration.servicebus.converter.ServiceBusMessageHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Sinks;

@RestController
@Profile("manual")
public class ServiceProducerController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBusTopicBinderApplication.class);

    @Autowired
    private Sinks.Many<Message<String>> many;

    @PostMapping("/messages")
    public ResponseEntity<String> sendMessage(@RequestParam String message) {
        LOGGER.info("Going to add message {} to Sinks.Many.", message);
        many.emitNext(MessageBuilder.withPayload(message).build(), Sinks.EmitFailureHandler.FAIL_FAST);
        return ResponseEntity.ok("Sent!");
    }

    /**
     * Set the session id scene.
     */
    @PostMapping("/setSessionId")
    public ResponseEntity<String> setSessionId(@RequestParam String message) {
        LOGGER.info("Going to add message {} to Sinks.Many.", message);
        many.emitNext(MessageBuilder.withPayload(message)
                                    .setHeader(ServiceBusMessageHeaders.SESSION_ID, "<custom-session-id>")
                                    .build(), Sinks.EmitFailureHandler.FAIL_FAST);
        return ResponseEntity.ok("Sent!");
    }

    /**
     * Set the ServiceBusMessageHeaders partition key scene.
     */
    @PostMapping("/setServiceBusMessageHeadersPartitionKey")
    public ResponseEntity<String> setServiceBusMessageHeadersPartitionKey(@RequestParam String message) {
        LOGGER.info("Going to add message {} to Sinks.Many.", message);
        many.emitNext(MessageBuilder.withPayload(message)
                                    .setHeader(ServiceBusMessageHeaders.PARTITION_KEY, "<custom-partition-key>")
                                    .build(), Sinks.EmitFailureHandler.FAIL_FAST);
        return ResponseEntity.ok("Sent!");
    }

    @GetMapping("/")
    public String welcome() {
        return "welcome";
    }
}