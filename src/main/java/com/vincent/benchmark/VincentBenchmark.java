package com.vincent.benchmark;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 *     Throughput("thrpt", "Throughput, ops/time"), 计算一个时间单位内操作数量
 *     AverageTime("avgt", "Average time, time/op"),计算平均运行时间
 *     SampleTime("sample", "Sampling time"),//计算一个方法的运行时间(包括百分位)
 *     SingleShotTime("ss", "Single shot invocation time"),方法仅运行一次(用于冷测试模式)
 *      或者特定批量大小的迭代多次运行(具体查看的“`@Measurement“`注解)——这种情况下JMH将计算批处理运行时间(一次批处理所有调用的总时间)
 *     All("all", "All benchmark modes");
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
/**
 * 测试的时候，我们可能需要向测试方法传入若干参数，这些参数还可能需要不同的隔离级别：每个线程单独一份还是每个benchmark一份还是一组线程共享等。
 *     Benchmark,运行相同测试的所有线程将共享实例。
 *     Group,实例分配给每个线程组(查看后面的测试线程组)
 *     Thread;实例将分配给运行给定测试的每个线程。
 */
@State(Scope.Thread)
//需要运行的试验(迭代集合)数量。每个试验运行在单独的JVM进程中。也可以指定(额外的)JVM参数。
@Fork(1)
//与@Measurement相同，但是用于预热阶段
@Warmup(iterations = 4)
//提供真正的测试阶段参数。指定迭代的次数，每次迭代的运行时间和每次迭代测试调用的数量(通常使用@BenchmarkMode(Mode.SingleShotTime)测试一组操作的开销——而不使用循环)
@Measurement(iterations = 4)
//该测试使用的线程数。默认是Runtime.getRuntime().availableProcessors()
@Threads(value = 2)
public class VincentBenchmark {

    //指定同一个方法的不同参数。
    @Param({"10","20","40","80"})
    private int n;

    private List<Integer> array;
    private List<Integer> list;

    /**
     * 数据的准备像极了JUnit和TestNG的方式，即：在测试开始前后分别进行处理。在JMH中对应到@Setup和@TearDown，
     * 我们可以在被进行标记的方法中对数据进行处理，并且处理的耗时不被记入正常测试的时间，也就是说不会影响我们测试结果。
     *     Trial,在每个benchmark之前/之后运行
     *     Iteration,在一次迭代之前/之后(一组调用)运行
     *     Invocation;每个方法调用之前/之后
     */
    @Setup(Level.Trial)
    public void init() {
        array = new ArrayList<>();
        list = new LinkedList<>();
        for (int i = 0; i < n; i++) {
            array.add(i);
            list.add(i);
        }
    }


    @Benchmark
    public void listInsert() {
        for (int i = 0; i < n; i++) {
            list.add(i);
        }
    }

    //标记某个方法进行基准测试，类比JUnit的@Test。
    @Benchmark
    public void arrayInsert() {
        for (int i = 0; i < n; i++) {
            array.add(i);
        }
    }

    @TearDown(Level.Trial)
    public void listRemove() {
        for (int i = 0; i < n; i++) {
            array.remove(0);
            list.remove(0);
        }
    }


    public static void main( String[] args ) throws RunnerException {
        Options options = new OptionsBuilder().include(VincentBenchmark.class.getSimpleName()).build();
        new Runner(options).run();
    }

}
