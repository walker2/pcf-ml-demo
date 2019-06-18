package org.ashipilo.nndashboard.conf;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.ashipilo.nndashboard.mq.Sender;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.ashipilo.nndashboard.mq.SenderRPC;

@Configuration
public class RabbitConfig {

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange("job_queue.rpc");
    }

    @Bean
    public SenderRPC client() {
        return new SenderRPC();
    }

    @Bean
    public Queue queueRecognize() {
        return new Queue("job_queue.rpc.recognize");
    }

    @Bean
    public Queue queueRelearn() {
        return new Queue("job_queue.rpc.relearn");
    }

    @Bean
    public Binding bindingRecognize(DirectExchange exchange,
        Queue queueRecognize) {
        return BindingBuilder.bind(queueRecognize)
            .to(exchange)
            .with("recognize");
    }

    @Bean
    public Binding bindingRelearn(DirectExchange exchange,
                           Queue queueRelearn) {
        return BindingBuilder.bind(queueRelearn)
                .to(exchange)
                .with("relearn");
    }
    /*@Bean
    public Queue queue() {
        return new Queue("TaskQueue");
    }

    @Bean
    public Sender sender() {
        return new Sender();
    }*/

    /*@Bean
    public Receiver receiver() {
        return new Receiver();
    }*/

}
