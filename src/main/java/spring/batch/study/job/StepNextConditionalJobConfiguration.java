package spring.batch.study.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class StepNextConditionalJobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job stepNextConditionalJob(){
        return jobBuilderFactory.get("stepNextConditionalJob")
                .start(conditionalStep1())
                .on("FAILED") // Failed 일 경우
                .to(conditionalStep3()) // step 3 으로 이동
                .on("*") // Step 3 의 결과 관계 없이
                .end() // step3 로 이도하면 Flow 가 종료
                .from(conditionalStep1()) // step1 로부터
                .on("*") // Failed 외에 모든 경우
                .to(conditionalStep2()) // step2 로 이동
                .next(conditionalStep3())// step2 가 정상 종료되면 step3로 이동
                .on("*") // step3 의 결과 관계 없이
                .end()// step3 로 이동하면 flow 가 종료
                .end()// job 종료
                .build();

    }
    @Bean
    public Step conditionalStep1(){
        return stepBuilderFactory.get("step1")
                .tasklet((contribution, chunkContext) -> {
                    log.info(">>>>> This is StepNextConditionalJob Step1");

                    /**
                     *  ExitStatus 를 FAILED 로 지정한다.
                     *  해당 status 를 보고 flow 가 진행된다.
                     */
//                    contribution.setExitStatus(ExitStatus.FAILED);

                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Step conditionalStep2(){
        return stepBuilderFactory.get("conditionalJobStep2")
                .tasklet((contribution, chunkContext) -> {
                    log.info(">>>>> This is stepNextConditionalJob Step2");

                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Step conditionalStep3(){
        return stepBuilderFactory.get("conditionalJobStep3")
                .tasklet((contribution, chunkContext) -> {
                    log.info(">>>>> This is stepNextConditionalJob Step3");

                    return RepeatStatus.FINISHED;
                })
                .build();
    }
}
