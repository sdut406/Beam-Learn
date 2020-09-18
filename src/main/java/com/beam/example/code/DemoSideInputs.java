package com.beam.example.code;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.Max;
import org.apache.beam.sdk.transforms.Min;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionView;

import java.util.Arrays;
import java.util.List;

/**
 * 功能描述
 *
 * @since 2020-08-05
 */
public class DemoSideInputs {
    public static void main(String[] args) {
        List<String> wordsList = Arrays.asList("amy", "carl", "julia","cl");
        List<Integer> lengthList = Arrays.asList(4,5,6,43,8,9);
        // 创建流水线
        PipelineOptions options = PipelineOptionsFactory.fromArgs(args).create();
        Pipeline pipeline = Pipeline.create(options);
        // 创建数据集
        PCollection<String> words = pipeline.apply(Create.of(wordsList));
        PCollection<Integer> wordLengths = pipeline.apply(Create.of(lengthList));
        // 创建侧面输入
        PCollectionView<Integer> min = wordLengths.apply(Min.integersGlobally().asSingletonView());
        PCollectionView<Integer> max = wordLengths.apply(Max.integersGlobally().asSingletonView());
        // 使用侧面输入，可以传入多个侧面输入
        PCollection<String> stringPCollection = words.apply(ParDo.of(new DoFn<String, String>() {
            @ProcessElement
            public void processElement(@Element String element, OutputReceiver<String> out, ProcessContext context) {
                // 获取侧面输入
                Integer lengthCutOff = context.sideInput(min);
                if (element.length() <= lengthCutOff) {
                    out.output(element);
                }
            }
        }).withSideInputs(max,min));
        // 输出结果
        stringPCollection.apply(ParDo.of(new DoFn<String, Void>() {
            @ProcessElement
            public void processElement(@Element String element, OutputReceiver<Void> out) {
                System.out.println(element);
            }
        }));
        pipeline.run().waitUntilFinish();
    }
}
