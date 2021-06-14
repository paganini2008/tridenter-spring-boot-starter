###  Trident --- Microservice Distributed Collaboration Framework 
<code>Tridenter</code> is a distributed collaboration framework of microservice based on <code>SpringBoot</code> framework. It can make multiple independent spring boot applications easily and quickly form a cluster without relying on external registration centers (such as Spring Cloud Framework).

**Message multicast** in cluster is a very important function of <code>tridenter</code>. The lower layer of Trident realizes multicast function through <code>redis (PubSub)</code> to realize mutual discovery of applications, and then forms application cluster. Each member of the cluster supports the ability to multicast and unicast messages.

Trident provides the **leader election** algorithm interface by using Trident's ability to support unicast of messages. It has two leader election algorithms, fast leader election algorithm (based on <code>redis</code> queue) and consistent election algorithm (<code>Paxos</code> algorithm)

Meanwhile, <code>tridenter</code> provides process pool by using <code>tridenter</code> to support message unicast, and realizes the ability of method call and method fragmentation across processes

On the other hand, <code>tridenter</code> itself also provides the basic functions of micro **service governance**:

<code>Tridenter</code> has its own **registration center**. Using the principle of message multicast, applications are found and registered with each other. Therefore, each member in the cluster has a full list of members, that is, each application is a registration center, which reflects the idea of decentralized design. Each member realizes the ability of calling HTTP interface between applications through naming service, and provides various annotations and restful configuration to decouple service publisher and consumer

<code>Tridenter</code> has its own **gateway function**, which can publish the application as a gateway service independently, and can distribute HTTP requests and download tasks by proxy (upload is not supported temporarily)

<code>Tridenter</code> also has a variety of **load balancing algorithms** and **current limiting degradation policies**. Users can also customize load balancing algorithm or degradation policy

<code>Tridenter</code> implements the health check interface of spring actuator. Besides monitoring the cluster status, it also has the function of statistical analysis of the interface, and preliminarily realizes the unified management and monitoring of the interface

So, based on the Trident framework, we can also build a micro service system similar to Spring Cloud Framework

<code>Tridenter</code> adopts the idea of decentralization, that is, developers don't need to know which is the master node, which node is the slave node, and should not explicitly define an application master node. This is determined by the leader election algorithm adopted by Trident. The default election algorithm is the fast election algorithm. According to the election algorithm, any application node in the cluster may become the master node. The first application initiated by default is the master node. However, if the consistency election algorithm is adopted, it may be different. According to the author's description, the consistency election algorithm is not stable at present, and it is recommended to use the fast election algorithm in the application.


###  Compatibility
1. JDK 1.8 (or later)
2. <code>SpringBoot Framework 2.2.x </code>(or later)
3. <code>Redis 4.x</code> (or later)

### Install

```xml
		<dependency>
			<groupId>indi.atlantis.framework</groupId>
			<artifactId>tridenter-spring-boot-starter</artifactId>
			<version>1.0-RC1</version>
		</dependency>
```

### Required Config
```properties
spring.application.name=demo
spring.application.cluster.name=demo-cluster

# Redis Configuration
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.password=123456
spring.redis.dbIndex=0
```

### Distributed Capabilities
####  Process pool
Multiple applications with the same name (<code>${spring.application.name}</code>) can be formed into a process pool, just like the thread pool allocates different threads to call a method, the process pool can call methods across applications, provided that the method is existing
**Example Code:**
``` java
	@MultiProcessing(value = "calc", defaultValue = "11")
	public int calc(int a, int b) {
		if (a % 3 == 0) {
			throw new IllegalArgumentException("a ==> " + a);
		}
		log.info("[" + counter.incrementAndGet() + "]Port: " + port + ", Execute at: " + new Date());
		return a * b * 20;
	}

	@OnSuccess("calc")
	public void onSuccess(Object result, MethodInvocation invocation) {
		log.info("Result: " + result + ", Take: " + (System.currentTimeMillis() - invocation.getTimestamp()));
	}

	@OnFailure("calc")
	public void onFailure(ThrowableProxy info, MethodInvocation invocation) {
		log.info("========================================");
		log.error("{}", info);
	}
```
#### Method slicing
Method slicing is also called method parallel processing. In fact, each parameter of a set of parameters is distributed to different applications to run using the process pool, and then merged and output, and the fragmentation rule interface needs to be implemented
**Example Code:**

``` java
    @ParallelizingCall(value = "loop-test", usingParallelization = TestCallParallelization.class)
	public long total(String arg) {// 0,1,2,3,4,5,6,7,8,9
		return 0L;
	}

	public static class TestCallParallelization implements Parallelization {

		@Override
		public Long[] slice(Object argument) {
			String[] args = ((String) argument).split(",");
			Long[] longArray = new Long[args.length];
			int i = 0;
			for (String arg : args) {
				longArray[i++] = Long.parseLong(arg);
			}
			return longArray;
		}

		@Override
		public Long merge(Object[] results) {
			long total = 0;
			for (Object o : results) {
				total += ((Long) o).longValue();
			}
			return total;
		}

	}
```
### Microservice capabilities
#### Rest Client
**Example Code:**

``` java
@RestClient(provider = "test-service")
// @RestClient(provider = "http://192.168.159.1:5050")
public interface TestRestClient {

	@PostMapping("/metrics/sequence/{dataType}")
	Map<String, Object> sequence(@PathVariable("dataType") String dataType, @RequestBody SequenceRequest sequenceRequest);

}
```
Description:
1. Annotate the interface modified by <code>@RestClient</code> to indicate that this is an Http client
2. In the annotation, the provider attribute represents the service provider, which can be an application name in the cluster (${spring.application.name}) or a specific http address
3. Support Spring annotations <code>(GetMapping, PostMapping, PutMapping, DeleteMapping)</code>, in addition, the annotation <code>@Api</code> can provide more fine-grained parameter settings


#### Gateway
``` java
@EnableGateway
@SpringBootApplication
@ComponentScan
public class GatewayMain {

	public static void main(String[] args) {
		final int port = 9000;
		System.setProperty("server.port", String.valueOf(port));
		SpringApplication.run(GatewayMain.class, args);
	}
}
```
Just quote the annotation <code>@EnableGateway</code>, the bottom layer of the <code>tridenter gateway</code> is implemented with Netty4
**Configure routing:**

``` java
@Primary
@Component
public class MyRouterCustomizer extends DefaultRouterCustomizer {

	@Override
	public void customize(RouterManager rm) {
		super.customize(rm);
		rm.route("/my/**").provider("tester5");
		rm.route("/test/baidu").url("https://www.baidu.com").resourceType(ResourceType.REDIRECT);
		rm.route("/test/stream").url("	https://www.baidu.com/img/PCtm_d9c8750bed0b3c7d089fa7d55720d6cf.png").resourceType(ResourceType.STREAM);
	}

}
```
<code>ResourceType:</code>
DEFAULT: Forward request
REDIRECT: Jump
STREAM: Binary stream
FILE: save the file

#### Current limit downgrade
Current limiting refers to limiting the current on the client side, not the server side
The current limit depends on 3 indicators:
1. Response timeout rate
2. Error rate
3. Concurrency
By default, when any of these three indicators exceeds 80%, the current limit will be triggered and the downgrade service will be invoked
Current limit indicator statistics: <code>RequestStatisticIndicator</code>
Downgrade service interface: <code>FallbackProvider</code>

#### Health monitoring
Currently <code>tridenter</code> provides 3 subclasses of <code>HealthIndicator</code>
1. <code>ApplicationClusterHealthIndicator</code>
     Display the overall health status of the cluster
2. <code>TaskExecutorHealthIndicator</code>
     Display the health status of the cluster thread pool
3. <code>RestClientHealthIndicator</code>
    Display the health status of the Rest client (response timeout rate, error rate, concurrency)