/* Copyright 2012 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.arbeitspferde.groningen;

import com.google.common.collect.Lists;
import com.google.inject.Injector;

import org.arbeitspferde.groningen.config.GroningenConfig;
import org.arbeitspferde.groningen.config.PipelineIterationScoped;
import org.arbeitspferde.groningen.executor.Executor;
import org.arbeitspferde.groningen.hypothesizer.Hypothesizer;
import org.arbeitspferde.groningen.profiling.ProfilingRunnable;
import org.arbeitspferde.groningen.utility.Metric;
import org.arbeitspferde.groningen.utility.MetricExporter;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * This class encapsulates the logic of one pipeline iteration. It's assumed that this class
 * is used withing @PipelineIterationScoped Guice scope with all the dependencies injected or
 * seeded by Guice.
 *
 * TODO(team): Current implementation doesn't allow more than one PipelineIteration instance
 * to be used simultaneously (see currentPipelineStage metrics, for example).
 */
@PipelineIterationScoped
public class PipelineIteration {
  private static final Logger log = Logger.getLogger(PipelineIteration.class.getCanonicalName());

  public static final class Builder {

    private List<ProfilingRunnable> pipelineStagesList = Lists.newArrayList();

    public Builder addPipelineStage(ProfilingRunnable pipelineStage) {
      pipelineStagesList.add(pipelineStage);
      return this;
    }

    public PipelineIteration build() {
      Injector injector = WorkhorseFactory.getInjector();
      return new PipelineIteration(
          injector.getInstance(GroningenConfig.class),
          injector.getInstance(PipelineSynchronizer.class),
          injector.getInstance(MetricExporter.class),
          injector.getInstance(PipelineStageInfo.class),
          pipelineStagesList.toArray(new ProfilingRunnable[0]));
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  private final GroningenConfig config;
  private final PipelineSynchronizer pipelineSynchronizer;
  private final ProfilingRunnable[] pipelineStages;

  private final PipelineStageInfo pipelineStageInfo;

  /**
   * Track the pipestage we are in. It's fine to have this metrics defined this way as there's only
   * one pipeline per Groningen instance.
   *
   * TODO(team): fix when we have multiple pipelines
   **/
  private static AtomicInteger currentPipelineStage = new AtomicInteger();

  private PipelineIteration(final GroningenConfig config,
                            final PipelineSynchronizer pipelineSynchronizer,
                            final MetricExporter metricExporter,
                            final PipelineStageInfo pipelineStageInfo,
                            final ProfilingRunnable[] pipelineStages) {
    this.config = config;
    this.pipelineSynchronizer = pipelineSynchronizer;
    this.pipelineStageInfo = pipelineStageInfo;
    this.pipelineStages = pipelineStages;

    metricExporter.register(
        "current_pipeline_stage",
        "Current stage: 0=Hypothesizer, 1=Generator, 2=Executor, 3=Validator 4=Scorer",
        Metric.make(currentPipelineStage));
  }

  public int getStage() {
    return currentPipelineStage.get();
  }

  /**
   * Provide the remaining time within the run of the experiment.
   *
   * This does not include any restart or wait times and is only valid when the experiment
   * is running.
   *
   * @return time in secs remaining in the experiment, -1 if the request was made outside the
   *    valid window.
   */
  public int getRemainingExperimentalSecs() {
    // TODO(sanragsood): Clean this up, PipelineIteration should be unaware of the specifics of
    // the stage pipeline iteration is in.
    for (ProfilingRunnable stage : pipelineStages) {
      if (stage instanceof Executor) {
        return (int)((Executor)stage).getRemainingDurationSeconds();
      }
    }
    return 0;
  }

  public boolean run() {
    for (ProfilingRunnable stage : pipelineStages) {
      stage.startUp();
    }

    // Synchronization within the pipeline iteration - after the config is updated
    pipelineSynchronizer.iterationStartHook();

    int stageCount = 0;
    boolean notComplete = true;
    for (ProfilingRunnable stage : pipelineStages) {
      currentPipelineStage.set(stageCount++);
      pipelineStageInfo.setStageFromRunnable(stage);
      stage.run(config);
      // TODO(sanragsood): Clean this up, remove dependency on stage specifics from pipeline
      // iteration
      if (stage instanceof Hypothesizer) {
        notComplete = ((Hypothesizer)stage).notComplete();
        if (!notComplete) {
          break;
        }
      }
    }

    return notComplete;
  }
}
