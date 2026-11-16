package cn.resumecraft.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisPlusConfig {
  @Bean
  MybatisPlusInterceptor mybatisPlusInterceptor() {
    var pagination = new PaginationInnerInterceptor(DbType.MYSQL);
    pagination.setMaxLimit(100L);
    var interceptor = new MybatisPlusInterceptor();
    interceptor.addInnerInterceptor(pagination);
    return interceptor;
  }
}
