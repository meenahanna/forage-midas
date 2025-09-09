package com.jpmc.midascore.config;
import com.jpmc.midascore.foundation.Transaction;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import java.util.HashMap;
import java.util.Map;
@Configuration
@EnableKafka
@Profile("kafka")

public class KafkaConsumerConfig {
    @Bean
    public ConsumerFactory<String, Transaction> tranConsumerFactory(
        @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers
    ) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "midas-core");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        JsonDeserializer<Transaction> JsonDeserializer = new JsonDeserializer<>(Transaction.class);
        JsonDeserializer.addTrustedPackages("com.jpmc.*");

        return new DefaultKafkaConsumerFactory<>(
            props,
            new StringDeserializer(),
            JsonDeserializer
        );
    }

    @Bean(name = "transactionKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, Transaction> transactiConcurrentKafkaListenerContainerFactory(
        ConsumerFactory<String, Transaction> transactionConsumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, Transaction> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(transactionConsumerFactory);
        return factory;
    }

}
