package com.beam.example.code;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.Partition;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionList;

import java.util.Arrays;
import java.util.List;

/**
 * 功能描述
 *
 * @since 2020-08-05
 */
public class DemoPartition {
    public static void main(String[] args) {
        List<Integer> list1 = Arrays.asList(
                12,123,3,4324,345,123,31,435,345,34,53,12,
                1,231,43,34,53,5,34,534,12,312,3,12,31,2323,
                42,1,31,234,23,12,12,16,78,67,86,78,67,867,867,
                8,4,6,4,89,789,78,745,5,34,67,86,7,4,5,4,876,6,
                354,55,6,57,45,23
        );
        // 创建beam流水线
        PipelineOptions options = PipelineOptionsFactory.fromArgs(args).create();
        Pipeline pipeline = Pipeline.create(options);
        // 创建数据集
        PCollection<Integer> pCollection = pipeline.apply(Create.of(list1));
        // 数据集分区
        PCollectionList<Integer> pCollectionList = pCollection.apply(Partition.of(10, new Partition.PartitionFn<Integer>() {
            @Override
            public int partitionFor(Integer elem, int numPartitions) {
                return elem % numPartitions;
            }
        }));
        // 分区个数已经确定了，所以这里可以直接查询得到
        System.out.println("分区个数：" + pCollectionList.getAll().size());
        // 获取第二个分区
        PCollection<Integer> integerPCollection = pCollectionList.get(2);
        integerPCollection.apply(ParDo.of(new DoFn<Integer, Void>() {
            @ProcessElement
            public void processElement(@Element Integer integer, OutputReceiver<Void> out) {
                System.out.println(integer);
            }
        }));
        // 这一步要最后执行
        pipeline.run().waitUntilFinish();
    }
}
