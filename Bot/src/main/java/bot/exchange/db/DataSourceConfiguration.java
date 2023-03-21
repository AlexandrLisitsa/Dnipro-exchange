package bot.exchange.db;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfiguration {

    @Bean
    public DataSource dataSource() {
        DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName("org.h2.Driver");
        dataSourceBuilder.url("jdbc:h2:file:./botDB;DB_CLOSE_DELAY=-1");
        dataSourceBuilder.username("SA");
        dataSourceBuilder.password("MyC00lPwd");
        return dataSourceBuilder.build();
    }

}
