package org.arbeitspferde.groningen;

import com.google.inject.Inject;

import org.arbeitspferde.groningen.generator.Generator;
import org.arbeitspferde.groningen.hypothesizer.Hypothesizer;
import org.arbeitspferde.groningen.profiling.ProfilingRunnable;
import org.arbeitspferde.groningen.scorer.IterationScorer;

/**
 * Ties together the iteration count and pipeline state into a value object in which the count
 * and state can be operated/retrieved atomically. Encapsulating them allows for state to be
 * updated locally within the {@link Pipeline}, the {@link PipelineIteration}, and the
 * {@link PipelineSynchronizer} as well.
 */
public class PipelineStageInfo {
  private int iterationNumber;
  private PipelineStageState state;

  @Inject
  public PipelineStageInfo() {
    this(0, PipelineStageState.INITIALIZED);
  }

  public PipelineStageInfo(int iterationNumber, PipelineStageState state) {
    this.iterationNumber = iterationNumber;
    this.state = state;
  }

  /** Set only the pipeline state. */
  public synchronized void set(PipelineStageState state) {
    this.state = state;
  }

  public synchronized void setStageFromRunnable(ProfilingRunnable runnable) {
     if (runnable instanceof Hypothesizer) {
       set(PipelineStageState.HYPOTHESIZER);
     } else if (runnable instanceof Generator) {
       set(PipelineStageState.GENERATOR);
     } else if (runnable instanceof IterationScorer) {
       set(PipelineStageState.SCORING);
     }
  }

  /** Increment the iteration count and set the stage atomically. */
  public synchronized void incrementIterationAndSetState(PipelineStageState state) {
    iterationNumber++;
    this.state = state;
  }

  /**
   * Get a tuple of the iteration count and state atomically.
   *
   * @return the iteration count and state
   */
  public synchronized ImmutablePipelineStageInfo getImmutableValueCopy() {
    return new ImmutablePipelineStageInfo(iterationNumber, state);
  }

  /**
   * An immutable value object that pairs the iteration count and state together.
   */
  public static class ImmutablePipelineStageInfo {
    public final int iterationNumber;
    public final PipelineStageState state;

    private ImmutablePipelineStageInfo(int iterationNumber, PipelineStageState state) {
      this.iterationNumber = iterationNumber;
      this.state = state;
    }
  }
}
