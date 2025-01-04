
# Dingo Spring Boot Starter

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-Compatible-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-Compatible-brightgreen.svg)](https://spring.io/projects/spring-cloud)
[![Netty](https://img.shields.io/badge/Netty-Based-brightgreen.svg)](https://netty.io/)

Dingo Spring Boot Starter is a robust and feature-rich RPC (Remote Procedure Call) data transmission framework designed to streamline communication in distributed systems. Built on top of Netty, with optional support for Mina and Grizzly, Dingo offers unmatched flexibility and scalability. Seamlessly integrating with Spring Boot and Spring Cloud, this framework is tailored for high-performance, non-HTTP-based service-to-service communication while ensuring reliability and resilience under high-concurrency scenarios.

---

## Features

### 1. Asynchronous Data Transmission
Dingo excels in handling high-concurrency environments with its support for asynchronous data sending and receiving. Key benefits include:
- **Scalable Event Handling**: Ideal for scenarios such as network crawling, log aggregation, and real-time event streaming.
- **Non-Blocking Architecture**: Powered by Netty's non-blocking IO, ensuring efficient resource utilization and high throughput.

---

### 2. Synchronous RPC Communication
For applications requiring strict request-response patterns, Dingo provides robust synchronous RPC communication:
- **Remote Method Invocation (RMI)**: Perform method calls on remote services without relying on traditional HTTP protocols.
- **Ordered Processing**: Ensure consistent data flow in pipelines and workflows.

---

### 3. Advanced Transport Layer Flexibility
Dingo supports multiple transport layers to adapt to diverse application needs:
- **Netty**: Default choice for high-performance and scalability.
- **Mina**: Lightweight and developer-friendly.
- **Grizzly**: Suitable for enterprise-grade applications, offering advanced HTTP/2 and WebSocket support.

The transport layer is configurable via application properties, providing effortless adaptability.

---

### 4. Resilience and Reliability Features
Dingo is designed to handle high-concurrency demands while maintaining service reliability. It includes:
- **Retry Mechanisms**: Automatically retries failed requests to ensure data delivery under transient failures.
- **Rate Limiting**: Controls the flow of requests to prevent overload during traffic spikes.
- **Fallbacks and Downgrades**: Provides alternative responses when primary services are unavailable.
- **Circuit Breaking**: Detects and isolates failing services to prevent cascading failures.

These features make Dingo an ideal framework for mission-critical systems requiring robust fault tolerance.

---

### 5. Seamless Integration with Spring Ecosystem
Dingo fully integrates with Spring Boot and Spring Cloud, enabling:
- **Auto-Configuration**: Simplifies setup with minimal boilerplate.
- **Service Discovery and Load Balancing**: Works with Spring Cloud registries like Eureka and Zookeeper.
- **Unified API**: Consistent API for developers, abstracting transport and discovery complexities.

---

## Getting Started

### 1. Add Dependency
Include the following dependency in your `pom.xml`:
```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>dingo-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. Configure Application Properties
Add the necessary configuration to `application.yml`:
```yaml
dingo:
  transport:
    type: netty # Options: netty, mina, grizzly
    port: 8081
  rpc:
    async: true
    retry:
      enabled: true
      maxAttempts: 3
    rateLimit:
      enabled: true
      limit: 100 # requests per second
    circuitBreaker:
      enabled: true
      failureThreshold: 50 # percentage
```

### 3. Define Services
Annotate your service methods with `@DingoService` to expose them as RPC endpoints:
```java
@DingoService
public class MyRpcService {
    public String processData(String input) {
        return "Processed: " + input;
    }
}
```

### 4. Invoke Remote Services
Use the `DingoClient` to perform remote calls:
```java
@Autowired
private DingoClient dingoClient;

public void sendRequest() {
    String response = dingoClient.invoke("remoteService", "processData", "Sample Data");
    System.out.println(response);
}
```

---

## Documentation
For detailed documentation and advanced usage, refer to the [Official Documentation](https://github.com/paganini2008/dingo-spring-boot-starter/wiki).

---

## Contributing
We welcome contributions! Please refer to the [Contributing Guide](CONTRIBUTING.md) for guidelines.

---

## License
This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---

Dingo Spring Boot Starter empowers developers to build high-performance, scalable, and fault-tolerant distributed systems. Whether handling asynchronous event streams or synchronous RPC calls, Dingo provides a comprehensive and customizable framework for modern microservice architectures.
