package org.arbeitspferde.groningen;

import org.arbeitspferde.groningen.config.PipelineIterationScoped;
import org.arbeitspferde.groningen.executor.Executor;
import org.arbeitspferde.groningen.generator.Generator;
import org.arbeitspferde.groningen.hypothesizer.Hypothesizer;
import org.arbeitspferde.groningen.scorer.IterationScorer;
import org.arbeitspferde.groningen.validator.Validator;

import com.google.inject.Inject;
import com.google.inject.Provider;

@PipelineIterationScoped
public class PipelineIterationProvider implements Provider<PipelineIteration> {

  private final PipelineIteration.Builder builder;

  @Inject
  public PipelineIterationProvider(final Hypothesizer hypothesizer,
                                   final Generator generator,
                                   final Executor executor,
                                   final Validator validator,
                                   final IterationScorer scorer) {
    builder = PipelineIteration.builder()
                               .addPipelineStage(hypothesizer)
                               .addPipelineStage(generator)
                               .addPipelineStage(executor)
                               .addPipelineStage(validator)
                               .addPipelineStage(scorer);
  }

  @Override
  public PipelineIteration get() {
    return builder.build();
  }

}
