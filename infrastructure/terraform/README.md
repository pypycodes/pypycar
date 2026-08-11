# pypycar Terraform infrastructure

This directory deploys the single `dev` environment for pypycar into `us-east-1`.
It intentionally uses only serverless AWS services and does not create a VPC, NAT Gateway, Route 53 zone, or custom domain.

## Resources

- API Gateway HTTP API with Cognito JWT authorization
- Lambda API function
- DynamoDB tables for users, rides, and bookings
- Cognito user pool and app client
- IAM execution role and CloudWatch log group

## Prerequisites

- Terraform >= 1.10
- AWS CLI configured with a Platform profile for the state bucket and a Workload profile or assume-role permission
- An S3 state bucket created in the Platform account
- Permission to assume/read the state bucket and deploy into the Workload account

The state bucket is deliberately not created by this stack. This avoids the bootstrap chicken-and-egg problem.

## Initialize

From this directory:

```bash
cp terraform.tfvars.example terraform.tfvars
terraform init -backend-config=backend.hcl.example
terraform plan
terraform apply
```

Replace the bucket placeholder in `backend.hcl.example` before running `terraform init`.

## Get the Android API URL

After applying:

```bash
terraform output -raw api_url
```

Set the resulting value as the GitHub Actions repository secret `PYPYCAR_API_URL`. The Android workflow reads it from `secrets.PYPYCAR_API_URL`, passes it to Gradle, and embeds it in the APK's `BuildConfig.API_BASE_URL`.

In GitHub, go to **Settings → Secrets and variables → Actions → Secrets → New repository secret** and create:

```text
Name:  PYPYCAR_API_URL
Value: https://<api-id>.execute-api.us-east-1.amazonaws.com
```

Note that the API URL is not a credential. It will still be visible inside the installed APK because the app must know where to connect. The secret only prevents the URL from being exposed in the repository and workflow configuration.

## Destroy

This is a development environment. To remove its AWS resources:

```bash
terraform destroy
```

Do not run destroy against shared state without reviewing the plan first.
