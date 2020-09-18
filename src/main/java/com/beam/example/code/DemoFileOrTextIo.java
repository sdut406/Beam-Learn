package com.beam.example.code;

import org.apache.beam.runners.direct.DirectRunner;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.Flatten;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionList;

/**
 * 功能描述
 *
 * @since 2020-08-07
 */
public class DemoFileOrTextIo {

    public static void main(String[] args) {
        // withoutStrictParsing 会跳过未知或者格式不正确的参数
        PipelineOptions options = PipelineOptionsFactory.fromArgs(args).withValidation().withoutStrictParsing().create();
        options.setRunner(DirectRunner.class);
        Pipeline pipeline = Pipeline.create(options);
        // 读取第一个文件 Create.empty(StringUtf8Coder.of())
        PCollection<String> lines1 = pipeline.apply(TextIO.read().from("logs/demo.txt"));
        // 读取第二个文件
        PCollection<String> lines2 = pipeline.apply(TextIO.read().from("logs/demo2.txt"));
        // 合并文件
        PCollection<String>  pCollection = PCollectionList.of(lines1).and(lines2).apply(Flatten.pCollections());

        // 单个输出 .withoutSharding() 会把结果聚合在一起输出
        // pCollection.apply(TextIO.write().to("logs/out.txt").withoutSharding());
        // 多个输出 
        // 输出格式 out.txt-00000-of-00003
        pCollection.apply(TextIO.write().to("logs/out.txt")); 
        // 输出格式 out-00000-of-00003.txt 这种是指明后缀
        pCollection.apply(TextIO.write().to("logs/out1").withSuffix(".txt"));
        pipeline.run().waitUntilFinish();
    }
}