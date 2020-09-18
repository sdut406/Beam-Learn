package com.beam.example.code;

import org.apache.beam.runners.direct.DirectRunner;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.KvCoder;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.coders.VarIntCoder;
import org.apache.beam.sdk.coders.VoidCoder;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;

import java.util.Arrays;
import java.util.List;

/**
 * 功能描述
 *
 * @author dell
 * @since 2020-07-31
 */
public class DemoParDo {
    public static void main(String[] args) {
        PipelineOptions pipelineOptions = PipelineOptionsFactory.fromArgs(args).create();
        pipelineOptions.setRunner(DirectRunner.class);
        Pipeline pipeline = Pipeline.create(pipelineOptions);
        List<String> dataList = Arrays.asList(
                "To be, or not to be: that is the question: ",
                "Whether 'tis nobler in the mind to suffer ",
                "The slings and arrows of outrageous fortune, ",
                "Or to take arms against a sea of troubles, ");
        PCollection<String> pCollection = pipeline.apply(Create.of(dataList)).setCoder(StringUtf8Coder.of());

        /**
         * 获取每行数据分割后的第一个单词
         */
        PCollection<String> stringPcollection = pCollection.apply(ParDo.of(new DoFn<String, String>() {
            @ProcessElement
            public void processElement(@Element String element, OutputReceiver<String> out) {
                out.output(element.split(" ",-1)[0]);
            }
        }));
        /**
         * 打印foreachPcollection
         */
        PCollection<Void> foreachStringPcollection = stringPcollection.apply(ParDo.of(new DoFn<String, Void>() {
            @ProcessElement
            public void processElement(@Element String element, OutputReceiver<Void> out) {
                System.out.println(String.format("key:%s", element));
            }
        })).setCoder(VoidCoder.of());
        /**
         *  每行数据转为KV对，K为每行数据，V为数据的长度
         */
        PCollection<KV<String, Integer>> kvpCollection = pCollection.apply(ParDo.of(new DoFn<String, KV<String, Integer>>() {
            @ProcessElement
            public void processElement(@Element String element, OutputReceiver<KV<String,Integer>> out, ProcessContext context) {
                out.output(KV.of(element,element.length()));
            }
        })).setCoder(KvCoder.of(StringUtf8Coder.of(), VarIntCoder.of()));
        /**
         * 打印foreachPcollection
         */
        PCollection<Void> foreachKvpCollection = kvpCollection.apply(ParDo.of(new DoFn<KV<String, Integer>, Void>() {
            @ProcessElement
            public void processElement(@Element KV<String, Integer> element, OutputReceiver<Void> out) {
                System.out.println(String.format("key:%s,value:%s", element.getKey(), element.getValue()));
            }
        })).setCoder(VoidCoder.of());
        pipeline.run().waitUntilFinish();
    }
}
