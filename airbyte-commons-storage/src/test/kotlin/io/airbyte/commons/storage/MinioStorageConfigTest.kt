/*
 * Copyright (c) 2020-2026 Airbyte, Inc., all rights reserved.
 */

package io.airbyte.commons.storage

import io.airbyte.commons.envvar.EnvVar
import io.airbyte.micronaut.runtime.AirbyteStorageConfig
import io.airbyte.micronaut.runtime.StorageEnvironmentVariableProvider
import io.airbyte.micronaut.runtime.StorageType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import software.amazon.awssdk.regions.Region

internal class MinioStorageConfigTest {
  @Test
  internal fun testToEnvVarMap() {
    val accessKey = "access-key"
    val secretAccessKey = "secret-access-key"
    val endpoint = "http://localhost:8080"
    val bucketConfig =
      AirbyteStorageConfig.AirbyteStorageBucketConfig(
        state = "state",
        workloadOutput = "workload-output",
        log = "log",
        activityPayload = "activity-payload",
        // Audit logging is null by default
        auditLogging = "",
        profilerOutput = "",
        replicationDump = "",
      )
    val s3StorageConfig =
      AirbyteStorageConfig.MinioStorageConfig(
        accessKey = accessKey,
        secretAccessKey = secretAccessKey,
        endpoint = endpoint,
      )
    val storageEnvironmentVariableProvider =
      StorageEnvironmentVariableProvider(
        buckets = bucketConfig,
        storageConfig = s3StorageConfig,
      )
    val envVarMap = storageEnvironmentVariableProvider.toEnvVarMap()
    assertEquals(11, envVarMap.size)
    assertEquals(bucketConfig.log, envVarMap[EnvVar.STORAGE_BUCKET_LOG.name])
    assertEquals(bucketConfig.workloadOutput, envVarMap[EnvVar.STORAGE_BUCKET_WORKLOAD_OUTPUT.name])
    assertEquals(bucketConfig.activityPayload, envVarMap[EnvVar.STORAGE_BUCKET_ACTIVITY_PAYLOAD.name])
    assertEquals(bucketConfig.state, envVarMap[EnvVar.STORAGE_BUCKET_STATE.name])
    assertEquals(bucketConfig.auditLogging, envVarMap[EnvVar.STORAGE_BUCKET_AUDIT_LOGGING.name])
    assertEquals(bucketConfig.replicationDump, envVarMap[EnvVar.STORAGE_BUCKET_REPLICATION_DUMP.name])
    assertEquals(StorageType.MINIO.name, envVarMap[EnvVar.STORAGE_TYPE.name])
    assertEquals(accessKey, envVarMap[EnvVar.AWS_ACCESS_KEY_ID.name])
    assertEquals(secretAccessKey, envVarMap[EnvVar.AWS_SECRET_ACCESS_KEY.name])
    assertEquals(endpoint, envVarMap[EnvVar.MINIO_ENDPOINT.name])
    assertEquals("us-east-1", envVarMap[EnvVar.MINIO_REGION.name])
  }

  @Test
  internal fun testToEnvVarMapWithAuditLogging() {
    val accessKey = "access-key"
    val secretAccessKey = "secret-access-key"
    val endpoint = "http://localhost:8080"
    val bucketConfig =
      AirbyteStorageConfig.AirbyteStorageBucketConfig(
        state = "state",
        workloadOutput = "workload-output",
        log = "log",
        activityPayload = "activity-payload",
        auditLogging = "audit-logging",
        profilerOutput = "",
        replicationDump = "",
      )
    val s3StorageConfig =
      AirbyteStorageConfig.MinioStorageConfig(
        accessKey = accessKey,
        secretAccessKey = secretAccessKey,
        endpoint = endpoint,
      )
    val storageEnvironmentVariableProvider =
      StorageEnvironmentVariableProvider(
        buckets = bucketConfig,
        storageConfig = s3StorageConfig,
      )
    val envVarMap = storageEnvironmentVariableProvider.toEnvVarMap()
    assertEquals(11, envVarMap.size)
    assertEquals(bucketConfig.log, envVarMap[EnvVar.STORAGE_BUCKET_LOG.name])
    assertEquals(bucketConfig.workloadOutput, envVarMap[EnvVar.STORAGE_BUCKET_WORKLOAD_OUTPUT.name])
    assertEquals(bucketConfig.activityPayload, envVarMap[EnvVar.STORAGE_BUCKET_ACTIVITY_PAYLOAD.name])
    assertEquals(bucketConfig.state, envVarMap[EnvVar.STORAGE_BUCKET_STATE.name])
    assertEquals(bucketConfig.auditLogging, envVarMap[EnvVar.STORAGE_BUCKET_AUDIT_LOGGING.name])
    assertEquals(bucketConfig.replicationDump, envVarMap[EnvVar.STORAGE_BUCKET_REPLICATION_DUMP.name])
    assertEquals(StorageType.MINIO.name, envVarMap[EnvVar.STORAGE_TYPE.name])
    assertEquals(accessKey, envVarMap[EnvVar.AWS_ACCESS_KEY_ID.name])
    assertEquals(secretAccessKey, envVarMap[EnvVar.AWS_SECRET_ACCESS_KEY.name])
    assertEquals(endpoint, envVarMap[EnvVar.MINIO_ENDPOINT.name])
    assertEquals("us-east-1", envVarMap[EnvVar.MINIO_REGION.name])
  }

  @Test
  internal fun testToString() {
    val accessKey = "access-key"
    val secretAccessKey = "secret-access-key"
    val endpoint = "http://localhost:8080"
    val s3StorageConfig =
      AirbyteStorageConfig.MinioStorageConfig(
        accessKey = accessKey,
        secretAccessKey = secretAccessKey,
        endpoint = endpoint,
      )
    val toString = s3StorageConfig.toString()
    assertEquals("MinioStorageConfig(accessKey=*******, secretAccessKey=*******, endpoint=$endpoint, region=us-east-1)", toString)
  }

  @Test
  internal fun testToEnvVarMapWithRegion() {
    val region = "us-ashburn-1"
    val storageConfig =
      AirbyteStorageConfig.MinioStorageConfig(
        accessKey = "access-key",
        secretAccessKey = "secret-access-key",
        endpoint = "http://localhost:8080",
        region = region,
      )
    val storageEnvironmentVariableProvider =
      StorageEnvironmentVariableProvider(
        buckets = AirbyteStorageConfig.AirbyteStorageBucketConfig(),
        storageConfig = storageConfig,
      )
    val envVarMap = storageEnvironmentVariableProvider.toEnvVarMap()
    assertEquals(region, envVarMap[EnvVar.MINIO_REGION.name])
  }

  @Test
  internal fun testS3ClientDefaultsToUsEast1() {
    val storageConfig =
      AirbyteStorageConfig.MinioStorageConfig(
        accessKey = "access-key",
        secretAccessKey = "secret-access-key",
        endpoint = "http://localhost:8080",
      )
    storageConfig.s3Client().use { assertEquals(Region.US_EAST_1, it.serviceClientConfiguration().region()) }
  }

  @Test
  internal fun testS3ClientFallsBackToUsEast1WhenRegionBlank() {
    val storageConfig =
      AirbyteStorageConfig.MinioStorageConfig(
        accessKey = "access-key",
        secretAccessKey = "secret-access-key",
        endpoint = "http://localhost:8080",
        region = "",
      )
    storageConfig.s3Client().use { assertEquals(Region.US_EAST_1, it.serviceClientConfiguration().region()) }
  }

  @Test
  internal fun testS3ClientUsesConfiguredRegion() {
    val storageConfig =
      AirbyteStorageConfig.MinioStorageConfig(
        accessKey = "access-key",
        secretAccessKey = "secret-access-key",
        endpoint = "http://localhost:8080",
        region = "us-ashburn-1",
      )
    storageConfig.s3Client().use { assertEquals(Region.of("us-ashburn-1"), it.serviceClientConfiguration().region()) }
  }
}
