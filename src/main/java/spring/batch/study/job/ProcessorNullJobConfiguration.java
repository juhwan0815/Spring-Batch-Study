package spring.batch.study.job;

import lombok.RequiredArgsConstructor;
import lombok.experimental.WithBy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import spring.batch.study.entity.Pay;

import javax.persistence.EntityManagerFactory;
import java.util.regex.Pattern;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class ProcessorNullJobConfiguration {

    public static final String JOB_NAME = "processorNullBatch";
    public static final String BEAN_PREFIX = JOB_NAME + "_";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory emf;

    @Value("${chunkSize:1000}")
    public int chunkSize;

    @Bean(JOB_NAME)
    public Job job(){
        return jobBuilderFactory.get(JOB_NAME)
                .preventRestart()
                .start(step())
                .build();
    }

    @Bean(BEAN_PREFIX + "step")
    @JobScope
    public Step step(){
        return stepBuilderFactory.get(BEAN_PREFIX+"step")
                .<Pay,Pay>chunk(chunkSize)
                .reader(reader1())
                .processor(processor1())
                .writer(writer1())
                .build();
    }

    @Bean
    public JpaPagingItemReader<Pay> reader1(){
        return new JpaPagingItemReaderBuilder<Pay>()
                .name(BEAN_PREFIX+"reader")
                .entityManagerFactory(emf)
                .pageSize(chunkSize)
                .queryString("select p from Pay p")
                .build();
    }

    @Bean
    public ItemProcessor<Pay,Pay> processor1(){
        return pay ->{
            boolean isIgnoreTarget = pay.getId() % 2 == 0L;
            if(isIgnoreTarget){
                log.info(">>>>> Pay name = {} isIgnoreTarget={}",pay.getTxName(), isIgnoreTarget);
                return null;
            }
            return pay;
        };
    }

    @Bean
    public ItemWriter<Pay> writer1(){
        return items -> {
            for (Pay item : items) {
                log.info("Pay name = {}",item.getTxName());
            }
        };
    }

}
