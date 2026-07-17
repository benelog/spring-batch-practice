package kr.co.wikibook.logbatch;

import java.util.List;
import org.springframework.classify.Classifier;

public class RoundRobinClassifier<C, T> implements Classifier<C, T> {
  private int index = -1;
  private final int rounds;
  private final List<T> targets;

  public RoundRobinClassifier(List<T> targets) {
    this.targets = targets;
    this.rounds = targets.size();
  }

  @Override
  public T classify(C ignored) {
    return targets.get(++index % rounds);
  }
}
