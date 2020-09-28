package com.beam.example.code;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.kafka.KafkaIO;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.transforms.windowing.AfterWatermark;
import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.KV;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.joda.time.Duration;
import org.joda.time.Instant;

/**
 * 触发器demo
 *
 * @since 2020-09-18
 */
public class DemoTrigger {
    private static long TIME_NOW = 1600332600000L; // 基本时间
    public static void main(String[] args) {
        PipelineOptions options = PipelineOptionsFactory.fromArgs(args).withValidation().withoutStrictParsing().create();
        Pipeline pipeline = Pipeline.create(options);
        KafkaIO.Read<String, String> readKafka = KafkaIO.<String, String>read().withBootstrapServers("127.0.0.1:9092").withTopic("BeamTest")
                .withKeyDeserializer(StringDeserializer.class)
                .withValueDeserializer(StringDeserializer.class)
                .withTimestampFn(new SerializableFunction<KV<String, String>, Instant>() {
                    @Override
                    public Instant apply(KV<String, String> input) {
                        return new Instant(TIME_NOW + Integer.parseInt(input.getValue()));
                    }
                }).withWatermarkFn(new SerializableFunction<KV<String, String>, Instant>() {
                    @Override
                    public Instant apply(KV<String, String> input) {
                        return null;
                    }
                });
        /*pipeline.apply(readKafka.withoutMetadata()).apply(Window.into(FixedWindows.of(Duration.standardSeconds(3)))
                .withAllowedLateness(Duration.standardSeconds(5)).discardingFiredPanes()
                .triggering(AfterWatermark.pastEndOfWindow().withLateFirings()))*/
        
    }
}
