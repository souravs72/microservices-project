package com.microservices.auth.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaHealthIndicator implements HealthIndicator {

    private final KafkaAdmin kafkaAdmin;

    @Override
    public Health health() {
        try (AdminClient adminClient = createAdminClient()) {
            DescribeClusterResult clusterResult = adminClient.describeCluster();
            
            // Get cluster ID and controller
            String clusterId = clusterResult.clusterId().get(5, TimeUnit.SECONDS);
            String controller = clusterResult.controller().get(5, TimeUnit.SECONDS).toString();
            
            return Health.up()
                    .withDetail("status", "Kafka is available")
                    .withDetail("clusterId", clusterId)
                    .withDetail("controller", controller)
                    .build();
        } catch (Exception e) {
            log.error("Kafka health check failed", e);
            return Health.down()
                    .withDetail("status", "Kafka is not available")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }

    private AdminClient createAdminClient() {
        Properties props = new Properties();
        props.putAll(kafkaAdmin.getConfigurationProperties());
        return AdminClient.create(props);
    }
}
