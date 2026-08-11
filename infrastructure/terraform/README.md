# pypycar Terraform infrastructure

This directory deploys the single `dev` environment for pypycar into **`us-east-1`**.
It intentionally uses low-cost serverless AWS services and does not create a VPC, NAT Gateway, Route 53 hosted zone, custom domain, or payment infrastructure.

## Account layout

| Resource | AWS account |
| --- | --- |
| AWS Organizations, billing, and governance | Management |
| Terraform state S3 bucket | Platform |
| pypycar application resources | Workload/Sandbox |

The application resources created by this stack are deployed to the Workload/Sandbox account. The Terraform state bucket is expected to already exist in the Platform account.

## Resources created

- API Gateway HTTP API with a default AWS HTTPS endpoint
- Cognito user pool and public app client
- Lambda API function running Python 3.12
- DynamoDB tables for users, rides, and bookings
- DynamoDB indexes for route, driver, passenger, and ride lookups
- IAM role for the Lambda function
- CloudWatch Lambda log group with 14-day retention

The Lambda API includes the initial profile, ride, and booking handlers. The Android UI currently still uses sample data; connecting the Android repository to these API endpoints is a separate application task.

## API routes

Public ride search:

```text
GET /rides
GET /rides/{rideId}
```

Cognito-authenticated routes:

```text
POST   /rides
POST   /rides/{rideId}/bookings
GET    /bookings
DELETE /bookings/{bookingId}
GET    /profile
PUT    /profile
```

Seat reservations use a DynamoDB transaction and return `409 Conflict` when there are not enough seats.

## Prerequisites

Install or configure:

- Terraform >= 1.10
- AWS CLI
- An AWS CLI profile with access to the Platform account state bucket
- A Workload/Sandbox account profile, or permission to assume a workload deployment role
- Permission to create the resources listed above in `us-east-1`

Check the active AWS identity before applying:

```bash
aws sts get-caller-identity --profile pypycar-workload
```

The state bucket is deliberately not created by this stack. This avoids the bootstrap chicken-and-egg problem.

## Create the Platform state bucket

Create a dedicated S3 bucket in the Platform account. The bucket name must be globally unique:

```bash
aws s3api create-bucket \
  --bucket pypycar-terraform-state-ACCOUNT_ID \
  --region us-east-1 \
  --profile pypycar-platform

aws s3api put-bucket-versioning \
  --bucket pypycar-terraform-state-ACCOUNT_ID \
  --versioning-configuration Status=Enabled \
  --profile pypycar-platform

aws s3api put-public-access-block \
  --bucket pypycar-terraform-state-ACCOUNT_ID \
  --public-access-block-configuration \
  BlockPublicAcls=true,IgnorePublicAcls=true,BlockPublicPolicy=true,RestrictPublicBuckets=true \
  --profile pypycar-platform
```

Enable default encryption and restrict the bucket policy to the Terraform operators in the Platform account. Do not commit credentials or the real backend configuration to Git.

Update `backend.hcl.example` with the real bucket name. The example uses the `pypycar-platform` profile for state access:

```hcl
bucket  = "pypycar-terraform-state-ACCOUNT_ID"
region  = "us-east-1"
profile = "pypycar-platform"
```

## Initialize and deploy

From this directory:

```bash
cp terraform.tfvars.example terraform.tfvars
```

If your current AWS identity already has deployment access in the Workload/Sandbox account, leave `workload_role_arn` empty. If you are operating from the Platform account, set the workload role ARN in `terraform.tfvars`:

```hcl
workload_role_arn = "arn:aws:iam::<WORKLOAD_ACCOUNT_ID>:role/pypycar-terraform-deployment"
```

Initialize the remote state backend:

```bash
terraform init -backend-config=backend.hcl.example
```

Review and apply the development plan:

```bash
terraform fmt -check -recursive .
terraform validate
terraform plan -out=pypycar-dev.tfplan
terraform apply pypycar-dev.tfplan
```

The plan file is local and should not be committed.

## Outputs

Get the API Gateway URL:

```bash
terraform output -raw api_url
```

Get the Cognito identifiers:

```bash
terraform output -raw cognito_user_pool_id
terraform output -raw cognito_app_client_id
```

## Configure the Android build

The Android build workflow reads the API URL from a GitHub Actions repository secret. Add it in:

**GitHub → repository Settings → Secrets and variables → Actions → Secrets → New repository secret**

```text
Name:  PYPYCAR_API_URL
Value: https://<api-id>.execute-api.us-east-1.amazonaws.com
```

The workflow reads `secrets.PYPYCAR_API_URL`, passes it to Gradle, and embeds it in the APK's `BuildConfig.API_BASE_URL`.

The API URL is not a credential. It will still be visible inside the installed APK because the app must know where to connect. The GitHub secret only prevents the URL from being exposed in the repository and workflow configuration.

The current Android UI has not yet replaced its local sample repository with API calls, so configuring this secret only prepares the APK for the API client integration.

## Build and publish the APK

The GitHub Actions workflow builds a debug APK on pushes to `main`, pull requests, and manual runs. It uploads the APK as an Actions artifact.

To also attach the APK to a GitHub Release, push a version tag:

```bash
git tag v1.0.0
git push origin v1.0.0
```

The tag workflow creates a release named `pypycar v1.0.0` and attaches the debug APK. This is not a signed Play Store build.

## Destroy

This is a development environment. Review the plan carefully before removing resources:

```bash
terraform plan -destroy
terraform destroy
```

Do not run destroy against shared state without explicit confirmation from the team.
