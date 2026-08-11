variable "name_prefix" {
  description = "Prefix for Cognito resource names."
  type        = string
}

variable "callback_urls" {
  type    = list(string)
  default = []
}

variable "logout_urls" {
  type    = list(string)
  default = []
}
