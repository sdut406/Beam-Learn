package com.beam.example.code;

import java.util.Arrays;
import java.util.List;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.coders.VarIntCoder;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionTuple;
import org.apache.beam.sdk.values.TupleTag;
import org.apache.beam.sdk.values.TupleTagList;

/**
 * 功能描述
 *
 * @since 2020-08-06
 */
public class DemoAdditionalOutputs {
    public static void main(String[] args) {
        List<String> list = Arrays.asList("helloasdasd","world","yeasdasds","am","lovasdase","hoasdme","hello","love","MARKERas");
        
        // 创建流水线
        PipelineOptions options = PipelineOptionsFactory.fromArgs(args).withValidation().create();
        Pipeline pipeline = Pipeline.create(options);
        // 创建数据集
        PCollection<String> words = pipeline.apply(Create.of(list)).setCoder(StringUtf8Coder.of());
        
        final int wordLengthCutOff = 10;
        // 必须将TupleTag声明为匿名类（构造函数调用后缀{}）。否则，编码器的推断会出现问题。
        TupleTag<String> wordsBelowCutOffTag = new TupleTag<String>(){};
        TupleTag<String> wordLengthsAboveCutOffTag = new TupleTag<String>(){};
        TupleTag<String> markedWordsTag = new TupleTag<String>(){};
        
        // 实现多路输出
        PCollectionTuple results = words.apply(ParDo.of(new DoFn<String, String>() {
            
            @ProcessElement
            public void processElement(@Element String word, ProcessContext c) {
                if (word.length() <= wordLengthCutOff) {
                    // 将长度较短的单词发送到主输出
                    // 在本例中，是wordsBelowCutOffTag代表的输出
                    c.output(word);
                } else {
                    // 将长度较长的单词发送到 wordLengthsAboveCutOffTag代表的输出中.
                    c.output(wordLengthsAboveCutOffTag, word);
                }
                if (word.startsWith("MARKER")) {
                    // 将以MARKER为开头的单词发送到markedWordsTag的输出中
                    c.output(markedWordsTag, word);
                }
            }
        }).withOutputTags(wordsBelowCutOffTag, TupleTagList.of(wordLengthsAboveCutOffTag).and(markedWordsTag)));
        // 获取其中一路输出
        PCollection<String> stringPCollection = results.get(wordLengthsAboveCutOffTag);
        // 打印结果
        stringPCollection.apply(ParDo.of(new DoFn<String, Void>() {
            @ProcessElement
            public void processElement(@Element String element, OutputReceiver<Void> out) {
                System.out.println(element);
            }
        }));
        pipeline.run().waitUntilFinish();
    }
}
