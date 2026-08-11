output "api_url" {
  description = "Default HTTPS API Gateway endpoint for the pypycar dev API."
  value       = module.api.api_url
}

output "cognito_user_pool_id" {
  description = "Cognito user pool ID for the dev environment."
  value       = module.auth.user_pool_id
}

output "cognito_app_client_id" {
  description = "Cognito app client ID for the Android application."
  value       = module.auth.user_pool_client_id
}

output "dynamodb_table_names" {
  description = "DynamoDB table names used by the API Lambda."
  value       = module.database.table_names
}
