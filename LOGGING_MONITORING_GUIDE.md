# Production Logging & Monitoring - Quick Reference

## 🚀 Server Running on Port 8080

### Essential Endpoints

```bash
# Health Check
curl http://localhost:8080/actuator/health

# Application Info
curl http://localhost:8080/actuator/info

# Metrics List
curl http://localhost:8080/actuator/metrics

# Prometheus Metrics
curl http://localhost:8080/actuator/prometheus

# Test Logging
curl http://localhost:8080/api/v1/test/logging

# Test Error Handling
curl http://localhost:8080/api/v1/test/error/not-found
curl http://localhost:8080/api/v1/test/error/business
```

## 📁 Log Files Location

```
logs/
├── application.log          # Standard logs
├── application-json.log     # JSON structured logs
└── application-error.log    # Error-only logs
```

## 🔍 View Logs in Real-Time

```bash
# Tail all logs
tail -f logs/application.log

# Tail errors only
tail -f logs/application-error.log

# View JSON logs
tail -f logs/application-json.log | jq .
```

## 📊 Key Features

✅ **Logging**
- Multiple log levels (TRACE, DEBUG, INFO, WARN, ERROR)
- File rotation (10MB max, 30 days retention)
- Async processing for performance
- Structured JSON logs for analysis

✅ **Error Handling**
- Standardized error responses
- Request ID for tracking
- Stack traces in error logs
- Custom exception types

✅ **Monitoring**
- Health checks (database, disk space, custom)
- Application metrics
- Prometheus integration
- Request/response tracking

✅ **Request Logging**
- Unique request ID per request
- Duration tracking
- Header logging (sensitive headers filtered)
- Response status tracking

## 🛠️ Useful Commands

```bash
# Check specific metric
curl http://localhost:8080/actuator/metrics/jvm.memory.used | jq .

# View logger levels
curl http://localhost:8080/actuator/loggers | jq .

# Change log level at runtime
curl -X POST http://localhost:8080/actuator/loggers/com.upsc.ai \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel": "DEBUG"}'

# Check environment properties
curl http://localhost:8080/actuator/env | jq .
```

## 📈 Production Checklist

- [ ] Set up centralized logging (ELK, Splunk, CloudWatch)
- [ ] Configure Prometheus scraping
- [ ] Set up alerting (PagerDuty, Opsgenie)
- [ ] Secure actuator endpoints
- [ ] Configure log retention policies
- [ ] Set up log rotation monitoring
- [ ] Create monitoring dashboards
- [ ] Configure error rate alerts

## 🔐 Security Notes

Current configuration allows public access to:
- `/actuator/**` - For monitoring
- `/api/v1/health` - Health check
- `/api/v1/test/**` - Test endpoints (remove in production!)
- `/h2-console/**` - Database console (remove in production!)

**For production**: Update `SecurityConfig.java` to restrict access.

## 📝 Log Levels

| Level | Usage | Example |
|-------|-------|---------|
| TRACE | Very detailed debugging | Method entry/exit |
| DEBUG | Debugging information | Variable values, flow |
| INFO | General information | Request received, processing |
| WARN | Warning messages | Deprecated usage, recoverable errors |
| ERROR | Error messages | Exceptions, failures |

## 🎯 Common Tasks

### View Request Flow
1. Make a request and note the Request ID from response header `X-Request-ID`
2. Search logs for that Request ID to see full flow:
   ```bash
   grep "604c6c77-5fd1-4253-bc68-4f90e94d8106" logs/application.log
   ```

### Monitor Error Rate
```bash
# Count errors in last hour
grep "ERROR" logs/application-error.log | grep "$(date +%Y-%m-%d\ %H)" | wc -l
```

### Check Application Health
```bash
# Simple health check
curl -s http://localhost:8080/actuator/health | jq -r '.status'
```

### Export Metrics for Analysis
```bash
# Save current metrics
curl -s http://localhost:8080/actuator/metrics | jq . > metrics-$(date +%Y%m%d-%H%M%S).json
```

## 🐛 Troubleshooting

### Logs not appearing?
- Check `logs/` directory exists and is writable
- Verify log level in `application.yml`
- Check Logback configuration in `logback-spring.xml`

### Actuator endpoints not accessible?
- Verify `management.endpoints.web.exposure.include` in `application.yml`
- Check security configuration in `SecurityConfig.java`

### High disk usage from logs?
- Adjust `maxFileSize` and `totalSizeCap` in `logback-spring.xml`
- Reduce log retention with `maxHistory`
- Consider log compression

## 📚 Documentation

- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Logback Configuration](https://logback.qos.ch/manual/configuration.html)
- [Micrometer Metrics](https://micrometer.io/docs)
- [Prometheus](https://prometheus.io/docs/introduction/overview/)
