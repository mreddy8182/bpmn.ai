package de.viadee.ki.sparkimporter.util;

import com.beust.jcommander.Parameter;

/**
 * Configures command line parameters of the import application.
 */
public class SparkImporterArguments {

	private static SparkImporterArguments sparkImporterArguments = null;

	@Parameter(names = { "--file-source",
			"-fs" }, required = true, description = "Path an name of the CSV-File to be processed. You can generate the file with a query such as this one: SELECT *\r\n"
					+ "FROM ACT_HI_PROCINST a\r\n" + "JOIN ACT_HI_VARINST v ON a.PROC_INST_ID_ = v.PROC_INST_ID_ \r\n"
					+ "AND a.proc_def_key_ = 'XYZ' \r\n" + "")
	private String fileSource;

	@Parameter(names = { "--delimiter",
			"-d" }, required = true, description = "Character or string that separates fields such as [ ;,  | or ||| ]. Please make sure that these are not contained in your data.")
	private String delimiter;

	@Parameter(names = { "--file-destination",
			"-fd" }, required = true, description = "The name of the target folder, where the resulting csv files are being stored, i.e. the data mining table.")
	private String fileDestination;

	@Parameter(names = { "--revision-count", "-rc" }, description = "Boolean toggle to enable the counting of changes "
			+ "to a variable. It results in a number of columns named <VARIABLE_NAME>_rev.", arity = 1)
	private boolean revisionCount = true;

	@Parameter(names = { "--step-results",
			"-sr" }, description = "Should intermediate results be written into CSV files?", arity = 1)
	private boolean writeStepResultsToCSV = false;

	/**
	 * Singleton.
	 */
	private SparkImporterArguments() {
	}

	public boolean isRevisionCount() {
		return revisionCount;
	}

	public String getFileSource() {
		return fileSource;
	}

	public String getDelimiter() {
		return delimiter;
	}

	public String getFileDestination() {
		return fileDestination;
	}

	public boolean isWriteStepResultsToCSV() {
		return writeStepResultsToCSV;
	}

	/**
	 * @return DataExtractorArguments-Instanz as Singleton
	 */
	public static SparkImporterArguments getInstance() {
		if (sparkImporterArguments == null) {
			sparkImporterArguments = new SparkImporterArguments();
		}
		return sparkImporterArguments;
	}

	@Override
	public String toString() {
		return "SpringImporterArguments{" + "fileSource='" + fileSource + '\'' + ", delimiter='" + delimiter
				+ '\'' + ", fileDestination='" + fileDestination + '\'' + ", revisionCount=" + revisionCount + '}';
	}
}
