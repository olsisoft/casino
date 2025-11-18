# Casino Platform - Monitoring & Observability Guide

**Version**: 1.0
**Last Updated**: 2025-01-18

## Overview

This guide covers the complete monitoring and observability stack for the Casino Platform, including metrics collection, log aggregation, distributed tracing, and alerting.

---

## Table of Contents

1. [Architecture](#architecture)
2. [Prometheus & Grafana](#prometheus--grafana)
3. [ELK Stack](#elk-stack)
4. [Distributed Tracing](#distributed-tracing)
5. [Custom Metrics](#custom-metrics)
6. [Alerts](#alerts)
7. [Dashboards](#dashboards)
8. [Quick Start](#quick-start)
9. [Troubleshooting](#troubleshooting)

---

## Architecture

### Monitoring Stack Components

```
┌─────────────────────────────────────────────────────────────┐
│                    Monitoring Stack                          │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  Prometheus  │  │ Elasticsearch│  │    Jaeger    │      │
│  │   (Metrics)  │  │    (Logs)    │  │   (Traces)   │      │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘      │
│         │                  │                  │               │
│  ┌──────▼───────┐  ┌──────▼───────┐  ┌──────▼───────┐      │
│  │   Grafana    │  │    Kibana    │  │  Jaeger UI   │      │
│  │ (Dashboards) │  │ (Dashboards) │  │ (Trace View) │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│                                                               │
└─────────────────────────────────────────────────────────────┘
                           ▲
                           │
        ┌──────────────────┴──────────────────┐
        │                                      │
┌───────▼───────┐                    ┌────────▼────────┐
│  Microservices│                    │   Exporters     │
│   (7 services)│                    │ (Postgres/Redis)│
└───────────────┘                    └─────────────────┘
```

### Port Allocation

| Service | Port | Purpose |
|---------|------|---------|
| Prometheus | 9090 | Metrics collection & queries |
| Grafana | 3000 | Metrics visualization |
| Alertmanager | 9093 | Alert management |
| Elasticsearch | 9200 | Log storage & search |
| Logstash | 5000, 5044 | Log processing |
| Kibana | 5601 | Log visualization |
| Jaeger | 16686 | Trace visualization |
| Node Exporter | 9100 | System metrics |
| cAdvisor | 8080 | Container metrics |
| Postgres Exporter | 9187 | Database metrics |
| Redis Exporter | 9121 | Cache metrics |

---

## Prometheus & Grafana

### Prometheus Configuration

**Location**: `infrastructure/monitoring/prometheus/prometheus.yml`

**Key Features**:
- Scrapes metrics from all 7 microservices every 15 seconds
- Collects system metrics via Node Exporter
- Monitors PostgreSQL and Redis
- Evaluates alerting rules
- 15-day retention period

**Targets**:
```yaml
- API Gateway (8080)
- Auth Service (8081)
- User Service (8082)
- Game Service (8083)
- Payment Service (8084)
- Tournament Service (8085)
- Notification Service (8086)
- PostgreSQL Exporter (9187)
- Redis Exporter (9121)
- Node Exporter (9100)
- cAdvisor (8080)
```

### Grafana Dashboards

#### 1. Service Overview Dashboard
**File**: `grafana/dashboards/service-overview.json`

**Panels**:
- **Service Health**: Up/Down status for all services
- **Request Rate**: Requests per second by service
- **Error Rate**: 5xx errors as percentage
- **Response Time (p95)**: 95th percentile latency
- **CPU Usage**: CPU utilization per service
- **Memory Usage**: JVM heap usage
- **Active Threads**: Thread count per service
- **Database Connections**: HikariCP pool metrics
- **GC Pause Time**: Garbage collection impact

**Access**: http://localhost:3000/d/service-overview

#### 2. Business Metrics Dashboard
**File**: `grafana/dashboards/business-metrics.json`

**Panels**:
- **Games Played**: Games per minute by type
- **Win Rate**: Player win percentage
- **Total Bets**: Hourly betting volume
- **Total Payouts**: Hourly payout volume
- **House Profit**: Revenue calculation
- **Active Players**: Current player count
- **Payment Volume**: Transaction volume by provider
- **Payment Success Rate**: Success percentage
- **Jackpot Wins**: 24-hour jackpot count
- **Big Wins**: 10x+ multiplier wins
- **Failed Logins**: Security metric
- **High Risk AML**: Compliance metric
- **Tournament Participation**: Active tournaments
- **Chat Messages**: Activity metric

**Access**: http://localhost:3000/d/business-metrics

---

## ELK Stack

### Elasticsearch

**Purpose**: Centralized log storage and full-text search

**Indexes**:
- `casino-logs-{service}-{date}`: General application logs
- `casino-errors-{date}`: Error logs only
- `casino-security-{date}`: Security events (auth, KYC, AML)
- `casino-payments-{date}`: Payment transactions

**Configuration**:
```yaml
Memory: 2GB heap (-Xms2g -Xmx2g)
Mode: Single-node
Security: Disabled (enable in production)
```

### Logstash

**Purpose**: Log processing and enrichment

**Configuration**: `infrastructure/monitoring/elk/logstash/logstash.conf`

**Inputs**:
1. **TCP (port 5000)**: Direct JSON logs from services
2. **File**: Log files from `/var/log/casino/**/*.log`
3. **Beats (port 5044)**: Container logs via Filebeat

**Filters**:
- JSON parsing
- Grok patterns for stack traces
- Service name extraction
- Geo-location for IP addresses
- HTTP access log parsing
- Payment transaction enrichment
- Game event enrichment
- Security event flagging
- Error event flagging

**Outputs**:
- Main index: `casino-logs-{service}-{date}`
- Error index: `casino-errors-{date}`
- Security index: `casino-security-{date}`
- Payment index: `casino-payments-{date}`

### Kibana

**Purpose**: Log visualization and exploration

**Dashboards**:
1. **Error Tracking**: Error analysis by service, type, and frequency
2. **Security Dashboard**: Authentication events, KYC, AML alerts
3. **Payment Dashboard**: Transaction logs, success rates, failures
4. **Game Activity**: Game plays, wins, RNG events

**Access**: http://localhost:5601

**Default Credentials**:
- Username: `elastic`
- Password: `changeme` (change in production)

---

## Distributed Tracing

### Jaeger

**Purpose**: End-to-end request tracing across microservices

**Features**:
- Trace complete request flows
- Identify performance bottlenecks
- Visualize service dependencies
- Root cause analysis

**Ports**:
- **16686**: Jaeger UI
- **6831/6832**: Jaeger agent (UDP)
- **14268**: Jaeger collector (HTTP)
- **9411**: Zipkin compatible endpoint

**Access**: http://localhost:16686

**Usage**:
1. Search traces by service, operation, or tags
2. View trace timeline and spans
3. Analyze latency distribution
4. Compare trace performance

---

## Custom Metrics

### Game Service Metrics

**File**: `services/game-service/src/main/java/com/casino/game/metrics/GameMetrics.java`

**Metrics**:
```java
// Counters
game_plays_total{game_type, result}          // Total games played
game_results_total{game_type, result}        // Win/loss results
game_jackpot_wins_total{game_type}           // Jackpot wins
game_big_wins_total{game_type}               // 10x+ multiplier wins
game_bonus_rounds_total{game_type}           // Bonus rounds triggered
game_rng_generation_total{result}            // RNG generation attempts
game_rng_generation_failures_total           // RNG failures

// Summaries/Histograms
game_bet_amount                              // Bet amounts
game_payout_amount                           // Payout amounts
game_profit                                  // Profit/loss per game
game_big_win_multiplier                      // Big win multipliers
game_bonus_payout                            // Bonus payouts

// Timers
game_duration_seconds{game_type}             // Game duration
game_session_duration_seconds                // Session duration

// Gauges
game_house_edge                              // Current house edge
game_active_players                          // Active player count
```

### Payment Service Metrics

**File**: `services/payment-service/src/main/java/com/casino/payment/metrics/PaymentMetrics.java`

**Metrics**:
```java
// Counters
payment_transactions_total{provider, type, status, currency}
payment_failures_total{provider, type}
payment_fees_total{provider}
payment_method_usage_total{provider, method}
payment_crypto_transactions_total{currency, type}
payment_verifications_total{provider, result}
payment_chargebacks_total{provider}
payment_refunds_total{provider, reason}

// Summaries
payment_transaction_amount
payment_fees_collected
payment_crypto_amount
payment_chargeback_amount
payment_refund_amount

// Timers
payment_processing_duration_seconds{provider, type}
payment_verification_duration_seconds{provider}

// Gauges
payment_provider_available
payment_daily_deposit_volume
payment_daily_withdrawal_volume
payment_pending_withdrawals_count
payment_pending_withdrawals_amount
```

### Usage Example

```java
@Service
@RequiredArgsConstructor
public class GameService {
    private final GameMetrics metrics;

    public GameResult playSlots(String userId, BigDecimal bet) {
        Instant start = Instant.now();

        // Play game
        GameResult result = slotsEngine.spin(bet);

        // Record metrics
        metrics.recordGamePlayed(
            "SLOTS",
            userId,
            result.isWin(),
            bet,
            result.getPayout()
        );

        metrics.recordGameDuration(
            "SLOTS",
            Duration.between(start, Instant.now())
        );

        if (result.isJackpot()) {
            metrics.recordJackpotWin("SLOTS", result.getPayout());
        }

        return result;
    }
}
```

---

## Alerts

### Alert Rules

**Location**: `infrastructure/monitoring/prometheus/alerts/service-alerts.yml`

#### Service Health Alerts

1. **ServiceDown**
   - Condition: `up == 0`
   - Duration: 1 minute
   - Severity: Critical
   - Description: Service is completely down

2. **HighErrorRate**
   - Condition: 5xx errors > 5% of total requests
   - Duration: 5 minutes
   - Severity: Warning
   - Description: Elevated error rate

3. **HighResponseTime**
   - Condition: p95 latency > 1 second
   - Duration: 5 minutes
   - Severity: Warning
   - Description: Slow response times

4. **HighCPUUsage**
   - Condition: CPU usage > 80%
   - Duration: 10 minutes
   - Severity: Warning
   - Description: High CPU utilization

5. **HighMemoryUsage**
   - Condition: Memory usage > 90%
   - Duration: 10 minutes
   - Severity: Warning
   - Description: Near memory exhaustion

6. **DatabaseConnectionPoolExhausted**
   - Condition: Active connections > 90% of max
   - Duration: 5 minutes
   - Severity: Critical
   - Description: Connection pool almost full

#### Business Metrics Alerts

1. **HighFailedLoginRate**
   - Condition: Failed logins > 10/second
   - Duration: 5 minutes
   - Severity: Warning
   - Description: Potential brute force attack

2. **PaymentProcessingFailures**
   - Condition: Payment failure rate > 10%
   - Duration: 5 minutes
   - Severity: Critical
   - Description: Payment system issues

3. **GameRNGFailures**
   - Condition: Any RNG failures
   - Duration: 1 minute
   - Severity: Critical
   - Description: Game fairness compromised

4. **HighRiskTransactionsSpike**
   - Condition: High-risk AML transactions > 5/second
   - Duration: 10 minutes
   - Severity: Warning
   - Description: Unusual AML activity

#### Infrastructure Alerts

1. **DiskSpaceLow**
   - Condition: Available disk < 10%
   - Duration: 5 minutes
   - Severity: Warning
   - Description: Running out of disk space

2. **PostgreSQLDown**
   - Condition: `pg_up == 0`
   - Duration: 1 minute
   - Severity: Critical
   - Description: Database is down

3. **RedisDown**
   - Condition: `redis_up == 0`
   - Duration: 1 minute
   - Severity: Critical
   - Description: Cache is down

4. **HighDatabaseConnections**
   - Condition: Active connections > 80
   - Duration: 5 minutes
   - Severity: Warning
   - Description: Too many database connections

### Alertmanager Configuration

**File**: `infrastructure/monitoring/prometheus/alertmanager.yml`

**Notification Channels** (configure in production):
- Email notifications
- Slack integration
- PagerDuty escalation
- Webhook callbacks

---

## Dashboards

### Accessing Dashboards

| Dashboard | URL | Description |
|-----------|-----|-------------|
| **Prometheus** | http://localhost:9090 | Metrics & alerts |
| **Grafana** | http://localhost:3000 | Visualizations |
| **Kibana** | http://localhost:5601 | Logs & analysis |
| **Jaeger** | http://localhost:16686 | Distributed traces |
| **Alertmanager** | http://localhost:9093 | Alert management |

### Default Credentials

**Grafana**:
- Username: `admin`
- Password: `admin123`

**Kibana**:
- Username: `elastic`
- Password: `changeme`

---

## Quick Start

### Start Monitoring Stack

```bash
# Navigate to monitoring directory
cd infrastructure/monitoring

# Start all monitoring services
docker-compose -f docker-compose.monitoring.yml up -d

# Check service status
docker-compose -f docker-compose.monitoring.yml ps

# View logs
docker-compose -f docker-compose.monitoring.yml logs -f
```

### Verify Services

```bash
# Check Prometheus targets
curl http://localhost:9090/api/v1/targets

# Check Elasticsearch health
curl http://localhost:9200/_cluster/health

# Check Grafana health
curl http://localhost:3000/api/health
```

### Access Dashboards

1. **Grafana**: http://localhost:3000
   - Login with admin/admin123
   - Navigate to Dashboards → Browse
   - Open "Service Overview" or "Business Metrics"

2. **Kibana**: http://localhost:5601
   - Configure index patterns (casino-logs-*)
   - Import dashboards from `elk/kibana/dashboards/`
   - Create visualizations

3. **Jaeger**: http://localhost:16686
   - Select service from dropdown
   - View recent traces
   - Analyze performance

### Configure Microservices

Add to `application.yml` of each service:

```yaml
# Prometheus metrics
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      application: ${spring.application.name}
      environment: production

# Logging to Logstash
logging:
  config: classpath:logback-spring.xml

# Add to logback-spring.xml:
<appender name="LOGSTASH" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
  <destination>logstash:5000</destination>
  <encoder class="net.logstash.logback.encoder.LogstashEncoder" />
</appender>

# Jaeger tracing
opentracing:
  jaeger:
    enabled: true
    service-name: ${spring.application.name}
    udp-sender:
      host: jaeger
      port: 6831
```

---

## Troubleshooting

### Prometheus Not Scraping Targets

**Symptoms**: Targets showing as "DOWN" in Prometheus

**Solutions**:
1. Check service endpoints are exposed:
   ```bash
   curl http://service-name:port/actuator/prometheus
   ```

2. Verify network connectivity:
   ```bash
   docker exec casino-prometheus ping service-name
   ```

3. Check Prometheus logs:
   ```bash
   docker logs casino-prometheus
   ```

### Elasticsearch Out of Memory

**Symptoms**: Elasticsearch crashes or slow performance

**Solutions**:
1. Increase heap size in `docker-compose.monitoring.yml`:
   ```yaml
   environment:
     - "ES_JAVA_OPTS=-Xms4g -Xmx4g"
   ```

2. Delete old indexes:
   ```bash
   curl -X DELETE "localhost:9200/casino-logs-*-2025.01.01"
   ```

3. Configure index lifecycle management

### Logstash Not Processing Logs

**Symptoms**: No logs appearing in Elasticsearch

**Solutions**:
1. Check Logstash pipeline:
   ```bash
   curl http://localhost:9600/_node/stats/pipelines
   ```

2. View Logstash logs:
   ```bash
   docker logs casino-logstash
   ```

3. Test configuration:
   ```bash
   docker exec casino-logstash bin/logstash --config.test_and_exit
   ```

### Grafana Dashboards Not Loading

**Symptoms**: Empty or broken dashboards

**Solutions**:
1. Verify Prometheus datasource is configured
2. Check query syntax in panels
3. Ensure metrics are being collected
4. Restart Grafana:
   ```bash
   docker restart casino-grafana
   ```

---

## Best Practices

### Metrics

1. **Use labels wisely**: Don't create high-cardinality labels
2. **Instrument critical paths**: Focus on user-facing operations
3. **Set appropriate retention**: Balance storage vs history needs
4. **Create SLO dashboards**: Track service level objectives

### Logging

1. **Use structured logging**: JSON format for better parsing
2. **Include context**: Request IDs, user IDs, transaction IDs
3. **Set appropriate log levels**: DEBUG for dev, INFO/WARN for prod
4. **Rotate logs**: Configure retention policies

### Tracing

1. **Sample intelligently**: 100% sampling for critical services
2. **Add custom tags**: Business context for better analysis
3. **Trace errors**: Always sample error traces
4. **Monitor overhead**: Tracing should be < 1% performance impact

### Alerts

1. **Actionable alerts**: Every alert should require action
2. **Avoid alert fatigue**: Tune thresholds carefully
3. **Document runbooks**: Include resolution steps
4. **Test alerts**: Regularly verify alerting works

---

## Production Checklist

- [ ] Enable Elasticsearch security
- [ ] Configure SSL/TLS for all services
- [ ] Set up backup for metrics and logs
- [ ] Configure external alert destinations (email, Slack, PagerDuty)
- [ ] Implement log retention policies
- [ ] Set up monitoring for the monitoring stack
- [ ] Configure high availability for critical components
- [ ] Document incident response procedures
- [ ] Set up capacity planning alerts
- [ ] Configure RBAC for dashboards

---

## Support

For issues or questions:
- Check logs: `docker-compose logs -f [service-name]`
- Review configuration files
- Consult component documentation

---

**Last Updated**: 2025-01-18
**Maintained By**: Platform Team
