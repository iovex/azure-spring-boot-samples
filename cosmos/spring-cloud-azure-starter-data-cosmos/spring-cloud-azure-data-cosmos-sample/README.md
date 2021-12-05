

# Sample project for Spring Cloud Azure Starter Data Cosmos

## Key concepts
This sample project demonstrates how to use Azure Cosmos DB via Spring Boot Starter `spring-cloud-azure-starter-data-cosmos` to store data in and retrieve data from your Azure Cosmos DB.

## Getting started



### Create an Azure Cosmos DB on Azure

1. Go to [Azure portal](https://portal.azure.com/) and click +New .
2. Click Databases, and then click Azure Cosmos DB to create your database. 
3. Navigate to the database you have created, and click Access keys and copy your URI and access keys for your database.
                                                                                                                                  
## Examples

### Config the sample

1. Navigate to `src/main/resources` and open `application.yml`.
2. replace below properties in `application.yml` with information of your database.

```yaml
spring:
  cloud:
    azure:
      cosmos:
        key: [your-cosmos-key]
        endpoint: [your-cosmos-endpoint]
        database: [your-cosmos-database]
```

3. (Optional) Add Spring Boot Actuator

```yaml
management:
  health:
    azure-cosmos:
      enabled: true
```   

    If you choose to add Spring Boot Actuator for Cosmos DB, add `management.health.azure-cosmos.enabled=true` to application.properties.
    Call `http://{hostname}:{port}/actuator/health/cosmos` to get the Cosmos DB health info. 

### Run with Maven
```
cd azure-spring-boot-samples/cosmos/spring-cloud-azure-starter-data-cosmos/spring-cloud-azure-data-cosmos-sample
mvn spring-boot:run
```

## Troubleshooting
### Known issue

Directly running the sample app from IDE IntelliJ or Eclipse has below security exception if using the *released* starter. The root cause is that the release `spring-data-azure-cosmosdb` jar is code-signed by us. We're working actively to resolve this issue. 

```
Caused by: java.lang.SecurityException: class "com.microsoft.azure.sample.User_Accessor_yhb3bq"'s signer information does not match signer information of other classes in the same package
	at java.lang.ClassLoader.checkCerts(ClassLoader.java:898) ~[na:1.8.0_131]
	at java.lang.ClassLoader.preDefineClass(ClassLoader.java:668) ~[na:1.8.0_131]
	at java.lang.ClassLoader.defineClass(ClassLoader.java:761) ~[na:1.8.0_131]
```

If `com.fasterxml.jackson.databind.JsonMappingException` is thrown during deserialization, with error message `Can not construct instance of {your.pojo.class}: no suitable constructor found, can not deserialize from Object value (missing default constructor...`, add [Lombok annotatations](https://projectlombok.org/features/all) `@Data` and `@AllArgsConstructor` for your POJO class, or use [Jackson annotations](https://github.com/FasterXML/jackson-annotations#using-constructors-or-factory-methods) `@JsonCreator` and `@JsonProperty` for the full argument constructor.

## Next steps

Please refer to [this article](https://docs.microsoft.com/java/azure/spring-framework/configure-spring-boot-starter-java-app-with-cosmos-db) for the tutorial about how to use the Spring Boot Starter with Azure Cosmos DB API.

## Contributing

<!-- LINKS -->