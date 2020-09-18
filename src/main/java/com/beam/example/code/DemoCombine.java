package com.beam.example.code;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.DefaultCoder;
import org.apache.beam.sdk.coders.VarIntCoder;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.Combine;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.transforms.Sum;
import org.apache.beam.sdk.values.PCollection;

import java.util.Arrays;
import java.util.List;

/**
 * 功能描述
 *
 * @since 2020-08-04
 */
public class DemoCombine {
    public static void main(String[] args) {

        List<Integer> list = Arrays.asList(2,1,4,2,6,7,1);
        PipelineOptions options = PipelineOptionsFactory.fromArgs(args).withValidation().create();
        Pipeline pipeline = Pipeline.create(options);
        PCollection<Integer> integerpCollection = pipeline.apply(Create.of(list)).setCoder(VarIntCoder.of());
        // 第一种方式 Combine.globally(SerializableFunction类)
        PCollection<Integer> countpCollection = integerpCollection.apply(Combine.globally(new SumInts()));
        // 第二种方式 用beam提供的sdk
        // Sum.integersGlobally() 等同于 Combine.globally(Sum.ofIntegers())
        PCollection<Integer> countpCollection2 = integerpCollection.apply(Combine.globally(Sum.ofIntegers()));
        // 第三种方式 自定义Combine.CombineFn
        PCollection<Integer> countpCollection3 = integerpCollection.apply(Combine.globally(new SumIntt())).setCoder(VarIntCoder.of());
        
        
        countpCollection.apply(ParDo.of(new DoFn<Integer, Void>() {
            @ProcessElement
            public void processElement(@Element Integer integer, OutputReceiver<Void> out) {
                System.out.println(integer);
            }
        }));

        countpCollection2.apply(ParDo.of(new DoFn<Integer, Void>() {
            @ProcessElement
            public void processElement(@Element Integer integer, OutputReceiver<Void> out) {
                System.out.println(integer);
            }
        }));
        countpCollection3.apply(ParDo.of(new DoFn<Integer, Void>() {
            @ProcessElement
            public void processElement(@Element Integer integer) {
                System.out.println(integer);
            }
        }));
        
        pipeline.run().waitUntilFinish();
    }

    /**
     * 第一种方式 Combine.globally(SerializableFunction类)，实现SerializableFunction接口 实现自己的apply方法
     */
    public static class SumInts implements SerializableFunction<Iterable<Integer>,Integer> {

        @Override
        public Integer apply(Iterable<Integer> input) {
            int sum = 0;
            for (int  item : input) {
                sum += item; 
            }
            return sum;
        }
    }

    /**
     * 第三种方法 自定义Combine.CombineFn，继承Combine.CombineFn
     */
    public static class SumIntt extends Combine.CombineFn<Integer, SumIntt.Accum, Integer> {
        // 管道程序定义了自定义数据类型，则可以使用 @DefaultCoder注释来指定与该类型一起使用的编码器
        @DefaultCoder(AvroCoder.class)
        public static class Accum {
            int sum = 0;
        }
        @Override
        public Accum createAccumulator() {
            return new Accum();
        }

        @Override
        public Accum addInput(Accum mutableAccumulator, Integer input) {
            mutableAccumulator.sum += input;
            return mutableAccumulator;
        }

        @Override
        public Accum mergeAccumulators(Iterable<Accum> accumulators) {
            Accum merged = createAccumulator();
            for (Accum accumulator : accumulators) {
                merged.sum += accumulator.sum;
            }
            return merged;
        }

        @Override
        public Integer extractOutput(Accum accumulator) {
            return accumulator.sum;
        }
    }
}
