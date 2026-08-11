provider "aws" {
  region = var.aws_region

  dynamic "assume_role" {
    for_each = var.workload_role_arn == "" ? [] : [var.workload_role_arn]

    content {
      role_arn = assume_role.value
    }
  }

  default_tags {
    tags = local.common_tags
  }
}
