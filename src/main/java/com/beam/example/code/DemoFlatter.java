package com.beam.example.code;

import java.util.Arrays;
import java.util.List;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.Flatten;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionList;

/**
 * 功能描述
 *
 * @since 2020-08-05
 */
public class DemoFlatter {
    public static void main(String[] args) {
        List<KV<String, Integer>> list1 = Arrays.asList(
                KV.of("cat",2),
                KV.of("dog",2),
                KV.of("cat",1),
                KV.of("dog",6)
        );
        List<KV<String, Integer>> list2 = Arrays.asList(
                KV.of("dog",6),
                KV.of("jump",3),
                KV.of("tree",8)
        );
        // 创建 beam 流水线
        PipelineOptions options = PipelineOptionsFactory.fromArgs(args).create();
        Pipeline pipeline = Pipeline.create(options);
        // 创建数据集
        PCollection<KV<String, Integer>> pCollection1 = pipeline.apply(Create.of(list1));
        PCollection<KV<String, Integer>> pCollection2 = pipeline.apply(Create.of(list2));
        // 展平数据
        PCollection<KV<String, Integer>> kvpCollection = PCollectionList.of(pCollection1).and(pCollection2).apply(Flatten.pCollections());
        // 输出
        kvpCollection.apply(ParDo.of(new DoFn<KV<String, Integer>, Void>() {
            @ProcessElement
            public void processElement(@Element KV<String, Integer> element, OutputReceiver<Void> out) {
                System.out.println(element.toString());
            }
        }));
        pipeline.run().waitUntilFinish();
    }
}
