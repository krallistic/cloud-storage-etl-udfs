package com.exasol.cloudetl.scriptclasses

import scala.collection.JavaConverters._

import com.exasol.ExaImportSpecification
import com.exasol.ExaMetadata

import org.mockito.Mockito._

class ImportPathSuite extends BaseSuite {

  test("`generateSqlForImportSpec` should create a sql statement") {
    val exaMeta = mock[ExaMetadata]
    val exaSpec = mock[ExaImportSpecification]

    when(exaMeta.getScriptSchema()).thenReturn(testSchema)
    when(exaSpec.getParameters()).thenReturn(params.asJava)

    val sqlExpected =
      s"""SELECT
         |  $testSchema.IMPORT_FILES(
         |    '$s3BucketPath', '$rest', filename
         |)
         |FROM (
         |  SELECT $testSchema.IMPORT_METADATA(
         |    '$s3BucketPath', '$rest', nproc()
         |  )
         |)
         |GROUP BY
         |  partition_index;
         |""".stripMargin

    assert(ImportPath.generateSqlForImportSpec(exaMeta, exaSpec) === sqlExpected)
    verify(exaMeta, atLeastOnce).getScriptSchema
    verify(exaSpec, times(1)).getParameters
  }

  test("`generateSqlForImportSpec` should throw an exception if any required param is missing") {
    val exaMeta = mock[ExaMetadata]
    val exaSpec = mock[ExaImportSpecification]

    val newParams = params - ("S3_ACCESS_KEY")

    when(exaMeta.getScriptSchema()).thenReturn(testSchema)
    when(exaSpec.getParameters()).thenReturn(newParams.asJava)

    val thrown = intercept[IllegalArgumentException] {
      ImportPath.generateSqlForImportSpec(exaMeta, exaSpec)
    }

    assert(thrown.getMessage === "The required parameter S3_ACCESS_KEY is not defined!")
    verify(exaSpec, times(1)).getParameters
  }

}
