package kr.co.wikibook.batch.report;

import kr.co.wikibook.batch.report.job.CreateAndSendReportJobConfig;
import kr.co.wikibook.batch.report.job.CreateReportJobConfig;
import kr.co.wikibook.batch.report.job.DeleteReportJobConfig;
import kr.co.wikibook.batch.report.job.SendReportJobConfig;
import kr.co.wikibook.batch.report.job.UserRankingJobConfig;
import org.springframework.batch.core.configuration.support.ApplicationContextFactory;
import org.springframework.batch.core.configuration.support.GenericApplicationContextFactory;
import org.springframework.context.annotation.Bean;

public class ReportJobGroupContexts {
  @Bean
  public ApplicationContextFactory userRankingJobContext() {
    return new GenericApplicationContextFactory(UserRankingJobConfig.class);
  }

  @Bean
  public ApplicationContextFactory deleteReportJobJobContext() {
    return new GenericApplicationContextFactory(DeleteReportJobConfig.class);
  }

  @Bean
  public ApplicationContextFactory createAndSendJobReportContext() {
    return new GenericApplicationContextFactory(
        CreateReportJobConfig.class,
        SendReportJobConfig.class,
        CreateAndSendReportJobConfig.class
    );
  }
}
