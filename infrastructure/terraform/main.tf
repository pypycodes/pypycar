module "database" {
  source = "./modules/database"

  name_prefix = local.name_prefix
}

module "auth" {
  source = "./modules/auth"

  name_prefix   = local.name_prefix
  callback_urls = var.cognito_callback_urls
  logout_urls   = var.cognito_logout_urls
}

module "api" {
  source = "./modules/api"

  name_prefix         = local.name_prefix
  rides_table_name    = module.database.rides_table_name
  bookings_table_name = module.database.bookings_table_name
  users_table_name    = module.database.users_table_name
  user_pool_client_id = module.auth.user_pool_client_id
  user_pool_endpoint  = module.auth.user_pool_endpoint
  lambda_source_dir   = "${path.module}/lambda/src"
}
