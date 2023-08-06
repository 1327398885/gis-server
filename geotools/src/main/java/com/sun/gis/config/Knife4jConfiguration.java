package com.sun.gis.config;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


@Configuration
@EnableSwagger2
@EnableKnife4j
public class Knife4jConfiguration {

    String name = "sungang";
    String url = "";
    String email = "1327398885@qq.com";


    Contact contact = new Contact(name, url, email);

    @Bean(value = "defaultApi2")
    public Docket defaultApi2() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(new ApiInfoBuilder()
                        .contact(contact)
                        .title("GIS Server")
                        .description("# 地理数据服务")
                        .termsOfServiceUrl("http://localhost:8210/")
                        .version("0.0.1")
                        .build())
                //分组名称
                .groupName("0.0.1版本")
                .select()
                //这里指定Controller扫描包路径
                .apis(RequestHandlerSelectors.basePackage("com.sun.gis.controller"))
                .paths(PathSelectors.any())
                .build();
    }
}