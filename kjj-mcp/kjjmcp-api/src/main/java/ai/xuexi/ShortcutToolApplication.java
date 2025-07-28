package ai.xuexi;

import ai.xuexi.service.KjjMcpService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;

/**
 * 快捷键管理工具主启动类
 * 
 * @author lingma
 */
@SpringBootApplication
//@ComponentScan(basePackages = "com.ai.kjjmcp")
public class ShortcutToolApplication {

    public static void main(String[] args) {
//        SpringApplication.run(ShortcutToolApplication.class, args);
        SpringApplicationBuilder builder = new SpringApplicationBuilder(ShortcutToolApplication.class);
        builder.headless(false).run(args);


    }
    @Bean
    public ToolCallbackProvider timeTools(KjjMcpService kjjMcpService) {
        return MethodToolCallbackProvider.builder().toolObjects(kjjMcpService).build();
    }
}