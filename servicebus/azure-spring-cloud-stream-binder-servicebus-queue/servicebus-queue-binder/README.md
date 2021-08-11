---
page_type: sample
languages:
- java
products:
- azure-service-bus
description: "Azure Spring Cloud Stream Binder Sample project for Service Bus queue client library"
urlFragment: "azure-spring-cloud-sample-service-bus-queue-binder"
---

# Spring Cloud Azure Stream Binder for Service Bus queue Sample shared library for Java

## Key concepts

This code sample demonstrates how to use the Spring Cloud Stream binder for 
Azure Service Bus queue. The sample app has two operating modes.
One way is to expose a Restful API to receive string message, another way is to automatically provide string messages.
These messages are published to a service bus queue. The sample will also consume messages from the same service bus queue.

## Getting started

Running this sample will be charged by Azure. You can check the usage and bill at 
[this link][azure-account].



### Create Azure resources

We have several ways to config the Spring Cloud Stream Binder for Service
Bus Queue. You can choose anyone of them.

>[!Important]
>
>  When using the Restful API to send messages, the **Active profiles** must contain `manual`.

#### Method 1: Connection string based usage

1.  Create Azure Service Bus namespace and queue.
    Please see [how to create][create-service-bus].

1.  Update [application.yaml][application.yaml].
    ```yaml
    spring:
      cloud:
        azure:
          servicebus:
            connection-string: [servicebus-namespace-connection-string] 
        stream:
          function:
            definition: consume;supply
          bindings: 
            consume-in-0: 
              destination: [servicebus-queue-name]
            supply-out-0:
              destination: [servicebus-queue-name-same-as-above]
          poller:
            fixed-delay: 1000
            initial-delay: 0
    ```

#### Method 2: Service principal based usage

1.  Create a service principal for use in by your app. Please follow 
    [create service principal from Azure CLI][create-sp-using-azure-cli].

1.  Create Azure Service Bus namespace and queue.
        Please see [how to create][create-service-bus].

1.  Add Role Assignment for Service Bus. See
    [Service principal for Azure resources with Service Bus][role-assignment]
    to add role assignment for Service Bus. Assign `Contributor` role for service bus.
    
1.  Update [application-sp.yaml][application-sp.yaml].
    ```yaml
    spring:
      cloud:
        azure:
          client-id: [service-principal-id]
          client-secret: [service-principal-secret]
          tenant-id: [tenant-id]
          resource-group: [resource-group]
          servicebus:
            namespace: [servicebus-namespace]
        stream:
          function:
            definition: consume;supply
          bindings:
            consume-in-0:
              destination: [servicebus-queue-name]
            supply-out-0:
              destination: [servicebus-queue-name-same-as-above]
          poller:
            fixed-delay: 1000
            initial-delay: 0
    ```
#### Method 3: MSI credential based usage

##### Set up managed identity

Please follow [create managed identity][create-managed-identity] to set up managed identity.

##### Create other Azure resources

1.  Create Azure Service Bus namespace and queue.
        Please see [how to create][create-service-bus].
        
1.  Add Role Assignment for Service Bus. See
    [Managed identities for Azure resources with Service Bus][role-assignment]
    to add role assignment for Service Bus. Assign `Contributor` role for managed identity.
    

##### Update MSI related properties

1.  Update [application-mi.yaml][application-mi.yaml].
    ```yaml
    spring:
      cloud:
        azure:
          msi-enabled: true
          client-id: [the-id-of-managed-identity]
          resource-group: [resource-group]
          subscription-id: [subscription-id]
          servicebus:
            namespace: [servicebus-namespace]
        stream:
          function:
            definition: consume;supply
          bindings:
            consume-in-0:
              destination: [servicebus-queue-name]
            supply-out-0:
              destination: [servicebus-queue-name-same-as-above]
          poller:
            fixed-delay: 1000
            initial-delay: 0
    ```
    > We should specify `spring.profiles.active=mi` to run the Spring Boot application. 
      For App Service, please add a configuration entry for this.

##### Redeploy Application

If you update the `spring.cloud.azure.managed-identity.client-id`
property after deploying the app, or update the role assignment for
services, please try to redeploy the app again.

> You can follow 
> [Deploy a Spring Boot JAR file to Azure App Service][deploy-spring-boot-application-to-app-service] 
> to deploy this application to App Service

#### Enable auto create

If you want to auto create the Azure Service Bus instances, make sure you add such properties 
(only support the service principal and managed identity cases):

```yaml
spring:
  cloud:
    azure:
      subscription-id: [subscription-id]
      auto-create-resources: true
      environment: Azure
      region: [region]
```


## Examples
### How to configure the partition key
**Example: Manually set the partition key of the message through application.yml**

This example demonstrates how to manually set the partition key for the message in the application.

**Way 1:**
This example requires that `spring.cloud.stream.default.producer.partitionKeyExpression` be set `"'partitionKey-' + headers[<message-header-key>]"`.
```yaml
spring:
  cloud:
    azure:
      servicebus:
        connection-string: [servicebus-namespace-connection-string]
    stream:
      default:
        producer:
          partitionKeyExpression:  "'partitionKey-' + headers[<message-header-key>]"
```
```java
@PostMapping("/messages")
public ResponseEntity<String> sendMessage(@RequestParam String message) {
    LOGGER.info("Going to add message {} to Sinks.Many.", message);
    many.emitNext(MessageBuilder.withPayload(message)
                                .setHeader("<message-header-key>", "Customize partirion key")
                                .build(), Sinks.EmitFailureHandler.FAIL_FAST);
    return ResponseEntity.ok("Sent!");
}
```

> **NOTE:** When using `application.yml` to configure the partition key, its priority will be the lowest.
> It will take effect only when the `ServiceBusMessageHeaders.SESSION_ID`, `ServiceBusMessageHeaders.PARTITION_KEY`, `AzureHeaders.PARTITION_KEY` are not configured.

**Way 2:**
Manually add the partition Key in the message header by code.

*Recommended:* Use `ServiceBusMessageHeaders.PARTITION_KEY` as the key of the header.
```java
@PostMapping("/messages")
public ResponseEntity<String> sendMessage(@RequestParam String message) {
    LOGGER.info("Going to add message {} to Sinks.Many.", message);
    many.emitNext(MessageBuilder.withPayload(message)
                                .setHeader(ServiceBusMessageHeaders.PARTITION_KEY, "Customize partirion key")
                                .build(), Sinks.EmitFailureHandler.FAIL_FAST);
    return ResponseEntity.ok("Sent!");
}
```

*Not recommended but currently supported:* `AzureHeaders.PARTITION_KEY` as the key of the header.
```java
@PostMapping("/messages")
public ResponseEntity<String> sendMessage(@RequestParam String message) {
    LOGGER.info("Going to add message {} to Sinks.Many.", message);
    many.emitNext(MessageBuilder.withPayload(message)
                                .setHeader(AzureHeaders.PARTITION_KEY, "Customize partirion key")
                                .build(), Sinks.EmitFailureHandler.FAIL_FAST);
    return ResponseEntity.ok("Sent!");
}
```
> **NOTE:** When both `ServiceBusMessageHeaders.PARTITION_KEY` and `AzureHeaders.PARTITION_KEY` are set in the message headers,
> `ServiceBusMessageHeaders.PARTITION_KEY` is preferred.

### How to configure session id
**Example: Set the session id for the message**

This example demonstrates how to manually set the session id of a message in the application.

```java
@PostMapping("/messages")
public ResponseEntity<String> sendMessage(@RequestParam String message) {
    LOGGER.info("Going to add message {} to Sinks.Many.", message);
    many.emitNext(MessageBuilder.withPayload(message)
                                .setHeader(ServiceBusMessageHeaders.SESSION_ID, "Customize session id")
                                .build(), Sinks.EmitFailureHandler.FAIL_FAST);
    return ResponseEntity.ok("Sent!");
}
```

> **NOTE:** When the `ServiceBusMessageHeaders.SESSION_ID` is set in the message headers, and a different `ServiceBusMessageHeaders.PARTITION_KEY` (or `AzureHeaders.PARTITION_KEY`) header is also set,
> the value of the session id will eventually be used to overwrite the value of the partition key.

### Run sample
1. Run the `mvn spring-boot:run` in the root of the code sample to get the app running.

2. Send a POST request

        $ curl -X POST http://localhost:8080/messages?message=hello

    or when the app runs on App Service or VM

        $ curl -d -X POST https://[your-app-URL]/messages?message=hello

3. Verify in your app’s logs that a similar message was posted:

        New message received: 'hello'
        Message 'hello' successfully checkpointed

4. Send a POST request

        $ curl -X POST http://localhost:8080/setSessionId?message=hello

5. It will be visible on Azure Portal that sent messages are configured with session id and partition key.
   ![Azure portal displays the session id.](docs/image-set-session-id.png )

6. Delete the resources on [Azure Portal][azure-portal] to avoid unexpected charges.

## Enhancement
### Set Service Bus message headers
The following table illustrates how Spring message headers are mapped to Service Bus message headers and properties.
When creat a message, developers can specify the header or property of a Service Bus message by below constants.

```java
    @Autowired
private Sinks.Many<Message<String>> many;

@PostMapping("/messages")
public ResponseEntity<String> sendMessage(@RequestParam String message) {
    many.emitNext(MessageBuilder.withPayload(message)
    .setHeader(SESSION_ID, "group1")
    .build(),
    Sinks.EmitFailureHandler.FAIL_FAST);
    return ResponseEntity.ok("Sent!");
    }
```

For some Service Bus headers that can be mapped to multiple Spring header constants, the priority of different Spring headers is listed.

Service Bus Message Headers and Properties | Spring Message Header Constants | Type | Priority Number (Descending priority)
:---|:---|:---|:---
ContentType | org.springframework.messaging.MessageHeaders.CONTENT_TYPE | String | N/A
CorrelationId | com.azure.spring.integration.servicebus.converter.ServiceBusMessageHeaders.CORRELATION_ID | String | N/A
**MessageId** | com.azure.spring.integration.servicebus.converter.ServiceBusMessageHeaders.MESSAGE_ID | String | 1
**MessageId** | com.azure.spring.integration.core.AzureHeaders.RAW_ID | String | 2
**MessageId** | org.springframework.messaging.MessageHeaders.ID | UUID | 3
PartitionKey | com.azure.spring.integration.servicebus.converter.ServiceBusMessageHeaders.PARTITION_KEY | String | N/A
ReplyTo | org.springframework.messaging.MessageHeaders.REPLY_CHANNEL | String | N/A
ReplyToSessionId | com.azure.spring.integration.servicebus.converter.ServiceBusMessageHeaders.REPLY_TO_SESSION_ID | String | N/A
**ScheduledEnqueueTimeUtc** | com.azure.spring.integration.core.AzureHeaders.SCHEDULED_ENQUEUE_MESSAGE | Integer | 1
**ScheduledEnqueueTimeUtc** | com.azure.spring.integration.servicebus.converter.ServiceBusMessageHeaders.SCHEDULED_ENQUEUE_TIME | Instant | 2
SessionID | com.azure.spring.integration.servicebus.converter.ServiceBusMessageHeaders.SESSION_ID | String | N/A
TimeToLive | com.azure.spring.integration.servicebus.converter.ServiceBusMessageHeaders.TIME_TO_LIVE | Duration | N/A
To | com.azure.spring.integration.servicebus.converter.ServiceBusMessageHeaders.TO | String | N/A

## Troubleshooting

## Next steps

## Contributing


<!-- LINKS -->

[azure-account]: https://azure.microsoft.com/account/
[azure-portal]: https://ms.portal.azure.com/
[create-service-bus]: https://docs.microsoft.com/azure/service-bus-messaging/service-bus-create-namespace-portal 
[create-azure-storage]: https://docs.microsoft.com/azure/storage/ 
[create-sp-using-azure-cli]: https://github.com/Azure-Samples/azure-spring-boot-samples/blob/main/create-sp-using-azure-cli.md
[create-managed-identity]: https://github.com/Azure-Samples/azure-spring-boot-samples/blob/main/create-managed-identity.md
[deploy-spring-boot-application-to-app-service]: https://docs.microsoft.com/java/azure/spring-framework/deploy-spring-boot-java-app-with-maven-plugin?toc=%2Fazure%2Fapp-service%2Fcontainers%2Ftoc.json&view=azure-java-stable

[role-assignment]: https://docs.microsoft.com/azure/role-based-access-control/role-assignments-portal
[application-mi.yaml]: https://github.com/Azure-Samples/azure-spring-boot-samples/blob/main/servicebus/azure-spring-cloud-stream-binder-servicebus-queue/servicebus-queue-binder/src/main/resources/application-mi.yaml
[application-sp.yaml]: https://github.com/Azure-Samples/azure-spring-boot-samples/blob/main/servicebus/azure-spring-cloud-stream-binder-servicebus-queue/servicebus-queue-binder/src/main/resources/application-sp.yaml
[application.yaml]: https://github.com/Azure-Samples/azure-spring-boot-samples/blob/main/servicebus/azure-spring-cloud-stream-binder-servicebus-queue/servicebus-queue-binder/src/main/resources/application.yaml