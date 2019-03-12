package com.sample;

/**
 * Hello world!
 *
 */
import org.apache.beam.runners.dataflow.DataflowRunner;
import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;

public class App {
    private static final String PROJECT = "[YOUR PROJECT]";
    private static final String STAGING_LOCATION = "gs://[YOUR GCS BACKET]/staging";
    private static final String TEMP_LOCATION = "gs://[YOUR GCS BACKET]/temp";
    private static final String SRC_PUBSUB_TOPIC = "projects/[YOUR PROJECT]/topics/[PUBSUB TOPIC 1]";
    private static final String DST_PUBSUB_TOPIC = "projects/[YOUR PROJECT]/topics/[PUBSUB TOPIC 2]";

    static class MyFn extends DoFn<String, String> {
        @ProcessElement
        public void processElement(ProcessContext c) {
            c.output("Hello," + c.element());
        }
    }

    public static void main(String[] args) {
        PipelineOptions options = PipelineOptionsFactory.create();
        DataflowPipelineOptions dataflowOptions = options.as(DataflowPipelineOptions.class);
        dataflowOptions.setRunner(DataflowRunner.class);
        dataflowOptions.setProject(PROJECT);
        dataflowOptions.setStagingLocation(STAGING_LOCATION);
        dataflowOptions.setTempLocation(TEMP_LOCATION);
        dataflowOptions.setNumWorkers(1);

        Pipeline p = Pipeline.create(dataflowOptions);
        p.apply(PubsubIO.readStrings().fromTopic(SRC_PUBSUB_TOPIC))
                .apply(ParDo.of(new MyFn()))
                .apply(PubsubIO.writeStrings().to(DST_PUBSUB_TOPIC));
        p.run();

        // Pull
        // $ gcloud pubsub subscriptions pull projects/[ProjectID]/subscriptions/[SubName] --auto-ack
    }
}
