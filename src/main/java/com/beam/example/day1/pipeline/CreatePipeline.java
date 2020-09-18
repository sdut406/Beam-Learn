package com.beam.example.day1.pipeline;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.runners.direct.DirectRunner;

/**
 * 1. 创建管道示例-Pipeline
 *
 * @since 2020 -07-20
 */
public class CreatePipeline {

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
            
    }


    /**
     * 第一种：创建默认管道
     */
    public void createPipelineOne() {
        // 创建pipeline属性
        PipelineOptions options = PipelineOptionsFactory.create();
        // 本地 options.setRunner(DirectRunner.class);
        // spark options.setRunner(SparkRunner.class);
        // flink options.setRunner(FlinkRunner.class);
        options.setRunner(DirectRunner.class);
        // 创建pipeline
        Pipeline pipeline = Pipeline.create(options);
    }

    
    /**
     * 第二种：创建带有额外参数的管道，通过运行jar的时候 添加 --<option>=<value>即可
     *
     * @param args the args
     */
    public void createPipelineTwo(String[] args) {
        // 创建pipeline属性-读取额外属性，这些额外的属性会被添加到DoFn中的@ProcessElement方法中
        // withValidation是用于检查必需的命令行参数并验证参数值
        PipelineOptions options = PipelineOptionsFactory.fromArgs(args).withValidation().create();
        options.setRunner(DirectRunner.class);
        // 创建pipeline
        Pipeline pipeline = Pipeline.create(options);
    }
    
    /**
     * 第三种：创建自定义选项的管道-移步官网参考 https://beam.apache.org/documentation/programming-guide/#configuring-pipeline-options
     */
     /*public void createPipelineThree(String[] args) {
         PipelineOptionsFactory.register(XXXX.class);
         XXXX options = PipelineOptionsFactory.fromArgs(args)
                 .withValidation()
                 .as(XXXX.class);
         Pipeline pipeline = Pipeline.create(options);
     }*/
}
