package kr.co.wikibook.batch.logbatch;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.core.io.PathResource;

class BlogPostXmlWriterTest {
  @Test
  void write(@TempDir Path tempPath) throws Exception {
    // given
    Path outputPath = tempPath.resolve("blogPosts.xml");
    var resource = new PathResource(outputPath);
    var post = new BlogPost(
        "백엔드 개발자를 꿈꾸는 학생개발자에게",
        "https://d2.naver.com/news/3435170",
        Instant.parse("2018-06-21T11:14:16Z")
    );

    var jobConfig = new CollectBlogPostJobConfig();
    StaxEventItemWriter<BlogPost> writer = jobConfig.blogPostXmlWriter(resource);
    writer.afterPropertiesSet();

    // when
    writer.open(new ExecutionContext());
    writer.write(Chunk.of(post));
    writer.close();

    // then
    String output = Files.readString(outputPath);
    assertThat(output).contains("<title>백엔드 개발자를 꿈꾸는 학생개발자에게</title>");
    assertThat(output).contains("<url>https://d2.naver.com/news/3435170");
    assertThat(output).contains("<updatedAt>2018-06-21T11:14:16Z</updatedAt>");
  }
}
