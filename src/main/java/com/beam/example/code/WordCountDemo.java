package com.beam.example.code;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.values.PCollection;

/**
 * 功能描述
 *
 * @since 2020-08-14
 */
public class WordCountDemo {
    public static void main(String[] args) {
        PipelineOptions options = PipelineOptionsFactory.fromArgs(args).withValidation().withValidation().create();

        Pipeline pipeline = Pipeline.create(options);

        // PCollection<String> readPCollection = pipeline.apply(TextIO.read().from(""));
    }
}
