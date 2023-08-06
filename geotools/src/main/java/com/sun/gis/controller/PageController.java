package com.sun.gis.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Api(tags = "页面路由管理")
@Controller
public class PageController {


    @Resource
    private HealthEndpoint healthEndpoint;

    @Resource
    private MetricsEndpoint metricsEndpoint;

    // 创建一个映射，将每个度量名称映射到其对应的中文注释
    private static final Map<String, String> METRIC_NAME_TO_CHINESE_COMMENT = new HashMap<>();

    static {
        METRIC_NAME_TO_CHINESE_COMMENT.put("jvm.memory.max", "JVM最大内存");
        METRIC_NAME_TO_CHINESE_COMMENT.put("jvm.threads.states", "JVM线程状态");
        METRIC_NAME_TO_CHINESE_COMMENT.put("jvm.gc.memory.promoted", "GC后提升的内存");
        METRIC_NAME_TO_CHINESE_COMMENT.put("jvm.memory.used", "JVM已使用内存");
        METRIC_NAME_TO_CHINESE_COMMENT.put("jvm.gc.max.data.size", "GC最大数据区大小");
        METRIC_NAME_TO_CHINESE_COMMENT.put("jvm.gc.pause", "GC暂停时间");
        METRIC_NAME_TO_CHINESE_COMMENT.put("jvm.memory.committed", "JVM已提交内存");
        METRIC_NAME_TO_CHINESE_COMMENT.put("system.cpu.count", "系统CPU数量");
        METRIC_NAME_TO_CHINESE_COMMENT.put("logback.events", "Logback日志事件");
        METRIC_NAME_TO_CHINESE_COMMENT.put("jvm.buffer.memory.used", "JVM缓冲区已使用内存");
        METRIC_NAME_TO_CHINESE_COMMENT.put("tomcat.sessions.created", "Tomcat创建的会话数");
        METRIC_NAME_TO_CHINESE_COMMENT.put("jvm.threads.daemon", "JVM守护线程数");
        METRIC_NAME_TO_CHINESE_COMMENT.put("system.cpu.usage", "系统CPU使用率");
        METRIC_NAME_TO_CHINESE_COMMENT.put("jvm.gc.memory.allocated", "GC后分配的内存");
        METRIC_NAME_TO_CHINESE_COMMENT.put("tomcat.sessions.expired", "Tomcat过期的会话数");
        METRIC_NAME_TO_CHINESE_COMMENT.put("jvm.threads.live", "JVM活动线程数");
        METRIC_NAME_TO_CHINESE_COMMENT.put("jvm.threads.peak", "JVM峰值线程数");
        METRIC_NAME_TO_CHINESE_COMMENT.put("process.uptime", "进程运行时间");
        METRIC_NAME_TO_CHINESE_COMMENT.put("tomcat.sessions.rejected", "Tomcat被拒绝的会话数");
        METRIC_NAME_TO_CHINESE_COMMENT.put("process.cpu.usage", "进程CPU使用率");
        METRIC_NAME_TO_CHINESE_COMMENT.put("jvm.classes.loaded", "JVM已加载类数");
        METRIC_NAME_TO_CHINESE_COMMENT.put("jvm.classes.unloaded", "JVM已卸载类数");
        METRIC_NAME_TO_CHINESE_COMMENT.put("tomcat.sessions.active.current", "Tomcat当前活动的会话数");
        METRIC_NAME_TO_CHINESE_COMMENT.put("tomcat.sessions.alive.max", "Tomcat会话的最长存活时间");
        METRIC_NAME_TO_CHINESE_COMMENT.put("jvm.gc.live.data.size", "GC后存活的数据区大小");
        METRIC_NAME_TO_CHINESE_COMMENT.put("jvm.buffer.count", "JVM缓冲区数量");
        METRIC_NAME_TO_CHINESE_COMMENT.put("jvm.buffer.total.capacity", "JVM缓冲区总容量");
        METRIC_NAME_TO_CHINESE_COMMENT.put("tomcat.sessions.active.max", "Tomcat最大活动会话数");
        METRIC_NAME_TO_CHINESE_COMMENT.put("process.start.time", "进程启动时间");
    }

    @ApiOperation(value = "获取服务主页面")
    @GetMapping("/")
    public String index(Model model) {
        // 将数据添加到模型中，以便在视图中使用
        model.addAttribute("message", "Hello, world!");
        HealthComponent health = healthEndpoint.health();
        model.addAttribute("healthData", health.getStatus());
        Map<String, Object> metricsData = new HashMap<>();
        for (String name : METRIC_NAME_TO_CHINESE_COMMENT.keySet()) {
            MetricsEndpoint.MetricResponse metric = metricsEndpoint.metric(name, null);
            Map<String, String> formattedMeasurements = new HashMap<>();
            for (MetricsEndpoint.Sample measurement : metric.getMeasurements()) {
                String formattedValue = formatMetricValue(name, measurement.getValue());
                formattedMeasurements.put(measurement.getStatistic().toString(), formattedValue);
            }
            metricsData.put(name, formattedMeasurements);
        }
        model.addAttribute("metricsData", metricsData);
        model.addAttribute("metricComments", METRIC_NAME_TO_CHINESE_COMMENT);
        return "index";
    }

    @ApiOperation(value = "监听服务页面")
    @GetMapping("monitor")
    public String monitor() {
        return "monitor";
    }

    private String formatMetricValue(String name, double value) {
        if (name.startsWith("jvm.memory") || name.startsWith("jvm.buffer")) {
            // 将内存的单位从字节转换为兆字节
            return String.format("%.2f MB", value / (1024 * 1024));
        } else if (name.startsWith("process.uptime")) {
            // 将时间的单位从毫秒转换为秒
            return String.format("%.2f seconds", value / 1000);
        } else if (name.startsWith("system.cpu") || name.startsWith("process.cpu")) {
            // 将CPU的使用率转换为百分比
            return String.format("%.2f%%", value * 100);
        } else {
            // 对于其他的度量，我们直接返回原始的值
            return String.valueOf(value);
        }
    }


}
