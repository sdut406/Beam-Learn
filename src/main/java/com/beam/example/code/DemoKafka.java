package com.beam.example.code;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.kafka.KafkaIO;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;

import org.apache.beam.sdk.transforms.Combine;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.Sample;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.transforms.Values;
import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 功能描述
 *
 * @since 2020-09-18
 */
public class DemoKafka {
    private static long TIME_NOW = 1600332600000L; // 基本时间
    private Logger logger = LoggerFactory.getLogger(DemoKafka.class);
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
                });
        PCollection<String> strPc = pipeline.apply(readKafka.withoutMetadata())
                .apply(Values.create())
                .apply(ParDo.of(new DoFn<String, String>() {
                    @ProcessElement
                    public void processElement(@Element String s, OutputReceiver<String> out, ProcessContext context) {
                        System.out.println(s + (context.timestamp().getMillis() - TIME_NOW)/1000 + "");
                        out.output(s);
                    }
                }));
        PCollection<String> strPcWindows = strPc.apply(Window.<String>into(FixedWindows.of(Duration.standardSeconds(3L)))
                .withAllowedLateness(Duration.standardSeconds(5)).accumulatingFiredPanes());
        strPcWindows.apply(Combine.globally(Sample.<String>anyCombineFn(100)).withoutDefaults())
                .apply(ParDo.of(new DoFn<Iterable<String>, Void>() {
                    @ProcessElement
                    public void processElement(@Element Iterable<String> iterable, ProcessContext context) {
                        List<String> collect = StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toList());
                        System.out.println("Group:" + collect + (context.timestamp().getMillis() - TIME_NOW)/1000 + "");
                    }
                }));
        pipeline.run().waitUntilFinish();
    }
}
