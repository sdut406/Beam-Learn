package com.beam.example.code;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.Sum;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;

import java.util.Arrays;
import java.util.List;

/**
 * 功能描述
 *
 * @since 2020-08-05
 */
public class DemoCombinePerKey {
    public static void main(String[] args) {
        List<KV<String, Integer>> list1 = Arrays.asList(
                KV.of("cat",2),
                KV.of("dog",2),
                KV.of("cat",1),
                KV.of("dog",6),
                KV.of("cat",3),
                KV.of("dog",8),
                KV.of("jump",2),
                KV.of("dog",2),
                KV.of("jump",1),
                KV.of("dog",6),
                KV.of("jump",3),
                KV.of("tree",8)
        );
        // 创建beam 流水线
        PipelineOptions options = PipelineOptionsFactory.fromArgs(args).withValidation().create();
        Pipeline pipeline = Pipeline.create(options);
        // 创建数据集
        PCollection<KV<String, Integer>> pCollection = pipeline.apply(Create.of(list1));
        // 分组求和
        PCollection<KV<String, Integer>> countByKeypCollection = pCollection.apply(Sum.integersPerKey());
        // 打印输出
        countByKeypCollection.apply(ParDo.of(new DoFn<KV<String, Integer>, Void>() {
            @ProcessElement
            public void processElement(@Element KV<String, Integer> element, OutputReceiver<Void> out) {
                System.out.println(element.toString());
            }
        }));
        pipeline.run().waitUntilFinish();
    }
}
