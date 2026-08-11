terraform {
  required_version = ">= 1.10.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.0"
    }
    archive = {
      source  = "hashicorp/archive"
      version = "~> 2.7"
    }
  }

  # Configure the remote state bucket with -backend-config values during init.
  # The bucket itself must be bootstrapped in the Platform account first.
  backend "s3" {
    key          = "pypycar/dev/terraform.tfstate"
    use_lockfile = true
    encrypt      = true
  }
}
