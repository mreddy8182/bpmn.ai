package de.viadee.ki.sparkimporter.processing.steps.dataprocessing;

import de.viadee.ki.sparkimporter.processing.interfaces.PreprocessingStepInterface;
import de.viadee.ki.sparkimporter.util.SparkBroadcastHelper;
import de.viadee.ki.sparkimporter.util.SparkImporterUtils;
import de.viadee.ki.sparkimporter.util.SparkImporterVariables;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;
import org.apache.spark.sql.expressions.Window;
import org.apache.spark.sql.expressions.WindowSpec;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.apache.spark.sql.functions.when;

public class FillActivityInstancesHistoryStep implements PreprocessingStepInterface {

    @Override
    public Dataset<Row> runPreprocessingStep(Dataset<Row> dataset, boolean writeStepResultIntoFile) {

        //repartition py process instance and order by start_time for this operation
        dataset = dataset.repartition(dataset.col(SparkImporterVariables.VAR_PROCESS_INSTANCE_ID)).sortWithinPartitions(SparkImporterVariables.VAR_START_TIME);

        // get variables
        Map<String, String> varMap = (Map<String, String>) SparkBroadcastHelper.getInstance().getBroadcastVariable(SparkBroadcastHelper.BROADCAST_VARIABLE.PROCESS_VARIABLES_ESCALATED);

        //convert to String array so it is serializable and can be used in map function
        Set<String> variables = varMap.keySet();
        String[] vars = new String[variables.size()];
        int vc = 0;
        for(String v : variables) {
            vars[vc++] = v;
        }

        // make empty values actually null
        for(String v : variables) {
            dataset = dataset.withColumn(v, when(dataset.col(v).equalTo(""), null).otherwise(dataset.col(v)));
        }

        Map<String, String> valuesToWrite = new HashMap<>();
        final String[] lastProcessInstanceId = {""};
        String[] columns = dataset.columns();


        //iterate through dataset and fill up values in each process instance
        Dataset<Row> changed_data = dataset.map(row -> {
            String currentProcessInstanceId = row.getAs(SparkImporterVariables.VAR_PROCESS_INSTANCE_ID);
            String[] newRow = new String[columns.length];

            if(!lastProcessInstanceId[0].equals(currentProcessInstanceId)) {
                // new process instance
                valuesToWrite.clear();
                lastProcessInstanceId[0] = currentProcessInstanceId;
            }

            int columnCount = 0;
            for(String c : columns) {
                String columnValue = null;
                if(Arrays.asList(vars).contains(c)) {
                    //variable
                    if(valuesToWrite.get(c) != null) {
                        columnValue = valuesToWrite.get(c);
                    } else {
                        String currentValue = row.getAs(c);
                        if(currentValue != null) {
                            valuesToWrite.put(c, currentValue);
                            columnValue = currentValue;
                        }
                    }
                } else {
                    //column
                    columnValue = row.getAs(c);
                }
                newRow[columnCount++] = columnValue;
            }

            return RowFactory.create(newRow);
        }, RowEncoder.apply(dataset.schema()));

        dataset = changed_data;

        if(writeStepResultIntoFile) {
            SparkImporterUtils.getInstance().writeDatasetToCSV(dataset, "fill_activity_instances_history");
        }

        //return preprocessed data
        return dataset;
    }
}
