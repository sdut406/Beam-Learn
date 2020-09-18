package com.beam.example.code;

import org.apache.beam.runners.direct.DirectRunner;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.GroupByKey;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.SimpleFunction;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;

import java.util.Arrays;
import java.util.List;

/**
 * 功能描述
 *
 * @since 2020-08-03
 */
public class DemoGroupBykey {
    public static void main(String[] args) {
        // 模拟数据
        List<String> list = Arrays.asList("hello","world","yes","am","love","home","hello","love");
        // 创建pipeline参数
        PipelineOptions options = PipelineOptionsFactory.fromArgs(args).create();
        options.setRunner(DirectRunner.class);
        // 创建pipeline
        Pipeline pipeline = Pipeline.create(options);
        // 创建数据集
        PCollection<String> stringPCollection = pipeline.apply(Create.of(list)).setCoder(StringUtf8Coder.of());
        // 数据转换
        PCollection<KV<String, Integer>> kvpCollection = stringPCollection.apply(MapElements.via(new SimpleFunction<String, KV<String, Integer>>() {
            @Override
            public KV<String, Integer> apply(String input) {
                return KV.of(input,1);
            }
        }));
        // 根据key分组
        PCollection<KV<String, Iterable<Integer>>> groupCollection = kvpCollection.apply(GroupByKey.create());
        // 打印输出
        groupCollection.apply(ParDo.of(new DoFn<KV<String, Iterable<Integer>>, Void>() {
            @ProcessElement
            public void processElement(@Element KV<String, Iterable<Integer>> element, OutputReceiver<Void> out) {
                System.out.println(element.toString());
            }
        }));
        // 运行
        pipeline.run().waitUntilFinish();
        /*
        KV{am, [1]}
        KV{love, [1, 1]}
        KV{world, [1]}
        KV{yes, [1]}
        KV{hello, [1, 1]}
        KV{home, [1]}
        */
    }
}
