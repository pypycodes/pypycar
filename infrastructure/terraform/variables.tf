variable "aws_region" {
  description = "AWS region for the pypycar development environment."
  type        = string
  default     = "us-east-1"
}

variable "environment" {
  description = "Deployment environment name. This project currently supports dev only."
  type        = string
  default     = "dev"

  validation {
    condition     = var.environment == "dev"
    error_message = "Only the dev environment is supported by this MVP configuration."
  }
}

variable "project_name" {
  description = "Project name used in AWS resource names."
  type        = string
  default     = "pypycar"
}

variable "workload_role_arn" {
  description = "Optional IAM role ARN to assume in the Workload/Sandbox account."
  type        = string
  default     = ""
}

variable "cognito_callback_urls" {
  description = "Callback URLs for the Cognito app client. Keep empty until an auth callback is configured."
  type        = list(string)
  default     = []
}

variable "cognito_logout_urls" {
  description = "Logout URLs for the Cognito app client."
  type        = list(string)
  default     = []
}
