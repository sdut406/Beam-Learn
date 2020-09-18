package com.beam.example.day1.Pcollections;

import java.util.Arrays;
import java.util.List;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.SetCoder;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.values.PCollection;

/**
 * 2. 创建数据集
 *
 * @since 2020 -07-20
 */
public class CreatePCollections {

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        
    }

    /**
     * 第一种: 通过外部文件创建数据集，外部文件可以使本地文件，HDFS文件等
     */
    public void createPCollectionFile() {
        // 创建通道
        PipelineOptions options = PipelineOptionsFactory.create();
        Pipeline pipeline = Pipeline.create(options);
        // 创建数据集通过外部文件
        PCollection<String> readMyfilePCollection = pipeline.apply("ReadMyFile", TextIO.read().from("文件路径"));
        // TextIO就属于Beam-provided I/O adapters，不同的适配器有不同的用途
    }

    /**
     * 第二种: 通过内存创建数据集，内存不止java内存，也可以是从数据库读取等
     */
    public void createPCollectionMemory() {
        final List<String> lines = Arrays.asList(
                "To be, or not to be: that is the question: ",
                "Whether 'tis nobler in the mind to suffer ",
                "The slings and arrows of outrageous fortune, ",
                "Or to take arms against a sea of troubles, ");
        // 创建通道
        PipelineOptions options = PipelineOptionsFactory.create();
        Pipeline pipeline = Pipeline.create(options);
        // 创建数据集通过内存
        PCollection<String> stringPCollection = pipeline.apply(Create.of(lines)).setCoder(StringUtf8Coder.of());
        // 这里得Coder是为了数据类型序列化和反序列化，以便在网络上传输
        // 这个有点类似于指定序列化和反序列类，其实可以做到对于输入指定反序列化类，输出指定序列化类，中间其他算子全都走统一的数据抽象
    }

    /*
    PCollection特性
    1. 元素类型
        数据集中的元素可以是任何类型，但必须都属于同一类型。
    2. 元素架构
        在许多情况下，数据集中的元素类型具有可以自省的结构。
    3. 不变性
        PCollection是不可变的。
    4. 随机访问
        PCollection不支持随机访问单个元素。
    5. 大小和边界
        PCollection是一个大型的，不变的元素“袋”。a PCollection可以包含多少个元素没有上限。任何给定的数据都PCollection可能适合单个计算机上的内存，或者可能表示由持久性数据存储支持的非常大的分布式数据集。
    6. 元素时间戳
        数据集中的每个元素都有一个关联的固有时间戳记。
    注意：PCollection为固定数据集创建边界的源也会自动分配时间戳，但是最常见的行为是为每个元素分配相同的时间戳（Long.MIN_VALUE）。
    详情地址：https://beam.apache.org/documentation/programming-guide/#pcollection-characteristics
    */
}
