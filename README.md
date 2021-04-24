###  Tridenter-Spring-Boot-Starter
tridenter-spring-boot-starter is an extended tool for spring application. It can easily and quickly build a spring cluster application without other framework like spring cloud. No specified register center location, no extra system configurations , once import tridenter-spring-boot-starter, your application  has had ability of cluster interactive between applications

For examle, Here are three applications: Application A, Application B, Application C. 
Now if you want to make them become a cluster and implement method invocation for each other, without tridenter-spring-boot-starter, the whole system has to add an extra role, register center, which has some abilities such as service register function and service discovery function and so on.

Let's look at how to make spring applications build a cluster first.
Generally,  your system must introduce register center, which provide register service and discovery service. and then each application register themselves on the register center, thereby working as a cluster like consul or eureka in Spring Cloud.  

``` mermaid
graph LR
A["Spring Application A"]-->D["Registry Center (or Cluster)"]
B["Spring Application B"]-->D["Registry Center (or Cluster)"]
C["Spring Application C"]-->D["Registry Center (or Cluster)"]
```
Compare to building a spring cluster in common way,  using tridenter-spring-boot-starter in your application would be a cluster at once. No extra application as register center, no extra worry about register center is a cluster or not. tridenter-spring-boot-starter make applications in cluster have the ability that interact with each each.  No specified register center, all applications are register center.

All applications will play two roles in the whole cluster, more exactly, the first started application will be the leader of cluster, the following application work as follower.

``` mermaid
graph LR
A("Spring Application A (Leader) ")-->B("Spring Application B")
A("Spring Application A (Leader)")-->C("Spring Application C")
D("Spring Application B (Follower)")-->E("Spring Application A")
D("Spring Application B (Follower)")-->F("Spring Application C")
G("Spring Application C (Follower)")-->H("Spring Application A")
G("Spring Application C (Follower)")-->I("Spring Application B")

```

### Architecture

| Component | Description                                                  |      |
| --------- | ------------------------------------------------------------ | ---- |
| Multicast | Make applications keep communicating with each other         |      |
| Election  | Launch a election when cluster is started                    |      |
| Http      | Provide ability that application send a http request in the cluster |      |
| Gateway   | Provide an unified http request entrance                     |      |
| Pool      | A process pool, which enhance the ability of invoking local method between applications |      |
| Monitor   | Watch and collect the runtime info from the cluster          |      |

###  Compatibility
1. JDK 1.8 (or later)
2. Spring Boot Framework 2.2.x (or later)
3. Redis 4.x (or later)

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

