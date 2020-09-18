package com.beam.example.code;

import com.mysql.cj.jdbc.MysqlDataSource;

import org.apache.beam.runners.direct.DirectRunner;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.KvCoder;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.coders.VarIntCoder;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.io.jdbc.JdbcIO;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.options.ValueProvider;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.SimpleFunction;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * 读取数据库数据，输入数据库数据
 * 使用JdbcIO 需要导入beam-sdks-java-io-jdbc jar包
 * @since 2020-08-11
 */
public class DemoFileJdbc {
    public static void main(String[] args) {
        PipelineOptions options = PipelineOptionsFactory.fromArgs(args).withoutStrictParsing().withValidation().create();
        options.setRunner(DirectRunner.class);

        Pipeline pipeline = Pipeline.create(options);
        /**
         * 读取数据库数据第一种方式：
         *  手动设置驱动类，url 通过withXXX设置密码和账号
         */
        JdbcIO.Read<KV<Integer, String>> jdbcRead = JdbcIO.<KV<Integer, String>>read()
                .withDataSourceConfiguration(JdbcIO.DataSourceConfiguration.create(
                        "com.mysql.cj.jdbc.Driver", "jdbc:mysql://127.0.0.1:3306/test")
                        .withUsername("root")
                        .withPassword("root"));
        /**
         *  读取数据库数据第二种方式：
         *  通过设置DataSource  
         */
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setDatabaseName("test");
        dataSource.setServerName("127.0.0.1");
        dataSource.setUser("root");
        dataSource.setPassword("root");
        JdbcIO.Read<KV<Integer, String>> jdbcRead2 = JdbcIO.<KV<Integer, String>>read()
                .withDataSourceConfiguration(JdbcIO.DataSourceConfiguration.create(dataSource));

        /**
         * 读取数据库数据第三种方式：
         *  ValueProvider.StaticValueProvider.of 封装参数，通过withXXX设置密码和账号
         */
        JdbcIO.Read<KV<Integer, String>> jdbcRead3 = JdbcIO.<KV<Integer, String>>read()
                .withDataSourceConfiguration(JdbcIO.DataSourceConfiguration.create(
                        ValueProvider.StaticValueProvider.of("com.mysql.cj.jdbc.Driver"),
                        ValueProvider.StaticValueProvider.of("jdbc:mysql://127.0.0.1:3306/test"))
                        .withUsername("root")
                        .withPassword("root"));

        // 如果需要定制化数据连接，则可以使用 SerializableFunction.
        // 默认情况下，提供的函数为每个执行线程实例化一个DataSource。在某些情况下，例如具有连接池的数据源，
        // 这可能会通过请求太多连接而很快使数据库不堪重负。在这种情况下，应将DataSource设为静态单例，以便每个JVM仅实例化一次。
        
        // 这里的数据连接池用的commons-pool的通用框架
        JdbcIO.Read<KV<Integer, String>> jdbcReadPool = JdbcIO.<KV<Integer, String>>read()
                .withDataSourceProviderFn(JdbcIO.PoolableDataSourceProvider.of(
                JdbcIO.DataSourceConfiguration.create("com.mysql.cj.jdbc.Driver",
                        "jdbc:mysql://127.0.0.1:3306/test")
                        .withUsername("root")
                        .withPassword("root")
        ));
        
        PCollection<KV<Integer, String>> kvpCollection = pipeline.apply(jdbcReadPool
                .withQuery("select id ,name from student")
                .withCoder(KvCoder.of(VarIntCoder.of(), StringUtf8Coder.of()))
                .withRowMapper(new JdbcIO.RowMapper<KV<Integer, String>>() {
                    @Override
                    public KV<Integer, String> mapRow(ResultSet resultSet) throws Exception {
                        return KV.of(resultSet.getInt(1), resultSet.getString(2));
                    }
                })
        );
        
        
        // 打印
        kvpCollection.apply(ParDo.of(new DoFn<KV<Integer, String>, Void>() {
            @ProcessElement
            public void processElement(@Element KV<Integer, String> element) {
                System.out.println(element);
            }
        }));
        
        // 输出到文件
        kvpCollection.apply(MapElements.via(new SimpleFunction<KV<Integer, String>, String>() {
            @Override
            public String apply(KV<Integer, String> input) {
                return input.getKey() + ":" + input.getValue(); 
            }
        })).apply(TextIO.write().to("logs/jdbc.txt").withoutSharding());
        
        // 输出到数据库
        // 注意：万一发生瞬态故障，Beam Runner可以执行JdbcIO的一部分，多次写入以提高容错能力。
        // 因此，您应该避免使用INSERT语句，因为这样可能会导致数据库中记录的重复或由于主键冲突而导致失败。
        // 考虑使用数据库支持的MERGE（“ upsert”）语句。
        kvpCollection.apply(JdbcIO.<KV<Integer, String>>write()
                .withDataSourceConfiguration(JdbcIO.DataSourceConfiguration.create(
                        "com.mysql.cj.jdbc.Driver", "jdbc:mysql://127.0.0.1:3306/test")
                        .withUsername("root")
                        .withPassword("root"))
                .withStatement("insert into student(id,name) values(?,?)")
                .withPreparedStatementSetter(new JdbcIO.PreparedStatementSetter<KV<Integer, String>>() {
                    @Override
                    public void setParameters(KV<Integer, String> element, PreparedStatement preparedStatement) throws Exception {
                        preparedStatement.setInt(1, element.getKey() + 100);
                        preparedStatement.setString(2, element.getValue() + "_name");
                    }
                })
        );
        pipeline.run().waitUntilFinish();
    }
}
