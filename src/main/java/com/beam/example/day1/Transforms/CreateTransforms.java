package com.beam.example.day1.Transforms;

import java.util.Arrays;
import java.util.List;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.state.Timer;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.Filter;
import org.apache.beam.sdk.transforms.Flatten;
import org.apache.beam.sdk.transforms.GroupByKey;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.transforms.display.DisplayData;
import org.apache.beam.sdk.transforms.join.CoGroupByKey;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.POutput;

/**
 * 3.转换是您的管道中的操作，并提供通用的 处理框架。 您以函数的形式提供处理逻辑 对象（俗称“用户代码”），并且您的用户代码被应用 输入“PCollection”（或多个“PCollection”）的 每个元素。 根据您选择的管道运行器和后端，有很多不同 群集中的员工可以并行执行用户代码的实例。 在每个工作人员上运行的用户代码生成输出元素 最终添加到转换产生的最终输出“PCollection”中。
 * 转换中包含了预编写好的复杂转换，也可以自定义转换
 * 转换的使用方式： [Output PCollection] = [Input PCollection].apply([Transform])
 * 通道中的转换：[Final Output PCollection] = [Initial Input PCollection].apply([First Transform])
 * .apply([Second Transform])
 * .apply([Third Transform])
 * <p>
 * 但是，请注意，转换不会占用或改变输入集合-请记住，a PCollection定义是不可变的。
 * [PCollection of database table rows] = [Database Table Reader].apply([Read Transform])
 * [PCollection of 'A' names] = [PCollection of database table rows].apply([Transform A])
 * [PCollection of 'B' names] = [PCollection of database table rows].apply([Transform B])
 * 详情地址：https://beam.apache.org/documentation/programming-guide/#transforms
 *
 * @since 2020 -07-20
 */
public class CreateTransforms {
    
    
     /*
     Beam中的核心转换：
        ParDo
        GroupByKey
        CoGroupByKey
        Combine
        Flatten
        Partition
      */
    /**
     * ParDo是用于通用并行处理的Beam转换。“ParDo” 处理范例类似于Map / Shuffle / Reduce风格的“Map”阶段 算法,
     * “ParDo”转换考虑了输入中的每个元素 PCollection，执行一些处理功能（你的用户代码） 元素，并向输出“PCollection”发出零个，一个或多个元素。
     * ParDo可用于各种常见的数据处理操作:
     *      过滤数据集. 
     *      格式化或类型转换数据集中的每个元素. 
     *      提取数据集中每个元素的部分. 
     *      对数据集中的每个元素执行计算.
     * 应用ParDo转换时，需要以DoFn对象的形式提供用户代码。DoFn是Beam SDK类，用于定义分布式处理功能。
     */
    public void parDoDemo() {
        List<String> list = Arrays.asList(
                "To be, or not to be: that is the question: ",
                "Whether 'tis nobler in the mind to suffer ",
                "The slings and arrows of outrageous fortune, ",
                "Or to take arms against a sea of troubles, ");
        PipelineOptions options = PipelineOptionsFactory.create();
        Pipeline pipeline = Pipeline.create(options);
        
        // 创建数据集
        PCollection<String> stringPCollection = pipeline.apply(Create.of(list)).setCoder(StringUtf8Coder.of());
        
        // 数据转换-返回单个
        PCollection<Integer> pCollection = stringPCollection.apply("transformName",ParDo.of(new DoFn<String, Integer>() {
            // 这个是Dofn的功能处理函数
            @ProcessElement
            public void processElement(@Element String element, OutputReceiver<Integer> out,ProcessContext context) {
                out.output(element.length());
            }
        }));
    }

    /**
     * Group by key demo.
     */
    public void groupByKeyDemo() {
        
    }

    /**
     * Co group by key demo.
     */
    public void coGroupByKeyDemo() {

    }

    /**
     * Combine demo.
     */
    public void combineDemo() {

    }

    /**
     * Flatten demo.
     */
    public void flattenDemo() {

    }

    /**
     * Partition demo.
     */
    public void partitionDemo() {

    }
    
}
