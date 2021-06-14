###  Trident: Microservice Distributed Collaboration Framework 
<code>Tridenter</code> is a distributed collaboration framework of microservice based on <code>SpringBoot</code> framework. It can make multiple independent spring boot applications easily and quickly form a cluster without relying on external registration centers (such as spring cloud).

Message multicast in cluster is a very important function of <code>tridenter</code>. The lower layer of Trident realizes multicast function through <code>redis (PubSub)</code> to realize mutual discovery of applications, and then forms application cluster. Each member of the cluster supports the ability to multicast and unicast messages.

Trident provides the leader election algorithm interface by using Trident's ability to support unicast of messages. It has two leader election algorithms, fast leader election algorithm (based on <code>redis</code> queue) and consistent election algorithm (<code>Paxos</code> algorithm)

Meanwhile, <code>tridenter</code> provides process pool by using <code>tridenter</code> to support message unicast, and realizes the ability of method call and method fragmentation across processes

On the other hand, <code>tridenter</code> itself also provides the basic functions of micro service governance:

<code>Tridenter</code> has its own registration center. Using the principle of message multicast, applications are found and registered with each other. Therefore, each member in the cluster has a full list of members, that is, each application is a registration center, which reflects the idea of decentralized design. Each member realizes the ability of calling HTTP interface between applications through naming service, and provides various annotations and restful configuration to decouple service publisher and consumer

<code>Tridenter</code> has its own gateway function, which can publish the application as a gateway service independently, and can distribute HTTP requests and download tasks by proxy (upload is not supported temporarily)

<code>Tridenter</code> also has a variety of load balancing algorithms and current limiting degradation policies. Users can also customize load balancing algorithm or degradation policy

<code>Tridenter</code> realizes the related interface of the actor. Besides monitoring the cluster status, it also has the function of statistical analysis of the interface, and preliminarily realizes the unified management and monitoring of the interface

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

