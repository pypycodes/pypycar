variable "name_prefix" {
  description = "Prefix for API resources."
  type        = string
}

variable "rides_table_name" {
  type = string
}

variable "bookings_table_name" {
  type = string
}

variable "users_table_name" {
  type = string
}

variable "user_pool_client_id" {
  type = string
}

variable "user_pool_endpoint" {
  type = string
}

variable "lambda_source_dir" {
  description = "Directory containing the Lambda source."
  type        = string
}
