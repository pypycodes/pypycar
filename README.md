# pypycar

Community carpool Android MVP. Drivers can offer rides and passengers can search and reserve seats. Initial ride payments are agreed directly between driver and passenger through UPI; pypycar does not process payments or charge commission.

## Repository layout

```text
app/                         Android Kotlin/Compose application
backend/openapi.yaml         API contract
infrastructure/terraform/    AWS dev infrastructure and Lambda source
.github/workflows/           Android build and GitHub Release workflow
```

## Current MVP scope

Implemented in the Android prototype:

- Search sample rides by origin and destination
- View ride and driver details
- Select seats and confirm a sample booking
- Offer a ride with fare and seat validation
- View bookings and profile screens
- Inject an API Gateway URL into the APK at build time

The Android UI currently uses local sample data. The Terraform Lambda API is available as the backend foundation, but the Android repository still needs to be connected to the API client for persistent cloud-backed rides and bookings.

## Build the Android app locally

Requirements:

- Android SDK with API 35
- JDK 17

Build without an API URL:

```bash
./gradlew --no-daemon assembleDebug
```

Build with the development API URL:

```bash
./gradlew --no-daemon \
  -PapiBaseUrl="https://<api-id>.execute-api.us-east-1.amazonaws.com" \
  assembleDebug
```

The value is available to Kotlin as `ApiConfig.baseUrl` and is embedded in `BuildConfig.API_BASE_URL`.

## GitHub Actions

The workflow at `.github/workflows/android-build.yml`:

- Runs on pushes to `main`, pull requests, and manual dispatch
- Uses JDK 17 and the Gradle wrapper
- Builds a debug APK
- Uploads the APK as a workflow artifact
- Creates a GitHub Release with the APK when a `v*` tag is pushed

Configure this repository secret before building with the cloud API:

```text
PYPYCAR_API_URL=https://<api-id>.execute-api.us-east-1.amazonaws.com
```

Add it under **Settings → Secrets and variables → Actions → Secrets**. The API URL is not a credential and will still be visible inside the installed APK.

Publish an APK release:

```bash
git tag v1.0.0
git push origin v1.0.0
```

The release is a debug APK and is not signed for Google Play distribution.

## AWS development infrastructure

Terraform deploys one development environment in `us-east-1` into the Workload/Sandbox account:

- API Gateway HTTP API
- Cognito user pool and app client
- Python 3.12 Lambda API
- DynamoDB users, rides, and bookings tables
- IAM and CloudWatch logs

Terraform state is stored in an S3 bucket in the Platform account. No production or staging environment, VPC, NAT Gateway, Route 53 hosted zone, custom domain, or payment gateway is included.

Read the deployment guide before applying infrastructure:

<https://github.com/pypycodes/pypycar/tree/main/infrastructure/terraform>

From the Terraform directory, the high-level flow is:

```bash
cp terraform.tfvars.example terraform.tfvars
terraform init -backend-config=backend.hcl.example
terraform validate
terraform plan -out=pypycar-dev.tfplan
terraform apply pypycar-dev.tfplan
terraform output -raw api_url
```

Do not commit `terraform.tfvars`, state files, credentials, or the real backend configuration.
