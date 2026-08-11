output "user_pool_id" {
  value = aws_cognito_user_pool.this.id
}

output "user_pool_client_id" {
  value = aws_cognito_user_pool_client.android.id
}

output "user_pool_issuer" {
  value = "https://${aws_cognito_user_pool.this.endpoint}"
}

output "user_pool_endpoint" {
  value = "https://${aws_cognito_user_pool.this.endpoint}"
}
