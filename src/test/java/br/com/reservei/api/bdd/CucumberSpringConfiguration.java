package br.com.reservei.api.bdd;

import br.com.reservei.api.ReserveiApplication;
import br.com.reservei.api.bdd.contexto.ContextoIds;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.test.context.ContextConfiguration;

@CucumberContextConfiguration
//@SpringBootTest(classes = ReserveiApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = ReserveiApplication.class)
public class CucumberSpringConfiguration {


    @Bean
    @Scope("cucumber-glue") // Define que este bean será usado no contexto do Cucumber
    public ContextoIds contextoIds() {
        return new ContextoIds();
    }
}
