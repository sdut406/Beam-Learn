package com.beam.example.code;

import java.util.Arrays;
import java.util.List;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.KvCoder;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.join.CoGbkResult;
import org.apache.beam.sdk.transforms.join.CoGroupByKey;
import org.apache.beam.sdk.transforms.join.KeyedPCollectionTuple;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TupleTag;

/**
 * 功能描述
 *
 * @since 2020-08-03
 */
public class DemoCoGroupByKey {
    public static void main(String[] args) {
        // 模拟数据
        final List<KV<String, String>> emailsList =
                Arrays.asList(
                        KV.of("amy", "amy@example.com"),
                        KV.of("carl", "carl@example.com"),
                        KV.of("julia", "julia@example.com"),
                        KV.of("carl", "carl@email.com"));

        final List<KV<String, String>> phonesList =
                Arrays.asList(
                        KV.of("amy", "111-222-3333"),
                        KV.of("james", "222-333-4444"),
                        KV.of("amy", "333-444-5555"),
                        KV.of("carl", "444-555-6666"));
        // pipeline属性
        PipelineOptions options = PipelineOptionsFactory.fromArgs(args).withValidation().create();
        // 创建pipeline
        Pipeline pipeline = Pipeline.create(options);
        // 创建两种数据集
        PCollection<KV<String, String>> emails = pipeline.apply(Create.of(emailsList)).setCoder(KvCoder.of(StringUtf8Coder.of(), StringUtf8Coder.of()));
        PCollection<KV<String, String>> phones = pipeline.apply(Create.of(phonesList)).setCoder(KvCoder.of(StringUtf8Coder.of(), StringUtf8Coder.of()));
        // 创建两种元祖标签
        final TupleTag<String> emailsTag = new TupleTag<>();
        final TupleTag<String> phonesTag = new TupleTag<>();
        // KeyedPCollectionTuple 合并连个数据集 ，CoGroupByKey 的创建
        PCollection<KV<String, CoGbkResult>> results  = KeyedPCollectionTuple.of(emailsTag, emails).and(phonesTag, phones).apply(CoGroupByKey.create());
        // 打印数据
        results.apply(ParDo.of(new DoFn<KV<String, CoGbkResult>, String>() {
            StringBuilder sb = new StringBuilder();
            @ProcessElement
            public void processElement(@Element KV<String, CoGbkResult> element, OutputReceiver<String> out) {
                sb.setLength(0);
                String name = element.getKey();
                Iterable<String> emailsIter = element.getValue().getAll(emailsTag);
                emailsIter.forEach(s -> {
                    sb.append("," + s);
                });
                Iterable<String> phonesIter = element.getValue().getAll(phonesTag);
                phonesIter.forEach(s -> {
                    sb.append("," + s);
                });
                String only = element.getValue().getOnly(phonesTag);
                System.out.println(only);
                System.out.println(String.format("{k:%s,v:%s}",name,sb.toString()));
            }
        }));
        pipeline.run().waitUntilFinish();
    }
}
