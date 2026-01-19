# Weighted Round Robin Load Balancer Demo

A demonstration project implementing a weighted round robin load balancer with health checks using Spring Boot 4.

## Architecture

```
                    ┌─────────────────────┐
                    │   Load Balancer     │
                    │    (port 8080)      │
                    │                     │
                    │  Weighted Round     │
                    │  Robin Algorithm    │
                    └─────────┬───────────┘
                              │
            ┌─────────────────┼─────────────────┐
            │                 │                 │
            ▼                 ▼                 ▼
   ┌────────────────┐ ┌────────────────┐ ┌────────────────┐
   │  Server 1      │ │  Server 2      │ │  Server 3      │
   │  (port 8081)   │ │  (port 8082)   │ │  (port 8083)   │
   │  weight: 3     │ │  weight: 2     │ │  weight: 1     │
   └────────────────┘ └────────────────┘ └────────────────┘
```

## How Weighted Round Robin Works

This implementation uses the **Smooth Weighted Round Robin** algorithm (Nginx-style):

1. Each server has a `weight` (configured) and a `currentWeight` (dynamic)
2. For each request:
   - Add each server's weight to its current weight
   - Select the server with the highest current weight
   - Subtract the total weight from the selected server's current weight

**Example with weights 3, 2, 1:**

| Request | Before Selection | Selected | After Selection |
|---------|------------------|----------|-----------------|
| 1       | [3, 2, 1]        | Server 1 | [-3, 2, 1]      |
| 2       | [0, 4, 2]        | Server 2 | [0, -2, 2]      |
| 3       | [3, 0, 3]        | Server 1 | [-3, 0, 3]      |
| 4       | [0, 2, 4]        | Server 3 | [0, 2, -2]      |
| 5       | [3, 4, -1]       | Server 2 | [3, -2, -1]     |
| 6       | [6, 0, 0]        | Server 1 | [0, 0, 0]       |

Result: Server 1 receives 3 requests, Server 2 receives 2, Server 3 receives 1 (3:2:1 ratio).

## Project Structure

```
load-balancer-demo/
├── pom.xml                    # Parent POM
├── resource-server/           # Simple REST backend
│   ├── pom.xml
│   └── src/main/java/
│       └── org/havryliuk/resourceserver/
│           ├── ResourceServerApplication.java
│           ├── config/ResourceServerProperties.java
│           ├── controller/ResourceController.java
│           └── dto/ServerInfoResponse.java
└── load-balancer/             # Weighted round robin load balancer
    ├── pom.xml
    └── src/main/java/
        └── org/havryliuk/loadbalancer/
            ├── LoadBalancerApplication.java
            ├── algorithm/WeightedRoundRobinAlgorithm.java
            ├── config/LoadBalancerProperties.java
            ├── controller/{LoadBalancerController,AdminController}.java
            ├── health/{HealthChecker,HealthCheckScheduler}.java
            ├── model/BackendServer.java
            ├── registry/ServerRegistry.java
            └── service/{LoadBalancerService,RequestForwarder}.java
```

## Building

```bash
mvn clean package
```

## Running

### 1. Start Resource Servers

Start three instances on different ports:

```bash
# Terminal 1
java -jar resource-server/target/resource-server-0.0.1-SNAPSHOT.jar \
  --server.port=8081 \
  --resource-server.instance.id=server-1

# Terminal 2
java -jar resource-server/target/resource-server-0.0.1-SNAPSHOT.jar \
  --server.port=8082 \
  --resource-server.instance.id=server-2

# Terminal 3
java -jar resource-server/target/resource-server-0.0.1-SNAPSHOT.jar \
  --server.port=8083 \
  --resource-server.instance.id=server-3
```

### 2. Start Load Balancer

```bash
java -jar load-balancer/target/load-balancer-0.0.1-SNAPSHOT.jar
```

### 3. Test Load Distribution

Send 12 requests and observe the distribution:

```bash
for i in {1..12}; do
  curl -s http://localhost:8080/api/resource | jq -r .instanceId
done
```

Expected output (3:2:1 ratio):
```
server-1
server-2
server-1
server-3
server-2
server-1
server-1
server-2
server-1
server-3
server-2
server-1
```

### 4. Check Statistics

```bash
# Get load balancer stats
curl -s http://localhost:8080/lb/stats | jq

# List all servers
curl -s http://localhost:8080/lb/servers | jq
```

### 5. Test Health Checks

Stop one of the resource servers and observe:
- The load balancer detects the server is down within 5 seconds
- Requests are redistributed to healthy servers
- Restart the server and it automatically rejoins the pool

## API Endpoints

### Load Balancer

| Endpoint | Description |
|----------|-------------|
| `ALL /**` | Forwards requests to backend servers |
| `GET /lb/stats` | Load balancer statistics |
| `GET /lb/servers` | List all servers with health status |
| `GET /actuator/health` | Load balancer health check |

### Resource Server

| Endpoint | Description |
|----------|-------------|
| `GET /api/resource` | Returns server info (id, name, port, timestamp) |
| `GET /actuator/health` | Server health check |

## Configuration

### Load Balancer (application.yml)

```yaml
loadbalancer:
  servers:
    - id: server-1
      url: http://localhost:8081
      weight: 3
    - id: server-2
      url: http://localhost:8082
      weight: 2
    - id: server-3
      url: http://localhost:8083
      weight: 1
  health-check:
    interval: 5000  # ms
    timeout: 2000   # ms
```

## Enterprise Considerations

For production use, consider adding:

- **TLS/HTTPS**: Secure communication between load balancer and backends
- **Circuit Breakers**: Prevent cascade failures (e.g., Resilience4j)
- **Metrics & Monitoring**: Prometheus/Micrometer metrics, Grafana dashboards
- **Rate Limiting**: Protect backends from overload
- **Request Retries**: Automatic retry on transient failures
- **Session Affinity**: Sticky sessions when needed
- **Dynamic Configuration**: Add/remove servers without restart
- **Distributed Tracing**: Request tracing across services (e.g., Zipkin)
- **Connection Pooling**: Reuse connections to backends
- **Request/Response Logging**: Audit and debugging
