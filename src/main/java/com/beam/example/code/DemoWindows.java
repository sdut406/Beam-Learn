package com.beam.example.code;


import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


import org.apache.beam.runners.direct.DirectRunner;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.Combine;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.Sample;
import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.GlobalWindows;
import org.apache.beam.sdk.transforms.windowing.Sessions;
import org.apache.beam.sdk.transforms.windowing.SlidingWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TimestampedValue;
import org.joda.time.Duration;
import org.joda.time.Instant;

/**
 * 测试流式数据的窗口功能
 *
 * @since 2020-09-17
 */
public class DemoWindows {
    private static long TIME_NOW = 1600332600000L; // 基本时间
    public static void main(String[] args) {
        PipelineOptions options = PipelineOptionsFactory.fromArgs(args).withValidation().create();
        options.setRunner(DirectRunner.class);
        Pipeline pipeline = Pipeline.create(options);
        // 伪造的流数据 时间戳是相对于TIME_NOW来说的
        PCollection<String> pCollection = pipeline.apply(Create.timestamped(
                TimestampedValue.of("xiaoming0.9s", new Instant(TIME_NOW + 999L)),
                TimestampedValue.of("xiaoming1.0s", new Instant(TIME_NOW + 1000L)),
                TimestampedValue.of("xiaoming1.5s", new Instant(TIME_NOW + 1500L)),
                TimestampedValue.of("xiaoming1.9s", new Instant(TIME_NOW + 1900L)),
                TimestampedValue.of("xiaoming2.0s", new Instant(TIME_NOW + 2000L)),
                TimestampedValue.of("xiaoming2.2s", new Instant(TIME_NOW + 2200L)),
                TimestampedValue.of("xiaoming3.0s", new Instant(TIME_NOW + 3000L)),
                TimestampedValue.of("xiaoming4.0s", new Instant(TIME_NOW + 4000L)),
                TimestampedValue.of("xiaoming8.0s", new Instant(TIME_NOW + 8000L))
        ).withCoder(StringUtf8Coder.of()));
        // 固定窗口
        //fixedWindows(pCollection);
        // 滑动窗口
        //slidingWindows(pCollection);
        // 会话窗口
        // sessionsWindows(pCollection);
        // 全局窗口
        globalWindows(pCollection);
        pipeline.run().waitUntilFinish();
    }

    /**
     * 固定窗口示例 2秒一个窗口，造的数据都是在时间戳1600332600000L上添加不定的秒数，分别为[0.9s,1.0s,1.5s,1.9s,2s,2.2s,3.0s,4.0s,8.0s]
     * 所以以2秒为一个固定窗口则，分为四个窗口，0.9,1,1.5,1.9为一个窗口，2,2.2,3为一个窗口，4为一个窗口，8为一个窗口
     * @param pCollection 
     * 输出结果：
     * [0,2)->[xiaoming1.9s, xiaoming0.9s, xiaoming1.5s, xiaoming1.0s]
     * ---------------------
     * [2,4)->[xiaoming3.0s, xiaoming2.0s, xiaoming2.2s]
     * ---------------------
     * [4,6)->[xiaoming4.0s]
     * ---------------------
     * [8,10)->[xiaoming8.0s]
     * ---------------------
     */
    public static void fixedWindows(PCollection<String> pCollection) {
        pCollection.apply(Window.into(FixedWindows.of(Duration.standardSeconds(2L))))
                .apply(Combine.globally(Sample.<String>anyCombineFn(100)).withoutDefaults())
                .apply(ParDo.of(new DoFn<Iterable<String>, Void>() {
                    @ProcessElement
                    public void processElement(@Element Iterable<String> iterable,OutputReceiver<Void> out, ProcessContext context) {
                        long millis = context.timestamp().getMillis();
                        System.out.print(String.format("[%d,%d)->", (millis+1 -TIME_NOW -2000L)/1000 , (millis+1 -TIME_NOW)/1000));
                        List<String> collect = StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toList());
                        System.out.println(collect);
                        System.out.println("---------------------");
                    }
                }));
    }
    
    /**
     * 滑动窗口示例 窗口总长3秒 每隔2秒生成一个窗口，造的数据都是在时间戳1600332600000L上添加不定的秒数，
     * 分别为[0.9s,1.0s,1.5s,1.9s,2s,2.2s,3.0s,4.0s,8.0s]
     * 则
     * 0.9,1,1.5,1.9,2,2.2属于[0,3)的窗口
     * 2,2.2,3,4属于[2,5)的窗口
     * 4属于[4,7)的窗口
     * 8属于[6,9)的窗口
     * 8也属于[8,11)的窗口
     * 不要忘了还有[-2,1)这个窗口，因为是滑动窗口，0.9属于这个窗口
     * 当窗口长度等于窗口启动间隔时间=固定窗口，注意启动时间间隔不能大于窗口长度时间
     * @param pCollection
     * [2,5)->[xiaoming2.0s, xiaoming2.2s, xiaoming3.0s, xiaoming4.0s]
     * ---------------------
     * [6,9)->[xiaoming8.0s]
     * ---------------------
     * [0,3)->[xiaoming2.0s, xiaoming0.9s, xiaoming2.2s, xiaoming1.5s, xiaoming1.0s, xiaoming1.9s]
     * ---------------------
     * [-2,1)->[xiaoming0.9s]
     * ---------------------
     * [8,11)->[xiaoming8.0s]
     * ---------------------
     * [4,7)->[xiaoming4.0s]
     * ---------------------
     */
    public static void slidingWindows (PCollection<String> pCollection) {
        pCollection.apply(Window.into(
                SlidingWindows.of(Duration.standardSeconds(3L))
                        .every(Duration.standardSeconds(2L))))
                .apply(Combine.globally(Sample.<String>anyCombineFn(100)).withoutDefaults())
                .apply(ParDo.of(new DoFn<Iterable<String>, Void>() {
                    @ProcessElement
                    public void processElement(@Element Iterable<String> iterable,ProcessContext context) {
                        long millis = context.timestamp().getMillis();
                        System.out.print(String.format("[%d,%d)->", (millis+1 -TIME_NOW -3000L)/1000 , (millis+1 -TIME_NOW)/1000));
                        List<String> collect = StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toList());
                        System.out.println(collect);
                        System.out.println("---------------------");
                    }
                }));
    }

    /**
     * 会话窗口，会话时间为2秒， 则如果有两个数据的时间超过2秒就会结束重新生成一个窗口，以第一个数据时间为起点
     * 第一个数据0.9开始，4结束，8和4超过了3 所以8在另一个窗口
     * 0.9，1.0,1.5,1.9,2.0,2.2,3,4, 这些数据没有超过3秒的会话 所以在一个窗口中
     * @param pCollection
     * group -> [xiaoming8.0s]
     * ---------------------
     * group -> [xiaoming0.9s, xiaoming1.5s, xiaoming3.0s, xiaoming1.9s, xiaoming1.0s, xiaoming2.2s, xiaoming2.0s, xiaoming4.0s]
     * ---------------------
     */
    public static void sessionsWindows (PCollection<String> pCollection) {
        pCollection.apply(Window.into(
                Sessions.withGapDuration(Duration.standardSeconds(2L))))
                .apply(Combine.globally(Sample.<String>anyCombineFn(100)).withoutDefaults())
                .apply(ParDo.of(new DoFn<Iterable<String>, Void>() {
                    @ProcessElement
                    public void processElement(@Element Iterable<String> iterable,ProcessContext context) {
                        System.out.print("group -> ");
                        List<String> collect = StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toList());
                        System.out.println(collect);
                        System.out.println("---------------------");
                    }
                }));
    }

    /**
     * 全局窗口，Beam默认使用的就是全局窗口
     * @param pCollection
     * group -> [xiaoming8.0s, xiaoming1.5s, xiaoming4.0s, xiaoming0.9s, xiaoming1.9s, xiaoming2.2s, xiaoming2.0s, xiaoming1.0s, xiaoming3.0s]
     * ---------------------
     */
    public static void globalWindows (PCollection<String> pCollection) {
        pCollection.apply(Window.into(
                new GlobalWindows()))
                .apply(Combine.globally(Sample.<String>anyCombineFn(100)).withoutDefaults())
                .apply(ParDo.of(new DoFn<Iterable<String>, Void>() {
                    @ProcessElement
                    public void processElement(@Element Iterable<String> iterable,ProcessContext context) {
                        System.out.print("group -> ");
                        List<String> collect = StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toList());
                        System.out.println(collect);
                        System.out.println("---------------------");
                    }
                }));
    }
}
