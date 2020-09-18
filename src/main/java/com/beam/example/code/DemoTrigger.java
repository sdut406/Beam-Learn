package com.beam.example.code;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;

/**
 * 触发器demo
 *
 * @since 2020-09-18
 */
public class DemoTrigger {
    public static void main(String[] args) {
        PipelineOptions options = PipelineOptionsFactory.fromArgs(args).withValidation().withoutStrictParsing().create();
        Pipeline pipeline = Pipeline.create(options);
        
        
    }
}
