#!/usr/bin/env node
import * as cdk from 'aws-cdk-lib';
import {
  aws_lambda as lambda,
  aws_iam as iam,
  Duration,
} from 'aws-cdk-lib';
import { ApiGatewayAccountStack, Backend, SubSite, Bucket, Dynamo } from '../genesis-cdk/src/index.js';

const app = new cdk.App();

new ApiGatewayAccountStack(app, 'apigw-account-setup', {
  env: { account: process.env.CDK_DEFAULT_ACCOUNT, region: 'eu-west-2' },
});

const stack = new cdk.Stack(app, 'aidiobook-sconi-com', {
  env: {
    account: process.env.CDK_DEFAULT_ACCOUNT,
    region: 'eu-west-2',
  },
});

const SITE_DOMAIN = "aidiobook";
const API_DOMAIN = "aidiobook-api";

const audioStore = new Bucket(stack, 'audio_store', { expirationDays: 1 });
const audioStore = new Bucket(stack, 'audio_samples');
const jobTable = new Dynamo(stack, 'job_table', {
  tableName: 'audiobook-jobs',
  partitionKey: 'jobId',
  ttlAttribute: 'ttl',
});

const site = new SubSite({
  scope: stack,
  domain: SITE_DOMAIN,
  src: '../frontend/dist',
});

const backend = new Backend({
  scope: stack,
  domain: API_DOMAIN,
  cloudfront: site.cloudfrontDist,
  codePath: '../backend/api/target/api-0.0.1-SNAPSHOT.jar',
  handler: 'com.audiobook.StreamLambdaHandler::handleRequest',
  runtime: lambda.Runtime.JAVA_21,
  memorySize: 1024,
  timeout: Duration.seconds(120),
  binaryMediaTypes: ['audio/*', 'application/octet-stream'],
  environment: {
    JOBS_TABLE_NAME: jobTable.tableName,
    GENERATED_AUDIO_BUCKET: audioStore.bucketName,
    BEDROCK_MODEL_ID: 'anthropic.claude-haiku-3-5-20241022-v1:0',
  },
});

audioStore.grantReadWrite(backend.lambdaFn);
jobTable.grantReadWrite(backend.lambdaFn);
backend.grantBedrockInvoke(['anthropic.claude-haiku-3-5-20241022-v1:0']);

backend.lambdaFn.addToRolePolicy(new iam.PolicyStatement({
  actions: ['polly:SynthesizeSpeech', 'polly:DescribeVoices'],
  resources: ['*'],
}));

// const vpc = new ec2.Vpc(stack, 'vpc', {
//   maxAzs: 2,
//   natGateways: 1,
//   subnetConfiguration: [
//     { name: 'public',  subnetType: ec2.SubnetType.PUBLIC,            cidrMask: 24 },
//     { name: 'private', subnetType: ec2.SubnetType.PRIVATE_WITH_EGRESS, cidrMask: 24 },
//   ],
// });

// const gpuInstance = new Ec2(stack, 'gpu', {
//   vpc,
//   subnets: vpc.privateSubnets,
//   instanceType: ec2.InstanceType.of(ec2.InstanceClass.G5, ec2.InstanceSize.XLARGE),
//   amiSsmPath: '/aws/service/deeplearning/ami/x86_64/base-oss-nvidia-driver-gpu-ubuntu-22.04/latest/ami-id',
//   blockDevices: [{
//     deviceName: '/dev/sda1',
//     volume: ec2.BlockDeviceVolume.ebs(150, { encrypted: true }),
//   }],
//   instanceInitiatedShutdownBehavior: ec2.InstanceInitiatedShutdownBehavior.STOP,
// });

// gpuInstance.allowInferenceFrom(backend);
// gpuInstance.allowStartStop(backend.lambdaFn.role!);
